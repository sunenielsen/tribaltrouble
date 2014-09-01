package com.oddlabs.tt.util;

import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.landscape.HeightMap;

public final strictfp class BezierPath {
	private final static int PREVIOUS = 0;
	private final static int START = 1;
	private final static int END = 2;
	private final static int NEXT = 3;

	private final static float[] debug_point = new float[2];
	private final static float[] debug_dir = new float[2];

	private final float[][] points = new float[4][2];
	private final float[] current_point = new float[2];
	private final float[] current_dir = new float[2];
	private float dt;
	private float t;

	public BezierPath() {
		initState();
	}

	private final void initState() {
		t = 1f;
	}

	public final boolean isDone() {
		return t >= 1f;
	}

	public final void computeCurvePoint(float speed) {
		assert !isDone();
		computeCurvePointFromTime(t, current_point, current_dir);
		t += dt*speed;
	}

	public final void dumpPoints() {
		for (int i = 0; i < points.length; i++)
			System.out.println("points[i][0] = " + points[i][0] + " | points[i][1] = " + points[i][1]);
	}

	private final void computeCurvePointFromTime(float t, float[] point, float[] dir) {
		float t2 = t*t;
		float t3 = t2*t;
		float b0 = 1 - 3*t + 3*t2 - t3;
		float b1 = 3*t3 - 6*t2 + 4;
		float b2 = -3*t3 + 3*t2 + 3*t + 1;
		float b3 = t3;
		point[0] = (1f/6f)*(points[PREVIOUS][0]*b0 + points[START][0]*b1 + points[END][0]*b2 + points[NEXT][0]*b3);
		point[1] = (1f/6f)*(points[PREVIOUS][1]*b0 + points[START][1]*b1 + points[END][1]*b2 + points[NEXT][1]*b3);

		float db0 = -3 + 6*t - 3*t2;
		float db1 = 9*t2 - 12*t;
		float db2 = -9*t2 + 6*t + 3;
		float db3 = 3*t2;
		float dx = (1f/6f)*(points[PREVIOUS][0]*db0 + points[START][0]*db1 + points[END][0]*db2 + points[NEXT][0]*db3);
		float dy = (1f/6f)*(points[PREVIOUS][1]*db0 + points[START][1]*db1 + points[END][1]*db2 + points[NEXT][1]*db3);
		// We can use Math here because directions are not game state affecting
		float dir_len_inv = 1f/(float)Math.sqrt(dx*dx + dy*dy);
		dir[0] = dx*dir_len_inv;
		dir[1] = dy*dir_len_inv;
		if (Float.isNaN(dir[0]) || Float.isNaN(dir[1])) {
			dir[0] = 1f;
			dir[1] = 0f;
		}
	}

	public final float getCurrentDirectionX() {
		return current_dir[0];
	}

	public final float getCurrentDirectionY() {
		return current_dir[1];
	}

	public final float getCurrentX() {
		return current_point[0];
	}

	public final float getCurrentY() {
		return current_point[1];
	}

	public final void init(float inv_length, float x1, float y1, float x2, float y2) {
		assert x1 != x2 || y1 != y2: x1 + " " + y1;
		points[START][0] = x1 + (x1 - x2);
		points[START][1] = y1 + (y1 - y2);
		points[END][0] = x1;
		points[END][1] = y1;
		points[NEXT][0] = x2;
		points[NEXT][1] = y2;
		initState();
		dt = inv_length;
	}

	public final void nextPoint(float inv_length, float x, float y) {
		assert x != points[NEXT][0] || y != points[NEXT][1]: x + " " + y;
		cyclePoints();
		points[NEXT][0] = x;
		points[NEXT][1] = y;
		t -= 1f;
		dt = inv_length;
	}

	public final float getNextX() {
		return points[NEXT][0];
	}

	public final float getNextY() {
		return points[NEXT][1];
	}

	private final void cyclePoints() {
		float[] previous = points[PREVIOUS];
		points[PREVIOUS] = points[START];
		points[START] = points[END];
		points[END] = points[NEXT];
		points[NEXT] = previous;
	}

	public final void endPath() {
		float next_x = points[NEXT][0] + (points[NEXT][0] - points[END][0]);
		float next_y = points[NEXT][1] + (points[NEXT][1] - points[END][1]);
		nextPoint(dt, next_x, next_y);
	}

	public final void debugRender(HeightMap heightmap) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1f, 1f, 1f);
		GL11.glBegin(GL11.GL_LINE_STRIP);
		for (float t = 0f; t < 1f; t += .01f) {
			computeCurvePointFromTime(t, debug_point, debug_dir);
			GL11.glVertex3f(debug_point[0], debug_point[1], heightmap.getNearestHeight(debug_point[0], debug_point[1]) + 0.5f);
		}
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
