package com.oddlabs.tt.util;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.landscape.HeightMap;

public strictfp class BoundingBox {
	private final static long serialVersionUID = 1;

	private final static Vector4f temp_vec = new Vector4f();
	private final static Vector4f temp_vec2 = new Vector4f();

	public float bmin_x = Float.POSITIVE_INFINITY;
	public float bmin_y = Float.POSITIVE_INFINITY;
	public float bmin_z = Float.POSITIVE_INFINITY;
	public float bmax_x = Float.NEGATIVE_INFINITY;
	public float bmax_y = Float.NEGATIVE_INFINITY;
	public float bmax_z = Float.NEGATIVE_INFINITY;
	private float cx;
	private float cy;
	private float cz;

	public String toString() {
		return "bmx " + bmin_x + " bmy " + bmin_y + " bmz " + bmin_z + " bxx " + bmax_x + " bxy " + bmax_y + " bxz " + bmax_z;
	}

	protected final boolean collides(float bmin_x, float bmax_x, float bmin_y, float bmax_y, float bmin_z, float bmax_z) {
		return (bmin_x <= this.bmax_x && bmax_x > this.bmin_x &&
				bmin_y <= this.bmax_y && bmax_y > this.bmin_y &&
				bmin_z <= this.bmax_z && bmax_z > this.bmin_z);
	}

	public final boolean collides(BoundingBox other) {
		return collides(other.bmin_x, other.bmax_x, other.bmin_y, other.bmax_y, other.bmin_z, other.bmax_z);
	}

	protected final boolean contains(float bmin_x, float bmax_x, float bmin_y, float bmax_y, float bmin_z, float bmax_z) {
		return (bmax_x <= this.bmax_x && bmin_x > this.bmin_x &&
				bmax_y <= this.bmax_y && bmin_y > this.bmin_y &&
				bmax_z <= this.bmax_z && bmin_z > this.bmin_z);
	}

	public final boolean contains(BoundingBox other) {
		return contains(other.bmin_x, other.bmax_x, other.bmin_y, other.bmax_y, other.bmin_z, other.bmax_z);
	}

	public final float computeMinDistanceSquared(float min_dist_to_box) {
		float box_radius = computeCenteredRadius();
		box_radius += min_dist_to_box;
		return box_radius*box_radius;
	}

	private final float computeCenteredRadius() {
		float radius_squared_x = cx - bmin_x;
		float radius_squared_y = cy - bmin_y;
		float radius_squared_z = cz - bmin_z;
		return (float)StrictMath.sqrt(radius_squared_x*radius_squared_x + radius_squared_y*radius_squared_y + radius_squared_z*radius_squared_z);
	}

	public final float getCX() {
		return cx;
	}

	public final float getCY() {
		return cy;
	}

	public final float getCZ() {
		return cz;
	}

	public final void checkBoundsY(float y) {
		if (y < bmin_y)
			bmin_y = y;
		else if (y > bmax_y)
			bmax_y = y;
		cy = (bmax_y + bmin_y)*0.5f;
	}

	public final void checkBoundsX(float x) {
		if (x < bmin_x)
			bmin_x = x;
		else if (x > bmax_x)
			bmax_x = x;
		cx = (bmax_x + bmin_x)*0.5f;
	}

	public final void checkBoundsZ(float z) {
		if (z < bmin_z)
			bmin_z = z;
		else if (z > bmax_z)
			bmax_z = z;
		cz = (bmax_z + bmin_z)*0.5f;
	}

	public final void checkBounds(float bmin_x, float bmax_x, float bmin_y, float bmax_y, float bmin_z, float bmax_z) {
		checkBoundsX(bmin_x);
		checkBoundsX(bmax_x);
		checkBoundsY(bmin_y);
		checkBoundsY(bmax_y);
		checkBoundsZ(bmin_z);
		checkBoundsZ(bmax_z);
	}

	public final void checkBoundsXY(BoundingBox other) {
		checkBoundsX(other.bmin_x);
		checkBoundsX(other.bmax_x);
		checkBoundsY(other.bmin_y);
		checkBoundsY(other.bmax_y);
	}

	public final void checkBounds(BoundingBox other) {
		checkBoundsXY(other);
		checkBoundsZ(other.bmin_z);
		checkBoundsZ(other.bmax_z);
	}

	public final void setInfiniteBounds() {
		this.bmin_x = Float.NEGATIVE_INFINITY;
		this.bmax_x = Float.POSITIVE_INFINITY;
		this.bmin_y = Float.NEGATIVE_INFINITY;
		this.bmax_y = Float.POSITIVE_INFINITY;
		this.bmin_z = Float.NEGATIVE_INFINITY;
		this.bmax_z = Float.POSITIVE_INFINITY;
	}

	public final void setBounds(BoundingBox other) {
		this.bmin_x = other.bmin_x;
		this.bmax_x = other.bmax_x;
		this.bmin_y = other.bmin_y;
		this.bmax_y = other.bmax_y;
		this.bmin_z = other.bmin_z;
		this.bmax_z = other.bmax_z;
		this.cx = other.cx;
		this.cy = other.cy;
		this.cz = other.cz;
	}

	public final void setBounds(float bmin_x, float bmax_x, float bmin_y, float bmax_y, float bmin_z, float bmax_z) {
		this.bmin_x = bmin_x;
		this.bmax_x = bmax_x;
		this.bmin_y = bmin_y;
		this.bmax_y = bmax_y;
		this.bmin_z = bmin_z;
		this.bmax_z = bmax_z;
		computeCenter();
	}

	private final void computeXYCenter() {
		cx = (bmax_x + bmin_x)*0.5f;
		cy = (bmax_y + bmin_y)*0.5f;
	}

	public final float computeCenteredXYRadius() {
		float radius_squared_x = cx - bmin_x;
		float radius_squared_y = cy - bmin_y;
		return (float)StrictMath.sqrt(radius_squared_x*radius_squared_x + radius_squared_y*radius_squared_y);
	}

	private final float computeXYRadius() {
		float longest_x = StrictMath.max(bmax_x, -bmin_x);
		float longest_y = StrictMath.max(bmax_y, -bmin_y);
		return (float)StrictMath.sqrt(longest_x*longest_x + longest_y*longest_y);
	}

	public final void transformBounds(Matrix4f matrix) {
		temp_vec.set(bmin_x, bmin_y, bmin_z, 1f);
		Matrix4f.transform(matrix, temp_vec, temp_vec2);
		bmin_x = temp_vec2.x;
		bmin_y = temp_vec2.y;
		bmin_z = temp_vec2.z;
		temp_vec.set(bmax_x, bmax_y, bmax_z, 1f);
		Matrix4f.transform(matrix, temp_vec, temp_vec2);
		bmax_x = temp_vec2.x;
		bmax_y = temp_vec2.y;
		bmax_z = temp_vec2.z;
		computeCenter();
	}

	private final void computeCenter() {
		computeXYCenter();
		cz = (bmax_z + bmin_z)*0.5f;
	}

	/**
	 * Make the bounding box contain all rotation of the current box, in the XY plane
	 */
	public final void maximizeXYPlane() {
		float len = computeXYRadius();

		bmin_x = -len;
		bmax_x = len;
		bmin_y = -len;
		bmax_y = len;

		computeXYCenter();
	}

	public final void setBoundsFromLandscape(HeightMap heightmap, int start_x, int start_y, int size_x, int size_y) {
		float corner1 = heightmap.getWrappedHeight(start_x, start_y);
		setBounds(start_x*HeightMap.METERS_PER_UNIT_GRID, (start_x + size_x)*HeightMap.METERS_PER_UNIT_GRID, start_y*HeightMap.METERS_PER_UNIT_GRID, (start_y + size_y)*HeightMap.METERS_PER_UNIT_GRID, corner1, corner1);
		for (int grid_y = 0; grid_y <= size_x; grid_y++)
			for (int grid_x = 0; grid_x <= size_y; grid_x++) {
				float height = heightmap.getWrappedHeight(start_x + grid_x, start_y + grid_y);
				checkBoundsZ(height);
			}
	}
}
