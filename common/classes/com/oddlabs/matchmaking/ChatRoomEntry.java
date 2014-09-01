package com.oddlabs.matchmaking;

import java.io.Serializable;

public final strictfp class ChatRoomEntry implements Serializable {
	private final static long serialVersionUID = 1;

	private final String name;
	private final int num_joined;

	public ChatRoomEntry(String name, int num_joined) {
		this.name = name;
		this.num_joined = num_joined;
	}

	public final String getName() {
		return name;
	}

	public final int getNumJoined() {
		return num_joined;
	}
}
