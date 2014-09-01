package com.oddlabs.matchmaking;

import java.io.Serializable;
import java.util.Arrays;

public final strictfp class GameSession implements Serializable {
	private static final long serialVersionUID = 2768150608081852612L;

	public final static int MIN_WINS_FOR_RANKING = 5;
	
	public final static int WIN = 0;
	public final static int LOSE = 1;

	private final int session_id;
	private final Participant[] participants;
	private final boolean rated;

	public GameSession(int session_id, Participant[] participants, boolean rated) {
		this.session_id = session_id;
		this.participants = participants;
		this.rated = rated;
	}

	private final boolean validateTeams() {
		boolean[] teams = new boolean[MatchmakingServerInterface.MAX_PLAYERS];
		int team_count = 0;
		for (int i = 0; i < participants.length; i++) {
			Participant p = participants[i];
			if (!p.validate())
				return false;
			if (!teams[p.getTeam()]) {
				teams[p.getTeam()] = true;
				team_count++;
			}
		}
		return team_count >= MatchmakingServerInterface.MIN_PLAYERS;
	}
	
	public final int hashCode() {
		return session_id;
	}
	
	public final boolean equals(Object other) {
		if (!(other instanceof GameSession))
			return false;
		GameSession other_game = (GameSession)other;
		return other_game.session_id == session_id && Arrays.equals(other_game.participants, participants) && rated == other_game.rated;
	}
	
	public final boolean validate() {
		return participants != null && participants.length <= MatchmakingServerInterface.MAX_PLAYERS && participants.length >= MatchmakingServerInterface.MIN_PLAYERS &&
			validateTeams();
	}

	public final int getID() {
		return session_id;
	}
	
	public final Participant[] getParticipants() {
		return participants;
	}

	public final boolean isRated() {
		return rated;
	}

	public final static int[][] calculateMatchPoints(int[] player_ratings, int[] player_teams) {
		assert player_ratings.length == player_teams.length;
		int num_players = player_ratings.length;

		int[] team_ratings = new int[2];
		int[] team_sizes = new int[2];

		for (int i = 0; i < num_players; i++) {
			int team = player_teams[i];
			assert team < 2: "Participant on team " + team;
			team_sizes[team]++;
			team_ratings[team] += player_ratings[i];
		}
		int[][] result = new int[num_players][2];
		if (team_sizes[0] > 0 && team_sizes[1] > 0) {
			final int K = 16*num_players;
			float E0 = 1f/(1f + (float)StrictMath.pow(10d, (team_ratings[1] - team_ratings[0])/400f));
			float E1 = 1f/(1f + (float)StrictMath.pow(10d, (team_ratings[0] - team_ratings[1])/400f));
			int team_0_wins = StrictMath.round(K*(1 - E0));
			int team_1_looses = StrictMath.round(K*(0 - E1));
			int team_0_looses = StrictMath.round(K*(0 - E0));
			int team_1_wins = StrictMath.round(K*(1 - E1));
			for (int i = 0; i < num_players; i++) {
				int team_size = team_sizes[player_teams[i]];
				if (player_teams[i] == 0) {
					result[i][WIN] = team_0_wins/team_size;
					result[i][LOSE] = team_0_looses/team_size;
				} else {
					result[i][WIN] = team_1_wins/team_size;
					result[i][LOSE] = team_1_looses/team_size;
				}
			}
		}
		return result;
	}

}
