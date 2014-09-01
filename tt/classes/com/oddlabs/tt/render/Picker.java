package com.oddlabs.tt.render;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Comparator;

import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.animation.TimerAnimation;
import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.animation.Updatable;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.camera.MapCamera;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.render.SpriteSorter;
import com.oddlabs.tt.model.SceneryModel;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.landscape.AbstractTreeGroup;
import com.oddlabs.tt.landscape.LandscapeTarget;
import com.oddlabs.tt.landscape.LandscapeTargetRespond;
import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.AbstractElementNode;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Model;
import com.oddlabs.tt.model.Army;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.landscape.LandscapeLeaf;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.player.PlayerInterface;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.util.BoundingBox;
import com.oddlabs.tt.util.StrictGLU;
import com.oddlabs.tt.util.StrictMatrix4f;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.util.ToolTip;
import com.oddlabs.tt.model.ModelToolTip;
import com.oddlabs.tt.viewer.Selection;

public final strictfp class Picker implements Updatable {
	private final static int PICK_SIZE = 5;
	private final static int SELECTION_THRESHOLD = 5;
	private final static float PATCH_PICK_PRECISION = .1f;
	private final static float PATCH_PICK_STEP = 2f;
	private final static float TOOL_TIP_DELAY = .1f;

	private final StrictMatrix4f proj = new StrictMatrix4f();
	private final StrictMatrix4f modl = new StrictMatrix4f();
	private final int[] viewport = new int[4];
	private final float[] hit_result = new float[3];
	private final float[] dir_vector = new float[3];

	private final List element_pick_list = new ArrayList();
	private final List tree_pick_list = new ArrayList();

	private final CameraState tmp_camera = new CameraState();
	private final SortedSet patch_pick_set = new TreeSet(new LandscapeLeafComparator());
	private final LandscapeRenderer landscape_renderer;
	private final ElementRenderer element_renderer;
	private final TreePicker tree_renderer;
	private final SpriteSorter sprite_sorter;
	private final RenderQueues render_queues;
	private final RespondManager respond_manager;
	private final Player local_player;

	private Target current_hovered;
	private ToolTip current_tooltip;
	private final TimerAnimation tool_tip_timer = new TimerAnimation(this, TOOL_TIP_DELAY);
	private boolean render_tool_tip = false;
	
	private float patch_hit_x;
	private float patch_hit_y;
	private float patch_hit_z;

	private Selectable[] old_target_selection = new Selectable[0];
	private int old_target_action;
	private boolean old_target_aggressive;

	private int old_landscape_target_grid_x;
	private int old_landscape_target_grid_y;

	private Target old_set_target_target;
	
	public Picker(AnimationManager manager, Player local_player, RenderQueues render_queues, LandscapeRenderer landscape_renderer, Selection selection) {
		this.local_player = local_player;
		this.render_queues = render_queues;
		this.sprite_sorter = new SpriteSorter();
		this.respond_manager = new RespondManager(manager);
		this.element_renderer = new ElementRenderer(local_player, landscape_renderer, render_queues, this, true, sprite_sorter, selection);
		this.tree_renderer = new TreePicker(sprite_sorter, respond_manager);
		this.landscape_renderer = landscape_renderer;
	}

	public final RespondManager getRespondManager() {
		return respond_manager;
	}

	private final Target getNearestPick(List pick_list, Class filter) {
		Target nearest_pickable = null;
		float nearest_squared_distance = Float.POSITIVE_INFINITY;
		for (int i = 0; i <pick_list.size(); i++) {
			Target pickable = (Target)pick_list.get(i);
			pick_list.set(i, null);
			float squared_distance = RenderTools.getCameraDistanceSquared(((BoundingBox)pickable), tmp_camera.getCurrentX(), tmp_camera.getCurrentY(), tmp_camera.getCurrentZ());
			if (filter.isInstance(pickable) && squared_distance < nearest_squared_distance) {
				nearest_squared_distance = squared_distance;
				nearest_pickable = pickable;
			}
		}
		return nearest_pickable;
	}

	public final void pickTarget(Army selected_army, CameraState camera, PlayerInterface player_interface, int x, int y, int action) {
		setupPicking(camera, x, y, PICK_SIZE, PICK_SIZE);
		pickObjects();
		Target nearest_pickable = getNearestPick(element_pick_list, Target.class);
		Selectable[] selection = selected_army.filter(Abilities.TARGET);
		if (nearest_pickable != null) {
			if (!(nearest_pickable instanceof SceneryModel) || ((SceneryModel)nearest_pickable).isOccupying())
				respond_manager.addResponder(nearest_pickable);
			if (isNewSetTarget(selection, nearest_pickable, action, Settings.getSettings().aggressive_units))
				player_interface.setTarget(selection, nearest_pickable, action, Settings.getSettings().aggressive_units);
		} else {
			pickResources();
			final TreeSupply supply = (TreeSupply)getNearestPick(tree_pick_list, Target.class);
			if (supply != null) {
			//	Target target = (Target)supply;
				respond_manager.addResponder(supply, new Runnable() {
					public final void run() {
						supply.changeRespondingTrees(-1);
					}
				});
				supply.changeRespondingTrees(1);
				if (isNewSetTarget(selection, supply, action, Settings.getSettings().aggressive_units))
					player_interface.setTarget(selection, supply, action, Settings.getSettings().aggressive_units);
			} else if (nearestLandscape(x, y)) {
				new LandscapeTargetRespond(local_player.getWorld(), patch_hit_x, patch_hit_y);
				int grid_x = UnitGrid.toGridCoordinate(patch_hit_x);
				int grid_y = UnitGrid.toGridCoordinate(patch_hit_y);
				if (isNewLandscapeTarget(selection, grid_x, grid_y, action, Settings.getSettings().aggressive_units))
					player_interface.setLandscapeTarget(selection, grid_x, grid_y, action, Settings.getSettings().aggressive_units);
			}
		}
	}

	private final boolean isNewSetTarget(Selectable[] selection, Target target, int action, boolean aggressive) {
		old_landscape_target_grid_x = -1;
		old_landscape_target_grid_y = -1;
		
		boolean new_target = isNewOrder(selection, action, aggressive);

		new_target |= target != old_set_target_target;

		old_set_target_target = target;
		return new_target;
	}

	private final boolean isNewLandscapeTarget(Selectable[] selection, int grid_x, int grid_y, int action, boolean aggressive) {
		old_set_target_target = null;
		
		boolean new_target = isNewOrder(selection, action, aggressive);

		new_target |= grid_x != old_landscape_target_grid_x;
		new_target |= grid_y != old_landscape_target_grid_y;

		old_landscape_target_grid_x = grid_x;
		old_landscape_target_grid_y = grid_y;
		return new_target;
	}

	private final boolean isNewOrder(Selectable[] selection, int action, boolean aggressive) {
		boolean new_order = false;
		if (selection.length == old_target_selection.length) {
			for (int i = 0; i < selection.length; i++)
				new_order |= selection[i] != old_target_selection[i];
		} else {
			new_order = true;
		}

		new_order |= action != old_target_action;
		new_order |= aggressive != old_target_aggressive;

		old_target_selection = selection;
		old_target_action = action;
		old_target_aggressive = aggressive;

		return new_order;
	}

	public final Selectable[] pickBoxed(CameraState camera, int x1, int y1, int x2, int y2, int clicks) {
		float cx = (x1 + x2)*0.5f;
		float cy = (y1 + y2)*0.5f;
		int width = StrictMath.abs(x1 - x2) + 1;
		int height = StrictMath.abs(y1 - y2) + 1;
		width = StrictMath.max(width, PICK_SIZE);
		height = StrictMath.max(height, PICK_SIZE);
		setupPicking(camera, cx, cy, width, height);
		pickObjects();
		if (StrictMath.abs(x1 - x2) < SELECTION_THRESHOLD && StrictMath.abs(y1 - y2) < SELECTION_THRESHOLD)
			return createSinglePick(camera, clicks);
		else
			return createBoxedPick();
	}

	private final Selectable[] createSinglePick(CameraState camera, int clicks) {
		Selectable nearest = (Selectable)getNearestPick(element_pick_list, Selectable.class);
		if (nearest != null) {
			if (clicks > 1) {
				if (nearest.getAbilities().hasAbilities(Abilities.THROW)) {
					return pickAll(camera, Abilities.THROW);
				} else if (nearest.getAbilities().hasAbilities(Abilities.HARVEST)) {
					return pickAll(camera, Abilities.HARVEST);
				} else {
					return new Selectable[]{nearest};
				}
			} else {
				return new Selectable[]{nearest};
			}
		} else {
			return new Selectable[0];
		}
	}

	private final Selectable[] createBoxedPick() {
		List selectables = new ArrayList();
		for (int i = 0; i < element_pick_list.size(); i++) {
			Target pickable = (Target)element_pick_list.get(i);
			element_pick_list.set(i, null);
			if (pickable instanceof Selectable)
				selectables.add(pickable);
		}
		Selectable[] array = new Selectable[selectables.size()];
		selectables.toArray(array);
		return array;
	}

	private final Selectable[] pickAll(CameraState camera, int ability_filter) {
		List result = new ArrayList();
		Selectable[] complete_list = pickBoxed(camera, 0, 0, LocalInput.getViewWidth() - 1, LocalInput.getViewHeight() - 1, 2);
		for (int i = 0; i < complete_list.length; i++) {
			Selectable selectable = complete_list[i];
			if (selectable.getAbilities().hasAbilities(ability_filter)) {
				result.add(selectable);
			}
		}
		Selectable[] array = new Selectable[result.size()];
		result.toArray(array);
		return array;
	}

	public final void pickRotate(GameCamera camera) {
		int x = LocalInput.getViewWidth()/2;
		int y = camera.getRotateY();
		setupPicking(camera.getState(), x, y, PICK_SIZE, PICK_SIZE);
		if (!nearestLandscape(x, y) || patch_hit_z < local_player.getWorld().getHeightMap().getSeaLevelMeters()) {
			float dz = tmp_camera.getCurrentZ() - local_player.getWorld().getHeightMap().getSeaLevelMeters();
			float factor = dz/dir_vector[2];
			patch_hit_x = tmp_camera.getCurrentX() - factor*dir_vector[0];
			patch_hit_y = tmp_camera.getCurrentY() - factor*dir_vector[1];
			patch_hit_z = local_player.getWorld().getHeightMap().getSeaLevelMeters();
		}
		int grid_x = UnitGrid.toGridCoordinate(patch_hit_x);
		int grid_y = UnitGrid.toGridCoordinate(patch_hit_y);
		camera.setRotationPoint(new LandscapeTarget(grid_x, grid_y));
	}

	private final void calcPosAndDir(int pixel_x, int pixel_y) {
		float pixel_z = 0.5f;
		StrictGLU.gluUnProject(pixel_x, pixel_y, pixel_z, modl, tmp_camera.getProjectionModelView(), viewport, hit_result);
		float hit_x = hit_result[0];
		float hit_y = hit_result[1];
		float hit_z = hit_result[2];

		pixel_z = 0.1f;
		StrictGLU.gluUnProject(pixel_x, pixel_y, pixel_z, modl, tmp_camera.getProjectionModelView(), viewport, hit_result);

		float dx = hit_x - hit_result[0];
		float dy = hit_y - hit_result[1];
		float dz = hit_z - hit_result[2];
		float vec_len_inv = 1f/(float)StrictMath.sqrt(dx*dx + dy*dy + dz*dz);
		dir_vector[0] = dx*vec_len_inv;
		dir_vector[1] = dy*vec_len_inv;
		dir_vector[2] = dz*vec_len_inv;
	}

	private final boolean nearestLandscape(int pixel_x, int pixel_y) {
		pickLandscape();
		calcPosAndDir(pixel_x, pixel_y);
		return doNearestLandscape(hit_result[0], hit_result[1], hit_result[2], dir_vector[0], dir_vector[1], dir_vector[2]);
	}

	private final static float computeTMax(float bmin, float bmax, float c, float d) {
		if (d == 0) {
			return Float.POSITIVE_INFINITY;
		}
		float t1 = (bmin - c)/d;
		float t2 = (bmax - c)/d;
		return StrictMath.max(t1, t2);
	}

	private final static float computeTMin(float bmin, float bmax, float c, float d) {
		if (d == 0) {
			return Float.NEGATIVE_INFINITY;
		}
		float t1 = (bmin - c)/d;
		float t2 = (bmax - c)/d;
		return StrictMath.min(t1, t2);
	}

	private final boolean doNearestLandscape(float x, float y, float z, float dx, float dy, float dz) {
		if (getHeight(x, y) > z) {
			return false;
		}
		while (!patch_pick_set.isEmpty()) {
			BoundingBox bb = (BoundingBox)patch_pick_set.first();
			assert patch_pick_set.contains(bb);
			patch_pick_set.remove(bb);
			float tx_min = computeTMin(bb.bmin_x, bb.bmax_x, x, dx);
			float ty_min = computeTMin(bb.bmin_y, bb.bmax_y, y, dy);
			float tz_min = computeTMin(bb.bmin_z, bb.bmax_z, z, dz);
			float tx_max = computeTMax(bb.bmin_x, bb.bmax_x, x, dx);
			float ty_max = computeTMax(bb.bmin_y, bb.bmax_y, y, dy);
			float tz_max = computeTMax(bb.bmin_z, bb.bmax_z, z, dz);

			float t_min = StrictMath.max(tx_min, StrictMath.max(ty_min, tz_min));
			float t_max = StrictMath.min(tx_max, StrictMath.min(ty_max, tz_max));
			if (t_min < 0)
				t_min = 0;
			// If t_min is greater than t_max, the pick ray does not intersect the BB, therefore we skip it
			if (t_min >= t_max) {
				continue;
			}
			float t_min_x = x + t_min*dx;
			float t_min_y = y + t_min*dy;
			float t_min_z = z + t_min*dz;
			float t_min_height = getHeight(t_min_x, t_min_y);
			if (t_min_height >= 0.001f + t_min_z) {
//				System.out.println(t_min_x + " " + t_min_y + " " + t_min_height + " " + t_min_z);
/*com.oddlabs.tt.landscape.LandscapeTileIndices.debug = true;
World.getHeightMap().getNearestHeight(t_min_x, t_min_y);
com.oddlabs.tt.landscape.LandscapeTileIndices.debug = false;*/
				assert false;
//				return false;
			}
			boolean found_t_range = false;
			for (float t_scan = t_min; t_scan <= t_max; t_scan += PATCH_PICK_STEP) {
				float t_scan_next = (float)StrictMath.min(t_scan + PATCH_PICK_STEP, t_max);
				float t_scan_x = x + t_scan_next*dx;
				float t_scan_y = y + t_scan_next*dy;
				float t_scan_z = z + t_scan_next*dz;
				float t_scan_height = getHeight(t_scan_x, t_scan_y);
				if (t_scan_height >= t_scan_z - 0.001f) {
					t_min = t_scan;
					t_max = t_scan_next;
					found_t_range = true;
					break;
				}
			}
			if (!found_t_range) {
				continue;
			}
			float t_mid_x;
			float t_mid_y;
			float t_mid_z;
			float t_mid_height;
			float height_diff;
			float old_t_mid;
			float t_mid = Float.NaN;
			do {
				old_t_mid = t_mid;
				t_mid = (t_max + t_min)*.5f;
				t_mid_x = x + t_mid*dx;
				t_mid_y = y + t_mid*dy;
				t_mid_z = z + t_mid*dz;
				t_mid_height = getHeight(t_mid_x, t_mid_y);
				height_diff = t_mid_height - t_mid_z;
				if (height_diff >= 0)
					t_max = t_mid;
				else
					t_min = t_mid;
			} while (StrictMath.abs(height_diff) > PATCH_PICK_PRECISION && t_mid != old_t_mid);
			patch_hit_x = t_mid_x;
			patch_hit_y = t_mid_y;
			patch_hit_z = t_mid_height;
			return true;
		}
		return false;
	}

	private float getHeight(float x, float y) {
		return local_player.getWorld().getHeightMap().getNearestHeight(x, y);
	}

	public final void pickMapGoto(int x, int y, MapCamera camera) {
		setupPicking(camera.getState(), x, y, PICK_SIZE, PICK_SIZE);
		if (nearestLandscape(x, y))
			camera.mapGoto(patch_hit_x, patch_hit_y);
	}

	public final Target pickRallyPoint(CameraState camera, int x, int y, Building building) {
		setupPicking(camera, x, y, PICK_SIZE, PICK_SIZE);
		pickObjects();
		Target nearest = getNearestPick(element_pick_list, Target.class);
		if (nearest != null && nearest instanceof Building) {
			return nearest;
		} else if (nearestLandscape(x, y)) {
			int grid_x = UnitGrid.toGridCoordinate(patch_hit_x);
			int grid_y = UnitGrid.toGridCoordinate(patch_hit_y);
			return building.getUnitGrid().findGridTargets(grid_x, grid_y, 1, false)[0];
		} else {
			return null;
		}
	}

	public final void pickHover(CameraState camera, int x, int y) {
		setupPicking(camera, x, y, PICK_SIZE, PICK_SIZE);
		pickObjects();
		Target nearest = getNearestPick(element_pick_list, Target.class);
		Target new_current_hovered;
		if (nearest != null) {
			new_current_hovered = nearest;
		} else {
			pickResources();
			new_current_hovered = (TreeSupply)getNearestPick(tree_pick_list, Target.class);
		}
		if (current_hovered != new_current_hovered) {
			tool_tip_timer.resetTime();
			boolean old_tip = current_hovered instanceof ModelToolTip;
			boolean new_tip = new_current_hovered instanceof ModelToolTip;
			if (!old_tip && new_tip) {
				tool_tip_timer.start();
				render_tool_tip = false;
			}
			if (old_tip && !new_tip) {
				if (!render_tool_tip)
					tool_tip_timer.stop();
				else
					render_tool_tip = false;
			}
			current_hovered = new_current_hovered;
			if (new_tip)
				current_tooltip = new ToolTipAdapter((ModelToolTip)current_hovered, local_player);
			else
				current_tooltip = null;
		}
	}

	public void update(Object anim) {
		render_tool_tip = true;
		tool_tip_timer.stop();
	}

	public final ToolTip getCurrentToolTip() {
		return canRenderToolTip() ? current_tooltip : null;
	}

	public final Target getCurrentHovered() {
		return current_hovered;
	}

	public final boolean canRenderToolTip() {
		return render_tool_tip;
	}

	public final void resetCurrentHovered() {
		current_hovered = null;
		current_tooltip = null;
	}

	public final boolean pickLocation(CameraState camera, LandscapeLocation landscape_location) {
		int x = LocalInput.getMouseX();
		int y = LocalInput.getMouseY();
		setupPicking(camera, x, y, PICK_SIZE, PICK_SIZE);
		if (!nearestLandscape(x, y))
			return false;
		landscape_location.x = patch_hit_x;
		landscape_location.y = patch_hit_y;
		return true;
	}

	private final void setupPicking(CameraState camera, float x_center, float y_center, int width, int height) {
		proj.setIdentity();
		viewport[0] = 0; viewport[1] = 0; viewport[2] = LocalInput.getViewWidth(); viewport[3] = LocalInput.getViewHeight();
		StrictGLU.gluPickMatrix(proj, x_center, y_center, width, height, viewport);
		Renderer.multProjection(proj);
		tmp_camera.set(camera);
		tmp_camera.setView(proj);
	}

	private final void pickLandscape() {
		patch_pick_set.clear();
		landscape_renderer.pick(tmp_camera, false, patch_pick_set);
	}

	private final void pickObjects() {
		element_pick_list.clear();
		element_renderer.setup(tmp_camera);
		local_player.getWorld().getElementRoot().visit(element_renderer);
		sprite_sorter.distributeModels();
		render_queues.getAllPicks(element_pick_list);
	}

	private final void pickResources() {
		tree_pick_list.clear();
		tree_renderer.setup(tmp_camera);
		local_player.getWorld().getTreeRoot().visit(tree_renderer);
		sprite_sorter.distributeModels();
		tree_renderer.getAllPicks(tree_pick_list);
	}

	public final void debugRender() {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glPointSize(10f);
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex3f(patch_hit_x, patch_hit_y, patch_hit_z);
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private final strictfp class LandscapeLeafComparator implements Comparator {
		private int compare(CameraState camera_state, LandscapeLeaf l1, LandscapeLeaf l2) {
			float l1_dist = RenderTools.getCameraDistanceXYSquared(l1, camera_state.getCurrentX(), camera_state.getCurrentY());
			float l2_dist = RenderTools.getCameraDistanceXYSquared(l2, camera_state.getCurrentX(), camera_state.getCurrentY());
			if (l1_dist < l2_dist)
				return -1;
			else if (l1_dist > l2_dist)
				return 1;
			else if (l1.bmin_x < l2.bmin_x)
				return -1;
			else if (l1.bmin_x > l2.bmin_x)
				return 1;
			else if (l1.bmin_y < l2.bmin_y)
				return -1;
			else if (l1.bmin_y > l2.bmin_y)
				return 1;
			else {
				assert l1 == l2;
				return 0;
			}
		}

		public final int compare(Object o1, Object o2) {
			LandscapeLeaf l1 = (LandscapeLeaf)o1;
			LandscapeLeaf l2 = (LandscapeLeaf)o2;
			return compare(tmp_camera, l1, l2);
		}
	}
}
