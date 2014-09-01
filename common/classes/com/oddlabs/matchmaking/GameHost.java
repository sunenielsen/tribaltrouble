package com.oddlabs.matchmaking;

import java.io.Serializable;

public final strictfp class GameHost implements Serializable {
	private final static long serialVersionUID = 1;

	private final Game game;
	private final int host_id;
	private final int revision;

	public GameHost(Game game, int host_id, int revision) {
		this.host_id = host_id;
		this.game = game;
		this.revision = revision;
	}

	public final Game getGame() {
		return game;
	}

	public final int getHostID() {
		return host_id;
	}
	
	public final int getRevision() {
		return revision;
	}
}
