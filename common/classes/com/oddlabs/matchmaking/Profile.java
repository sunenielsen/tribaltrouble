package com.oddlabs.matchmaking;

import java.io.Serializable;

public final strictfp class Profile implements Serializable {
	private final static long serialVersionUID = -3399364532017471737l;
	
	private final String nick;
	private final int rating;
	private final int wins;
	private final int losses;
	private final int invalid;
	private final int revision;

	public Profile(String nick, int rating, int wins, int losses, int invalid, int revision) {
		this.nick = nick;
		this.rating = rating;
		this.wins = wins;
		this.losses = losses;
		this.invalid = invalid;
		this.revision = revision;
	}

	public final String toString() {
		return nick;
	}
	
	public final String getNick() {
		return nick;
	}

	public final int getRating() {
		return rating;
	}

	public final int getWins() {
		return wins;
	}

	public final int getLosses() {
		return losses;
	}

	public final int getInvalid() {
		return invalid;
	}

	public final int getRevision() {
		return revision;
	}
}
