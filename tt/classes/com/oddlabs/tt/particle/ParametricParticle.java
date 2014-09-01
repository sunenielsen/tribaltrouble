package com.oddlabs.tt.particle;

final strictfp class ParametricParticle extends Particle {
	private final ParametricFunction function;
	private final float offset_x;
	private final float offset_y;
	private final float offset_z;

	private float velocity_u = 0f;
	private float velocity_v = 0f;
	private float u = 0f;
	private float v = 0f;

	public ParametricParticle(ParametricFunction function, float u, float v, float offset_x, float offset_y, float offset_z) {
		this.function = function;
		this.u = u;
		this.v = v;
		this.offset_x = offset_x;
		this.offset_y = offset_y;
		this.offset_z = offset_z;
	}

	public final void update(float t, float scale_x, float scale_y, float scale_z) {
		super.update(t);
		u += velocity_u*t;
		v += velocity_v*t;

		float x = offset_x + scale_x*function.getX(u, v);
		float y = offset_y + scale_y*function.getY(u, v);
		float z = offset_z + scale_z*function.getZ(u, v);
		setPos(x, y, z);
	}

	public final void setVelocity(float u, float v) {
		velocity_u = u;
		velocity_v = v;
	}

	public final float getVelocityU() {
		return velocity_u;
	}

	public final float getVelocityV() {
		return velocity_v;
	}
}
