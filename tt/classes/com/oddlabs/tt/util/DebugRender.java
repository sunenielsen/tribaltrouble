package com.oddlabs.tt.util;

import org.lwjgl.opengl.GL11;

public final strictfp class DebugRender {
	private final static float[][] debug_colors = {{7f, 1f, 1f}, {7f, 1f, 0f}, {7f, 0f, 1f}, {.3f, .7f, 0f},
												   {0f, 1f, 1f}, {0f, 1f, 0f}, {0f, 0f, 1f}, {0f, 0f, 0f},
												   {0f, .5f, .5f}, {0f, .5f, 0f}, {0f, 0f, .5f}, {.5f, .8f, .8f},
												   {.3f, .5f, 1f}, {5f, .5f, .8f}, {.3f, .2f, .5f}, {.3f, .3f, .3f},
												   {.5f, 1f, 1f}, {.5f, 1f, .5f}, {.5f, .5f, 1f}, {.5f, .5f, .5f}};
	private final static float CIRCLE_DELTA = (float)java.lang.StrictMath.PI/2;
	private final static float ANGLE_DELTA = (float)java.lang.StrictMath.PI/20;
	private final static float SUBDIV = 0.4f;

	private DebugRender() {
	}

	public final static void setColor(int i) {
		float[] color = debug_colors[i%debug_colors.length];
		GL11.glColor3f(color[0], color[1], color[2]);
	}

	public final static void drawBox(float bmin_x, float bmax_x, float bmin_y, float bmax_y, float bmin_z, float bmax_z, float r, float g, float b) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor3f(r, g, b);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3f(bmin_x, bmin_y, bmin_z);
		GL11.glVertex3f(bmin_x, bmin_y, bmax_z);

		GL11.glVertex3f(bmin_x, bmin_y, bmin_z);
		GL11.glVertex3f(bmin_x, bmax_y, bmin_z);

		GL11.glVertex3f(bmin_x, bmin_y, bmin_z);
		GL11.glVertex3f(bmax_x, bmin_y, bmin_z);


		GL11.glVertex3f(bmax_x, bmax_y, bmax_z);
		GL11.glVertex3f(bmax_x, bmax_y, bmin_z);

		GL11.glVertex3f(bmax_x, bmax_y, bmax_z);
		GL11.glVertex3f(bmax_x, bmin_y, bmax_z);

		GL11.glVertex3f(bmax_x, bmax_y, bmax_z);
		GL11.glVertex3f(bmin_x, bmax_y, bmax_z);


		GL11.glVertex3f(bmin_x, bmin_y, bmax_z);
		GL11.glVertex3f(bmin_x, bmax_y, bmax_z);

		GL11.glVertex3f(bmin_x, bmin_y, bmax_z);
		GL11.glVertex3f(bmax_x, bmin_y, bmax_z);

		GL11.glVertex3f(bmin_x, bmax_y, bmin_z);
		GL11.glVertex3f(bmin_x, bmax_y, bmax_z);

		GL11.glVertex3f(bmin_x, bmax_y, bmin_z);
		GL11.glVertex3f(bmax_x, bmax_y, bmin_z);

		GL11.glVertex3f(bmax_x, bmin_y, bmin_z);
		GL11.glVertex3f(bmax_x, bmax_y, bmin_z);

		GL11.glVertex3f(bmax_x, bmin_y, bmin_z);
		GL11.glVertex3f(bmax_x, bmin_y, bmax_z);
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public final static void drawPoint(float x, float y, float z, float size, float r, float g, float b) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(r, g, b);
		GL11.glPointSize(size);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3f(x, y, z);
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	public final static void drawLine(float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(r, g, b);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3f(x1, y1, z1);
		GL11.glVertex3f(x2, y2, z2);
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public final static void drawQuad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, float z, float r, float g, float b) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(r, g, b);
		GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
		GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_LINE);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex3f(x1, y1, z);
			GL11.glVertex3f(x2, y2, z);
			GL11.glVertex3f(x3, y3, z);
			GL11.glVertex3f(x4, y4, z);
		GL11.glEnd();
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
		GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_FILL);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public final static void drawCylinder(float origin_x, float origin_y, float origin_z, float radius, int num_circles/*, float height*/, float r, float g, float b) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(r, g, b);
//		for (float z = origin_z; z < origin_z + height; z += SUBDIV)
		float z = 0f;
		for (int i = 0; i < num_circles; i++) {
			 drawCircle(radius, origin_x, origin_y,  origin_z + z);
			 z += SUBDIV;
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private final static void drawCircle(float radius, float origin_x, float origin_y, float origin_z) {
		float x, y;
		GL11.glBegin(GL11.GL_LINE_LOOP);
		for (float phi = 0f; phi < (float)java.lang.StrictMath.PI*2; phi += ANGLE_DELTA) {
			x = radius*(float)java.lang.StrictMath.cos(phi);
			y = radius*(float)java.lang.StrictMath.sin(phi);
			GL11.glVertex3f(x + origin_x, y + origin_y, origin_z);
		}
		GL11.glEnd();
	}

	public final static void drawSphere(float origin_x, float origin_y, float origin_z, float radius, float r, float g, float b) {
		float x, y, z;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor3f(r, g, b);
		for (float phi = 0; phi < (float)java.lang.StrictMath.PI; phi += CIRCLE_DELTA) {
			GL11.glBegin(GL11.GL_LINE_LOOP);
			for (float rho = 0f; rho < (float)java.lang.StrictMath.PI*2; rho += ANGLE_DELTA) {
				x = radius*(float)java.lang.StrictMath.cos(rho);
				z = radius*(float)java.lang.StrictMath.sin(rho);
				y = x*(float)java.lang.StrictMath.sin(phi);
				x *= (float)java.lang.StrictMath.cos(phi);
				GL11.glVertex3f(x + origin_x, y + origin_y, z + origin_z);
			}
			GL11.glEnd();
		}
		drawCircle(radius, origin_x, origin_y, origin_z);
//		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
