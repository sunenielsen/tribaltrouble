package com.oddlabs.tt.model.weapon;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.oddlabs.tt.audio.AudioParameters;
import com.oddlabs.tt.audio.AudioPlayer;
import com.oddlabs.tt.audio.AbstractAudioPlayer;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.particle.RingEmitter;
import com.oddlabs.tt.pathfinder.FindOccupantFilter;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.util.StateChecksum;

public final strictfp class SonicBlast implements Magic {
	private final static float SECONDS_AFTER_FIRST = 0.005f;
	private final static float SECONDS_BETWEEN_RINGS = 0.01f;
	private final static int NUM_RINGS = 3;

	private final float hit_radius;
	private final float hit_chance_closest;
	private final float hit_chance_farthest;
	private final int damage_closest;
	private final int damage_farthest;
	private final float seconds;
	private final Player owner;
	private final float start_x;
	private final float start_y;
	private final float start_z;
	private final AbstractAudioPlayer lur;
	private final AbstractAudioPlayer rumble;

	private float time = 0f;
	private List target_list;

	private int rings_sent = 0;
	private boolean first_ring_sent = false;

	public SonicBlast(float offset_x, float offset_y, float offset_z, float hit_radius, float hit_chance_closest, float hit_chance_farthest, int damage_closest, int damage_farthest, float seconds, Unit src) {
		this.hit_radius = hit_radius;
		this.hit_chance_closest = hit_chance_closest;
		this.hit_chance_farthest = hit_chance_farthest;
		this.damage_closest = damage_closest;
		this.damage_farthest = damage_farthest;
		this.seconds = seconds;
		owner = src.getOwner();

		start_x = src.getPositionX() + offset_x*src.getDirectionX() - offset_y*(-src.getDirectionY());
		start_y = src.getPositionY() + offset_x*src.getDirectionY() + offset_y*src.getDirectionX();
		start_z = src.getPositionZ() + offset_z;

		FindOccupantFilter filter = new FindOccupantFilter(src.getPositionX(), src.getPositionY(), hit_radius, src, Selectable.class);
		UnitGrid unit_grid = owner.getWorld().getUnitGrid();
		unit_grid.scan(filter, UnitGrid.toGridCoordinate(src.getPositionX()), UnitGrid.toGridCoordinate(src.getPositionY()));
		target_list = filter.getResult();

		lur = owner.getWorld().getAudio().newAudio(new AudioParameters(owner.getWorld().getRacesResources().getBlastLurSound(owner.getWorld().getRandom()), start_x, start_y, start_z,
				AudioPlayer.AUDIO_RANK_MAGIC,
				AudioPlayer.AUDIO_DISTANCE_MAGIC,
				AudioPlayer.AUDIO_GAIN_BLAST_LUR,
				AudioPlayer.AUDIO_RADIUS_BLAST_LUR,
				1f));
		rumble = owner.getWorld().getAudio().newAudio(new AudioParameters(owner.getWorld().getRacesResources().getBlastRumbleSound(), start_x, start_y, start_z,
				AudioPlayer.AUDIO_RANK_MAGIC,
				AudioPlayer.AUDIO_DISTANCE_MAGIC,
				AudioPlayer.AUDIO_GAIN_BLAST_RUMBLE,
				AudioPlayer.AUDIO_RADIUS_BLAST_RUMBLE,
				1f));
	}

	public final void animate(float t) {
		time = StrictMath.min(time + t, seconds);
		if (time >= seconds) {
			owner.getWorld().getAnimationManagerGameTime().removeAnimation(this);
		}

		if (!first_ring_sent) {
			first_ring_sent = true;
			float alpha_start = 8f;
			float energy = 1f;
			new RingEmitter(owner.getWorld(), new Vector3f(start_x, start_y, start_z), .5f,
					0.01f, 1f,
					45, 1000f,
					new Vector3f(0f, 0f, 27f), new Vector3f(0f, 0f, 0f),
					new Vector4f(1f, 1f, 1f, alpha_start), new Vector4f(0f, 0f, 0f, -alpha_start/energy),
					new Vector3f(.2f, .2f, .5f), new Vector3f(3.5f, 3.7f, .5f), energy, 1f,
					GL11.GL_SRC_ALPHA, GL11.GL_ONE,
					owner.getWorld().getRacesResources().getSonicTextures(),
					owner.getWorld().getAnimationManagerGameTime());

			owner.getWorld().getAudio().newAudio(new AudioParameters(owner.getWorld().getRacesResources().getBlastBlastSound(), start_x, start_y, start_z,
					AudioPlayer.AUDIO_RANK_MAGIC,
					AudioPlayer.AUDIO_DISTANCE_MAGIC,
					AudioPlayer.AUDIO_GAIN_BLAST_BLAST,
					AudioPlayer.AUDIO_RADIUS_BLAST_BLAST,
					1f));
			lur.stop(.3f, Settings.getSettings().sound_gain);
			rumble.stop(.2f, Settings.getSettings().sound_gain);
		}

		while (rings_sent <= NUM_RINGS && rings_sent < time/SECONDS_BETWEEN_RINGS && time >= SECONDS_AFTER_FIRST) {
			rings_sent++;
			float alpha_start = .8f;
//			float alpha_start = .4f*(1f - rings_sent/(float)NUM_RINGS) + .2f;
			float energy = 1.5f;
			float velocity = 27f - rings_sent*5f;
			new RingEmitter(owner.getWorld(), new Vector3f(start_x, start_y, start_z), .5f,
					0.01f, 1f,
					15, 1000f,
					new Vector3f(0f, 0f, velocity), new Vector3f(0f, 0f, 0f),
					new Vector4f(1f, 1f, 1f, alpha_start), new Vector4f(0f, 0f, 0f, -alpha_start/energy),
					new Vector3f(.2f, .2f, .5f), new Vector3f(12.5f, 12.5f, .5f), energy, 1f,
					GL11.GL_SRC_ALPHA, GL11.GL_ONE,
					owner.getWorld().getRacesResources().getSonicTextures(),
					owner.getWorld().getAnimationManagerGameTime());
		}

		float current_radius = hit_radius*time/seconds;
		float squared_radius = current_radius*current_radius;
		int i = 0;
		while (i < target_list.size()) {
			Selectable s = (Selectable)target_list.get(i);
			float dx = s.getPositionX() - start_x;
			float dy = s.getPositionY() - start_y;
			float squared_dist = dx*dx + dy*dy;
			if (squared_dist < squared_radius) {
				if (!s.isDead()) {
					float hit_chance = calculateValueFromCurrentRadius(current_radius, hit_chance_closest, hit_chance_farthest);
					if (owner.getWorld().getRandom().nextFloat() < hit_chance*(1 - s.getDefenseChance())) {
						int damage = (int)calculateValueFromCurrentRadius(current_radius, damage_closest, damage_farthest);
						float inv_dist = 1f/((float)StrictMath.sqrt(squared_dist));
						s.hit(damage, dx*inv_dist, dy*inv_dist, owner);
					}
				}
				target_list.remove(i);
			} else {
				i++;
			}
		}
	}

	private final float calculateValueFromCurrentRadius(float current_radius, float max, float min) {
		float base_factor = 4f/7f;
		float error = (float)StrictMath.pow(base_factor, hit_radius);
		float factor = (float)StrictMath.pow(base_factor, current_radius);
		float result = (max - min + error)*factor + min - error;
		return result;
	}

	public void updateChecksum(StateChecksum checksum) {
	}

	public final void interrupt() {
		if (lur != null) {
			lur.stop(.2f, Settings.getSettings().sound_gain);
		}
		if (rumble != null) {
			rumble.stop(.2f, Settings.getSettings().sound_gain);
		}
	}
}
