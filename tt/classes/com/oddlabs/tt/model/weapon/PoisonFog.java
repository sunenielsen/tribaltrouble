package com.oddlabs.tt.model.weapon;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.oddlabs.tt.audio.AudioPlayer;
import com.oddlabs.tt.audio.AbstractAudioPlayer;
import com.oddlabs.tt.audio.AudioParameters;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.particle.RandomVelocityEmitter;
import com.oddlabs.tt.pathfinder.FindOccupantFilter;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.util.StateChecksum;

public final strictfp class PoisonFog implements Magic {
	public final static float OFFSET_Z = 1.1f;

	private final static int PARTICLES_PER_BURST = 4;
	private final static float SECONDS_BETWEEN_BURSTS = .15f;
	private final static float BURST_RADIUS = 2f;
	private final static float GAUSSIAN_LIMIT = 2.5f;
	private final static int MIN_BURSTS_PER_SOUND = 2;

	private final float hit_radius;
	private final float hit_chance;
	private final float interval;
	private final int damage;
	private final Unit src;
	private final Player owner;
	private final float start_x;
	private final float start_y;
	private final float total_time;
	private final AbstractAudioPlayer bubbling_sound;

	private int next_sound = 1;
	private float time = 0f;
	private List target_list;
	private int bursts = 0;
	private int num_hits = 0;
	private boolean first_run = true;

	public PoisonFog(float offset_x, float offset_y, float offset_z, float hit_radius, float hit_chance, float interval, float time, int damage, Unit src) {
		this.hit_radius = hit_radius;
		this.hit_chance = hit_chance;
		this.interval = interval;
		total_time = time;
		this.damage = damage;
		this.src = src;
		owner = src.getOwner();

		start_x = src.getPositionX() + offset_x*src.getDirectionX() - offset_y*(-src.getDirectionY());
		start_y = src.getPositionY() + offset_x*src.getDirectionY() + offset_y*src.getDirectionX();

		bubbling_sound = owner.getWorld().getAudio().newAudio(new AudioParameters(owner.getWorld().getRacesResources().getBubblingSound(), start_x, start_y, owner.getWorld().getHeightMap().getNearestHeight(start_x, start_y),
				AudioPlayer.AUDIO_RANK_MAGIC,
				AudioPlayer.AUDIO_DISTANCE_MAGIC,
				AudioPlayer.AUDIO_GAIN_BUBBLING,
				AudioPlayer.AUDIO_RADIUS_BUBBLING,
				1f, true, false));
	}

	public final void animate(float t) {
		time += t;
		if (time >= total_time) {
			owner.getWorld().getAnimationManagerGameTime().removeAnimation(this);
		}
		if (first_run) {
			bubbling_sound.stop(.2f, Settings.getSettings().sound_gain);
			first_run = false;
		}

		if (bursts*SECONDS_BETWEEN_BURSTS < time) {
			float gaussian = (float)(GAUSSIAN_LIMIT - StrictMath.abs(StrictMath.max(-GAUSSIAN_LIMIT, StrictMath.min(GAUSSIAN_LIMIT, owner.getWorld().getRandom().nextGaussian()))))/GAUSSIAN_LIMIT;
			float r = gaussian*(hit_radius - BURST_RADIUS - 5f);
			float a = owner.getWorld().getRandom().nextFloat()*(float)StrictMath.PI*2;
			float x = start_x + (float)StrictMath.cos(a)*r;
			float y = start_y + (float)StrictMath.sin(a)*r;
			float z = owner.getWorld().getHeightMap().getNearestHeight(x, y);
			float alpha = 8f;
			float energy = 2f;

			new RandomVelocityEmitter(owner.getWorld(), new Vector3f(x, y, z), OFFSET_Z, owner.getWorld().getRandom().nextFloat()*(float)StrictMath.PI*2,
					BURST_RADIUS, 0f, 0f, 0f,
					PARTICLES_PER_BURST, PARTICLES_PER_BURST,
					new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 0f),
					new Vector4f(1f, 1f, 1f, alpha), new Vector4f(0f, 0f, 0f, -alpha/energy),
					new Vector3f(0f, 0f, .25f), new Vector3f(3.5f, 3.5f, 0f), energy, 1f,
					GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
					owner.getWorld().getRacesResources().getPoisonTextures(),
					owner.getWorld().getAnimationManagerGameTime());
			if (bursts%next_sound == 0) {
				next_sound = MIN_BURSTS_PER_SOUND + owner.getWorld().getRandom().nextInt(5);
				owner.getWorld().getAudio().newAudio(new AudioParameters(owner.getWorld().getRacesResources().getGasSound(), x, y, z,
						AudioPlayer.AUDIO_RANK_GAS,
						AudioPlayer.AUDIO_DISTANCE_MAGIC,
						AudioPlayer.AUDIO_GAIN_GAS,
						AudioPlayer.AUDIO_RADIUS_GAS,
						1f));
			}
			bursts++;
		}

		if ((num_hits + 1)*interval < time) {
			hitUnits(hit_radius);
			num_hits++;
		}
	}

	private final void hitUnits(float radius) {
		FindOccupantFilter filter = new FindOccupantFilter(start_x, start_y, radius, src, Unit.class);
		UnitGrid unit_grid = owner.getWorld().getUnitGrid();
		unit_grid.scan(filter, unit_grid.toGridCoordinate(start_x), unit_grid.toGridCoordinate(start_y));
		target_list = filter.getResult();
		for (int i = 0; i < target_list.size(); i++) {
			Selectable s = (Selectable)target_list.get(i);
			float dx = s.getPositionX() - start_x;
			float dy = s.getPositionY() - start_y;
			float squared_dist = dx*dx + dy*dy;
			if (!s.isDead() && ((owner.isEnemy(s.getOwner()) && owner.getWorld().getRandom().nextFloat() < hit_chance*(1 - s.getDefenseChance()))
						|| (!owner.isEnemy(s.getOwner()) && owner.getWorld().getRandom().nextFloat() < (hit_chance/4f)*(1 - s.getDefenseChance())
							&& s != owner.getChieftain()))) {
				float inv_dist = 1f/((float)StrictMath.sqrt(squared_dist));
				s.hit(damage, dx*inv_dist, dy*inv_dist, owner);
			}
		}
	}

	public void updateChecksum(StateChecksum checksum) {
	}

	public final void interrupt() {
		if (bubbling_sound != null)
			bubbling_sound.stop(.2f, Settings.getSettings().sound_gain);
	}
}
