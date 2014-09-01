package com.oddlabs.tt.audio;

import java.net.URL;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import com.oddlabs.tt.global.Settings;
import com.oddlabs.util.ByteBufferOutputStream;
import com.oddlabs.util.Utils;

final strictfp class QueuedAudioPlayer extends AbstractAudioPlayer {
	private final static int NUM_BUFFERS = 12;
	private final ByteBufferOutputStream buffer_stream;
	private final Audio audio;
	private final IntBuffer al_return_buffers = BufferUtils.createIntBuffer(1);
	private final URL url;
	private final int channels;

	private OGGStream ogg_stream;
	private int oldest_buffer = 0;

	QueuedAudioPlayer(AudioSource source, AudioParameters params) {
		super(source, params);
		this.url = Utils.makeURL((String)params.sound);
		this.buffer_stream = new ByteBufferOutputStream(true);
		if ((!params.music && !Settings.getSettings().play_sfx) || this.source == null) {
			this.ogg_stream = null;
			this.channels = 0;
			this.audio = null;
			return;
		}

		if (params.relative)
			AL10.alSourcei(source.getSource(), AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
		else
			AL10.alSourcei(source.getSource(), AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
		
		setGain(params.gain);
		setPos(params.x, params.y, params.z);

		audio = new Audio(NUM_BUFFERS);
		IntBuffer al_buffers = audio.getBuffers();
		this.ogg_stream = new OGGStream(url);
		this.channels = ogg_stream.getChannels();
		for (int i = 0; i < al_buffers.capacity(); i++) {
			fillBuffer(al_buffers.get(i));
		}

		AL10.alSourcei(source.getSource(), AL10.AL_LOOPING, AL10.AL_FALSE);
		AL10.alSourcef(source.getSource(), AL10.AL_ROLLOFF_FACTOR, ROLLOFF_FACTOR);
		AL10.alSourcef(source.getSource(), AL10.AL_REFERENCE_DISTANCE, params.radius);
		AL10.alSourcef(source.getSource(), AL10.AL_MIN_GAIN, 0f);
		AL10.alSourcef(source.getSource(), AL10.AL_MAX_GAIN, 1f);
		AL10.alSourcef(source.getSource(), AL10.AL_PITCH, params.pitch);
		AL10.alSourceQueueBuffers(source.getSource(), al_buffers);
		if (params.music || AudioManager.getManager().startPlaying())
			AL10.alSourcePlay(source.getSource());

		AudioManager.getManager().registerQueuedPlayer(this);
	}

	private final void fillBufferFromStream(int al_buffer) {
		buffer_stream.buffer().flip();
/*		buffer_stream.buffer().limit(256);
		buffer_stream.buffer().position(0);
		for (int i = 0; i < buffer_stream.buffer().remaining(); i++)
			buffer_stream.buffer().put(i, (byte)0);*/
//System.out.println("al_buffer = " + al_buffer);
//		assert buffer_stream.buffer().remaining()%512 == 0;
		AL10.alBufferData(al_buffer, Wave.getFormat(channels, 16), buffer_stream.buffer(), ogg_stream.getRate());
	}

	private final int fillBuffer(int al_buffer) {
		buffer_stream.reset();
		int bytes = ogg_stream.read(buffer_stream);
		if (bytes > 0) {
			fillBufferFromStream(al_buffer);
		} else if (getParameters().looping) {
			ogg_stream = new OGGStream(url);
			bytes = ogg_stream.read(buffer_stream);
			fillBufferFromStream(al_buffer);
		}
		return bytes;
	}

	public final void refill() { // Run by the Refiller thread
		int processed = AL10.alGetSourcei(source.getSource(), AL10.AL_BUFFERS_PROCESSED);
//System.out.println("this = " + this + " | processed = " + processed);
		while (processed > 0) {
//			assert processed <= al_buffers.capacity();
//			al_buffers.position(oldest_buffer);
//			al_buffers.limit(oldest_buffer + 1);
//			assert AL10.alIsBuffer(al_buffers.get(al_buffers.position())): al_buffers.get(al_buffers.position()) + " is not a buffer";
			AL10.alSourceUnqueueBuffers(source.getSource(), al_return_buffers);
//			assert al_return_buffers.get(0) == al_buffers.get(al_buffers.position()): "Unexpected buffer removed: " + al_return_buffers.get(0) + " should be " + al_buffers.get(al_buffers.position());
			int bytes = fillBuffer(al_return_buffers.get(0));
			if (bytes == 0) {
				stop();
				return;
			}
//			assert AL10.alIsBuffer(al_buffers.get(al_buffers.position())): al_buffers.get(al_buffers.position()) + " is not a buffer";
			AL10.alSourceQueueBuffers(source.getSource(), al_return_buffers);
//System.out.println("oldest_buffer = " + oldest_buffer + " | processed = " + processed + " | capacity = " + buffer_streams[oldest_buffer].buffer().capacity() + " | position " + buffer_streams[oldest_buffer].buffer().position() + " | limit " + buffer_streams[oldest_buffer].buffer().limit() + " al_size = " + AL10.alGetBufferi(al_buffers.get(oldest_buffer), AL10.AL_SIZE));
			oldest_buffer = (oldest_buffer + 1)%NUM_BUFFERS;
			processed--;
/*			int test_processed = AL10.alGetSourcei(source.getSource(), AL10.AL_BUFFERS_PROCESSED);
			assert test_processed >= processed: test_processed + " " + processed;*/
		}
		if (AL10.alGetSourcei(source.getSource(), AL10.AL_SOURCE_STATE) == AL10.AL_STOPPED)
			AL10.alSourcePlay(source.getSource());
//System.out.println("		AL10.alGetSourcei(source_index,AL10.AL_SOURCE_STATE) = " + 		AL10.alGetSourcei(source.getSource(),AL10.AL_SOURCE_STATE) + " | AL10.AL_STOPPED = " + AL10.AL_STOPPED + " | AL10.AL_PLAYING = " + AL10.AL_PLAYING);
	}

	public final void stop() {
		if (isPlaying()) {
			AudioManager.getManager().removeQueuedPlayer(this);
			super.stop();
		}
	}
}
