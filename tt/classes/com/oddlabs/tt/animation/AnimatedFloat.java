package com.oddlabs.tt.animation;

public final strictfp class AnimatedFloat {
	public float val;
	public float cval;
	public float dval;

	public AnimatedFloat() {
		reset(0f);
	}

	public final void change(float animation_complete) {
		if (animation_complete >= 1.0f)
			val = dval;
		cval = (dval - val) * animation_complete + val;
	}

	public final void updateVal() {
		val = cval;
	}

	public final void reset(float new_val) {
		val = new_val; dval = new_val; cval = new_val;
	}
}
