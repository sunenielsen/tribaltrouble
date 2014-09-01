package com.oddlabs.tt.particle;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.lwjgl.util.vector.Vector3f;

public strictfp class Particle {
	private final float u1;
	private final float v1;
	private final float u2;
	private final float v2;
	private final float u3;
	private final float v3;
	private final float u4;
	private final float v4;

	private float position_x = 0f;
	private float position_y = 0f;
	private float position_z = 0f;
	private float color_r = 0f;
	private float color_g = 0f;
	private float color_b = 0f;
	private float color_a = 0f;
	private float delta_color_r = 0f;
	private float delta_color_g = 0f;
	private float delta_color_b = 0f;
	private float delta_color_a = 0f;
	private float growth_rate_x;
	private float growth_rate_y;
	private float growth_rate_z;
	private float radius_x;
	private float radius_y;
	private float radius_z;
	private int type;
	private float energy;

	public Particle() {
		this(0f);
	}

	public Particle(float angle) {
		Matrix4f rot_matrix = new Matrix4f();
		Vector3f axis = new Vector3f();
		Vector4f uv_vector = new Vector4f();
		Vector4f transform_uv_vector = new Vector4f();
		
		rot_matrix.setIdentity();
		axis.set(0f, 0f, 1f);
		rot_matrix.rotate(angle, axis);

		uv_vector.set(-.5f, -.5f, 0f, 0f);
		Matrix4f.transform(rot_matrix, uv_vector, transform_uv_vector);
		u1 = transform_uv_vector.getX() + .5f;
		v1 = transform_uv_vector.getY() + .5f;
		
		uv_vector.set(.5f, -.5f, 0f, 0f);
		Matrix4f.transform(rot_matrix, uv_vector, transform_uv_vector);
		u2 = transform_uv_vector.getX() + .5f;
		v2 = transform_uv_vector.getY() + .5f;

		uv_vector.set(.5f, .5f, 0f, 0f);
		Matrix4f.transform(rot_matrix, uv_vector, transform_uv_vector);
		u3 = transform_uv_vector.getX() + .5f;
		v3 = transform_uv_vector.getY() + .5f;

		uv_vector.set(-.5f, .5f, 0f, 0f);
		Matrix4f.transform(rot_matrix, uv_vector, transform_uv_vector);
		u4 = transform_uv_vector.getX() + .5f;
		v4 = transform_uv_vector.getY() + .5f;
	}

	public final float getU1() {
		return u1;
	}

	public final float getV1() {
		return v1;
	}

	public final float getU2() {
		return u2;
	}

	public final float getV2() {
		return v2;
	}

	public final float getU3() {
		return u3;
	}

	public final float getV3() {
		return v3;
	}

	public final float getU4() {
		return u4;
	}

	public final float getV4() {
		return v4;
	}

	public void update(float t) {
		color_r += delta_color_r*t;
		color_g += delta_color_g*t;
		color_b += delta_color_b*t;
		color_a += delta_color_a*t;
		setRadius(radius_x + growth_rate_x*t, radius_y + growth_rate_y*t, radius_z + growth_rate_z*t);
		energy -= t;
	}

	public final void setPos(float x, float y, float z) {
		position_x = x;
		position_y = y;
		position_z = z;
	}

	public final float getPosX() {
		return position_x;
	}

	public final float getPosY() {
		return position_y;
	}

	public final float getPosZ() {
		return position_z;
	}

	final void setColor(float r, float g, float b, float a) {
		color_r = r;
		color_g = g;
		color_b = b;
		color_a = a;
	}

	public final float getColorR() {
		return color_r;
	}

	public final float getColorG() {
		return color_g;
	}

	public final float getColorB() {
		return color_b;
	}

	public final float getColorA() {
		return color_a;
	}

	public final void setDeltaColor(float r, float g, float b, float a) {
		delta_color_r = r;
		delta_color_g = g;
		delta_color_b = b;
		delta_color_a = a;
	}

	public final float getDeltaColorR() {
		return delta_color_r;
	}

	public final float getDeltaColorG() {
		return delta_color_g;
	}

	public final float getDeltaColorB() {
		return delta_color_b;
	}

	public final float getDeltaColorA() {
		return delta_color_a;
	}

	public final void setEnergy(float energy) {
		this.energy = energy;
	}

	public final float getEnergy() {
		return energy;
	}

	public final void setType(int type) {
		this.type = type;
	}

	public final int getType() {
		return type;
	}

	public final void setGrowthRate(float growth_rate_x, float growth_rate_y, float growth_rate_z) {
		this.growth_rate_x = growth_rate_x;
		this.growth_rate_y = growth_rate_y;
		this.growth_rate_z = growth_rate_z;
	}

	public final float getGrowthRateX() {
		return growth_rate_x;
	}

	public final float getGrowthRateY() {
		return growth_rate_y;
	}

	public final float getGrowthRateZ() {
		return growth_rate_z;
	}

	public final void setRadius(float radius_x, float radius_y, float radius_z) {
		this.radius_x = radius_x;
		this.radius_y = radius_y;
		this.radius_z = radius_z;
	}

	public final float getRadiusX() {
		return radius_x;
	}

	public final float getRadiusY() {
		return radius_y;
	}

	public final float getRadiusZ() {
		return radius_z;
	}
}
