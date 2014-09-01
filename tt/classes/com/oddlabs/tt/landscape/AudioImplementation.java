package com.oddlabs.tt.landscape;

import com.oddlabs.tt.audio.AbstractAudioPlayer;
import com.oddlabs.tt.audio.AudioParameters;

public strictfp interface AudioImplementation {
	AbstractAudioPlayer newAudio(AudioParameters params);
}
