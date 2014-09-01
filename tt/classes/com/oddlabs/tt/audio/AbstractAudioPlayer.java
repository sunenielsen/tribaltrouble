package com.oddlabs.tt.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.util.StateChecksum;

public strictfp class AbstractAudioPlayer implements Animated {
	protected final static float ROLLOFF_FACTOR = .03f; // was 0.05
	
	protected final AudioSource source;
	private final AudioParameters parameters;
	private boolean playing = false;

	private float fadeout_time;
	private float end_gain;
	private float fadeout_gain;

	protected AbstractAudioPlayer(AudioSource source, AudioParameters params) {
		this.parameters = params;
		if (source == null || (!params.music && !Settings.getSettings().play_sfx)) {
			this.source = null;
			return;
		}
		this.source = source;
		source.setPlayer(this);
		playing = true;
	}

	protected final boolean isPlaying() {
		return playing;
	}

	public final AudioParameters getParameters() {
		return parameters;
	}

	public final void setGain(float gain) {
		if (playing) {
			if (parameters.music) {
				AL10.alSourcef(source.getSource(), AL10.AL_GAIN, gain*Settings.getSettings().music_gain);
			} else {
				AL10.alSourcef(source.getSource(), AL10.AL_GAIN, gain*Settings.getSettings().sound_gain);
			}
		}
	}

	public final void setPos(float x, float y, float z) {
		if (playing)
			AL10.alSource3f(source.getSource(), AL10.AL_POSITION, x, y, z);

	}

	public void stop() {
		if (playing) {
			AL10.alSourceStop(source.getSource());
			AL10.alSourcei(source.getSource(), AL10.AL_BUFFER, AL10.AL_NONE);
			AL10.alSourceRewind(source.getSource());
			playing = false;
		}
	}

	public final void registerAmbient() {
		if (source != null)
			AudioManager.getManager().registerAmbient(source);
	}

	public final void removeAmbient() {
		if (source != null)
			AudioManager.getManager().removeAmbient(source);
	}

	public final void stop(float delay, float end_gain) {
		this.end_gain = end_gain;
		fadeout_gain = end_gain;
		fadeout_time = delay;
		LocalEventQueue.getQueue().getManager().registerAnimation(this);
	}

	public final void animate(float t) {
		fadeout_gain -= t*(end_gain/fadeout_time);
		if (fadeout_gain <= 0) {
			stop();
			LocalEventQueue.getQueue().getManager().removeAnimation(this);
		} else {
			setGain(fadeout_gain);
		}
	}

	public void updateChecksum(StateChecksum checksum) {
	}
}
