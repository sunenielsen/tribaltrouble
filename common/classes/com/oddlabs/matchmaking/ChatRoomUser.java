package com.oddlabs.matchmaking;

import java.io.Serializable;

public final strictfp class ChatRoomUser implements Serializable {
	private final static long serialVersionUID = 1;

	private final String nick;
	private final boolean playing;

	public ChatRoomUser(String nick, boolean playing) {
		this.nick = nick;
		this.playing = playing;
	}

	public final String getNick() {
		return nick;
	}

	public final boolean isPlaying() {
		return playing;
	}

	public final int hashCode() {
		return nick.hashCode();
	}

	public final boolean equals(Object other) {
		if (!(other instanceof ChatRoomUser))
			return false;
		return ((ChatRoomUser)other).nick.equals(nick);
	}
}
