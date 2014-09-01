package com.oddlabs.tt.audio;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.OpenALException;

import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.landscape.AudioImplementation;

public final strictfp class AudioManager implements AudioImplementation {
	private final static int MAX_NUM_SOURCES = 32;

	private final static AudioManager singleton;

	private final List ambients = new ArrayList();
	private final RefillerList queued_players = new RefillerList();
	private AudioSource[] sources;

	private int sound_play_counter = Settings.getSettings().play_sfx ? 1 : 0;

	static {
		singleton = new AudioManager();
	}

	public static AudioManager getManager() {
		return singleton;
	}

	private AudioManager() {
		generateSources(MAX_NUM_SOURCES);
	}

	private final void generateSources(int max) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < max; i++) {
			try {
				AudioSource source = new AudioSource();
				list.add(source);
			} catch (OpenALException e) {
				break;
			}
		}
		sources = new AudioSource[list.size()];
		list.toArray(sources);
	}

	public final void stopSources() {
		sound_play_counter--;
		if (sound_play_counter == 0) {
			for (int i = 0; i < sources.length; i++) {
				AudioSource source = sources[i];
				int rank = source.getRank();
				if (rank == AudioPlayer.AUDIO_RANK_MUSIC) {
					continue;
				} else if (rank == AudioPlayer.AUDIO_RANK_AMBIENT) {
					AL10.alSourcePause(sources[i].getSource());
				} else {
					AL10.alSourceStop(sources[i].getSource());
				}
			}
		}
	}

	public final AbstractAudioPlayer newAudio(CameraState camera_state, AudioParameters params) {
		AudioSource source = getSource(camera_state, params);
		return doNewAudio(source, params);
	}

	public final AbstractAudioPlayer newAudio(AudioParameters params) {
		AudioSource source = getSource(params);
		return doNewAudio(source, params);
	}

	private static AbstractAudioPlayer doNewAudio(AudioSource source, AudioParameters params) {
		if (params.sound instanceof Audio)
			return new AudioPlayer(source, params);
		else
			return new QueuedAudioPlayer(source, params);
	}

	public final void startSources() {
		if (sound_play_counter == 0) {
			for (int i = 0; i < ambients.size(); i++) {
				AL10.alSourcePlay(((AudioSource)ambients.get(i)).getSource());
			}
		}
		sound_play_counter++;
	}

	public final void registerAmbient(AudioSource source) {
		ambients.add(source);
	}

	public final void removeAmbient(AudioSource source) {
		ambients.remove(source);
	}

	final boolean startPlaying() {
		return sound_play_counter > 0;
	}

	private AudioSource findSource(AudioParameters params) {
		// Check for free sources
		int worst_rank = Integer.MAX_VALUE;
		for (int i = 0; i < sources.length; i++) {
			AudioSource source = (AudioSource)sources[i];
			int source_index = source.getSource();
			if ((AL10.alGetSourcei(source_index, AL10.AL_SOURCE_STATE) == AL10.AL_STOPPED
				|| AL10.alGetSourcei(source_index, AL10.AL_SOURCE_STATE) == AL10.AL_INITIAL) && source.getRank() < AudioPlayer.AUDIO_RANK_AMBIENT) {
				if (source.getAudioPlayer() != null)
					source.getAudioPlayer().stop();
				return source;
			}
			if (worst_rank > source.getRank())
				worst_rank = source.getRank();
		}

		if (params.rank > worst_rank) {
			FloatBuffer position = BufferUtils.createFloatBuffer(3);
			for (int i = 0; i < sources.length; i++) {
				AudioSource source = (AudioSource)sources[i];
				if (source.getRank() == worst_rank) {
					return source;
				}
			}
		}
		return null;
	}

	private AudioSource getSource(AudioParameters params) {
		if (!AL.isCreated())
			return null;
		AudioSource best_source = findSource(params);
		stopSource(best_source);
		return best_source;
	}

	private AudioSource getSource(CameraState camera_state, AudioParameters params) {
		if (!AL.isCreated())
			return null;
		float this_dist_squared;
		if (params.relative)
			this_dist_squared = params.x*params.x + params.y*params.y + params.z*params.z;
		else
			this_dist_squared = getCamDistSquared(camera_state, params.x, params.y, params.z);

		if (this_dist_squared > params.distance*params.distance) {
		   return null;
		}

		AudioSource best_source = findSource(params);

		if (best_source == null) {
			float max_dist_squared = this_dist_squared;
			FloatBuffer position = BufferUtils.createFloatBuffer(3);
			for (int i = 0; i < sources.length; i++) {
				AudioSource source = (AudioSource)sources[i];
				if (source.getRank() == params.rank) {
					int source_index = source.getSource();
					AL10.alGetSource(source_index, AL10.AL_POSITION, position);

					float dist_squared = getCamDistSquared(camera_state, position.get(0), position.get(1), position.get(2));
					if (dist_squared > max_dist_squared) {
						max_dist_squared = dist_squared;
						best_source = source;
					}
				}
			}
		}
		stopSource(best_source);
		return best_source;
	}

	private static void stopSource(AudioSource source) {
		if (source != null && source.getAudioPlayer() != null) {
			source.getAudioPlayer().stop();
		}
	}

	private static float getCamDistSquared(CameraState camera_state, float x, float y, float z) {
		float dx = x - camera_state.getCurrentX();
		float dy = y - camera_state.getCurrentY();
		float dz = z - camera_state.getCurrentZ();
		return dx*dx + dy*dy + dz*dz;
	}

	public final void registerQueuedPlayer(QueuedAudioPlayer q) {
		queued_players.registerQueuedPlayer(q);
	}

	public final void removeQueuedPlayer(QueuedAudioPlayer q) {
		queued_players.removeQueuedPlayer(q);
	}

	public final void destroy() {
		queued_players.destroy();
	}
}
