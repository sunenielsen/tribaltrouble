package com.oddlabs.tt.render;

import com.oddlabs.tt.model.Model;
import com.oddlabs.tt.util.BoundingBox;
import com.oddlabs.tt.util.DebugRender;
import com.oddlabs.tt.global.Globals;

import org.lwjgl.opengl.GL11;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;

final strictfp class RenderTools {
	private final static FloatBuffer transform_matrix;

	final static int NOT_IN_FRUSTUM = 1;
	final static int IN_FRUSTUM = 2;
	final static int ALL_IN_FRUSTUM = 3;

	static {
		transform_matrix = BufferUtils.createFloatBuffer(16);
		transform_matrix.put(1f).put(0f).put(0f).put(0f);
		transform_matrix.put(0f).put(1f).put(0f).put(0f);
		transform_matrix.put(0f).put(0f).put(1f).put(0f);
		transform_matrix.put(0f).put(0f).put(0f).put(1f);
		transform_matrix.rewind();
	}

	static void translateAndRotate(Model model) {
		translateAndRotate(model.getPositionX(), model.getPositionY(), model.getPositionZ(), model.getDirectionX(), model.getDirectionY());
	}
	
	static void translateAndRotate(float x, float y, float z, float dir_x, float dir_y) {
		// Rotate and translate model
/*		float c = dir_x;
		float s = dir_y;
		float oneminusc = 1.0f - c;
		float xy = 0;//axis.x*axis.y;
		float yz = 0;//axis.y*axis.z;
		float xz = 0;//axis.x*axis.z;
		float xs = 0;//axis.x*s;
		float ys = 0;//axis.y*s;
		float zs = s;//axis.z*s;

		float f00 = c;//axis.x*axis.x*oneminusc+c;
		float f01 = zs;//xy*oneminusc+zs;
		float f02 = 0;//xz*oneminusc-ys;
		// n[3] not used
		float f10 = -zs;//xy*oneminusc-zs;
		float f11 = c;//axis.y*axis.y*oneminusc+c;
		float f12 = 0;//yz*oneminusc+xs;
		// n[7] not used
		float f20 = 0;//xz*oneminusc+ys;
		float f21 = 0;//yz*oneminusc-xs;
		float f22 = 1f;//axis.z*axis.z*oneminusc+c;
		transform_matrix.put(f00).put(f01).put(f02).put(0f);
		transform_matrix.put(f10).put(f11).put(f12).put(0f);
		transform_matrix.put(f20).put(f21).put(f22).put(0f);
		transform_matrix.put(x).put(y).put(render_pos_z).put(1f);*/

/*		transform_matrix.put(dir_x).put(dir_y).put(0f).put(0f);
		transform_matrix.put(-dir_y).put(dir_x).put(0f).put(0f);
		transform_matrix.put(0f).put(0f).put(1f).put(0f);
		transform_matrix.put(x).put(y).put(render_pos_z).put(1f);
		transform_matrix.rewind();*/
		transform_matrix.put(0, dir_x);
		transform_matrix.put(1, dir_y);
		transform_matrix.put(4, -dir_y);
		transform_matrix.put(5, dir_x);
		transform_matrix.put(12, x);
		transform_matrix.put(13, y);
		transform_matrix.put(14, z);
		GL11.glMultMatrix(transform_matrix);
	}

	static int inFrustum(BoundingBox box, float[][] frustum) {
		boolean all_in = true;

		for (int f = 0; f < 6; f++) {
			boolean one_in = false;

			if (frustum[f][0] * box.bmin_x + frustum[f][1] * box.bmin_y + frustum[f][2] * box.bmin_z + frustum[f][3] > 0) {
				if (!all_in)
					continue;
				else
					one_in = true;
			} else {
				all_in = false;
				if (one_in)
					continue;
			}

			if (frustum[f][0] * box.bmin_x + frustum[f][1] * box.bmin_y + frustum[f][2] * box.bmax_z + frustum[f][3] > 0) {
				if (!all_in)
					continue;
				else
					one_in = true;
			} else {
				all_in = false;
				if (one_in)
					continue;
			}

			if (frustum[f][0] * box.bmin_x + frustum[f][1] * box.bmax_y + frustum[f][2] * box.bmin_z + frustum[f][3] > 0) {
				if (!all_in)
					continue;
				else
					one_in = true;
			} else {
				all_in = false;
				if (one_in)
					continue;
			}

			if (frustum[f][0] * box.bmin_x + frustum[f][1] * box.bmax_y + frustum[f][2] * box.bmax_z + frustum[f][3] > 0) {
				if (!all_in)
					continue;
				else
					one_in = true;
			} else {
				all_in = false;
				if (one_in)
					continue;
			}

			if (frustum[f][0] * box.bmax_x + frustum[f][1] * box.bmin_y + frustum[f][2] * box.bmin_z + frustum[f][3] > 0) {
				if (!all_in)
					continue;
				else
					one_in = true;
			} else {
				all_in = false;
				if (one_in)
					continue;
			}

			if (frustum[f][0] * box.bmax_x + frustum[f][1] * box.bmin_y + frustum[f][2] * box.bmax_z + frustum[f][3] > 0) {
				if (!all_in)
					continue;
				else
					one_in = true;
			} else {
				all_in = false;
				if (one_in)
					continue;
			}

			if (frustum[f][0] * box.bmax_x + frustum[f][1] * box.bmax_y + frustum[f][2] * box.bmin_z + frustum[f][3] > 0) {
				if (!all_in)
					continue;
				else
					one_in = true;
			} else {
				all_in = false;
				if (one_in)
					continue;
			}

			if (frustum[f][0] * box.bmax_x + frustum[f][1] * box.bmax_y + frustum[f][2] * box.bmax_z + frustum[f][3] > 0) {
				if (!all_in)
					continue;
				else
					one_in = true;
			} else {
				all_in = false;
				if (one_in)
					continue;
			}

			if (!one_in)
				return NOT_IN_FRUSTUM;
		}
		if (all_in)
			return ALL_IN_FRUSTUM;
		else
			return IN_FRUSTUM;
	}

	static float getEyeDistanceSquared(BoundingBox box, float camera_x, float camera_y, float camera_z) {
		float distx = camera_x - box.getCX();
		float disty = camera_y - box.getCY();
		float distz = camera_z - box.getCZ();
		float dist2 = distx*distx + disty*disty + distz*distz;
		return dist2;
	}

	static float getCameraDistanceXYSquared(BoundingBox box, float camera_x, float camera_y) {
		float distx = camera_x - box.getCX();
		float disty = camera_y - box.getCY();
		float dist2 = distx*distx + disty*disty;
		return dist2;
	}

	static float getCameraDistanceSquared(BoundingBox box, float camera_x, float camera_y, float camera_z) {
		float distz = camera_z - box.getCZ();
		float dist2 = getCameraDistanceXYSquared(box, camera_x, camera_y) + distz*distz;
		return dist2;
	}

	static void draw(BoundingBox box) {
		draw(box, 1f, 1f, 1f);
	}
	
	static void draw(BoundingBox box, float r, float g, float b) {
		DebugRender.drawBox(box.bmin_x, box.bmax_x, box.bmin_y, box.bmax_y, box.bmin_z, box.bmax_z, r, g, b);
	}

	static void draw(BoundingBox box, int bound_type, float r, float g, float b) {
		if (Globals.isBoundsEnabled(bound_type))
			draw(box, r, g, b);
	}
}
