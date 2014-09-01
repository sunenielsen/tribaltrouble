package com.oddlabs.tt.render;

import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.util.PocketList;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.camera.CameraState;

public final strictfp class SpriteSorter {
	public final static int DETAIL_POINT = 1;
	public final static int DETAIL_POLYGON = 2;

	private final static int LOW_DETAIL_DIST = 200;

	private final PocketList sorted_models = new PocketList(LOW_DETAIL_DIST);
	private final int polycount_limit;

	private int used_polys = 0;
	
	public SpriteSorter() {
		this(Globals.UNIT_HIGH_POLY_COUNT[Settings.getSettings().graphic_detail]);
	}

	private SpriteSorter(int polycount_limit) {
		this.polycount_limit = polycount_limit;
	}

	public final int add(LODObject model, CameraState camera, boolean point) {
		if (point && camera.inNoDetailMode()) {
			model.markDetailPoint();
			return DETAIL_POINT;
		}
		used_polys += model.getTriangleCount(SpriteRenderer.LOW_POLY);

		float dist_squared = model.getEyeDistanceSquared();
		if (dist_squared >= LOW_DETAIL_DIST*LOW_DETAIL_DIST) {
			model.markDetailPolygon(SpriteRenderer.LOW_POLY);
		} else {
			addToPocket(dist_squared, model);
		}
		return DETAIL_POLYGON;
	}

	private final void addToPocket(float dist_squared, LODObject model) {
		// We can use Math here instead of StrictMath because the dist does not affect game state
		int dist = (int)Math.sqrt(dist_squared);
		sorted_models.add(dist, model);
	}

	public final void distributeModels() {
		distributeHighPolygons();
		while (sorted_models.size() > 0) {
			LODObject model = (LODObject)sorted_models.removeBest();
			model.markDetailPolygon(SpriteRenderer.LOW_POLY);
		}
		assert sorted_models.size() == 0;
		sorted_models.clear();
		used_polys = 0;
	}

	private final void distributeHighPolygons() {
		while (used_polys < polycount_limit) {
			if (sorted_models.size() > 0) {
				LODObject model = (LODObject)sorted_models.removeBest();
				used_polys -= model.getTriangleCount(SpriteRenderer.LOW_POLY);
				used_polys += model.getTriangleCount(SpriteRenderer.HIGH_POLY);
				model.markDetailPolygon(SpriteRenderer.HIGH_POLY);
			} else
				return;
		}
	}
}
