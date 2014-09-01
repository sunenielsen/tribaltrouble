package com.oddlabs.procedural;

public final strictfp class Tools {
	private Tools() {
	}

	public final static float modulo(float x, float n) {
		return x - n*(float)StrictMath.floor(x/n);
	}

	public final static int modulo(int x, int n) {
		return (int)(x - n*(float)StrictMath.floor((float)x/n));
	}

	public final static float interpolateLinear(float v1, float v2, float fraction) {
		return v1*(1f - fraction) + v2*fraction;
	}

	public final static float interpolateSmooth(float v1, float v2, float fraction) {
		if (fraction < 0.5f) {
			fraction = 2f*fraction*fraction;
		} else {
			fraction = 1f - 2f*(fraction - 1f)*(fraction - 1f);
		}
		return v1*(1f - fraction) + v2*fraction;
	}

	public final static float interpolateSmooth2(float v1, float v2, float fraction) {
		float fraction2 = fraction*fraction;
		fraction = 3*fraction2 - 2f*fraction*fraction2;
		return v1*(1f - fraction) + v2*fraction;
	}

	public final static float interpolateCubic(float v0, float v1, float v2, float v3, float fraction) {
		float p = (v3 - v2) - (v0 - v1);
		float q = (v0 - v1) - p;
		float r = v2 - v0;
		float fraction2 = fraction*fraction;
		return p*fraction*fraction2 + q*fraction2 + r*fraction + v1;
	}

	public final static float rampLinear(float start, float end, float segment_length, float x) {
		x = modulo(x, segment_length);
		if (x < start) return 0f;
		if (x > end) return 1f;
		return interpolateLinear(0f, 1f, (x - start)/(end - start));
	}

	public final static float step(float start, float end, float segment_length, float x) {
		x = modulo(x, segment_length);
		if (x < start || x > end) return 0f;
		return 1f;
	}

	public final static float sawtooth(float x) {
		x++; // hack!
		float x_frac = x - (int)x;
		if (x_frac < 0.5) {
			return 2*x_frac;
		} else {
			return -2*x_frac + 2;
		}
	}

	public final static float gaussify(float x) {
		if (x >=0 && x < 0.5) {
			return 0.5f*(float)StrictMath.sqrt(2*x);
		}
		if (x >=0.5f && x <= 1f) {
			return 1f - 0.5f*(float)StrictMath.sqrt(-2*x + 2);
		}
		return 0;
	}

	public final static float gaussify(float x, float exponent) {
		if (x >=0 && x < 0.5) {
			return 0.5f*(float)StrictMath.pow(2*x, exponent);
		}
		if (x >=0.5f && x <= 1f) {
			return 1f - 0.5f*(float)StrictMath.pow(-2*x + 2, exponent);
		}
		return 0;
	}

	public final static float gain(float gain, float x) {
		if (x < 0.5f)
			return (float)(StrictMath.pow(2 * x, StrictMath.log(1 - gain)/StrictMath.log(0.5d))/2f);
		else
			return 1f - (float)(StrictMath.pow(2 - 2 * x, StrictMath.log(1 - gain)/StrictMath.log(0.5d))/2f);
	}
}
