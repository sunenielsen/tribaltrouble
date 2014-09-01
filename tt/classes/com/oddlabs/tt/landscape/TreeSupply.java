package com.oddlabs.tt.landscape;

import java.util.ResourceBundle;

import com.oddlabs.tt.net.DistributableTable;
import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.audio.AudioPlayer;
import com.oddlabs.tt.audio.AudioParameters;
import com.oddlabs.tt.gui.Icons;
import com.oddlabs.tt.gui.ToolTipBox;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.Supply;
import com.oddlabs.tt.model.SupplyManager;
import com.oddlabs.tt.pathfinder.Occupant;
import com.oddlabs.tt.pathfinder.Region;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.util.BoundingBox;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.model.ModelToolTip;
import com.oddlabs.tt.model.ToolTipVisitor;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.util.StrictVector3f;
import com.oddlabs.tt.util.StrictMatrix4f;
import com.oddlabs.tt.util.StrictVector4f;

public final strictfp class TreeSupply extends AbstractTreeGroup implements Supply, Target, Animated, ModelToolTip {
	private final static int INITIAL_SUPPLIES = 10;
	private final static float SECOND_PER_TREEFALL = 3f;
	
	private static final StrictVector3f low_detail_x_axis = new StrictVector3f();
	private static final StrictVector3f low_detail_translate = new StrictVector3f();
	private static final StrictVector3f low_detail_scale = new StrictVector3f();
	private static final StrictMatrix4f low_detail_matrix = new StrictMatrix4f();

	private final StrictMatrix4f matrix;
	private final int tree_type_index;
	private final float x;
	private final float y;
	private final int grid_x;
	private final int grid_y;
	private final int grid_size;
	private final float size;
	private final World world;

	private int low_detail_start_vertex_index;
	private int num_supplies = INITIAL_SUPPLIES;
	private float animation_time;
	private boolean hide = false;
	private float scale = 1f;
	private int hit_counter = 0;

	static {
		low_detail_x_axis.set(1f, 0f, 0f);
	}

	public TreeSupply(World world, AbstractTreeGroup parent, float x, float y, int grid_x, int grid_y, int grid_size, float size, StrictMatrix4f matrix, int tree_type_index, float[] vertices) {
		super(parent);
		this.world = world;
		this.x = x;
		this.y = y;
		this.grid_x = grid_x;
		this.grid_y = grid_y;
		this.grid_size = grid_size;
		this.size = size;
		this.tree_type_index = tree_type_index;
		this.matrix = matrix;
		StrictVector4f src = new StrictVector4f();
		StrictVector4f dest = new StrictVector4f();
		for (int i = 0; i < vertices.length; i += 3) {
			src.set(vertices[i], vertices[i + 1], vertices[i + 2], 1f);
			StrictMatrix4f.transform(matrix, src, dest);
			checkBoundsX(dest.x);
			checkBoundsY(dest.y);
			checkBoundsZ(dest.z);
		}
		if (world.getUnitGrid().getOccupant(grid_x, grid_y) == null)
			occupyTree();
		world.getSupplyManager(getClass()).newSupply();
	}
	
	public final World getWorld() {
		return world;
	}

	public final int getLowDetailStartIndex() {
		return low_detail_start_vertex_index;
	}

	public final void visit(ToolTipVisitor visitor) {
		visitor.visitSupply(this);
	}

	public final void setLowDetailStartIndex(int index) {
		low_detail_start_vertex_index = index;
	}

	public final float getScale() {
		return scale;
	}

	public final float getTreeFallProgress() {
		return animation_time/SECOND_PER_TREEFALL;
	}

	public final Supply respawn() {
		occupyTree();
		hide = false;
		num_supplies = INITIAL_SUPPLIES;
		return this;
	}

	public final void animateSpawn(float t, float progress) {
		float inv = 1 - progress;
		scale = 1 - inv*inv*inv*inv*inv*inv;

		low_detail_scale.set(scale, scale, scale);
		StrictMatrix4f.scale(matrix, low_detail_scale, low_detail_matrix);
		world.getNotificationListener().updateTreeLowDetail(low_detail_matrix, this);
	}

	public final void spawnComplete() {
	}

	public final String toString() {
		return "Tree at " + grid_x + " " + grid_y + " isEmpty() " + isEmpty();
	}

	private final void occupyTree() {
		UnitGrid grid = world.getUnitGrid();
		world.getNotificationListener().registerTarget(this);
		Region region = grid.getRegion(getGridX(), getGridY());
		region.registerObject(getClass(), this);
		for (int y = 0; y < grid_size; y++) {
			int occ_y = grid_y + y - (grid_size - 1)/2;
			for (int x = 0; x < grid_size; x++) {
				int occ_x = grid_x + x - (grid_size - 1)/2;
				if (!grid.isGridOccupied(occ_x, occ_y)) {
					assert !(grid.getOccupant(occ_x, occ_y) instanceof TreeSupply): "Trees placed too close";
				}
			}
		}
		grid.occupyGrid(grid_x, grid_y, this);
	}

	private final void unoccupyTree() {
		UnitGrid grid = world.getUnitGrid();
		world.getNotificationListener().unregisterTarget(this);
		Region region = grid.getRegion(grid_x, grid_y);
		region.unregisterObject(getClass(), this);
		grid.freeGrid(grid_x, grid_y, this);
	}

	public final float getSize() {
		return size;
	}

	public final float getPositionX() {
		return x;
	}

	public final float getPositionY() {
		return y;
	}

	public final int getGridX() {
		return grid_x;
	}

	public final int getGridY() {
		return grid_y;
	}

	public final int getPenalty() {
		return Occupant.STATIC;
	}

	public final boolean isEmpty() {
		return num_supplies == 0;
	}

	public final boolean hit() {
		hit_counter++;
		if (hit_counter == Supply.HITS_PER_HARVEST) {
			hit_counter = 0;
			decreaseSupply();
			return true;
		}
		return false;
	}
	
	public final boolean isDead() {
		return isEmpty();
	}
	
	private final void decreaseSupply() {
		num_supplies --;
		if (isEmpty()) {
			unoccupyTree();
			world.getSupplyManager(getClass()).emptySupply(this);
			world.getAudio().newAudio(new AudioParameters(world.getRacesResources().getTreeFallSound()[tree_type_index%2]/* reusing native tree sounds*/, getCX(), getCY(), getCZ(), AudioPlayer.AUDIO_RANK_TREE_FALL, AudioPlayer.AUDIO_DISTANCE_TREE_FALL, AudioPlayer.AUDIO_GAIN_TREE_FALL, AudioPlayer.AUDIO_RADIUS_TREE_FALL));
			world.getAnimationManagerRealTime().registerAnimation(this);
			animation_time = 0f;
		}
	}

	public final int getTreeTypeIndex() {
		return tree_type_index;
	}

	protected final boolean initBounds() {
		super.initBounds();
		return true;
	}

	public final void animate(float t) {
		animation_time += t;
		float time = getTreeFallProgress();
		low_detail_translate.set(0f, 0f, -13f*(time*time*time*time*time*time));
		StrictMatrix4f.translate(matrix, low_detail_translate, low_detail_matrix);
		low_detail_matrix.rotate((.5f*(float)StrictMath.PI)*time*time, low_detail_x_axis);
		world.getNotificationListener().updateTreeLowDetail(low_detail_matrix, this);
		if (animation_time >= SECOND_PER_TREEFALL) {
			world.getAnimationManagerRealTime().removeAnimation(this);
			low_detail_matrix.setZero();
			world.getNotificationListener().updateTreeLowDetail(low_detail_matrix, this);
			hide = true;
		}
	}

	public final StrictMatrix4f getMatrix() {
		return matrix;
	}

	public final boolean isHidden() {
		return hide;
	}

	public void updateChecksum(StateChecksum checksum) {}

	public final void visit(TreeNodeVisitor visitor) {
		visitor.visitTree(this);
	}
}
