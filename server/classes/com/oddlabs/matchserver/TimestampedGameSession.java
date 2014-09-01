package com.oddlabs.matchserver;

import com.oddlabs.matchmaking.GameSession;
import com.oddlabs.matchmaking.Participant;
import com.oddlabs.matchmaking.MatchmakingServerInterface;

import java.sql.SQLException;

public final strictfp class TimestampedGameSession {
	private final static long JOIN_MAX_TIME = 3*60*1000;
	private final static long END_GAME_MIN_TIME = 1*60*1000; // replace with clients reporting back there end time

	private final static int PARTICIPANT_UNKNOWN = 0;
	private final static int PARTICIPANT_JOINED = 1;
	private final static int PARTICIPANT_FREE_QUIT = 2;
	private final static int PARTICIPANT_QUIT = 3;
	private final static int PARTICIPANT_LOST = 4;
	private final static int PARTICIPANT_WON = 5;

	private final static String[] PARTICIPANT_DEBUG_CHARS = {"U", "J", "F", "Q", "L", "W"};
	
	private final static int TEAM_UNKNOWN = 0;
	private final static int TEAM_QUIT = 1;
	private final static int TEAM_LOST = 2;
	private final static int TEAM_WON = 3;

	private final static int GAME_STARTING = 1;
	private final static int GAME_ALL_JOINED = 2;
	private final static int GAME_INVALID = 3;

	private final static float STATUS_WINNING_FACTOR = 2f;
	private final static int STATUS_WINNING_TICK = 10000;

	private final long create_timestamp;
	private final int[] participant_state;
	private final GameSession session;
	private final int database_id;
	private int game_state = GAME_STARTING;
	private long start_timestamp;
	private boolean free_quit = true;
	private int[] last_status;
	private int last_tick = -1;
	private boolean game_ended;
	
	private boolean all_5_wins;
	private int[] player_ratings;
	
	public TimestampedGameSession(GameSession session, int database_id) {
		this.session = session;
		this.database_id = database_id;
		int num_participants = session.getParticipants().length;
		participant_state = new int[num_participants];
		last_status = new int[num_participants];
		this.create_timestamp = System.currentTimeMillis();
		String nicks = " ";
		for (int i = 0; i < num_participants; i++)
			nicks += session.getParticipants()[i].getNick() + " ";
		MatchmakingServer.getLogger().info("Game " + database_id + " created. [" + nicks + "] " + getParticipantStates());
	}

	private final String getParticipantStates() {
		String result = "";
		for (int i = 0; i < participant_state.length; i++)
			result += PARTICIPANT_DEBUG_CHARS[participant_state[i]];
		return result;

	}

	public final GameSession getSession() {
		return session;
	}

	public final int getDatabaseID() {
		return database_id;
	}

	public final boolean join(MatchmakingServer server, Client client) {
		if (!free_quit || game_state != GAME_STARTING || System.currentTimeMillis() - create_timestamp > JOIN_MAX_TIME)
			return false;
		int index = findIndex(server, client);
		
		if (index != -1 && participant_state[index] == PARTICIPANT_UNKNOWN) {
			Participant[] participants = session.getParticipants();
			participant_state[index] = PARTICIPANT_JOINED;
			MatchmakingServer.getLogger().info("Game " + database_id + ": joined " + getParticipantStates());
			
			int num_joined = 0;
			for (int i = 0; i < participant_state.length; i++)
				if (participant_state[i] != PARTICIPANT_UNKNOWN)
					num_joined++;
			if (num_joined == participant_state.length) {
				start_timestamp = System.currentTimeMillis();
				game_state = GAME_ALL_JOINED;
				MatchmakingServer.getLogger().info("Game " + database_id + ": all joined game " + getParticipantStates());

				//saving rated player info (someone could lose and delete a profile before the game ends)
				all_5_wins = true;
				player_ratings = new int[participants.length];
				for (int i = 0; i < participants.length; i++) {
					String nick = participants[i].getNick();
					try {
						if (DBInterface.getWins(nick) < GameSession.MIN_WINS_FOR_RANKING)
							all_5_wins = false;
					} catch (SQLException e) {
						all_5_wins = false; //participant must be a guest
						return true;
					}
					try {
						player_ratings[i] = DBInterface.getRating(participants[i].getNick());
					} catch (SQLException e) {
						throw new RuntimeException("Could you find rating for nick=" + nick + " e=" + e);
					}
				}
			}
			return true;
		}
		return false;
	}

	public final long getStartTime() {
		return start_timestamp;
	}

	public final void freeQuitStop() {
		free_quit = false;
	}

	public final void updateGameStatus(int tick, int[] status) {
		if (status.length != session.getParticipants().length || game_state != GAME_ALL_JOINED)
			return;
		if (last_status != null) {
			if (tick > last_tick) {
				last_tick = tick;
				last_status = status;
				DBInterface.saveGameReport(database_id, tick, getTeamScores(status));
			} else if (tick == last_tick) {
				for (int i = 0; i < status.length; i++)
					if (last_status[i] != status[i]) {
						last_status = null;
						return;
					}
			}
		}
	}

	private final int getWinningTeamFromLastStatus() {
		if (last_status != null && last_tick > STATUS_WINNING_TICK) {
			int[] team_score = getTeamScores(last_status);
			int best_team = -1;
			int best_score = 1;
			int next_score = 1;
			for (int i = 0; i < team_score.length; i++) {
				if (team_score[i] > best_score) {
					next_score = best_score;
					best_score = team_score[i];
					best_team = i;
				} else if (team_score[i] > next_score) {
					next_score = team_score[i];
				}
			}
			float factor = best_score/(float)next_score;
			if (factor >= STATUS_WINNING_FACTOR) {
				return best_team;
			}
		}
		return -1;
	}
	
	private final int[] getTeamScores(int[] status) {
		int[] team_score = new int[MatchmakingServerInterface.MAX_PLAYERS];
		Participant[] participants = session.getParticipants();
		for (int i = 0; i < participants.length; i++)
			team_score[participants[i].getTeam()] += status[i];
		return team_score;
	}

	public final void participantQuit(MatchmakingServer server, Client client) {
		if (!free_quit) {
			return;
		}
		int index = findIndex(server, client);
		if (index == -1) {
			return;
		}
		if (participant_state[index] == PARTICIPANT_JOINED)
			participant_state[index] = PARTICIPANT_FREE_QUIT;
	}
	
	public final void gameQuit(MatchmakingServer server, Client client) {
		int index = findIndex(server, client);
		
		if (!free_quit && !(participant_state[index] == PARTICIPANT_FREE_QUIT)) {
			game_state = GAME_INVALID;
			MatchmakingServer.getLogger().warning("Game " + database_id + " is now invalid. " + client.getUsername() + " tried to free_quit. " + getParticipantStates());
		}
		gameDone(server, client, PARTICIPANT_QUIT, "quit");
	}
	
	public final void gameLost(MatchmakingServer server, Client client) {
		gameDone(server, client, PARTICIPANT_LOST, "lost");
	}

	public final void gameWon(MatchmakingServer server, Client client) {
		int index = findIndex(server, client);
		if (participant_state[index] == PARTICIPANT_FREE_QUIT) {
			game_state = GAME_INVALID;
			MatchmakingServer.getLogger().warning("Game " + database_id + " is now invalid. " + client.getUsername() + " tried to win while having free_quit. " + getParticipantStates());
		}
		gameDone(server, client, PARTICIPANT_WON, "won");
	}

	private final int findIndex(MatchmakingServer server, Client client) {
		Participant[] participants = session.getParticipants();
		for (int i = 0; i < participants.length; i++) {
			Client search_client = server.getClientFromID(participants[i].getMatchID());
			if (search_client == client)
				return i;
		}
		return -1;
	}
	
	private final void gameDone(MatchmakingServer server, Client client, int result, String result_string) {
		participant_state[findIndex(server, client)] = result;
		MatchmakingServer.getLogger().info("Game " + database_id + ": " + client.getUsername() + " finished. Result " + result_string + " " + getParticipantStates());
		//if (game_state != GAME_STARTING)
			evaluateGame(server);
	}
	
	private final void evaluateGame(MatchmakingServer server) {
		Participant[] participants = session.getParticipants();
		int[] team_sizes = new int[MatchmakingServerInterface.MAX_PLAYERS];
		int[] team_done = new int[MatchmakingServerInterface.MAX_PLAYERS];
		int[] team_result = new int[MatchmakingServerInterface.MAX_PLAYERS];
		for (int i = 0; i < participants.length; i++) {
			int team = participants[i].getTeam();
			int state = participant_state[i];
			team_sizes[team]++;
			if (state == PARTICIPANT_QUIT || state == PARTICIPANT_LOST || state == PARTICIPANT_WON) {
				team_done[team]++;
				if (team_result[team] == TEAM_UNKNOWN && state == PARTICIPANT_QUIT)
					team_result[team] = TEAM_QUIT;
				if ((team_result[team] == TEAM_UNKNOWN || team_result[team] == TEAM_QUIT) && state == PARTICIPANT_LOST)
					team_result[team] = TEAM_LOST;
				if (state == PARTICIPANT_WON)
					team_result[team] = TEAM_WON;
			}
		}
		int winning_teams = 0;
		int winning_team_index = -1;
		boolean  teams_lost = false;
		for (int i = 0; i < team_sizes.length; i++) {
			if (team_sizes[i] == team_done[i]) {
				if (team_result[i] == TEAM_WON) {
					winning_teams++;
					winning_team_index = i;
				} else if (team_result[i] == TEAM_LOST)
					teams_lost = true;
			} else
				return; // someone is still playing
		}
		long end_time = System.currentTimeMillis();
		if (winning_teams == 0) {
			MatchmakingServer.getLogger().info("Game " + database_id + ". No winning teams " + getParticipantStates());
			DBInterface.endGame(this, end_time, -1);
			game_ended = true;
			return; // last players disconnected
		}
		
		if (winning_teams > 1 || game_state == GAME_INVALID) {
			winning_team_index = getWinningTeamFromLastStatus();
			if (winning_team_index != -1) {
				MatchmakingServer.getLogger().info("Game " + database_id + ". Team "+(winning_team_index+1)+" won from status reports. " + getParticipantStates());
				teams_lost = true;
				for (int i = 0; i < team_result.length; i++)
					if (i == winning_team_index)
						team_result[i] = TEAM_WON;
					else
						team_result[i] = TEAM_LOST;
			} else {
				// someone cheated - everyone gets an invalid_game
				for (int i = 0; i < participants.length; i++) {
					String nick = participants[i].getNick();
					MatchmakingServer.getLogger().warning("Game " + database_id + ". " + nick + " ended invalid game " + getParticipantStates());
					DBInterface.increaseInvalidGames(nick);
					Client client = server.getClientFromID(participants[i].getMatchID());
					if (client != null)
						client.updateProfile();
				}
				MatchmakingServer.getLogger().warning("Game " + database_id + " was invalid. " + winning_teams + " winning teams. " + getParticipantStates());
				DBInterface.endGame(this, end_time, -1);
				game_ended = true;
				return;
			}
		}
		
		if (teams_lost)
			teamWon(server, team_result);
		else {
			MatchmakingServer.getLogger().warning("Game " + database_id + ". No one lost. Playing agains AI " + getParticipantStates());
			DBInterface.endGame(this, end_time, -1);
			game_ended = true;
			return;
		}

		DBInterface.endGame(this, end_time, winning_team_index);
		game_ended = true;
	}

	protected final void finalize() {
		if (!game_ended)
			DBInterface.endGame(this, System.currentTimeMillis(), -1);
	}

	private final void teamWon(MatchmakingServer server, int[] team_result) {
		Participant[] participants = session.getParticipants();
		for (int i = 0; i < participants.length; i++) {
			String nick = participants[i].getNick();
			int team = participants[i].getTeam();
			if (team_result[team] == TEAM_WON) {
				MatchmakingServer.getLogger().info("Game " + database_id + ". " + nick + " won game");
				DBInterface.increaseWins(nick);
			} else if (team_result[team] == TEAM_LOST) {
				MatchmakingServer.getLogger().info("Game " + database_id + ". " + nick + " lost game");
				DBInterface.increaseLosses(nick);
			}
			Client client = server.getClientFromID(participants[i].getMatchID());
			if (client != null)
				client.updateProfile();
		}
		if (session.isRated() && all_5_wins)
			rerateParticipants(server, team_result);
	}

	private final void rerateParticipants(MatchmakingServer server, int[] team_result) {
		Participant[] participants = session.getParticipants();
		int[] player_teams = new int[participants.length];
		
		for (int i = 0; i < participants.length; i++) {
			int team = participants[i].getTeam();
			assert team < 2: "Participant on team " + team;
			player_teams[i] = team;
		}
		int[][] points = GameSession.calculateMatchPoints(player_ratings, player_teams);
		for (int i = 0; i < participants.length; i++) {
			String nick = participants[i].getNick();
			int dpoints;
			if (team_result[player_teams[i]] == TEAM_WON)
				dpoints = points[i][GameSession.WIN];
			else
				dpoints = points[i][GameSession.LOSE];
			
			MatchmakingServer.getLogger().info("Game " + database_id + ". " + nick + " rating change was " + dpoints);
			DBInterface.updateRating(participants[i].getNick(), dpoints);
			
			Client client = server.getClientFromID(participants[i].getMatchID());
			if (client != null)
				client.updateProfile();
		}
	}
}
