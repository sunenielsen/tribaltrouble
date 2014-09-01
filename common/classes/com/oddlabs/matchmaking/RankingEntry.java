package com.oddlabs.matchmaking;

import java.io.Serializable;

public final strictfp class RankingEntry implements Serializable {
	private final static long serialVersionUID = 1;

	private final int ranking;
	private final String name;
	private final int rating;
	private final int wins;
	private final int losses;
	private final int invalid;

	public RankingEntry(int ranking, String name, int rating, int wins, int losses, int invalid) {
		this.ranking = ranking;
		this.name = name;
		this.rating = rating;
		this.wins = wins;
		this.losses = losses;
		this.invalid = invalid;
	}

	public final int getRanking() {
		return ranking;
	}

	public final String getName() {
		return name;
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
}
