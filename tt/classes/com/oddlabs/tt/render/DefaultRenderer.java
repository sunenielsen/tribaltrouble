package com.oddlabs.tt.render;

import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Icons;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.landscape.AbstractTreeGroup;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.resource.WorldInfo;
import com.oddlabs.tt.resource.FogInfo;
import com.oddlabs.tt.scenery.Water;
import com.oddlabs.tt.scenery.Sky;
import com.oddlabs.tt.model.AbstractElementNode;
import com.oddlabs.tt.model.Model;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.particle.Lightning;
import com.oddlabs.tt.particle.Emitter;
import com.oddlabs.tt.resource.WorldGenerator;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.util.ToolTip;
import com.oddlabs.tt.viewer.Selection;
import com.oddlabs.tt.viewer.Cheat;
import com.oddlabs.tt.viewer.AmbientAudio;
import com.oddlabs.tt.util.DebugRender;
import com.oddlabs.tt.util.BoundingBox;

import java.util.List;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.BufferUtils;

public final strictfp class DefaultRenderer implements UIRenderer {
	private final Picker picker;
	private final FogInfo fog_info;
	private final Water water;
	private final Sky sky;
	private final LandscapeRenderer landscape_renderer;
	private final World world;
	private final ElementRenderer element_renderer;
	private final TreeRenderer tree_renderer;
	private final SpriteSorter sprite_sorter;
	private final RenderQueues render_queues;
	private final FloatBuffer light_array;
	private final Cheat cheat;

	private Building selected_building;

	private void drawAxes() {
		if (Globals.draw_axes) {
			float center = world.getHeightMap().getMetersPerWorld()/2;
			float z = world.getHeightMap().getNearestHeight(center, center);
			GL11.glTranslatef(center, center, z);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glBegin(GL11.GL_LINES);
			// X axis
			GL11.glColor3f(1, 0, 0);
			GL11.glVertex3f(0, 0, 0);
			GL11.glVertex3f(10, 0, 0);
			// Y axis
			GL11.glColor3f(0, 1, 0);
			GL11.glVertex3f(0, 0, 0);
			GL11.glVertex3f(0, 10, 0);
			// Z axis
			GL11.glColor3f(0, 0, 1);
			GL11.glVertex3f(0, 0, 0);
			GL11.glVertex3f(0, 0, 10);
			GL11.glEnd();

			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}

	public DefaultRenderer(Cheat cheat, Player local_player, RenderQueues render_queues, int terrain_type, WorldInfo world_info, LandscapeRenderer landscape_renderer, Picker picker, Selection selection, WorldGenerator generator) {
		this.world = local_player.getWorld();
		this.cheat = cheat;
		this.light_array = BufferUtils.createByteBuffer(4*4).asFloatBuffer();
		light_array.put(new float[]{-1.0f, 0.0f, 1.0f, 0.0f});
		light_array.rewind();
		this.render_queues = render_queues;
		this.sprite_sorter = new SpriteSorter();
		this.picker = picker;
		this.element_renderer = new ElementRenderer(local_player, landscape_renderer, render_queues, picker, false, sprite_sorter, selection);
		this.tree_renderer = new TreeRenderer(world, cheat, terrain_type, world_info.trees, world_info.palm_trees, sprite_sorter, picker.getRespondManager());
		this.landscape_renderer = landscape_renderer;
		this.fog_info = Landscape.getFogInfo(generator.getTerrainType(), generator.getMetersPerWorld());
		this.water = new Water(world.getHeightMap(), generator.getTerrainType());
		this.sky = new Sky(landscape_renderer, generator.getTerrainType());
	}

	public final void renderGUI(GUIRoot gui_root) {
		if (cheat.isEnabled())
			Icons.getIcons().getCheatIcon().render(gui_root.getWidth() - Icons.getIcons().getCheatIcon().getWidth() - 10, 5);
	}

	public final void setSelectedBuilding(Building building) {
		this.selected_building = building;
	}

	private void renderRallyPoint(CameraState camera_state) {
		if (selected_building != null && !selected_building.isDead() && selected_building.hasRallyPoint())
			doRenderRallyPoint(camera_state);
	}

	private final void doRenderRallyPoint(CameraState camera_state) {
		float rally_point_dir_x = 1f;
		float rally_point_dir_y = 0f;
		Target rally_point = selected_building.getRallyPoint();
		float dx = camera_state.getCurrentX() - rally_point.getPositionX();
		float dy = camera_state.getCurrentY() - rally_point.getPositionY();
		float len = (float)StrictMath.sqrt(dx*dx + dy*dy);
		if (len > 0.1f) {
			float inv_len = 1f/len;
			rally_point_dir_x = dx*inv_len;
			rally_point_dir_y = dy*inv_len;
		}
		Race race = selected_building.getOwner().getRace();
		SpriteRenderer rally_point_renderer = render_queues.getRenderer(race.getRallyPoint());
		rally_point_renderer.setup(0, false);
		float x = rally_point.getPositionX();
		float y = rally_point.getPositionY();
		float z = world.getHeightMap().getNearestHeight(rally_point.getPositionX(), rally_point.getPositionY());
		if (rally_point instanceof Building) {
			Building rally_building = (Building)rally_point;
			x += rally_building.getBuildingTemplate().getRallyX();
			y += rally_building.getBuildingTemplate().getRallyY();
			z += rally_building.getBuildingTemplate().getRallyZ();
		}
		RenderTools.translateAndRotate(x, y, z, rally_point_dir_x, rally_point_dir_y);
		Sprite.setupDecalColor(SelectableVisitor.getTeamColor(selected_building));
		rally_point_renderer.getSpriteList().render(0, 0, 0f);
		rally_point_renderer.getSpriteList().reset(0, false, false);
	}

	public final void pickHover(boolean can_hover_behind, CameraState camera, int x, int y) {
		if (can_hover_behind) {
			picker.pickHover(camera, LocalInput.getMouseX(), LocalInput.getMouseY());
		} else {
			picker.resetCurrentHovered();
		}
	}

	public final ToolTip getToolTip() {
		return picker.getCurrentToolTip();
	}

	public final TreeRenderer getTreeRenderer() {
		return tree_renderer;
	}

	public final boolean clearColorBuffer() {
		return Globals.clear_frame_buffer || cheat.line_mode;
	}

	public void render(AmbientAudio ambient, CameraState frustum_state, GUIRoot gui_root) {
		ambient.updateSoundListener(frustum_state, world.getHeightMap());
		if (Globals.line_mode || cheat.line_mode) {
			GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
			GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_LINE);
		}

		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, light_array);
		if (Globals.draw_sky) {
			sky.render();
		}

		fog_info.enableFog(frustum_state.getCurrentZ());
		if (Globals.draw_sky) {
			sky.renderSeaBottom();
		}

		// render landscape (must be before trees and misc, cause they use data calculated here)
		if (Globals.process_landscape) {
			landscape_renderer.prepareAll(frustum_state, false);
			landscape_renderer.renderAll();
		}
		// frustum check and placement
		if (Globals.process_trees) {
			tree_renderer.setup(frustum_state);
			world.getTreeRoot().visit(tree_renderer);
		}
		if (Globals.process_misc) {
			element_renderer.setup(frustum_state);
			world.getElementRoot().visit(element_renderer);
		}
		
		// Sort sprites
		sprite_sorter.distributeModels();
		
		// render shadows
		if (Globals.process_shadows) {
			render_queues.renderShadows(landscape_renderer);
		}

		gui_root.getDelegate().render3D(landscape_renderer, render_queues);
		
		// render
		if (Globals.process_trees) {
			tree_renderer.renderAll();
		}
		if (Globals.process_misc) {
			render_queues.renderAll();
		}

		drawAxes();

		if (Globals.draw_water)
			water.render(sky);

		if (Globals.process_misc) 
			render_queues.renderBlends();

		LightningRenderer.render(render_queues, element_renderer.getRenderState().getLightningQueue(), frustum_state);
		EmitterRenderer.render(render_queues, element_renderer.getRenderState().getEmitterQueue(), frustum_state);
		renderRallyPoint(frustum_state);

/*		float landscape_x = GUIRoot.getGUIRoot().getLandscapeLocationX();
		float landscape_y = GUIRoot.getGUIRoot().getLandscapeLocationY();
		if (Globals.isBoundsEnabled(Globals.BOUNDING_REGIONS)) {
			UnitGrid.getGrid().debugRenderRegions(landscape_x, landscape_y);
		}
*/
		if (Globals.isBoundsEnabled(Globals.BOUNDING_OCCUPATION)) {
			picker.debugRender();
		}

/*		if (Globals.isBoundsEnabled(Globals.BOUNDING_UNIT_GRID)) {
			java.util.Iterator it = com.oddlabs.tt.model.SelectionArmy.getSelection().getSet().iterator();
			while (it.hasNext()) {
				Object next = it.next();
				if (next instanceof com.oddlabs.tt.model.Unit) {
					com.oddlabs.tt.model.Unit s_unit = (com.oddlabs.tt.model.Unit)next;
					s_unit.debugRender();
				}
			}
//			UnitGrid.getGrid().debugRender(landscape_x, landscape_y);
		}*/
		fog_info.disableFog();
		if (Globals.line_mode || (cheat.line_mode)) {
			GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
			GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_FILL);
		}
	}
}
