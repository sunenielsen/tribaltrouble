package com.oddlabs.matchserver;

import java.util.List;
import java.util.ArrayList;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.io.UnsupportedEncodingException;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.StackKeyedObjectPoolFactory;

import com.oddlabs.matchmaking.Profile;
import com.oddlabs.matchmaking.Login;
import com.oddlabs.matchmaking.Game;
import com.oddlabs.matchmaking.Participant;
import com.oddlabs.matchmaking.LoginDetails;
import com.oddlabs.matchmaking.RankingEntry;
import com.oddlabs.matchmaking.GameSession;
import com.oddlabs.util.CryptUtils;
import com.oddlabs.util.DBUtils;

public final strictfp class DBInterface {
	
	public final static String getRegKeyUsername(String reg_key) throws IllegalArgumentException {
		try {
			PreparedStatement stmt = DBUtils.createStatement("SELECT username FROM registrations R WHERE R.reg_key = ? AND NOT R.disabled AND NOT R.banned");
			try {
				stmt.setString(1, reg_key);
				ResultSet result = stmt.executeQuery();
				try {
					result.next();
					return result.getString("username");
				} finally {
					result.close();
				}
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getRegKeyUsername", e);
			throw new IllegalArgumentException("key " + reg_key + " not i DB");
		}
	}
	
	public final static boolean usernameExists(String username) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("SELECT username FROM registrations R WHERE lower(R.username) = lower(?)");
			try {
				stmt.setString(1, username);
				ResultSet result = stmt.executeQuery();
				try {
					boolean user_exists = result.first();
					return user_exists;
				} finally {
					result.close();
				}
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "queryUser", e);
			throw new RuntimeException(e);
		}
	}

	public final static void createUser(Login login, LoginDetails login_details, String reg_key) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("UPDATE registrations R SET username = ?, email = ?, password = ? WHERE R.reg_key = ? AND R.username IS NULL AND R.password IS NULL AND R.email IS NULL");
			try {
				stmt.setString(1, login.getUsername());
				stmt.setString(2, login_details.getEmail());
				stmt.setString(3, CryptUtils.digest(login.getPasswordDigest()));
				stmt.setString(4, reg_key);
				int row_count = stmt.executeUpdate();
				assert row_count == 1;
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "createUser", e);
			throw new RuntimeException(e);
		}
	}

	public final static boolean queryUser(String username, String password) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("SELECT username, password FROM registrations R WHERE lower(R.username) = lower(?) AND R.password = ? AND NOT R.disabled AND NOT R.banned");
			try {
				stmt.setString(1, username);
				stmt.setString(2, CryptUtils.digest(password));
				ResultSet result = stmt.executeQuery();
				try {
					boolean user_exists = result.first();
					return user_exists;
				} finally {
					result.close();
				}
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "queryUser", e);
			throw new RuntimeException(e);
		}
	}
	
	public final static Profile[] getProfiles(String username, int revision) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("SELECT nick, rating, wins, losses, invalid FROM profiles P, registrations R WHERE P.reg_id = R.id AND R.username = ?");
			try {
				stmt.setString(1, username);
				ResultSet result = stmt.executeQuery();
				try {
					List profiles = new ArrayList();
					int index = 1;
					while (result.next()) {
						String nick = result.getString("nick").trim();
						int rating = result.getInt("rating");
						int wins = result.getInt("wins");
						int losses = result.getInt("losses");
						int invalid = result.getInt("invalid");
						profiles.add(new Profile(nick, rating, wins, losses, invalid, revision));
					}
					Profile[] profile_array = new Profile[profiles.size()];
					for (int i = 0; i < profile_array.length; i++)
						profile_array[i] = (Profile)profiles.get(i);
						
					return profile_array;
				} finally {
					result.close();
				}
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getProfiles", e);
			throw new RuntimeException(e);
		}
	}

	public final static Profile getProfile(String username, String nick, int revision) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("SELECT rating, wins, losses, invalid FROM profiles P, registrations R WHERE P.reg_id = R.id AND R.username = ? AND P.nick = ?");
			try {
				stmt.setString(1, username);
				stmt.setString(2, nick);
				ResultSet result = stmt.executeQuery();
				try {
					result.next();
					int rating = result.getInt("rating");
					int wins = result.getInt("wins");
					int losses = result.getInt("losses");
					int invalid = result.getInt("invalid");
					return new Profile(nick, rating, wins, losses, invalid, revision);
				} finally {
					result.close();
				}
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			return null;
		}
	}

	public final static void setLastUsedProfile(String username, String nick) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("UPDATE registrations R SET last_used_profile = ? WHERE R.username = ?");
			try {
				stmt.setString(1, nick);
				stmt.setString(2, username);
				int row_count = stmt.executeUpdate();
				assert row_count == 1;
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "setLastUsedProfile", e);
			throw new RuntimeException(e);
		}
	}
	
	public final static String getLastUsedProfile(String username) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("SELECT last_used_profile FROM registrations R WHERE R.username = ?");
			try {
				stmt.setString(1, username);
				ResultSet result = stmt.executeQuery();
				try {
					result.next();
					String nick = result.getString("last_used_profile");
					return nick;
				} finally {
					result.close();
				}
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getLastUsedProfile", e);
			throw new RuntimeException(e);
		}
	}

		
	private final static int getRegID(String username) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("SELECT id FROM registrations R WHERE R.username = ?");
			try {
				stmt.setString(1, username);
				ResultSet result = stmt.executeQuery();
				try {
					result.next();
					return result.getInt("id");
				} finally {
					result.close();
				}
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "private getRegID", e);
			throw new RuntimeException(e);
		}
	}
	
	public final static boolean nickExists(String nick) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("SELECT nick FROM profiles P WHERE lower(P.nick) = lower(?)");
			try {
				stmt.setString(1, nick);
				ResultSet result = stmt.executeQuery();
				try {
					boolean nick_exists = result.first();
					return nick_exists;
				} finally {
					result.close();
				}
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "queryUser", e);
			throw new RuntimeException(e);
		}
	}

	public final static void saveGameReport(int game_id, int tick, int[] team_score) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("INSERT INTO game_reports (game_id, tick, team1, team2, team3, team4, team5, team6) " + 
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			try {
				stmt.setInt(1, game_id);
				stmt.setInt(2, tick);
				stmt.setInt(3, team_score[0]);
				stmt.setInt(4, team_score[1]);
				stmt.setInt(5, team_score[2]);
				stmt.setInt(6, team_score[3]);
				stmt.setInt(7, team_score[4]);
				stmt.setInt(8, team_score[5]);
				int row_count = stmt.executeUpdate();
				assert row_count == 1;
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "createProfile", e);
			throw new RuntimeException(e);
		}
	}
	
	public final static void logPriority(int game_id, String nick1, String nick2, int priority) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("INSERT INTO connections (game_id, nick1, nick2, priority) " + 
					"VALUES (?, ?, ?, ?)");
			try {
				stmt.setInt(1, game_id);
				stmt.setString(2, nick1);
				stmt.setString(3, nick2);
				stmt.setInt(4, priority);
				int row_count = stmt.executeUpdate();
				assert row_count == 1;
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "logPriority", e);
			throw new RuntimeException(e);
		}
	}
	
	public final static void createProfile(String username, String nick) {
		int reg_id = getRegID(username);
		try {
			PreparedStatement stmt = DBUtils.createStatement("INSERT INTO profiles (reg_id, nick, rating, wins, losses, invalid) " + 
					"VALUES (?, ?, 1000, 0, 0, 0)");
			try {
				stmt.setInt(1, reg_id);
				stmt.setString(2, nick);
				int row_count = stmt.executeUpdate();
				assert row_count == 1;
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "createProfile", e);
			throw new RuntimeException(e);
		}
	}
	
	public final static void deleteProfile(String username, String nick) {
		Profile profile = getProfile(username, nick, -1);
		if (profile != null) {
			int reg_id = getRegID(username);
			try {
				PreparedStatement stmt = DBUtils.createStatement("INSERT INTO deleted_profiles (reg_id, nick, rating, wins, losses, invalid) " + 
						"VALUES (?, ?, ?, ?, ?, ?)");
				try {
					stmt.setInt(1, reg_id);
					stmt.setString(2, profile.getNick());
					stmt.setInt(3, profile.getRating());
					stmt.setInt(4, profile.getWins());
					stmt.setInt(5, profile.getLosses());
					stmt.setInt(6, profile.getInvalid());
					int row_count = stmt.executeUpdate();
					assert row_count == 1: row_count;
				} finally {
					stmt.getConnection().close();
				}
			} catch (SQLException e) {
				MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "deleteProfile INSERT", e);
			}
			// drop profile
			try {
				PreparedStatement stmt = DBUtils.createStatement("DELETE FROM profiles WHERE nick = ?");
				try {
					stmt.setString(1, nick);
					int row_count = stmt.executeUpdate();
					assert row_count == 1: row_count;
				} finally {
					stmt.getConnection().close();
				}
			} catch (SQLException e) {
				MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "deleteProfile DELETE", e);
			}
		}
	}
	
	public final static void increaseLosses(String nick) {
		increaseField("losses", nick );
	}

	public final static void increaseWins(String nick) {
		increaseField("wins", nick);
	}

	public final static void increaseInvalidGames(String nick) {
		increaseField("invalid", nick);
	}
	
	public final static void increaseField(String field, String nick) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("UPDATE profiles P SET " + field + " = " + field + " + 1 WHERE P.nick = ?");
			try {
				stmt.setString(1, nick);
				int result = stmt.executeUpdate();
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "update" + field, e);
		}
	}

	public final static void updateRating(String nick, int rating_delta) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("UPDATE profiles P SET rating = rating + ? WHERE P.nick = ?");
			try {
				stmt.setInt(1, rating_delta);
				stmt.setString(2, nick);
				int result = stmt.executeUpdate();
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "updateRating", e);
		}
	}

	public final static int getWins(String nick) throws SQLException {
		return getIntField("wins", nick);
	}
	
	public final static int getRating(String nick) throws SQLException {
		return getIntField("rating", nick);
	}
	
	public final static int getIntField(String int_field, String nick) throws SQLException {
//		try {
			PreparedStatement stmt = DBUtils.createStatement("SELECT " + int_field + " FROM profiles P WHERE P.nick = ?");
			try {
				stmt.setString(1, nick);
				ResultSet result = stmt.executeQuery();
				try {
					result.next();
					return result.getInt(int_field);
				} finally {
					result.close();
				}
			} finally {
				stmt.getConnection().close();
			}
/*		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getIntField", e);
			return 0;
		}*/
	}
	
	public final static String getSetting(String setting) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("SELECT value FROM settings S WHERE S.property = ?");
			try {
				stmt.setString(1, setting);
				ResultSet result = stmt.executeQuery();
				try {
					result.next();
					return result.getString("value");
				} finally {
					result.close();
				}
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getSetting", e);
			throw new RuntimeException(e);
		}
	}
	
	public final static int getSettingsInt(String setting) {
		try {
			String value = getSetting(setting);
			return (Integer.valueOf(value)).intValue();
		} catch (Exception e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getSettingsInt", e);
			throw new RuntimeException(e);
		}
	}
	
	public final static RankingEntry[] getTopRankings(int number) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("SELECT nick, rating, wins, losses, invalid FROM profiles P WHERE P.wins >= "+ GameSession.MIN_WINS_FOR_RANKING +" ORDER BY rating DESC, (wins - losses) DESC, wins DESC LIMIT ?");
			try {
				stmt.setInt(1, number);
				ResultSet result = stmt.executeQuery();
				try {
					List rankings = new ArrayList();
					int index = 1;
					while (result.next()) {
						String nick = result.getString("nick").trim();
						int rating = result.getInt("rating");
						int wins = result.getInt("wins");
						int losses = result.getInt("losses");
						int invalid = result.getInt("invalid");
						rankings.add(new RankingEntry(index++, nick, rating, wins, losses, invalid));
					}
					RankingEntry[] ranking_array = new RankingEntry[rankings.size()];
					for (int i = 0; i < ranking_array.length; i++)
						ranking_array[i] = (RankingEntry)rankings.get(i);
						
					return ranking_array;
				} finally {
					result.close();
				}
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getTopRankings", e);
			return new RankingEntry[0];
		}
	}

	public final static void createGame(Game game, String nick) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("INSERT INTO games (player1_name, time_create, name, rated, speed, size, hills, trees, resources, mapcode, status) " + 
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			try {
				stmt.setString(1, nick);
				stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
				stmt.setString(3, game.getName());
				if (game.isRated())
					stmt.setString(4, "Y");
				else
					stmt.setString(4, "N");
				stmt.setString(5, "" + game.getGamespeed());
				stmt.setString(6, "" + (game.getSize() + 1));
				stmt.setInt(7, game.getHills());
				stmt.setInt(8, game.getTrees());
				stmt.setInt(9, game.getSupplies());
				stmt.setString(10, game.getMapcode());
				stmt.setString(11, "created");
				
				int row_count = stmt.executeUpdate();
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "createGame", e);
			return;
		}
		try {
			PreparedStatement stmt = DBUtils.createStatement("SELECT id FROM games WHERE player1_name = ? AND status = ?");
			try {
				stmt.setString(1, nick);
				stmt.setString(2, "created");
				ResultSet result = stmt.executeQuery();
				try {
					result.next();
					int id = result.getInt("id");
					game.setDatabaseID(id);
				} finally {
					result.close();
				}
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "createGame", e);
			return;
		}
	}

	public final static void initDropGames() {
		try {
			PreparedStatement stmt = DBUtils.createStatement("UPDATE games G SET status = ? WHERE G.status = ?");
			try {
				stmt.setString(1, "dropped");
				stmt.setString(2, "created");
				int result = stmt.executeUpdate();
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "initDropGames", e);
		}
	}
	
	public final static void dropGame(String nick) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("UPDATE games G SET status = ? WHERE G.player1_name = ? AND G.status = ?");
			try {
				stmt.setString(1, "dropped");
				stmt.setString(2, nick);
				stmt.setString(3, "created");
				int result = stmt.executeUpdate();
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "dropGame", e);
		}
	}
	
	public final static void startGame(TimestampedGameSession tgs, MatchmakingServer server) {
		GameSession session = tgs.getSession();
		Participant[] participants = session.getParticipants();
		String participant_sql = "";
		for (int i = 0; i < participants.length; i++)
			participant_sql = participant_sql + "G.player"+(i+1)+"_name = ?, G.player"+(i+1)+"_race = ?, G.player"+(i+1)+"_team = ?, ";
		
		try {
			PreparedStatement stmt = DBUtils.createStatement("UPDATE games G SET " + participant_sql + "G.time_start = ?, G.status = ? WHERE G.id = ?");
			try {
				int index = 1;
				for (int i = 0; i < participants.length; i++) {
					String nick = "Unknown";
					Client client = server.getClientFromID(participants[i].getMatchID());
					if (client != null)
						nick = client.getProfile().getNick();
					stmt.setString(index++, nick);
					if (participants[i].getRace() == 0)
						stmt.setString(index++, "N");
					else
						stmt.setString(index++, "V");
			
					stmt.setInt(index++, participants[i].getTeam());
				}
				stmt.setTimestamp(index++, new Timestamp(System.currentTimeMillis()));
				stmt.setString(index++, "started");
				stmt.setInt(index++, tgs.getDatabaseID());
				int result = stmt.executeUpdate();
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "startGame", e);
		}
	}

	public final static void endGame(TimestampedGameSession tgs, long end_time, int winner) {
		GameSession session = tgs.getSession();
		Participant[] participants = session.getParticipants();
		
		try {
			PreparedStatement stmt = DBUtils.createStatement("UPDATE games G SET G.time_stop = ?, G.status = ?, G.winner = ? WHERE G.id = ?");
			try {
				stmt.setTimestamp(1, new Timestamp(end_time));
				stmt.setString(2, "completed");
				stmt.setInt(3, winner);
				stmt.setInt(4, tgs.getDatabaseID());
				int result = stmt.executeUpdate();
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "endGame", e);
		}
	}
	
	public final static void profileOnline(String nick) {
		MatchmakingServer.getLogger().info("profileOnline '" + nick + "'");
		try {
			PreparedStatement stmt = DBUtils.createStatement("INSERT INTO online_profiles (nick) VALUES (?)");
			try {
				stmt.setString(1, nick);
				int row_count = stmt.executeUpdate();
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "profileOnline", e);
		}
	}
	
	public final static void profileOffline(String nick) {
		MatchmakingServer.getLogger().info("profileOffline '" + nick + "'");
		try {
			PreparedStatement stmt = DBUtils.createStatement("DELETE FROM online_profiles WHERE nick = ?");
			try {
				stmt.setString(1, nick);
				int row_count = stmt.executeUpdate();
				assert row_count == 1: row_count;
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "profileOffline", e);
		}
	}
	
	public final static void profileSetGame(String nick, int game_id) {
		try {
			PreparedStatement stmt = DBUtils.createStatement("UPDATE online_profiles O SET O.game_id = ? WHERE O.nick = ?");
			try {
				stmt.setInt(1, game_id);
				stmt.setString(2, nick);
				int row_count = stmt.executeUpdate();
				assert row_count == 1: row_count + " nick = '" + nick + "' | game_id = " + game_id;
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "profileOffline", e);
		}
	}

	public final static void clearOnlineProfiles() {
		try {
			PreparedStatement stmt = DBUtils.createStatement("TRUNCATE TABLE online_profiles");
			try {
				int row_count = stmt.executeUpdate();
			} finally {
				stmt.getConnection().close();
			}
		} catch (SQLException e) {
			MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "clearOnlineProfiles", e);
		}
	}

}
