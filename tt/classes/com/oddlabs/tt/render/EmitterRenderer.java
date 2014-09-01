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

final strictfp class EmitterRenderer {
	private final static float SQRT_2 = (float)StrictMath.sqrt(2f);
	private final static float ROTATION_FACTOR = 60f;

	private final static StrictVector3f right_plus_up = new StrictVector3f();
	private final static StrictVector3f right_minus_up = new StrictVector3f();
	private final static FloatBuffer color_buffer = BufferUtils.createFloatBuffer(4);

	private final static StrictMatrix4f view_matrix = new StrictMatrix4f();
	private final static CameraState tmp_camera = new CameraState();

	public static void render(RenderQueues render_queues, List emitter_queue, CameraState state) {
		tmp_camera.set(state);
		view_matrix.setIdentity();
		tmp_camera.setView(view_matrix);
		float rx = tmp_camera.getModelView().m00; float ry = tmp_camera.getModelView().m10; float rz = tmp_camera.getModelView().m20;
		float upx = tmp_camera.getModelView().m01; float upy = tmp_camera.getModelView().m11; float upz = tmp_camera.getModelView().m21;
		right_plus_up.set(rx + upx, ry + upy, rz + upz);
		right_minus_up.set(rx - upx, ry - upy, rz - upz);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0f);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(false);
		for (int i = 0; i < emitter_queue.size(); i++) {
			Emitter emitter = (Emitter)emitter_queue.get(i);
			if (Globals.draw_particles)
				render(render_queues, emitter);
		}
		emitter_queue.clear();
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}

	private static void render2DParticle(Particle particle, Emitter emitter) {
		float x = particle.getPosX();
		float y = particle.getPosY();
		float z = particle.getPosZ();
		float radius_x = particle.getRadiusX()*emitter.getScaleX();
		float radius_y = particle.getRadiusY()*emitter.getScaleY();
		float radius_z = particle.getRadiusZ()*emitter.getScaleZ();

		GL11.glColor4f(particle.getColorR(), particle.getColorG(), particle.getColorB(), particle.getColorA());
		GL11.glTexCoord2f(particle.getU1(), particle.getV1());
		GL11.glVertex3f(x - right_plus_up.getX()*radius_x, y - right_plus_up.getY()*radius_y, z - right_plus_up.getZ()*radius_z);
		GL11.glTexCoord2f(particle.getU2(), particle.getV2());
		GL11.glVertex3f(x + right_minus_up.getX()*radius_x, y + right_minus_up.getY()*radius_y, z + right_minus_up.getZ()*radius_z);
		GL11.glTexCoord2f(particle.getU3(), particle.getV3());
		GL11.glVertex3f(x + right_plus_up.getX()*radius_x, y + right_plus_up.getY()*radius_y, z + right_plus_up.getZ()*radius_z);
		GL11.glTexCoord2f(particle.getU4(), particle.getV4());
		GL11.glVertex3f(x - right_minus_up.getX()*radius_x, y - right_minus_up.getY()*radius_y, z - right_minus_up.getZ()*radius_z);
	}

	private static void render(RenderQueues render_queues, Emitter emitter) {
		if (Globals.isBoundsEnabled(Globals.BOUNDING_PLAYERS)) {
			RenderTools.draw(emitter, 1f, 1f, 1f);
		}

		TextureKey[] textures = emitter.getTextures();
		List[] particles = emitter.getParticles();
		SpriteKey[] sprite_renderers = emitter.getSpriteRenderers();
		if (textures != null) {
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			GL11.glBlendFunc(emitter.getSrcBlendFunc(), emitter.getDstBlendFunc());

			for (int j = 0; j < particles.length; j++) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, render_queues.getTexture(textures[j]).getHandle());
				GL11.glBegin(GL11.GL_QUADS);
				for (int i = particles[j].size() - 1; i >= 0; i--) {
					Particle particle = (Particle)particles[j].get(i);
					render2DParticle(particle, emitter);
				}
				GL11.glEnd();
			}

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
		} else if (sprite_renderers != null) {
			for (int j = 0; j < particles.length; j++) {
				for (int i = particles[j].size() - 1; i >= 0; i--) {
					Particle particle = (Particle)particles[j].get(i);
					int index = particle.getType();
					color_buffer.put(0, particle.getColorR());
					color_buffer.put(1, particle.getColorG());
					color_buffer.put(2, particle.getColorB());
					color_buffer.put(3, StrictMath.min(particle.getColorA(), 1f));
					SpriteRenderer sprite_renderer = render_queues.getRenderer(sprite_renderers[index]);
					sprite_renderer.setupWithColor(0, color_buffer, false, false);
//					sprite_renderer.setup(0, false);
					float x = particle.getPosX();
					float y = particle.getPosY();
					float z = particle.getPosZ();
					GL11.glPushMatrix();
					GL11.glTranslatef(x, y, z);
					GL11.glRotatef(ROTATION_FACTOR*(y + x), SQRT_2, SQRT_2, 0f);
//					GL11.glScalef(scale_x, scale_y, scale_z);
					sprite_renderer.getSpriteList().render(0, 0, 0f);
					sprite_renderer.getSpriteList().reset(0, false, false);
					GL11.glPopMatrix();
				}
			}
		}
	}
}
