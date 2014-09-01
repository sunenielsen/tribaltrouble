package com.oddlabs.tt.particle;

import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.render.TextureKey;
import com.oddlabs.tt.model.AbstractElementNode;

public strictfp class ParametricEmitter extends Emitter {
	private final static float SQRT_2 = (float)StrictMath.sqrt(2f);
	private final Random random;
	private final Vector3f randomized_offset = new Vector3f();
	private final ParametricFunction function;
	private final float area_xy;
	private final float area_z;
	private final float velocity_u;
	private final float velocity_v;
	private final float velocity_random_margin;
	private final float particles_per_second;
	private final Vector4f color;
	private final Vector3f particle_radius;
	private final Vector3f growth_rate;

	private Vector4f delta_color;
	private float energy;
	private int num_particles;
	private float particle_counter = 0;
	private boolean started = true;

	public ParametricEmitter(World world, ParametricFunction function, Vector3f position,
				   float area_xy, float area_z, float velocity_u, float velocity_v, float velocity_random_margin,
				   int num_particles, float particles_per_second,
				   Vector4f color, Vector4f delta_color,
				   Vector3f particle_radius, Vector3f growth_rate, float energy,
				   int src_blend_func, int dst_blend_func, TextureKey[] textures,
				   AnimationManager manager) {
		super(world, position, src_blend_func, dst_blend_func, textures, null, textures.length, manager);
		this.function = function;
		this.area_xy = area_xy;
		this.area_z = area_z;
		this.velocity_u = velocity_u;
		this.velocity_v = velocity_v;
		this.velocity_random_margin = velocity_random_margin;
		this.num_particles = num_particles;
		this.particles_per_second = particles_per_second;
		this.color = color;
		this.delta_color = delta_color;
		this.particle_radius = particle_radius;
		this.growth_rate = growth_rate;
		this.energy = energy;
		random = world.getRandom();

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
		if (isRegistered()) {
			remove();
		}
	}

	public final void animate(float t) {
		if (started)
			particle_counter += particles_per_second*t;

		while (particle_counter >= 1 && (num_particles == -1 || num_particles != 0) && started) {
			int initiated = initParticle(function, velocity_u, velocity_v, color, delta_color, particle_radius, growth_rate, energy);
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
				ParametricParticle particle = (ParametricParticle)particles[j].get(i);
				if (particle.getEnergy() > 0f) {
					particle.update(t, getScaleX(), getScaleY(), getScaleZ());
					float x = particle.getPosX();
					float y = particle.getPosY();
					float z = particle.getPosZ();
					particle.setPos(getX() + x, getY() + y, getZ() + z);
					x = particle.getPosX();
					y = particle.getPosY();
					z = particle.getPosZ();
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
		if (size == 0 && num_particles == 0) {
			remove();
		}
	}

	protected int initParticle(ParametricFunction function, float velocity_u, float velocity_v, Vector4f color, Vector4f delta_color, Vector3f particle_radius, Vector3f growth_rate, float energy) {

		Vector3f offset = randomOffset(area_xy, area_xy, area_z);
		ParametricParticle particle = new ParametricParticle(function, random.nextFloat()*(float)StrictMath.PI*2f, random.nextFloat()*(float)StrictMath.PI*2f,
				offset.getX(), offset.getY(), offset.getZ());
		offset = randomOffset(velocity_random_margin, velocity_random_margin, 0f);
		particle.setVelocity(velocity_u + offset.getX(), velocity_v + offset.getY());
		particle.setColor(color.getX(), color.getY(), color.getZ(), color.getW());
		particle.setDeltaColor(delta_color.getX(), delta_color.getY(), delta_color.getZ(), delta_color.getW());
		particle.setRadius(particle_radius.getX(), particle_radius.getY(), particle_radius.getZ());
		particle.setGrowthRate(growth_rate.getX(), growth_rate.getY(), growth_rate.getZ());
		particle.setEnergy(energy);
		particle.setType(random.nextInt(getTypes()));
		particle.update(0);
		add(particle);
		return 1;
	}

	protected final Vector3f randomOffset(float a, float b, float c) {
		float x = random.nextFloat()*2*a - a;
		float y = random.nextFloat()*2*b - b;
		float z = random.nextFloat()*2*c - c;

		randomized_offset.setX(x);
		randomized_offset.setY(y);
		randomized_offset.setZ(z);
		return randomized_offset;
	}
}
