package com.oddlabs.tt.particle;

public final strictfp class CloudFunction implements ParametricFunction {
	private final float radius_xy;
	private final float radius_z;

	public CloudFunction(float radius_xy, float radius_z) {
		this.radius_xy = radius_xy;
		this.radius_z = radius_z;
	}

	public final float getX(float u, float v) {
		return radius_xy*(float)StrictMath.sin(u)*(float)StrictMath.cos(v);
	}

	public final float getY(float u, float v) {
		return radius_xy*(float)StrictMath.sin(u)*(float)StrictMath.sin(v);
	}

	public final float getZ(float u, float v) {
		return radius_z*(float)StrictMath.cos(u);
	}
}
