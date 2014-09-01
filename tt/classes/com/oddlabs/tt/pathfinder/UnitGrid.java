package com.oddlabs.tt.pathfinder;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.font.Font;
import com.oddlabs.tt.font.TextLineRenderer;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.landscape.HeightMap;
import com.oddlabs.tt.util.DebugRender;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.landscape.HeightMap;

public final strictfp class UnitGrid {
	private final Region[][] regions;
	private final Occupant[][] occupants;
	private final HeightMap heightmap;

	private final boolean filter(ScanFilter filter, int x, int y) {
		if (x < 0 || y < 0 || x >= occupants.length || y >= occupants.length)
			return false;
		return filter.filter(x, y, occupants[y][x]);
	}

	public final Target[] findGridTargets(int center_grid_x, int center_grid_y, int num_targets, boolean grid_targets_only) {
		FindTargetsFilter filter = new FindTargetsFilter(num_targets, occupants.length, grid_targets_only);
		scan(filter, center_grid_x, center_grid_y);
		return filter.getTargets();
	}

	public final void scan(ScanFilter filter, int center_grid_x, int center_grid_y) {
		int radius = filter.getMinRadius();
		if (radius == 0) {
			if (filter(filter, center_grid_x, center_grid_y))
				return;
			radius++;
		}
		while (radius <= filter.getMaxRadius()) {
			int x = center_grid_x - radius;
			int x2 = center_grid_x + radius;
			for (int i = 0; i < 2*radius - 1; i++) {
				int y_i = center_grid_y - radius + 1 + i;
				if (filter(filter, x, y_i))
					return;
				if (filter(filter, x2, y_i))
					return;
			}
			int y = center_grid_y - radius;
			int y2 = center_grid_y + radius;
			for (int i = 0; i < 2*radius + 1; i++) {
				int x_i = center_grid_x - radius + i;
				if (filter(filter, x_i, y))
					return;
				if (filter(filter, x_i, y2))
					return;
			}
			radius++;
		}
	}

	public static float coordinateFromGrid(int g) {
		return (g + .5f)*HeightMap.METERS_PER_UNIT_GRID;
	}

	public static int toGridCoordinate(float c) {
		return (int)(c/HeightMap.METERS_PER_UNIT_GRID);
	}

	public final int getGridSize() {
		return occupants.length;
	}

	public UnitGrid(HeightMap heightmap) {
		this.heightmap = heightmap;
		int unit_grid_size = heightmap.getAccessGrid().length;
		occupants = new Occupant[unit_grid_size][unit_grid_size];
		regions = new Region[unit_grid_size][unit_grid_size];
	}

	public final Region getRegion(int grid_x, int grid_y) {
		Region region = regions[grid_y][grid_x];
		return region;
	}

	public final void setRegion(int grid_x, int grid_y, Region r) {
		assert regions[grid_y][grid_x] == null && !isGridOccupied(grid_x, grid_y);
		regions[grid_y][grid_x] = r;
	}

	public final boolean isGridOccupied(int grid_x, int grid_y) {
		return occupants[grid_y][grid_x] != null;
	}

	public final Occupant getOccupant(int grid_x, int grid_y) {
		return occupants[grid_y][grid_x];
	}

	public final void occupyGrid(int grid_x, int grid_y, Occupant occupant) {
		assert !isGridOccupied(grid_x, grid_y);
		occupants[grid_y][grid_x] = occupant;
	}

	public final void freeGrid(int grid_x, int grid_y, Occupant occupant) {
		assert occupants[grid_y][grid_x] == occupant: occupant + " trying to free " + grid_x + " " + grid_y + " where " + occupants[grid_y][grid_x] + " is.";
		occupants[grid_y][grid_x] = null;
	}

	public final void debugRenderRegions(float landscape_x, float landscape_y) {
		int RADIUS = 30;
		int center_x = toGridCoordinate(landscape_x);
		int center_y = toGridCoordinate(landscape_y);
		int start_x = StrictMath.max(0, center_x - RADIUS);
		int end_x = StrictMath.min(occupants.length - 0, center_x + RADIUS);
		int start_y = StrictMath.max(0, center_y - RADIUS);
		int end_y = StrictMath.min(occupants.length - 0, center_y + RADIUS);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glPointSize(3f);
		GL11.glBegin(GL11.GL_POINTS);
		Region last_region = null;
		for (int y = start_y; y < end_y; y++)
			for (int x = start_x; x < end_x; x++) {
				float xf = coordinateFromGrid(x);
				float yf = coordinateFromGrid(y);
				Region region = getRegion(x, y);
				if (region == null) {
					GL11.glColor3f(1f, 0f, 0f);
				} else {
					last_region = region;
					DebugRender.setColor(region.hashCode());
				}
				GL11.glVertex3f(xf, yf, heightmap.getNearestHeight(xf, yf) + 2f);
			}
		GL11.glEnd();
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(1f, 0f, 0f);
		if (last_region != null) {
			last_region.debugRenderConnections(heightmap);
			last_region.debugRenderConnectionsReset();
		}
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private final void debugRenderQuad(int x, int y) {
		final float OFFSET = 2f;
		final float RADIUS = .5f;
		int s = HeightMap.METERS_PER_UNIT_GRID;
		float xf = (x + .5f)*s;
		float yf = (y + .5f)*s;
		float z = heightmap.getNearestHeight(xf, yf) + OFFSET;
		GL11.glVertex3f(xf - RADIUS, yf - RADIUS, z);
		GL11.glVertex3f(xf + RADIUS, yf + RADIUS, z);
		GL11.glVertex3f(xf + RADIUS, yf - RADIUS, z);
		GL11.glVertex3f(xf - RADIUS, yf + RADIUS, z);
	}

	public final HeightMap getHeightMap() {
		return heightmap;
	}

	public final void debugRender(float landscape_x, float landscape_y) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(1f, 1f, 0f);
		int RADIUS = 30;
		int center_x = toGridCoordinate(landscape_x);
		int center_y = toGridCoordinate(landscape_y);
		int start_x = StrictMath.max(0, center_x - RADIUS);
		int end_x = StrictMath.min(occupants.length - 0, center_x + RADIUS);
		int start_y = StrictMath.max(0, center_y - RADIUS);
		int end_y = StrictMath.min(occupants.length - 0, center_y + RADIUS);
		for (int y = start_y; y < end_y; y++)
			for (int x = start_x; x < end_x; x++)
				if (isGridOccupied(x, y)) {
					debugRenderQuad(x, y);
				}
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		Font font = Skin.getSkin().getEditFont();
		final float OFFSET = 2f;
		int s = HeightMap.METERS_PER_UNIT_GRID;
		TextLineRenderer renderer = new TextLineRenderer(font);
		if (Globals.draw_axes) {
			GL11.glColor3f(1f, 1f, 1f);
			GL11.glEnable(GL11.GL_BLEND);
			for (int y = start_y; y < end_y; y++)
				for (int x = start_x; x < end_x; x++)
					if (!isGridOccupied(x, y)) {
						float xf = (x + .5f)*s;
						float yf = (y + .5f)*s;
						float z = heightmap.getNearestHeight(xf, yf) + OFFSET;
						GL11.glPushMatrix();
						GL11.glTranslatef(xf, yf, z);
						GL11.glScalef(.08f, .08f, .08f);
						font.setupQuads();
						renderer.render(4, 4, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, "" + heightmap.getBuildValue(x, y));
						font.resetQuads();
						GL11.glPopMatrix();
					}
			GL11.glDisable(GL11.GL_BLEND);
		}
		GL11.glColor3f(1f, 0f, 0f);
		List last_path_search = PathFinder.visited_list;
		if (last_path_search != null) {
			for (int i = 0; i < last_path_search.size(); i++) {
//				int[] coords = (int[])last_path_search.get(i);
				Node node = (Node)last_path_search.get(i);
				GL11.glPushMatrix();
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glBegin(GL11.GL_LINES);
				debugRender(node.getGridX(), node.getGridY());
				GL11.glEnd();

				GL11.glEnable(GL11.GL_TEXTURE_2D);
				float xf = node.getGridX()*s;
				float yf = node.getGridY()*s;
				float z = heightmap.getNearestHeight(xf, yf) + OFFSET;
				GL11.glTranslatef(xf, yf, z);
				GL11.glScalef(.08f, .08f, .08f);
				GL11.glEnable(GL11.GL_BLEND);
				font.setupQuads();
				renderer.render(4, 4, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, "" + node.getTotalCost());
				font.resetQuads();
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glPopMatrix();
			}
		}
	}
}
