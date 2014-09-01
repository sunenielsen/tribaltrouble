package com.oddlabs.tt.audio;

public final strictfp class AudioParameters {
	final float x;
	final float y;
	final float z;
	final Object sound;
	final int rank;
	final float distance;
	final float gain;
	final float radius;
	final float pitch;
	final boolean looping;
	final boolean relative;
	final boolean music;

	public AudioParameters(Object sound, float x, float y, float z, int rank, float distance) {
		this(sound, x, y, z, rank, distance, 1f, .5f);
	}

	public AudioParameters(Object sound, float x, float y, float z, int rank, float distance, float gain, float radius) {
		this(sound, x, y, z, rank, distance, gain, radius, 1f);
	}

	public AudioParameters(Object sound, float x, float y, float z, int rank, float distance, float gain, float radius, float pitch) {
		this(sound, x, y, z, rank, distance, gain, radius, pitch, false, false);
	}

	public AudioParameters(Object sound, float x, float y, float z, int rank, float distance, float gain, float radius, float pitch, boolean looping, boolean relative) {
		this(sound, x, y, z, rank, distance, gain, radius, pitch, looping, relative, false);
	}

	public AudioParameters(Object sound, float x, float y, float z, int rank, float distance, float gain, float radius, float pitch, boolean looping, boolean relative, boolean music) {
		this.sound = sound;
		this.x = x;
		this.y = y;
		this.z = z;
		this.rank = rank;
		this.distance = distance;
		this.gain = gain;
		this.radius = radius;
		this.pitch = pitch;
		this.looping = looping;
		this.relative = relative;
		this.music = music;
	}
}
