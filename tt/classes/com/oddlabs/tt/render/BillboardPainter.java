package com.oddlabs.tt.render;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public abstract strictfp class BillboardPainter {
	private final static FloatBuffer matrix_buf = BufferUtils.createFloatBuffer(16);
	private final static DoubleBuffer plane_buf = BufferUtils.createDoubleBuffer(4);

	/* Team Penguin */
	private final static void initClipPlane(int clip_enum, int face_index, int vertex_index1, int vertex_index2, int[] indices, float[] face_tex_coords, float handedness) {
	/* End Penguin */
		float u1 = getElement(face_index, vertex_index1, 0, 2, indices, face_tex_coords);
		float v1 = getElement(face_index, vertex_index1, 1, 2, indices, face_tex_coords);
		float u2 = getElement(face_index, vertex_index2, 0, 2, indices, face_tex_coords);
		float v2 = getElement(face_index, vertex_index2, 1, 2, indices, face_tex_coords);
		Vector3f vec1 = new Vector3f(0f, 0f, 1f);
		Vector3f vec2 = new Vector3f(u2 - u1, v2 - v1, 0);
		Vector3f vec3 = new Vector3f();
		Vector3f.cross(vec1, vec2, vec3);
		vec3.scale(handedness);
		vec3.normalise();
		vec1.set(u1, v1, 0f);
		float d = -Vector3f.dot(vec3, vec1);
		plane_buf.put(0, vec3.x).put(1, vec3.y).put(2, vec3.z).put(3, d);
		GL11.glClipPlane(clip_enum, plane_buf);
	}

	public final static void finish() {
		GL11.glDisable(GL11.GL_CLIP_PLANE0);
		GL11.glDisable(GL11.GL_CLIP_PLANE1);
		GL11.glDisable(GL11.GL_CLIP_PLANE2);
	}

	public final static void init() {
		GL11.glEnable(GL11.GL_CLIP_PLANE0);
		GL11.glEnable(GL11.GL_CLIP_PLANE1);
		GL11.glEnable(GL11.GL_CLIP_PLANE2);
	}

	/* Team Penguin */
	private final static float getElement(int face_index, int vertex_index, int element_index, int vertex_size, int[] indices, float[] vertices) {
		int vertices_index = indices[face_index*3 + vertex_index];
		return vertices[vertices_index*vertex_size + element_index];
	}

	public final static void loadFaceMatrixAndClipPlanes(int face_index, int[] indices, float[] face_vertices, float[] face_tex_coords) {
	/* End Penguin */
		// Find object space to texture space matrix, mapping vectors in object space to vectors in texture space
		Vector3f v1 = new Vector3f(getElement(face_index, 0, 0, 3, indices, face_vertices),
								   getElement(face_index, 0, 1, 3, indices, face_vertices),
								   getElement(face_index, 0, 2, 3, indices, face_vertices));
		Vector3f v2 = new Vector3f(getElement(face_index, 1, 0, 3, indices, face_vertices),
								   getElement(face_index, 1, 1, 3, indices, face_vertices),
								   getElement(face_index, 1, 2, 3, indices, face_vertices));
		Vector3f v3 = new Vector3f(getElement(face_index, 2, 0, 3, indices, face_vertices),
								   getElement(face_index, 2, 1, 3, indices, face_vertices),
								   getElement(face_index, 2, 2, 3, indices, face_vertices));
		Vector2f w1 = new Vector2f(getElement(face_index, 0, 0, 2, indices, face_tex_coords),
								   getElement(face_index, 0, 1, 2, indices, face_tex_coords));
		Vector2f w2 = new Vector2f(getElement(face_index, 1, 0, 2, indices, face_tex_coords),
								   getElement(face_index, 1, 1, 2, indices, face_tex_coords));
		Vector2f w3 = new Vector2f(getElement(face_index, 2, 0, 2, indices, face_tex_coords),
								   getElement(face_index, 2, 1, 2, indices, face_tex_coords));

		float x1 = v2.x - v1.x;
		float x2 = v3.x - v1.x;
		float y1 = v2.y - v1.y;
		float y2 = v3.y - v1.y;
		float z1 = v2.z - v1.z;
		float z2 = v3.z - v1.z;

		float s1 = w2.x - w1.x;
		float s2 = w3.x - w1.x;
		float t1 = w2.y - w1.y;
		float t2 = w3.y - w1.y;

		float r = 1.0f/(s1 * t2 - s2 * t1);
		Vector3f tan1 = new Vector3f((t2 * x1 - t1 * x2) * r, (t2 * y1 - t1 * y2) * r, (t2 * z1 - t1 * z2) * r);
		Vector3f tan2 = new Vector3f((s1 * x2 - s2 * x1) * r, (s1 * y2 - s2 * y1) * r, (s1 * z2 - s2 * z1) * r);

		Vector3f v1v2 = new Vector3f();
		Vector3f v1v3 = new Vector3f();
		Vector2f w1w2 = new Vector2f();
		Vector3f.sub(v2, v1, v1v2);
		Vector3f.sub(v3, v1, v1v3);
		Vector2f.sub(w2, w1, w1w2);
		Vector3f n = new Vector3f();
		Vector3f.cross(v1v2, v1v3, n);
		n.normalise();
		Vector3f tangent3 = new Vector3f(n);
		float dot = Vector3f.dot(n, tan1);
		tangent3.scale(dot);
		Vector3f.sub(tan1, tangent3, tangent3);
		tangent3.normalise();
		Vector3f cross = new Vector3f();
		Vector3f.cross(n, tan1, cross);
		float handedness = Vector3f.dot(cross, tan2) < 0f ? -1f : 1f;
		Vector3f.cross(n, tangent3, cross);
		cross.scale(handedness);

		Matrix4f obj_to_tex_matrix = new Matrix4f();
		obj_to_tex_matrix.m00 = tangent3.x; obj_to_tex_matrix.m01 = tangent3.y; obj_to_tex_matrix.m02 = tangent3.z; obj_to_tex_matrix.m03 = 0;
		obj_to_tex_matrix.m10 = cross.x; obj_to_tex_matrix.m11 = cross.y; obj_to_tex_matrix.m12 = cross.z; obj_to_tex_matrix.m13 = 0;
		obj_to_tex_matrix.m20 = n.x; obj_to_tex_matrix.m21 = n.y; obj_to_tex_matrix.m22 = n.z; obj_to_tex_matrix.m23 = 0;
		obj_to_tex_matrix.m30 = 0; obj_to_tex_matrix.m31 = 0; obj_to_tex_matrix.m32 = 0; obj_to_tex_matrix.m33 = 1;

		Matrix4f result = new Matrix4f();
		Matrix4f result1 = new Matrix4f();
		Matrix4f result2 = new Matrix4f();
		// Find the texture translation
		Matrix4f tex_translation = new Matrix4f();
		tex_translation.translate(new Vector3f(-w1.x, -w1.y, 0f));
		// Find the object space translation
		Matrix4f vert_translation = new Matrix4f();
		vert_translation.translate(new Vector3f(v1.x, v1.y, v1.z));
		// Find the relative scaling between object space and texture space
		Matrix4f scaling = new Matrix4f();
		float scale_factor = v1v2.length()/w1w2.length();
		scaling.scale(new Vector3f(scale_factor, scale_factor, scale_factor));
		// Combine matrices resulting in the matrix converting points in texture space to points in object space
		Matrix4f.mul(vert_translation, obj_to_tex_matrix, result1);
		Matrix4f.mul(result1, scaling, result2);
		Matrix4f.mul(result2, tex_translation, result);
		//Invert the matrix to get the matrix from points in object space to points in texture space
		result.invert();
		result.store(matrix_buf);

		GL11.glLoadIdentity();
		initClipPlane(GL11.GL_CLIP_PLANE0, face_index, 0, 1, indices, face_tex_coords, handedness);
		initClipPlane(GL11.GL_CLIP_PLANE1, face_index, 1, 2, indices, face_tex_coords, handedness);
		initClipPlane(GL11.GL_CLIP_PLANE2, face_index, 2, 0, indices, face_tex_coords, handedness);
		matrix_buf.rewind();
		GL11.glLoadMatrix(matrix_buf);
	}
}
