package com.oddlabs.tt.pathfinder;

import com.oddlabs.tt.model.Selectable;

import java.util.*;

public final strictfp class FindOccupantFilter implements ScanFilter {
	private final float x;
	private final float y;
	private final float radius;
	private final Selectable src;
	private final Class type;
	private final List result;

	public FindOccupantFilter(float x, float y, float radius, Selectable src, Class type) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.src = src;
		this.type = type;
		result = new ArrayList();
	}

	public final int getMinRadius() {
		return 0;
	}

	public final int getMaxRadius() {
		return UnitGrid.toGridCoordinate(radius);
	}

	public final boolean filter(int grid_x, int grid_y, Occupant occ) {
		if (type.isInstance(occ) && occ != src) {
			Selectable s = (Selectable)occ;
			float dx = s.getPositionX() - x;
			float dy = s.getPositionY() - y;
			float squared_dist = dx*dx + dy*dy;
			if (!result.contains(s) && squared_dist < radius*radius) {
				result.add(s);
			}
		}
		return false;
	}

	public final List getResult() {
		return result;
	}
}
