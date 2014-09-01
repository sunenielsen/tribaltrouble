package com.oddlabs.matchmaking;

import java.io.Serializable;

public final strictfp class Participant implements Serializable {
	private static final long serialVersionUID = -3344403341742210958L;

	private final int match_id;
	private final String nick;
	private final int team;
	private final int race;

	public Participant(int match_id, String nick, int team, int race) {
		this.match_id = match_id;
		this.nick = nick;
		this.team = team;
		this.race = race;
	}

	public final boolean validate() {
		return team >= 0 && team < MatchmakingServerInterface.MAX_PLAYERS;
	}

	public final int getMatchID() {
		return match_id;
	}

	public final String getNick() {
		return nick;
	}

	public final int getTeam() {
		return team;
	}

	public final int getRace() {
		return race;
	}

	public final int hashCode() {
		return match_id ^ team;
	}

	public final boolean equals(Object other) {
		if (!(other instanceof Participant))
			return false;
		Participant other_part = (Participant)other;
		return match_id == other_part.match_id && team == other_part.team && race == other_part.race;
	}
}
