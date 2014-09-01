package com.oddlabs.tt.delegate;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.render.RenderQueues;
import com.oddlabs.tt.render.LandscapeRenderer;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.landscape.HeightMap;
import com.oddlabs.tt.render.SpriteRenderer;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.BuildingTemplate;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.player.BuildingSiteScanFilter;
import com.oddlabs.tt.render.BuildingSiteRenderer;
import com.oddlabs.tt.render.LandscapeLocation;
import com.oddlabs.tt.render.Picker;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.gui.*;

public final strictfp class PlacingDelegate extends ControllableCameraDelegate {
	private final static int GRID_RADIUS = 20;
	private final static FloatBuffer color;
	private final static LandscapeLocation landscape_hit = new LandscapeLocation();
	
	private final BuildingSiteRenderer site_renderer = new BuildingSiteRenderer();
	private final int building_index;

	static {
		color = BufferUtils.createFloatBuffer(4).put(new float[]{1f, 1f, 1f, 1f});
		color.rewind();
	}

	public PlacingDelegate(WorldViewer viewer, CameraState old_camera, int building_index) {
		super(viewer, new GameCamera(viewer, old_camera));
		this.building_index = building_index;
	}

	private final BuildingTemplate getTemplate() {
		return getViewer().getLocalPlayer().getRace().getBuildingTemplate(building_index);
	}

	public final void placeObject() {
		getViewer().getPicker().pickLocation(getCamera().getState(), landscape_hit);
		UnitGrid unit_grid = getViewer().getWorld().getUnitGrid();
		int placing_grid_x = UnitGrid.toGridCoordinate(landscape_hit.x);
		int placing_grid_y = UnitGrid.toGridCoordinate(landscape_hit.y);
		if (Building.isPlacingLegal(getViewer().getWorld().getUnitGrid(), getTemplate(), placing_grid_x, placing_grid_y)) {
			Selectable[] peons = getViewer().getSelection().getCurrentSelection().filter(Abilities.BUILD);
			if (peons.length > 0) {
				getViewer().getPeerHub().getPlayerInterface().placeBuilding(peons, building_index, placing_grid_x, placing_grid_y);
			}
			pop();
		}
	}

	/*private final void glGridVertex(int grid_x, int grid_y, int meters_per_grid_unit) {
		float x = grid_x*meters_per_grid_unit;
		float y = grid_y*meters_per_grid_unit;
		float z = getViewer().getWorld().getHeightMap().getVisuallyCorrectHeight(x, y);
		GL11.glVertex3f(x, y, z);
	}
	*/
	public final void keyPressed(KeyboardEvent event) {
		getCamera().keyPressed(event);
		switch (event.getKeyCode()) {
			case Keyboard.KEY_ESCAPE:
				pop();
				break;
			default:
				if (event.getKeyCode() != Keyboard.KEY_SPACE && event.getKeyCode() != Keyboard.KEY_RETURN)
					super.keyPressed(event);
				break;
		}
	}

	public void keyReleased(KeyboardEvent event) {
		getCamera().keyReleased(event);
	}

	public final void mousePressed(int button, int x, int y) {
		if (button == LocalInput.LEFT_BUTTON) {
			placeObject();
		} else if (button == LocalInput.RIGHT_BUTTON) {
			pop();
		} else {
			super.mousePressed(button, x, y);
		}						
	}

	public boolean renderCursor() {
		return true;
	}

	public final void render3D(LandscapeRenderer renderer, RenderQueues queues) {
		getViewer().getPicker().pickLocation(getCamera().getState(), landscape_hit);
		UnitGrid unit_grid = getViewer().getWorld().getUnitGrid();
		int placing_grid_x = UnitGrid.toGridCoordinate(landscape_hit.x) - (getTemplate().getPlacingSize() - 1);
		int placing_grid_y = UnitGrid.toGridCoordinate(landscape_hit.y) - (getTemplate().getPlacingSize() - 1);
		int placing_center_grid_x = UnitGrid.toGridCoordinate(landscape_hit.x);
		int placing_center_grid_y = UnitGrid.toGridCoordinate(landscape_hit.y);

		float center_x = HeightMap.METERS_PER_UNIT_GRID*(placing_grid_x + (getTemplate().getPlacingSize() - .5f));
		float center_y = HeightMap.METERS_PER_UNIT_GRID*(placing_grid_y + (getTemplate().getPlacingSize() - .5f));

		BuildingSiteScanFilter filter = new BuildingSiteScanFilter(unit_grid, getTemplate(), GRID_RADIUS, false);
		unit_grid.scan(filter, placing_center_grid_x, placing_center_grid_y);
		List target_list = filter.getResult();
		site_renderer.renderSites(renderer, target_list, center_x, center_y, 2*GRID_RADIUS);
		

		
	//	GL11.glEnable(GL11.GL_BLEND);
		//renderPlaceGrid(placing_grid_x, placing_grid_y, getTemplate().getPlacingSize()*2 - 1);
	//	GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		SpriteRenderer built_renderer = queues.getRenderer(getTemplate().getBuiltRenderer());
		built_renderer.setupWithColor(0, color, false, true);
		if (Building.isPlacingLegal(unit_grid, getTemplate(), placing_center_grid_x, placing_center_grid_y))
			GL11.glColor4f(1f, 1f, 1f, .8f);
		else 
			GL11.glColor4f(1f, 0f, 0f, .8f);
		float z = getViewer().getWorld().getHeightMap().getNearestHeight(center_x, center_y);
		GL11.glPushMatrix();
		GL11.glTranslatef(center_x, center_y, z);
		//Model.setupDecalColor(getViewer().getLocalPlayer().getColor());
		built_renderer.getSpriteList().render(0, 0, 0);
		built_renderer.getSpriteList().reset(0, false, true);
		GL11.glPopMatrix();
	//	GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
	//	GL11.glDisable(GL11.GL_BLEND);
	}
}
