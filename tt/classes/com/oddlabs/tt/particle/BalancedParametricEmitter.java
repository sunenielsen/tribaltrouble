package com.oddlabs.tt.particle;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.render.TextureKey;
import com.oddlabs.tt.model.AbstractElementNode;

public strictfp final class BalancedParametricEmitter extends ParametricEmitter {
	private final int num_particles;
	private final float dist_u;
	private final float dist_v;
	private final float margin_u;
	private final float margin_v;

	public BalancedParametricEmitter(World world, ParametricFunction function, Vector3f position,
			float velocity_u, float velocity_v, float dist_u, float dist_v,
			int num_particles, float margin_u, float margin_v,
			Vector4f color, Vector4f delta_color,
			Vector3f particle_radius, Vector3f growth_rate, float energy,
			int src_blend_func, int dst_blend_func, TextureKey[] textures,
			AnimationManager manager) {
		super(world, function, position,
				0f, 0f, velocity_u, velocity_v, 0f,
				num_particles, Float.MAX_VALUE,
				color, delta_color,
				particle_radius, growth_rate, energy,
				src_blend_func, dst_blend_func, textures,
				manager);
		this.num_particles = num_particles;
		this.dist_u = dist_u;
		this.dist_v = dist_v;
		this.margin_u = margin_u;
		this.margin_v = margin_v;

//		register();
	}

	protected int initParticle(ParametricFunction function, float velocity_u, float velocity_v, Vector4f color, Vector4f delta_color, Vector3f particle_radius, Vector3f growth_rate, float energy) {

		for (int i = 0; i < num_particles; i++) {
			float u = dist_u*i/(float)num_particles;
			float v = dist_v*i/(float)num_particles;
			ParametricParticle particle = new ParametricParticle(function, u, v, 0f, 0f, 0f);
			Vector3f offset = randomOffset(margin_u, margin_v, 0f);
			particle.setVelocity(velocity_u + offset.getX(), velocity_v + offset.getY());
			particle.setColor(color.getX(), color.getY(), color.getZ(), color.getW());
			particle.setDeltaColor(delta_color.getX(), delta_color.getY(), delta_color.getZ(), delta_color.getW());
			particle.setRadius(particle_radius.getX(), particle_radius.getY(), particle_radius.getZ());
			particle.setGrowthRate(growth_rate.getX(), growth_rate.getY(), growth_rate.getZ());
			particle.setEnergy(energy);
			particle.setType(getWorld().getRandom().nextInt(getTypes()));
			particle.update(0);
			add(particle);
		}
		return num_particles;
	}
}
