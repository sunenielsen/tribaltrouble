package com.oddlabs.tt.particle;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.render.TextureKey;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.AbstractElementNode;

public final strictfp class RingEmitter extends LinearEmitter {
	private final int num_particles;

	public RingEmitter(World world, Vector3f position, float offset_z,
				   float emitter_radius, float emitter_height,
				   int num_particles, float particles_per_second,
				   Vector3f velocity, Vector3f acceleration,
				   Vector4f color, Vector4f delta_color,
				   Vector3f particle_radius, Vector3f growth_rate, float energy, float friction,
				   int src_blend_func, int dst_blend_func,
				   TextureKey[] textures, AnimationManager manager) {
		super(world, position,
				offset_z,
				emitter_radius,
				emitter_height,
				num_particles,
				particles_per_second,
				velocity,
				acceleration,
				color,
				delta_color,
				particle_radius,
				growth_rate,
				energy,
				friction,
				src_blend_func,
				dst_blend_func,
				textures,
				null,
				textures.length,
				manager);
		this.num_particles = num_particles;
	}

	protected int initParticle(Vector3f position, Vector3f velocity, Vector3f acceleration, Vector4f color, Vector4f delta_color, Vector3f particle_radius, Vector3f growth_rate, float energy) {
		float angle = 2*(float)StrictMath.PI/num_particles;
		for (int i = 0; i < num_particles; i++) {
			LinearParticle particle = new LinearParticle();
			Vector3f pos = position;
			particle.setPos(pos.getX(), pos.getY(), pos.getZ());
			// in this special case velocity.getZ() is the actual velocity. not the velocity in the z direction
			particle.setVelocity(velocity.getZ()*(float)StrictMath.cos(angle*i), velocity.getZ()*(float)StrictMath.sin(angle*i), 0);
			particle.setAcceleration(acceleration.getX(), acceleration.getY(), acceleration.getZ());
			particle.setColor(color.getX(), color.getY(), color.getZ(), color.getW());
			particle.setDeltaColor(delta_color.getX(), delta_color.getY(), delta_color.getZ(), delta_color.getW());
			particle.setRadius(particle_radius.getX(), particle_radius.getY(), particle_radius.getZ());
			particle.setGrowthRate(growth_rate.getX(), growth_rate.getY(), growth_rate.getZ());
			particle.setEnergy(energy);
			particle.setType(0);
			add(particle);
		}
		return num_particles;
	}
}
