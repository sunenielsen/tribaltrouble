package com.oddlabs.tt.landscape;

import java.util.ArrayList;
import java.util.List;

import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.geometry.LowDetailModel;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.model.Model;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.util.BoundingBox;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.util.StrictMatrix4f;
import com.oddlabs.tt.util.StrictVector3f;

public abstract strictfp class AbstractTreeGroup extends BoundingBox {
	public final static int TREE_INDEX = 0;
	public final static int PALMTREE_INDEX = 1;
	public final static int OAKTREE_INDEX = 2;
	public final static int PINETREE_INDEX = 3;
	
	private AbstractTreeGroup parent;
	private int start;
	private int count;
	
	private int num_responding_trees = 0;

	private float group_min_squared;

	public AbstractTreeGroup(AbstractTreeGroup parent) {
		this.parent = parent;
	}

	public final int getLowDetailStart() {
		return start;
	}

	public final int getLowDetailCount() {
		return count;
	}

	protected final AbstractTreeGroup getParent() {
		return parent;
	}

	public final boolean hasTrees() {
		return count > 0;
	}
	
	public final void changeRespondingTrees(int delta) {
		num_responding_trees += delta;
		if (parent != null)
			parent.changeRespondingTrees(delta);
	}

	public final boolean hasRespondingTrees() {
		return num_responding_trees > 0;
	}

	public final static AbstractTreeGroup newRoot(World world, LowDetailModel[] tree_low_details, List tree_positions, List palm_tree_positions, int terrain_type) {
		AbstractTreeGroup root = new TreeGroup(null, 0);

		switch (terrain_type) {
			case Landscape.NATIVE:
				root.buildTrees(world, tree_low_details, TREE_INDEX, 3, 2.3f, tree_positions, 0.25f, 0.75f);
				root.buildTrees(world, tree_low_details, PALMTREE_INDEX, 1, 1.6f, palm_tree_positions, 0.5f, 1f);
				break;
			case Landscape.VIKING:
				root.buildTrees(world, tree_low_details, OAKTREE_INDEX, 3, 2.3f, tree_positions, 0.5f, 1f);
				root.buildTrees(world, tree_low_details, PINETREE_INDEX, 1, 1.6f, palm_tree_positions, 0.5f, 1f);
				break;
			default:
				throw new RuntimeException();
		}
				
		root.initBounds();
		return root;
	}

	private final void buildTrees(final World world, LowDetailModel[] tree_low_details, final int tree_type_index, final int grid_size, final float radius, List tree_positions, float scale_factor, float min_size) {
		StrictMatrix4f matrix2 = new StrictMatrix4f();
		StrictVector3f vector = new StrictVector3f();
		final float[] tree_low_vertices = tree_low_details[tree_type_index].getVertices();
		for (int i = 0; i < tree_positions.size(); i++) {
			final StrictMatrix4f matrix = new StrictMatrix4f();
			int[] coords = (int[])tree_positions.get(i);
			final int center_grid_x = coords[0];
			final int center_grid_y = coords[1];
			final float tree_x = UnitGrid.coordinateFromGrid(center_grid_x);
			final float tree_y = UnitGrid.coordinateFromGrid(center_grid_y);
			float rotation = world.getRandom().nextFloat()*360f;
			float scale_base = world.getRandom().nextFloat()*scale_factor + min_size;
			float scale_x = scale_base + world.getRandom().nextFloat()*0.2f - 0.1f;
			float scale_y = scale_base + world.getRandom().nextFloat()*0.2f - 0.1f;
			float scale_z = scale_base + world.getRandom().nextFloat()*0.2f - 0.1f;
			matrix.setIdentity();
			vector.set(scale_x, scale_y, scale_z);
			matrix.scale(vector);
			vector.set(0f, 0f, 1f);
			matrix.rotate(rotation, vector);
			matrix2.setIdentity();
			vector.set(tree_x, tree_y, world.getHeightMap().getNearestHeight(tree_x, tree_y));
			matrix2.translate(vector);
			StrictMatrix4f.mul(matrix2, matrix, matrix);
			visit(new TreeNodeVisitor() {
				private int child_size = world.getHeightMap().getMetersPerWorld();
				private int x;
				private int y;

				public final void visitLeaf(TreeLeaf tree_leaf) {
					TreeSupply tree = new TreeSupply(world, tree_leaf, tree_x, tree_y, center_grid_x, center_grid_y, grid_size, radius, matrix, tree_type_index, tree_low_vertices);
					tree_leaf.insertTree(tree);
				}
				public final void visitNode(TreeGroup tree_group) {
					int old_x = x;
					int old_y = y;
					int old_size = child_size;
					child_size >>= 1;
					if (tree_x < x + child_size) {
						if (tree_y < y + child_size) {
							tree_group.getChild0().visit(this);
						} else {
							y += child_size;
							tree_group.getChild2().visit(this);
						}
					} else {
						if (tree_y < y + child_size) {
							x += child_size;
							tree_group.getChild1().visit(this);
						} else {
							x += child_size;
							y += child_size;
							tree_group.getChild3().visit(this);
						}
					}
					x = old_x;
					y = old_y;
					child_size = old_size;
				}
				public final void visitTree(TreeSupply tree_supply) {
					throw new RuntimeException();
				}
			});
		}
	}

	public final void initLowDetailBuffer(int start, int end) {
		this.start = start;
		this.count = end - start;
	}

	protected boolean initBounds() {
		group_min_squared = computeMinDistanceSquared(Globals.TREE_ERROR_DISTANCE);
		return true;
	}

	public final float getGroupMinSquared() {
		return group_min_squared;
	}

	public abstract void visit(TreeNodeVisitor visitor);
}
