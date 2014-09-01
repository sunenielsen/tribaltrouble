package com.oddlabs.tt.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.util.StateChecksum;

public final strictfp class AudioPlayer extends AbstractAudioPlayer {
	public final static int AUDIO_RANK_AMBIENT = 75;
	public final static int AUDIO_RANK_MUSIC = 50;
	public final static int AUDIO_RANK_NOTIFICATION = 40;
	public final static int AUDIO_RANK_BUILDING_COLLAPSE = 20;
	public final static int AUDIO_RANK_DEATH = 10;
	public final static int AUDIO_RANK_MAGIC = 8;
	public final static int AUDIO_RANK_WEAPON_HIT = 7;
	public final static int AUDIO_RANK_WEAPON_ATTACK = 6;
	public final static int AUDIO_RANK_TREE_FALL = 5;
	public final static int AUDIO_RANK_GAS = 4;
	public final static int AUDIO_RANK_ARMORY = 3;
	public final static int AUDIO_RANK_HARVEST = 2;
	public final static int AUDIO_RANK_CHICKEN = 1;
	public final static int AUDIO_RANK_NOT_INITIALIZED = 0;

	public final static float AUDIO_DISTANCE_MUSIC = Float.MAX_VALUE;
	public final static float AUDIO_DISTANCE_AMBIENT = Float.MAX_VALUE;
	public final static float AUDIO_DISTANCE_NOTIFICATION = Float.MAX_VALUE;
	public final static float AUDIO_DISTANCE_BUILDING_COLLAPSE = 150f;
	public final static float AUDIO_DISTANCE_DEATH = 150f;
	public final static float AUDIO_DISTANCE_MAGIC = Float.MAX_VALUE;
	public final static float AUDIO_DISTANCE_WEAPON_HIT = 150f;
	public final static float AUDIO_DISTANCE_WEAPON_ATTACK = 150f;
	public final static float AUDIO_DISTANCE_TREE_FALL = 150f;
	public final static float AUDIO_DISTANCE_ARMORY = Float.MAX_VALUE;
	public final static float AUDIO_DISTANCE_HARVEST = 150f;
	public final static float AUDIO_DISTANCE_CHICKEN = 150f;

	public final static float AUDIO_GAIN_AMBIENT_FOREST = .01f;
	public final static float AUDIO_GAIN_AMBIENT_BEACH = .05f;
	public final static float AUDIO_GAIN_AMBIENT_WIND = .01f;
	public final static float AUDIO_GAIN_BUILDING_COLLAPSE = 1f;
	public final static float AUDIO_GAIN_WEAPON_HIT = .5f;
	public final static float AUDIO_GAIN_WEAPON_ATTACK = 1f;
	public final static float AUDIO_GAIN_HARVEST = 1f;
	public final static float AUDIO_GAIN_CHICKEN_IDLE = .25f;
	public final static float AUDIO_GAIN_CHICKEN_PECK = .25f;
	public final static float AUDIO_GAIN_CHICKEN_DEATH = .25f;
	public final static float AUDIO_GAIN_DEATH = 1f;
	public final static float AUDIO_GAIN_TREE_FALL = 1f;
	public final static float AUDIO_GAIN_LIGHTNING = 1f;
	public final static float AUDIO_GAIN_CLOUD = .4f;
	public final static float AUDIO_GAIN_BUBBLING = 1f;
	public final static float AUDIO_GAIN_GAS = .25f;
	public final static float AUDIO_GAIN_STUN_LUR = 1f;
	public final static float AUDIO_GAIN_BLAST_LUR = 1f;
	public final static float AUDIO_GAIN_BLAST_RUMBLE = 1f;
	public final static float AUDIO_GAIN_BLAST_BLAST = 1f;
	public final static float AUDIO_GAIN_ARMORY = 1f;

	public final static float AUDIO_RADIUS_AMBIENT_FOREST = 1f;
	public final static float AUDIO_RADIUS_AMBIENT_BEACH = 1f;
	public final static float AUDIO_RADIUS_AMBIENT_WIND = 1f;
	public final static float AUDIO_RADIUS_BUILDING_COLLAPSE = 1f;
	public final static float AUDIO_RADIUS_WEAPON_HIT = .5f;
	public final static float AUDIO_RADIUS_WEAPON_ATTACK = .5f;
	public final static float AUDIO_RADIUS_HARVEST = .1f;
	public final static float AUDIO_RADIUS_CHICKEN_IDLE = .1f;
	public final static float AUDIO_RADIUS_CHICKEN_PECK = .1f;
	public final static float AUDIO_RADIUS_CHICKEN_DEATH = .1f;
	public final static float AUDIO_RADIUS_DEATH = .5f;
	public final static float AUDIO_RADIUS_TREE_FALL = .1f;
	public final static float AUDIO_RADIUS_LIGHTNING = 1f;
	public final static float AUDIO_RADIUS_CLOUD = 1f;
	public final static float AUDIO_RADIUS_BUBBLING = 1f;
	public final static float AUDIO_RADIUS_GAS = .2f;
	public final static float AUDIO_RADIUS_STUN_LUR = 1f;
	public final static float AUDIO_RADIUS_BLAST_LUR = 1f;
	public final static float AUDIO_RADIUS_BLAST_RUMBLE = 1f;
	public final static float AUDIO_RADIUS_BLAST_BLAST = 1f;
	public final static float AUDIO_RADIUS_ARMORY = .05f;

	AudioPlayer(AudioSource source, AudioParameters params) {
		super(source, params);
		if (this.source == null) {
			return;
		}
		AL10.alSourcei(source.getSource(), AL10.AL_LOOPING, params.looping ? AL10.AL_TRUE : AL10.AL_FALSE);

		AL10.alSourcei(source.getSource(), AL10.AL_SOURCE_RELATIVE, params.relative ? AL10.AL_TRUE : AL10.AL_FALSE);

		setGain(params.gain);
		setPos(params.x, params.y, params.z);
//System.out.println("source.getSource() = " + source.getSource() + " | sound.getBuffer() = " + sound.getBuffer());
		Audio sound = (Audio)params.sound;
		assert sound.getBuffer() != AL10.AL_NONE;
		int source_state = AL10.alGetSourcei(source.getSource(), AL10.AL_SOURCE_STATE);
		assert source_state == AL10.AL_STOPPED || source_state == AL10.AL_INITIAL;
			
		AL10.alSourcei(source.getSource(), AL10.AL_BUFFER, sound.getBuffer());
		AL10.alSourcef(source.getSource(), AL10.AL_ROLLOFF_FACTOR, ROLLOFF_FACTOR);
		AL10.alSourcef(source.getSource(), AL10.AL_REFERENCE_DISTANCE, params.radius);
		AL10.alSourcef(source.getSource(), AL10.AL_MIN_GAIN, 0f);
		AL10.alSourcef(source.getSource(), AL10.AL_MAX_GAIN, 1f);
		AL10.alSourcef(source.getSource(), AL10.AL_PITCH, params.pitch);
		if (params.music || AudioManager.getManager().startPlaying()) {
			AL10.alSourcePlay(source.getSource());
		}
	}
}
