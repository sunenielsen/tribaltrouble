package com.oddlabs.tt.render;

import com.oddlabs.tt.util.*;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.particle.*;
import com.oddlabs.tt.render.Texture;

import java.util.List;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.BufferUtils;

final strictfp class LightningRenderer {
	private final static float SQRT_2 = (float)StrictMath.sqrt(2f);
	private final static StrictVector3f right_vector = new StrictVector3f();

	private final static StrictMatrix4f view_matrix = new StrictMatrix4f();
	private final static CameraState tmp_camera = new CameraState();
	public static void render(RenderQueues render_queues, List emitter_queue, CameraState state) {
		tmp_camera.set(state);
		view_matrix.setIdentity();
		tmp_camera.setView(view_matrix);
		float rx = tmp_camera.getModelView().m00; float ry = tmp_camera.getModelView().m10; float rz = tmp_camera.getModelView().m20;
		right_vector.set(rx, ry, rz);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0f);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(false);
		for (int i = 0; i < emitter_queue.size(); i++) {
			Lightning emitter = (Lightning)emitter_queue.get(i);
			if (Globals.draw_particles)
				render(render_queues, emitter);
		}
		emitter_queue.clear();
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	private static void render2DParticle(StretchParticle particle) {
		float src_x = particle.getSrcX();
		float src_y = particle.getSrcY();
		float src_z = particle.getSrcZ();
		float dst_x = particle.getDstX();
		float dst_y = particle.getDstY();
		float dst_z = particle.getDstZ();
		/*
		   GL11.glColor4f(particle.getColorR(), particle.getColorG(), particle.getColorB(), particle.getColorA());
		   GL11.glTexCoord2f(0f, 0f);
		   GL11.glVertex3f(dst_x - right_vector.getX()*particle.getDstWidth(), dst_y - right_vector.getY()*particle.getDstWidth(), dst_z);
		   GL11.glTexCoord2f(1f, 0f);
		   GL11.glVertex3f(dst_x + right_vector.getX()*particle.getDstWidth(), dst_y + right_vector.getY()*particle.getDstWidth(), dst_z);
		   GL11.glTexCoord2f(1f, 1f);
		   GL11.glVertex3f(src_x + right_vector.getX()*particle.getSrcWidth(), src_y + right_vector.getY()*particle.getSrcWidth(), src_z);
		   GL11.glTexCoord2f(0f, 1f);
		   GL11.glVertex3f(src_x - right_vector.getX()*particle.getSrcWidth(), src_y - right_vector.getY()*particle.getSrcWidth(), src_z);
		 */
		GL11.glColor4f(particle.getColorR(), particle.getColorG(), particle.getColorB(), particle.getColorA());
		GL11.glTexCoord2f(0f, 0f);
		GL11.glVertex3f(dst_x - particle.getDstWidth(), dst_y, dst_z);
		GL11.glTexCoord2f(1f, 0f);
		GL11.glVertex3f(dst_x + particle.getDstWidth(), dst_y, dst_z);
		GL11.glTexCoord2f(1f, 1f);
		GL11.glVertex3f(src_x + particle.getSrcWidth(), src_y, src_z);
		GL11.glTexCoord2f(0f, 1f);
		GL11.glVertex3f(src_x - particle.getSrcWidth(), src_y, src_z);

		GL11.glTexCoord2f(0f, 0f);
		GL11.glVertex3f(dst_x, dst_y - particle.getDstWidth(), dst_z);
		GL11.glTexCoord2f(1f, 0f);
		GL11.glVertex3f(dst_x, dst_y + particle.getDstWidth(), dst_z);
		GL11.glTexCoord2f(1f, 1f);
		GL11.glVertex3f(src_x, src_y + particle.getSrcWidth(), src_z);
		GL11.glTexCoord2f(0f, 1f);
		GL11.glVertex3f(src_x, src_y - particle.getSrcWidth(), src_z);

	}

	private static void render(RenderQueues render_queues, Lightning lightning) {
		if (Globals.isBoundsEnabled(Globals.BOUNDING_PLAYERS)) {
			RenderTools.draw(lightning, 1f, 1f, 1f);
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, render_queues.getTexture(lightning.getTexture()).getHandle());
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glBegin(GL11.GL_QUADS);

		List particles = lightning.getParticles();
		for (int i = particles.size() - 1; i >= 0; i--) {
			StretchParticle particle = (StretchParticle)particles.get(i);
			render2DParticle(particle);
		}

		GL11.glEnd();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
	}
}
