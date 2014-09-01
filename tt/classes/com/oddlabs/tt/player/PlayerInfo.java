package com.oddlabs.tt.player;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public final strictfp class PlayerInfo implements Serializable {
	private final static long serialVersionUID = 3;

	public final static int TEAM_NEUTRAL = -1;

	private final int race;
	private final String name;
	private final int team;

	public PlayerInfo(int team, int race, String name) {
		this.team = team;
		this.race = race;
		this.name = name;
	}

	public final boolean equals(Object other) {
		if (!(other instanceof PlayerInfo))
			return false;
		PlayerInfo player = (PlayerInfo)other;
		return team == player.team && race == player.race;
	}

	public final int getRace() {
		return race;
	}

	public final String getName() {
		return name;
	}

	public final int getTeam() {
		return team;
	}

	public final String toString() {
		return name;
	}
}
