package com.oddlabs.tt.particle;

import com.oddlabs.tt.render.TextureKey;
import com.oddlabs.tt.animation.*;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.model.AbstractElementNode;

import java.util.*;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public final strictfp class RandomVelocityEmitter extends LinearEmitter {
	private final Random random;

	private final float uv_angle;
	private final float angle_bound;
	private final float angle_max_jump;
	private final Vector3f velocity;
	private final Vector3f offset_velocity;

	private float x_angle = 0;
	private float y_angle = 0;

	private RandomVelocityEmitter(World world, Vector3f position, float offset_z, float uv_angle,
				   float emitter_radius, float emitter_height, float angle_bound, float angle_max_jump,
				   int num_particles, float particles_per_second,
				   Vector3f velocity, Vector3f acceleration,
				   Vector4f color, Vector4f delta_color,
				   Vector3f particle_radius, Vector3f growth_rate, float energy, float friction,
				   int src_blend_func, int dst_blend_func,
				   TextureKey[] textures, SpriteKey[] sprite_renderers, int types,
				   AnimationManager manager) {
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
				sprite_renderers,
				types,
				manager);
		this.random = world.getRandom();
		this.uv_angle = uv_angle;
		this.velocity = velocity;
		offset_velocity = new Vector3f(velocity.getX(), velocity.getY(), velocity.getZ());
		this.angle_bound = angle_bound;
		this.angle_max_jump = angle_max_jump;
	}

	public RandomVelocityEmitter(World world, Vector3f position, float offset_z, float uv_angle,
				   float emitter_radius, float emitter_height, float angle_bound, float angle_max_jump,
				   int num_particles, float particles_per_second,
				   Vector3f velocity, Vector3f acceleration,
				   Vector4f color, Vector4f delta_color,
				   Vector3f particle_radius, Vector3f growth_rate, float energy, float friction,
				   int src_blend_func, int dst_blend_func,
				   TextureKey[] textures, AnimationManager manager) {
		this(world, position,
				offset_z,
				uv_angle,
				emitter_radius,
				emitter_height,
				angle_bound,
				angle_max_jump,
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
	}

	public RandomVelocityEmitter(World world, Vector3f position, float offset_z,
				   float emitter_radius, float emitter_height, float angle_bound, float angle_max_jump,
				   int num_particles, float particles_per_second,
				   Vector3f velocity, Vector3f acceleration,
				   Vector4f color, Vector4f delta_color,
				   Vector3f particle_radius, Vector3f growth_rate, float energy, float friction,
				   SpriteKey[] sprite_renderers, AnimationManager manager) {
		this(world, position,
				offset_z,
				0f,
				emitter_radius,
				emitter_height,
				angle_bound,
				angle_max_jump,
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
				0,
				0,
				null,
				sprite_renderers,
				sprite_renderers.length,
				manager);
	}

	protected int initParticle(Vector3f position, Vector3f velocity, Vector3f acceleration, Vector4f color, Vector4f delta_color, Vector3f particle_radius, Vector3f growth_rate, float energy) {
		randomizeVelocity();

		LinearParticle particle = new LinearParticle(uv_angle);
		Vector3f pos = randomPosition();
		particle.setPos(pos.getX(), pos.getY(), pos.getZ());
		particle.setVelocity(velocity.getX(), velocity.getY(), velocity.getZ());
		particle.setAcceleration(acceleration.getX(), acceleration.getY(), acceleration.getZ());
		particle.setColor(color.getX(), color.getY(), color.getZ(), color.getW());
		particle.setDeltaColor(delta_color.getX(), delta_color.getY(), delta_color.getZ(), delta_color.getW());
		particle.setRadius(particle_radius.getX(), particle_radius.getY(), particle_radius.getZ());
		particle.setGrowthRate(growth_rate.getX(), growth_rate.getY(), growth_rate.getZ());
		particle.setEnergy(energy);
		particle.setType(random.nextInt(getTypes()));
		add(particle);
		return 1;
	}

	private final void randomizeVelocity() {
		float dx_angle = random.nextFloat()*angle_max_jump - .5f*angle_max_jump;
		float dy_angle = random.nextFloat()*angle_max_jump - .5f*angle_max_jump;

		if ((x_angle + dx_angle < -angle_bound) || (x_angle + dx_angle > angle_bound))
			x_angle -= dx_angle;
		else
			x_angle += dx_angle;

		if ((y_angle + dy_angle < -angle_bound) || (y_angle + dy_angle > angle_bound))
			y_angle -= dy_angle;
		else
			y_angle += dy_angle;

		float x = offset_velocity.getX() + offset_velocity.getZ()*(float)StrictMath.sin(x_angle);
		float y = offset_velocity.getY() + offset_velocity.getZ()*(float)StrictMath.sin(y_angle);
		velocity.set(x, y, offset_velocity.getZ());
	}

}
