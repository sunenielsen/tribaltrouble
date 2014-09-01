package com.oddlabs.tt.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.Element;
import com.oddlabs.tt.model.ElementVisitor;
import com.oddlabs.tt.model.AbstractElementNode;
import com.oddlabs.tt.render.TextureKey;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.util.StrictMatrix4f;

public final strictfp class Lightning extends Element implements Animated {
	private final static float SQRT_2 = (float)StrictMath.sqrt(2f);

	private final AnimationManager manager;
	private final List particles = new ArrayList();
	private final Vector3f src;
	private final Vector3f dst;
	private final float width;
	private final int num_particles;
	private final Vector4f color;
	private final Vector4f delta_color;
	private final TextureKey texture;
	private final World world;

	private float energy;

	public Lightning(World world, Vector3f src, Vector3f dst, float width,
			int num_particles, Vector4f color, Vector4f delta_color,
			TextureKey texture, float energy,
			AnimationManager manager) {
		super(world.getElementRoot());
		this.world = world;
		this.src = src;
		this.dst = dst;
		this.width = width;
		this.num_particles = num_particles;
		this.color = color;
		this.delta_color = delta_color;
		this.texture = texture;
		this.energy = energy;
		this.manager = manager;
		initParticles();
		register();
	}

	public final List getParticles() {
		return particles;
	}

	public final TextureKey getTexture() {
		return texture;
	}

	private final void initParticles() {
		Random random = world.getRandom();
		random.nextFloat();
		float x = src.getX();
		float y = src.getY();
		float z = src.getZ();
		float height = dst.getZ() - src.getZ();
		float random_limit = height/6f;
		float dz = (height)/num_particles;

		for (int i = 0; i < num_particles; i++) {
			float base_dx = (dst.getX() - x)/(num_particles - i);
			float base_dy = (dst.getY() - y)/(num_particles - i);
			float dx = base_dx + (random.nextFloat() - .5f)*random_limit;
			float dy = base_dy + (random.nextFloat() - .5f)*random_limit;
			StretchParticle particle = new StretchParticle();
			particle.setSrc(x, y, z);

			if (i == num_particles - 1) {
				x = dst.getX();
				y = dst.getY();
				z = dst.getZ();
				particle.setDstWidth(width/2);
			} else {
				x += dx;
				y += dy;
				z += dz;
				particle.setDstWidth(width);
			}
			particle.setDst(x, y, z);
			initParticle(particle);
			particles.add(particle);
		}
	}

	private final void initParticle(StretchParticle particle) {
		particle.setSrcWidth(width);
		particle.setColor(color.getX(), color.getY(), color.getZ(), color.getW());
		particle.setDeltaColor(delta_color.getX(), delta_color.getY(), delta_color.getZ(), delta_color.getW());
		particle.setRadius(0f, 0f, 0f);
		particle.setGrowthRate(0f, 0f, 0f);
		particle.setEnergy(energy);
	}

	public final void animate(float t) {
		float x_min = Float.POSITIVE_INFINITY;
		float x_max = Float.NEGATIVE_INFINITY;
		float y_min = Float.POSITIVE_INFINITY;
		float y_max = Float.NEGATIVE_INFINITY;
		float z_min = Float.POSITIVE_INFINITY;
		float z_max = Float.NEGATIVE_INFINITY;

		for (int i = 0; i < particles.size(); i++) {
			StretchParticle particle = (StretchParticle)particles.get(i);
			if (particle.getEnergy() > 0f) {
				particle.update(t);
				float x = particle.getSrcX();
				float y = particle.getSrcY();
				float z = particle.getSrcZ();
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
				particles.remove(i);
			}
		}
		setBounds(x_min, x_max, y_min, y_max, z_min, z_max);
		reregister();
		if (particles.size() == 0) {
			remove();
		}
	}

	protected void register() {
		super.register();
		manager.registerAnimation(this);
	}

	public final void visit(ElementVisitor visitor) {
		visitor.visitLightning(this);
	}

	protected final void remove() {
		super.remove();
		manager.removeAnimation(this);
	}

	public final void updateChecksum(StateChecksum checksum) {
	}
}
