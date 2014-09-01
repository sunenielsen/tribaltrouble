package com.oddlabs.tt.particle;

public final strictfp class StretchParticle extends Particle {
	private float src_x = 0f;
	private float src_y = 0f;
	private float src_z = 0f;
	private float dst_x = 0f;
	private float dst_y = 0f;
	private float dst_z = 0f;
	private float src_width = 0f;
	private float dst_width = 0f;

	public final void update(float t) {
		super.update(t);
	}

	public final void setSrc(float x, float y, float z) {
		src_x = x;
		src_y = y;
		src_z = z;
	}

	public final float getSrcX() {
		return src_x;
	}

	public final float getSrcY() {
		return src_y;
	}

	public final float getSrcZ() {
		return src_z;
	}

	public final void setDst(float x, float y, float z) {
		dst_x = x;
		dst_y = y;
		dst_z = z;
	}

	public final float getDstX() {
		return dst_x;
	}

	public final float getDstY() {
		return dst_y;
	}

	public final float getDstZ() {
		return dst_z;
	}

	public final void setSrcWidth(float width) {
		src_width = width;
	}

	public final float getSrcWidth() {
		return src_width;
	}

	public final void setDstWidth(float width) {
		dst_width = width;
	}

	public final float getDstWidth() {
		return dst_width;
	}
}
