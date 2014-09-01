package com.oddlabs.tt.particle;

import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.render.TextureKey;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.model.AbstractElementNode;

public strictfp abstract class LinearEmitter extends Emitter {
	private final static float SQRT_2 = (float)StrictMath.sqrt(2f);

	private final Random random;
	private final Vector3f randomized_position = new Vector3f();
	private final float offset_z;
	private final float emitter_radius;
	private final float emitter_height;
	private final float particles_per_second;
	private final Vector3f velocity;
	private final Vector3f acceleration;
	private final Vector4f color;
	private final Vector3f particle_radius;
	private final Vector3f growth_rate;
	private final float friction;

	private Vector4f delta_color;
	private float energy;
	private int num_particles;
	private float particle_counter = 0;
	private boolean started = true;

	protected LinearEmitter(World world, Vector3f position, float offset_z,
				   float emitter_radius, float emitter_height,
				   int num_particles, float particles_per_second,
				   Vector3f velocity, Vector3f acceleration,
				   Vector4f color, Vector4f delta_color,
				   Vector3f particle_radius, Vector3f growth_rate, float energy, float friction,
				   int src_blend_func, int dst_blend_func,
				   TextureKey[] textures, SpriteKey[] sprite_renderers, int types,
				   AnimationManager manager) {
		super(world, position, src_blend_func, dst_blend_func, textures, sprite_renderers, types, manager);
		this.offset_z = offset_z;
		this.emitter_radius = emitter_radius;
		this.emitter_height = emitter_height;
		this.num_particles = num_particles;
		this.particles_per_second = particles_per_second;
		this.velocity = velocity;
		this.acceleration = acceleration;
		this.color = color;
		this.delta_color = delta_color;
		this.particle_radius = particle_radius;
		this.growth_rate = growth_rate;
		this.energy = energy;
		this.friction = friction;
		random = new Random((long)(position.getX() * position.getY() * position.getZ()));
		position.setZ(position.getZ() + offset_z);

		register();
	}

	public final void setDeltaColor(Vector4f delta_color) {
		this.delta_color = delta_color;
	}
	
	public final void setEnergy(float energy) {
		this.energy = energy;
	}

	public final void start() {
		started = true;
	}

	public final void stop() {
		started = false;
	}

	public final void done() {
		num_particles = 0;
	}

	public final void animate(float t) {
		if (started)
			particle_counter += particles_per_second*t;

		while (particle_counter >= 1 && (num_particles == -1 || num_particles != 0) && started) {
			int initiated = initParticle(getPosition(), velocity, acceleration, color, delta_color, particle_radius, growth_rate, energy);
			assert initiated <= num_particles || num_particles == -1: "Too many particles initiated";
			particle_counter -= initiated;
			if (num_particles > 0)
				num_particles -= initiated;
		}

		float x_min = Float.POSITIVE_INFINITY;
		float x_max = Float.NEGATIVE_INFINITY;
		float y_min = Float.POSITIVE_INFINITY;
		float y_max = Float.NEGATIVE_INFINITY;
		float z_min = Float.POSITIVE_INFINITY;
		float z_max = Float.NEGATIVE_INFINITY;

		List[] particles = getParticles();
		int size = 0;

		for (int j = 0; j < particles.length; j++) {
			for (int i = 0; i < particles[j].size(); i++) {
				LinearParticle particle = (LinearParticle)particles[j].get(i);
				if (particle.getEnergy() > 0f) {
					particle.update(t);

					float x = particle.getPosX();
					float y = particle.getPosY();
					float z = particle.getPosZ();
					float landscape_z = getWorld().getHeightMap().getNearestHeight(x, y);
					if (z < landscape_z + particle.getRadiusZ() + offset_z) {
						particle.setPos(x, y, landscape_z + particle.getRadiusZ() + offset_z);
						particle.setVelocity(particle.getVelocityX()*friction, particle.getVelocityY()*friction, -particle.getVelocityZ()*friction);
					}

					float radius_x = particle.getRadiusX()*SQRT_2;
					float radius_y = particle.getRadiusY()*SQRT_2;
					float radius_z = particle.getRadiusZ()*SQRT_2;
					x_min = StrictMath.min(x_min, x - radius_x);
					x_max = StrictMath.max(x_max, x + radius_x);
					y_min = StrictMath.min(y_min, y - radius_y);
					y_max = StrictMath.max(y_max, y + radius_y);
					z_min = StrictMath.min(z_min, z - radius_z);
					z_max = StrictMath.max(z_max, z + radius_z);
				} else {
					particles[j].remove(i);
				}
			}
			size += particles[j].size();
		}
		setBounds(x_min, x_max, y_min, y_max, z_min, z_max);
		reregister();
		if (size == 0 && num_particles == 0)
			remove();
	}

	protected abstract int initParticle(Vector3f position, Vector3f velocity, Vector3f acceleration, Vector4f color, Vector4f delta_color, Vector3f particle_radius, Vector3f growth_rate, float energy);

	protected final Vector3f randomPosition() {
		float r = emitter_radius*(float)(1 - random.nextGaussian());
		float a = random.nextFloat()*(float)StrictMath.PI*2;
		float x = (float)StrictMath.cos(a)*r;
		float y = (float)StrictMath.sin(a)*r;
		float z = random.nextFloat()*emitter_height;

		randomized_position.setX(getX() + x);
		randomized_position.setY(getY() + y);
		randomized_position.setZ(getZ() + z);
		return randomized_position;
	}
}
