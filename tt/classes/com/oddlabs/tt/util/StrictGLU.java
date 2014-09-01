package com.oddlabs.tt.util;

public final strictfp class StrictGLU {
	private final static StrictMatrix4f final_matrix = new StrictMatrix4f();
	private final static StrictMatrix4f perspective_matrix = new StrictMatrix4f();
	private final static StrictMatrix4f proj_matrix = new StrictMatrix4f();
	private final static StrictVector4f in = new StrictVector4f();
	private final static StrictVector4f out = new StrictVector4f();
	private final static StrictVector3f vector_3f = new StrictVector3f();

	public static void gluPerspective(StrictMatrix4f proj, float fovy, float aspect, float zNear, float zFar) {
		float sine, cotangent, deltaZ;
		float radians = fovy / 2 * (float)StrictMath.PI / 180;

		deltaZ = zFar - zNear;
		sine = (float) StrictMath.sin(radians);

		if ((deltaZ == 0) || (sine == 0) || (aspect == 0)) {
			return;
		}

		cotangent = (float) StrictMath.cos(radians) / sine;

//		__gluMakeIdentityf(matrix);
		perspective_matrix.setIdentity();
		/*
		matrix.put(0 * 4 + 0, cotangent / aspect);
		matrix.put(1 * 4 + 1, cotangent);
		matrix.put(2 * 4 + 2, - (zFar + zNear) / deltaZ);
		matrix.put(2 * 4 + 3, -1);
		matrix.put(3 * 4 + 2, -2 * zNear * zFar / deltaZ);
		matrix.put(3 * 4 + 3, 0);
		*/
		perspective_matrix.m00 = cotangent / aspect;
		perspective_matrix.m11 = cotangent;
		perspective_matrix.m22 = -(zFar + zNear) / deltaZ;
		perspective_matrix.m23 = -1;
		perspective_matrix.m32 = -2 * zNear * zFar / deltaZ;
		perspective_matrix.m33 = 0;

//		GL11.glMultMatrix(matrix);
		proj_matrix.load(proj);
		StrictMatrix4f.mul(proj_matrix, perspective_matrix, proj);

	}

	public static boolean gluUnProject(float winx, float winy, float winz, StrictMatrix4f model_matrix, StrictMatrix4f proj_matrix, int[] viewport, float[] obj_pos) {
		StrictMatrix4f.mul(proj_matrix, model_matrix, final_matrix);

		if (final_matrix.invert() == null) {
			return false;
		}

		in.set(winx, winy, winz, 1f);

		// Map x and y from window coordinates
		in.set((in.getX() - viewport[0]) / viewport[2], (in.getY() - viewport[1]) / viewport[3]);

		// Map to range -1 to 1
		in.set(in.getX() * 2 - 1, in.getY() * 2 - 1, in.getZ() * 2 - 1);

		StrictMatrix4f.transform(final_matrix, in, out);

		if (out.getW() == 0.0)
			return false;

		out.setW(1.0f / out.getW());

		obj_pos[0] = out.getX() * out.getW();
		obj_pos[1] = out.getY() * out.getW();
		obj_pos[2] = out.getZ() * out.getW();

		return true;
	}

	public static void gluPickMatrix(StrictMatrix4f proj, float x, float y, float width, float height, int[] viewport) {
		if (width <= 0 || height <= 0) 
			return;

		/* Translate and scale the picked region to the entire window */
//		GL11.glTranslatef((viewport[2] - 2 * (x - viewport[0])) / width, (viewport[3] - 2 * (y - viewport[1])) / height, 0);
		vector_3f.set((viewport[2] - 2 * (x - viewport[0])) / width, (viewport[3] - 2 * (y - viewport[1])) / height, 0);
		proj.translate(vector_3f);
//		GL11.glScalef(viewport[2] / width, viewport[3] / height, 1.0f);
		vector_3f.set(viewport[2] / width, viewport[3] / height, 1.0f);
		proj.scale(vector_3f);

	}
}
