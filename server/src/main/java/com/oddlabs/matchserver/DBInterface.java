package com.oddlabs.matchserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.matchmaking.GamePlayer;
import com.oddlabs.matchmaking.GameSession;
import com.oddlabs.matchmaking.Login;
import com.oddlabs.matchmaking.LoginDetails;
import com.oddlabs.matchmaking.OpenSkillLeaderboardRankingEntry;
import com.oddlabs.matchmaking.OpenSkillRating;
import com.oddlabs.matchmaking.Participant;
import com.oddlabs.matchmaking.Profile;
import com.oddlabs.matchmaking.RankingEntry;
import com.oddlabs.matchserver.models.GameDataModel;
import com.oddlabs.matchserver.models.GamePlayerModel;
import com.oddlabs.matchserver.models.VersusMatchupModel;
import com.oddlabs.matchserver.models.VersusMatchupResultModel;
import com.oddlabs.util.CryptUtils;
import com.oddlabs.util.DBUtils;

public final class DBInterface {

    public static boolean usernameExists(String username) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT username FROM registrations R WHERE lower(R.username) = lower(?)")) {
            stmt.setString(1, username);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "usernameExists", e);
            throw new RuntimeException(e);
        }
    }

    public static void createUser(Login login, LoginDetails login_details, String reg_key) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO registrations (username, email, password) VALUES (?, ?, ?)")) {
            stmt.setString(1, login.getUsername());
            stmt.setString(2, login_details.getEmail());
            stmt.setString(3, CryptUtils.digest(login.getPasswordDigest()));
            int row_count = stmt.executeUpdate();
            assert row_count == 1;
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "createUser", e);
            throw new RuntimeException(e);
        }
    }

    public static boolean queryUser(String username, String password) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT username, password FROM registrations R WHERE lower(R.username)" + " = lower(?) AND R.password = ? AND NOT R.disabled AND NOT R.banned")) {
            stmt.setString(1, username);
            stmt.setString(2, CryptUtils.digest(password));
            try (ResultSet result = stmt.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "queryUser", e);
            throw new RuntimeException(e);
        }
    }

    public static Profile[] getProfiles(String username, int revision) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT nick, rating, wins, losses, invalid FROM profiles P," + " registrations R WHERE P.reg_id = R.id AND R.username = ?")) {
            stmt.setString(1, username);
            try (ResultSet result = stmt.executeQuery()) {
                List<Profile> profiles = new ArrayList<>();
                while (result.next()) {
                    String nick = result.getString("nick").trim();
                    int rating = result.getInt("rating");
                    int wins = result.getInt("wins");
                    int losses = result.getInt("losses");
                    int invalid = result.getInt("invalid");
                    profiles.add(new Profile(nick, rating, wins, losses, invalid, revision));
                }
                return profiles.toArray(Profile[]::new);
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getProfiles", e);
            throw new RuntimeException(e);
        }
    }

    public static Profile getProfile(String username, String nick, int revision) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT rating, wins, losses, invalid FROM profiles P, registrations R" + " WHERE P.reg_id = R.id AND R.username = ? AND P.nick = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, nick);
            try (ResultSet result = stmt.executeQuery()) {
                result.next();
                int rating = result.getInt("rating");
                int wins = result.getInt("wins");
                int losses = result.getInt("losses");
                int invalid = result.getInt("invalid");
                return new Profile(nick, rating, wins, losses, invalid, revision);
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            return null;
        }
    }

    public static void setLastUsedProfile(String username, String nick) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE registrations R SET last_used_profile = ? WHERE R.username = ?")) {
            stmt.setString(1, nick);
            stmt.setString(2, username);
            int row_count = stmt.executeUpdate();
            assert row_count == 1;
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "setLastUsedProfile", e);
            throw new RuntimeException(e);
        }
    }

    public static String getLastUsedProfile(String username) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT last_used_profile FROM registrations R WHERE R.username = ?")) {
            stmt.setString(1, username);
            try (ResultSet result = stmt.executeQuery()) {
                result.next();
                return result.getString("last_used_profile");
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getLastUsedProfile", e);
            throw new RuntimeException(e);
        }
    }

    private static int getRegID(String username) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT id FROM registrations R WHERE R.username = ?")) {
            stmt.setString(1, username);
            try (ResultSet result = stmt.executeQuery()) {
                result.next();
                return result.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "private getRegID", e);
            throw new RuntimeException(e);
        }
    }

    public static boolean nickExists(String nick) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT nick FROM profiles P WHERE lower(P.nick) = lower(?)")) {
            stmt.setString(1, nick);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "nickExists", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns [word, match_type] rows from banned_words. Fails open (empty list) so a missing or
     * broken table degrades to no filtering instead of blocking logins and profile creation.
     */
    public static List<String[]> getBannedWords() {
        List<String[]> words = new ArrayList<>();
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT word, match_type FROM banned_words")) {
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    words.add(new String[]{result.getString("word"), result.getString("match_type")});
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getBannedWords", e);
        }
        return words;
    }

    public static boolean addBannedWord(String word, String match_type) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT IGNORE INTO banned_words (word, match_type) VALUES (?, ?)")) {
            stmt.setString(1, word);
            stmt.setString(2, match_type);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "addBannedWord", e);
            throw new RuntimeException(e);
        }
    }

    public static boolean removeBannedWord(String word) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM banned_words WHERE word = ?")) {
            stmt.setString(1, word);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "removeBannedWord", e);
            throw new RuntimeException(e);
        }
    }

    public static boolean setBannedByNick(String nick, boolean banned) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE registrations R INNER JOIN profiles P ON P.reg_id = R.id SET" + " R.banned = ? WHERE lower(P.nick) = lower(?)")) {
            stmt.setBoolean(1, banned);
            stmt.setString(2, nick);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "setBannedByNick", e);
            throw new RuntimeException(e);
        }
    }

    public static void saveGameReport(int game_id, int tick, int[] team_score) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO game_report_teams (game_id, tick, team_index, score) VALUES (?, ?, ?, ?)")) {
            for (int i = 0; i < team_score.length; i++) {
                if (team_score[i] == 0) continue;
                stmt.setInt(1, game_id);
                stmt.setInt(2, tick);
                stmt.setInt(3, i);
                stmt.setInt(4, team_score[i]);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "saveGameReport", e);
            throw new RuntimeException(e);
        }
    }

    public static void logPriority(int game_id, String nick1, String nick2, int priority) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO connections (game_id, nick1, nick2, priority) VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, game_id);
            stmt.setString(2, nick1);
            stmt.setString(3, nick2);
            stmt.setInt(4, priority);
            int row_count = stmt.executeUpdate();
            assert row_count == 1;
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "logPriority", e);
            throw new RuntimeException(e);
        }
    }

    public static void createProfile(String username, String nick) {
        int reg_id = getRegID(username);
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO profiles (reg_id, nick, rating, wins, losses, invalid) " + "VALUES (?, ?, 1000, 0, 0, 0)")) {
            stmt.setInt(1, reg_id);
            stmt.setString(2, nick);
            int row_count = stmt.executeUpdate();
            assert row_count == 1;
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "createProfile", e);
            throw new RuntimeException(e);
        }
        createOpenSkillRating(nick);
    }

    public static void deleteProfile(String username, String nick) {
        Profile profile = getProfile(username, nick, -1);
        if (profile != null) {
            int reg_id = getRegID(username);
            try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO deleted_profiles (reg_id, nick, rating, wins, losses," + " invalid) VALUES (?, ?, ?, ?, ?, ?)")) {
                stmt.setInt(1, reg_id);
                stmt.setString(2, profile.getNick());
                stmt.setInt(3, profile.getRating());
                stmt.setInt(4, profile.getWins());
                stmt.setInt(5, profile.getLosses());
                stmt.setInt(6, profile.getInvalid());
                int row_count = stmt.executeUpdate();
                assert row_count == 1 : row_count;
            } catch (SQLException e) {
                System.out.println("Exception: " + e);
                MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "deleteProfile INSERT", e);
            }
            // drop profile
            try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM profiles WHERE nick = ?")) {
                stmt.setString(1, nick);
                int row_count = stmt.executeUpdate();
                assert row_count == 1 : row_count;
            } catch (SQLException e) {
                System.out.println("Exception: " + e);
                MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "deleteProfile DELETE", e);
            }
            deleteOpenSkillRating(nick);
        }
    }

    public static String getOrCreateSteamRegistration(long steamId) {
        // Look up existing registration by Steam ID
        boolean isBannedOrDisabled = false;
        String existingUsername = null;

        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT username, banned, disabled FROM registrations WHERE steam_id = ?")) {
            stmt.setLong(1, steamId);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    boolean banned = result.getBoolean("banned");
                    boolean disabled = result.getBoolean("disabled");
                    if (banned || disabled) {
                        isBannedOrDisabled = true;
                    } else {
                        existingUsername = result.getString("username").trim();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getOrCreateSteamRegistration", e);
            throw new RuntimeException(e);
        }

        if (isBannedOrDisabled) {
            return null;
        }
        if (existingUsername != null) {
            return existingUsername;
        }

        // No registration yet — create one. Profile is left unset; the client will
        // be routed to the profile-creation screen so the player picks their own nick.
        String username = "steam_" + steamId;
        String email = "steam_" + steamId + "@steam.internal";

        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO registrations (username, email, password, steam_id," + " disabled, banned) VALUES (?, ?, 'LOCKED', ?, 0, 0)")) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setLong(3, steamId);
            int row_count = stmt.executeUpdate();
            assert row_count == 1;
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getOrCreateSteamRegistration", e);
            throw new RuntimeException(e);
        }

        return username;
    }

    public static String getProfileNickBySteamId(long steamId) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT p.nick FROM profiles p INNER JOIN registrations r ON p.reg_id =" + " r.id WHERE r.steam_id = ?")) {
            stmt.setLong(1, steamId);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return result.getString("nick").trim();
                }
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getProfileNickBySteamId", e);
            return null;
        }
    }

    public static Long getSteamIdByNick(String nick) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT r.steam_id FROM profiles p INNER JOIN registrations r ON" + " p.reg_id = r.id WHERE p.nick = ?")) {
            stmt.setString(1, nick);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    long steamId = result.getLong("steam_id");
                    if (result.wasNull()) {
                        return null; // Not a Steam player
                    }
                    return steamId;
                }
                return null; // Player not found
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getSteamIdByNick", e);
            return null;
        }
    }

    public static void updateStreaks(String nick, int currentStreak, int bestStreak) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE profiles SET current_win_streak = ?, best_win_streak = ? WHERE nick = ?")) {
            stmt.setInt(1, currentStreak);
            stmt.setInt(2, bestStreak);
            stmt.setString(3, nick);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "updateStreaks", e);
        }
    }

    public static int[] getStreaks(String nick) throws SQLException {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT current_win_streak, best_win_streak FROM profiles WHERE nick = ?")) {
            stmt.setString(1, nick);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    int currentStreak = result.getInt("current_win_streak");
                    int bestStreak = result.getInt("best_win_streak");
                    return new int[]{currentStreak, bestStreak};
                }
                return new int[]{0, 0}; // Player not found, return defaults
            }
        }
    }

    /**
     * Nicks with a streak above zero mapped to that streak, longest first.
     */
    public static Map<String, Integer> getStreakLeaders(boolean current, int count) {
        String column = current ? "current_win_streak" : "best_win_streak";
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT nick, " + column + " AS streak FROM profiles WHERE " + column + " > 0 ORDER BY " + column + " DESC, nick LIMIT ?")) {
            stmt.setInt(1, count);
            try (ResultSet result = stmt.executeQuery()) {
                Map<String, Integer> leaders = new LinkedHashMap<>();
                while (result.next()) {
                    leaders.put(result.getString("nick").trim(), result.getInt("streak"));
                }
                return leaders;
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getStreakLeaders", e);
        }
        return new LinkedHashMap<>();
    }

    public static void increaseLosses(String nick) {
        increaseField("losses", nick);
    }

    public static void increaseWins(String nick) {
        increaseField("wins", nick);
    }

    public static void increaseInvalidGames(String nick) {
        increaseField("invalid", nick);
    }

    public static void increaseField(String field, String nick) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE profiles P SET " + field + " = " + field + " + 1 WHERE P.nick = ?")) {
            stmt.setString(1, nick);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "update" + field, e);
        }
    }

    public static void updateRating(String nick, int rating_delta) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE profiles P SET rating = rating + ? WHERE P.nick = ?")) {
            stmt.setInt(1, rating_delta);
            stmt.setString(2, nick);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "updateRating", e);
        }
    }

    public static int getWins(String nick) throws SQLException {
        return getIntField("wins", nick);
    }

    public static int getRating(String nick) throws SQLException {
        return getIntField("rating", nick);
    }

    public static int getIntField(String int_field, String nick) throws SQLException {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT " + int_field + " FROM profiles P WHERE P.nick = ?")) {
            stmt.setString(1, nick);
            try (ResultSet result = stmt.executeQuery()) {
                result.next();
                return result.getInt(int_field);
            }
        }
    }

    public static String getSetting(String setting) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT value FROM settings S WHERE S.property = ?")) {
            stmt.setString(1, setting);
            try (ResultSet result = stmt.executeQuery()) {
                result.next();
                return result.getString("value");
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getSetting", e);
            throw new RuntimeException(e);
        }
    }

    public static int getSettingsInt(String setting) {
        try {
            return Integer.parseInt(getSetting(setting));
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getSettingsInt", e);
            throw new RuntimeException(e);
        }
    }

    public static RankingEntry[] getRankings(String nick, int radius) {
        String sql = "SELECT nick, rating, wins, losses, invalid, row_num FROM (  SELECT nick," + " rating, wins, losses, invalid, ROW_NUMBER() OVER (ORDER BY rating DESC," + " (wins - losses) DESC, wins DESC) AS row_num   FROM profiles) ranked" + " WHERE ABS(CAST(row_num AS SIGNED) - (  SELECT CAST(row_num AS SIGNED)" + " FROM (    SELECT nick, ROW_NUMBER() OVER (ORDER BY rating DESC, (wins -" + " losses) DESC, wins DESC) AS row_num FROM profiles  ) sub WHERE nick =" + " ?)) <= ? ORDER BY row_num";

        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                sql)) {
            stmt.setString(1, nick);
            stmt.setInt(2, radius);
            try (ResultSet result = stmt.executeQuery()) {
                List<RankingEntry> rankings = new ArrayList<>();
                while (result.next()) {
                    String nick_name = result.getString("nick");
                    int rating = result.getInt("rating");
                    int wins = result.getInt("wins");
                    int losses = result.getInt("losses");
                    int invalid = result.getInt("invalid");
                    int ranking = result.getInt("row_num");
                    rankings.add(
                            new RankingEntry(ranking, nick_name, rating, wins, losses, invalid));
                }
                return rankings.toArray(RankingEntry[]::new);
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getRankings(nick, radius)", e);
            return new RankingEntry[0];
        }
    }

    public static RankingEntry[] getRankings(int start, int count) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT nick, rating, wins, losses, invalid FROM profiles P WHERE" + " (P.wins > 0 OR P.losses > 0) ORDER BY rating DESC, (wins -" + " losses) DESC, wins DESC LIMIT ? OFFSET ?")) {
            stmt.setInt(1, count);
            stmt.setInt(2, start);
            try (ResultSet result = stmt.executeQuery()) {
                List<RankingEntry> rankings = new ArrayList<>();
                int index = 1;
                while (result.next()) {
                    String nick = result.getString("nick").trim();
                    int rating = result.getInt("rating");
                    int wins = result.getInt("wins");
                    int losses = result.getInt("losses");
                    int invalid = result.getInt("invalid");
                    rankings.add(
                            new RankingEntry(index++, nick, rating, wins, losses, invalid));
                }
                return rankings.toArray(RankingEntry[]::new);
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getRankings(start, count)", e);
            return new RankingEntry[0];
        }
    }

    public static RankingEntry[] getTopRankings(int number) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT nick, rating, wins, losses, invalid FROM profiles P WHERE" + " P.wins >= " + GameSession.MIN_WINS_FOR_RANKING + " ORDER BY rating DESC, (wins - losses) DESC, wins DESC LIMIT ?")) {
            stmt.setInt(1, number);
            try (ResultSet result = stmt.executeQuery()) {
                List<RankingEntry> rankings = new ArrayList<>();
                int index = 1;
                while (result.next()) {
                    String nick = result.getString("nick").trim();
                    int rating = result.getInt("rating");
                    int wins = result.getInt("wins");
                    int losses = result.getInt("losses");
                    int invalid = result.getInt("invalid");
                    rankings.add(
                            new RankingEntry(index++, nick, rating, wins, losses, invalid));
                }
                return rankings.toArray(RankingEntry[]::new);
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getTopRankings", e);
            return new RankingEntry[0];
        }
    }

    //region OpenSkill

    @NullMarked
    public static @Nullable OpenSkillRating getOpenSkillRating(String nick) throws SQLException {
        try (
             Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                     "SELECT mu, sigma FROM openskill_rating WHERE nick = ?");
        ) {
            stmt.setString(1, nick);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    var mu = result.getDouble("mu");
                    var sigma = result.getDouble("sigma");
                    return new OpenSkillRating(nick, mu, sigma);
                }
            }
        }
        return null;
    }

    @NullMarked
    public static void upsertOpenSkillRating(OpenSkillRating rating) throws SQLException {
        try (
             Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO openskill_rating (nick, mu, sigma) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE mu = VALUES(mu), sigma = VALUES(sigma)");
        ) {
            stmt.setString(1, rating.nick());
            stmt.setDouble(2, rating.mu());
            stmt.setDouble(3, rating.sigma());
            stmt.executeUpdate();
        }
    }

    @NullMarked
    private static void createOpenSkillRating(String nick) {
        try {
            upsertOpenSkillRating(new OpenSkillRating(nick, OpenSkillRatingSystem.INITIAL_MU,
                    OpenSkillRatingSystem.INITIAL_SIGMA));
        } catch (SQLException e) {
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "seedOpenSkillRating", e);
        }
    }

    @NullMarked
    private static void deleteOpenSkillRating(String nick) {
        try (
             Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM openskill_rating WHERE nick = ?");
        ) {
            stmt.setString(1, nick);
            stmt.executeUpdate();
        } catch (SQLException e) {
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "deleteOpenSkillRating", e);
        }
    }

    /**
     * Returns the top {@code n} OpenSkill players ordered by display rating
     * ({@code SCALING_FACTOR*(mu - sigma)}). Players who have never played a game (sigma still at
     * the default value) are excluded; players who have played but whose skill is still uncertain
     * (sigma above the provisional threshold) are included and marked provisional so the client
     * can show a {@code ?} next to their rating. Players tied on rating share the same rank.
     */
    @NullMarked
    public static OpenSkillLeaderboardRankingEntry[] getTopOpenSkillRankingEntries(int n) {
        try (
             Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                     """
                             SELECT
                                 nick,
                                 mu,
                                 sigma,
                                 RANK() OVER (ORDER BY ? * (mu - sigma) DESC) AS `rank`
                             FROM openskill_rating
                             WHERE sigma < ?
                             ORDER BY ? * (mu - sigma) DESC
                             LIMIT ?
                             """
             );
        ) {
            stmt.setDouble(1, OpenSkillRatingSystem.SCALING_FACTOR);
            stmt.setDouble(2, OpenSkillRatingSystem.INITIAL_SIGMA);
            stmt.setDouble(3, OpenSkillRatingSystem.SCALING_FACTOR);
            stmt.setInt(4, n);
            try (ResultSet result = stmt.executeQuery()) {
                List<OpenSkillLeaderboardRankingEntry> rankings = new ArrayList<>();
                while (result.next()) {
                    String nick = result.getString("nick");
                    double mu = result.getDouble("mu");
                    double sigma = result.getDouble("sigma");
                    int rank = result.getInt("rank");
                    int rating = OpenSkillRatingSystem.displayRating(mu, sigma);
                    boolean provisional = OpenSkillRatingSystem.isProvisional(sigma);
                    rankings.add(new OpenSkillLeaderboardRankingEntry(rank, nick, rating, provisional, mu, sigma));
                }
                return rankings.toArray(OpenSkillLeaderboardRankingEntry[]::new);
            }
        } catch (SQLException e) {
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getTopOpenSkillRankingEntries", e);
            return new OpenSkillLeaderboardRankingEntry[0];
        }
    }

    /**
     * Returns the OpenSkill leaderboard entry for a single nick, with the rank computed against
     * the same ranking population and ordering as {@link #getTopOpenSkillRankingEntries}
     * (players who have never played a game are unranked). Returns {@code null} if the nick has
     * no OpenSkill rating row or is unranked.
     */
    @NullMarked
    public static @Nullable OpenSkillLeaderboardRankingEntry getOpenSkillRankingEntry(String nick) {
        try (
             Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                     """
                             WITH t AS (
                                 SELECT
                                     nick,
                                     mu,
                                     sigma,
                                     RANK() OVER (ORDER BY ? * (mu - sigma) DESC) AS `rank`
                                 FROM openskill_rating
                                 WHERE sigma < ?
                             )
                             SELECT mu, sigma, `rank`
                             FROM t
                             WHERE nick = ?
                             """
             );
        ) {
            stmt.setDouble(1, OpenSkillRatingSystem.SCALING_FACTOR);
            stmt.setDouble(2, OpenSkillRatingSystem.INITIAL_SIGMA);
            stmt.setString(3, nick);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    double mu = result.getDouble("mu");
                    double sigma = result.getDouble("sigma");
                    int rank = result.getInt("rank");
                    int rating = OpenSkillRatingSystem.displayRating(mu, sigma);
                    boolean provisional = OpenSkillRatingSystem.isProvisional(sigma);
                    return new OpenSkillLeaderboardRankingEntry(rank, nick, rating, provisional, mu, sigma);
                }
            }
        } catch (SQLException e) {
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getOpenSkillRankingEntry", e);
        }
        return null;
    }

    //endregion

    public static void createGame(Game game, String nick, int sim_version) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO games (time_create, name, rated, speed, size, hills," + " trees, resources, mapcode, status, sim_version) VALUES (?, ?, ?, ?, ?, ?, ?," + " ?, ?, ?, ?)",
                java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, game.getName());
            stmt.setString(3, game.isRated() ? "Y" : "N");
            stmt.setString(4, String.valueOf(game.getGamespeed()));
            stmt.setString(5, String.valueOf(game.getSize() + 1));
            stmt.setInt(6, game.getHills());
            stmt.setInt(7, game.getTrees());
            stmt.setInt(8, game.getSupplies());
            stmt.setString(9, game.getMapcode());
            stmt.setString(10, "created");
            stmt.setInt(11, sim_version);

            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    game.setDatabaseID(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception(createGame): " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "createGame", e);
        }
    }

    public static GameDataModel getGame(int game_id, boolean get_player_data) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT time_create,name,rated,speed,size,hills,trees,resources,mapcode," + "status,id,winner,time_stop,time_start FROM games G WHERE G.id = ?")) {
            stmt.setInt(1, game_id);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    GameDataModel gameData = new GameDataModel();
                    gameData.setTimeCreate(result.getTimestamp("time_create"));
                    gameData.setName(result.getString("name"));
                    gameData.setRated(result.getString("rated"));
                    int speed = result.getInt("speed");
                    if (!result.wasNull()) {
                        gameData.setSpeed(speed);
                    }
                    int size = result.getInt("size");
                    if (!result.wasNull()) {
                        gameData.setSize(size);
                    }
                    int hills = result.getInt("hills");
                    if (!result.wasNull()) {
                        gameData.setHills(hills);
                    }
                    int trees = result.getInt("trees");
                    if (!result.wasNull()) {
                        gameData.setTrees(trees);
                    }
                    int resources = result.getInt("resources");
                    if (!result.wasNull()) {
                        gameData.setResources(resources);
                    }
                    gameData.setMapcode(result.getString("mapcode"));
                    gameData.setStatus(result.getString("status"));
                    int id = result.getInt("id");
                    if (!result.wasNull()) {
                        gameData.setId(id);
                    }
                    int winner = result.getInt("winner");
                    if (!result.wasNull()) {
                        gameData.setWinner(winner);
                    }
                    gameData.setTimeStop(result.getTimestamp("time_stop"));
                    gameData.setTimeStart(result.getTimestamp("time_start"));

                    if (get_player_data) {
                        System.out.println("Fetching player data for game ID: " + game_id);
                        try (Connection conn2 = DBUtils.createDatabaseConnection(); PreparedStatement playerStmt = conn2.prepareStatement(
                                "SELECT * FROM game_players WHERE game_id = ?")) {
                            playerStmt.setInt(1, game_id);
                            try (ResultSet playerResult = playerStmt.executeQuery()) {
                                ArrayList<GamePlayerModel> nicks = new ArrayList<>();
                                while (playerResult.next()) {
                                    GamePlayerModel player = new GamePlayerModel(
                                            playerResult.getString("nick"),
                                            playerResult.getString("race"),
                                            playerResult.getInt("team"));
                                    nicks.add(player);
                                }
                                Profile[] fetchedProfiles = DBInterface.getProfilesByNick(
                                        nicks.stream().map(GamePlayerModel::getPlayerName).toArray(String[]::new));
                                System.out.println(
                                        "Fetched " + fetchedProfiles.length + " profiles for game ID: " + game_id);
                                // Map nick to Profile
                                java.util.Map<String, Profile> nickToProfile = new java.util.HashMap<>();
                                for (Profile pf : fetchedProfiles) {
                                    if (pf != null && pf.getNick() != null) {
                                        nickToProfile.put(pf.getNick(), pf);
                                    }
                                }
                                // Map player names to their profiles
                                for (GamePlayerModel player : nicks) {
                                    Profile profile = nickToProfile.get(player.getPlayerName());
                                    if (profile != null) {
                                        player.setProfile(profile);
                                    }
                                }
                            }
                        }
                    }
                    return gameData;
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getGame", e);
        }
        return null;
    }

    // Helper function to create IN clause with placeholders
    private static String createInClause(int parameterCount) {
        if (parameterCount <= 0) {
            return "()";
        }

        StringBuilder inClause = new StringBuilder("(");
        for (int i = 0; i < parameterCount; i++) {
            if (i > 0) {
                inClause.append(",");
            }
            inClause.append("?");
        }
        inClause.append(")");
        return inClause.toString();
    }

    public static VersusMatchupResultModel getMatchupStats(
            String player1, String player2, boolean only1v1Matchups) {
        String query = "WITH two_player_games AS (   SELECT game_id FROM game_players GROUP BY game_id" + " HAVING COUNT(*) = 2 ) SELECT g.id AS game_id, CASE   WHEN g.winner =" + " gp.team THEN 'Player1'   WHEN g.winner = gp2.team THEN 'Player2'   ELSE" + " 'Neither' END AS vsResult, gp.nick AS player1_name, gp2.nick AS" + " player2_name,  g.name, g.mapcode, g.time_start FROM game_players gp   INNER" + " JOIN game_players gp2 ON gp.game_id = gp2.game_id AND gp.team <> gp2.team  " + " INNER JOIN games g ON g.id = gp.game_id   INNER JOIN two_player_games tpg" + " ON tpg.game_id = g.id WHERE g.winner IS NOT NULL   AND gp.nick = ?   AND" + " gp2.nick = ?   AND gp.team <> gp2.team ORDER BY gp.game_id DESC;";

        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                query)) {
            stmt.setString(1, player1);
            stmt.setString(2, player2);
            int p1Wins = 0;
            int p2Wins = 0;
            int neitherWins = 0;
            ArrayList<VersusMatchupModel> recentMatchups = new ArrayList<>();
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    String vsResult = result.getString("vsResult").trim();
                    if (vsResult.equals("Player1")) {
                        p1Wins++;
                    } else if (vsResult.equals("Player2")) {
                        p2Wins++;
                    } else {
                        neitherWins++;
                    }
                    if (!vsResult.equals("Neither") && recentMatchups.size() < 10) {
                        int gameId = result.getInt("game_id");
                        String player1Name = result.getString("player1_name");
                        String player2Name = result.getString("player2_name");
                        String winnerName = null;
                        if (vsResult.equals("Player1")) {
                            winnerName = player1Name;
                        } else if (vsResult.equals("Player2")) {
                            winnerName = player2Name;
                        }
                        String gameName = result.getString("name");
                        String mapSeed = result.getString("mapcode");
                        java.sql.Timestamp startTime = result.getTimestamp("time_start");
                        recentMatchups.add(
                                new VersusMatchupModel(
                                        player1Name,
                                        player2Name,
                                        winnerName,
                                        gameId,
                                        gameName,
                                        mapSeed,
                                        startTime));
                    }
                }
            }
            System.out.println("p1 wins: " + p1Wins);
            System.out.println("p2 wins: " + p2Wins);
            System.out.println("neither wins: " + neitherWins);

            return new VersusMatchupResultModel(
                    player1, player2, p1Wins, p2Wins, neitherWins, recentMatchups);
        } catch (Exception e) {
            System.out.println("err" + e.getMessage());
        }

        return null;
    }

    public static Profile[] getProfilesByNick(String[] nicks) {
        if (nicks == null || nicks.length == 0) {
            return new Profile[0];
        }

        // Filter out null nicks
        List<String> nonNullNicksList = new ArrayList<>();
        for (String nick : nicks) {
            if (nick != null) {
                nonNullNicksList.add(nick);
            }
        }
        if (nonNullNicksList.isEmpty()) {
            return new Profile[0];
        }
        String[] filteredNicks = nonNullNicksList.toArray(String[]::new);

        String inClause = createInClause(filteredNicks.length);
        String sql = "SELECT nick, rating, wins, losses, invalid FROM profiles WHERE nick IN " + inClause;

        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                sql)) {
            // Set the nicks as parameters
            for (int i = 0; i < filteredNicks.length; i++) {
                stmt.setString(i + 1, filteredNicks[i]);
            }
            try (ResultSet result = stmt.executeQuery()) {
                List<Profile> profiles = new ArrayList<>();
                while (result.next()) {
                    String nick = result.getString("nick").trim();
                    int rating = result.getInt("rating");
                    int wins = result.getInt("wins");
                    int losses = result.getInt("losses");
                    int invalid = result.getInt("invalid");
                    profiles.add(new Profile(nick, rating, wins, losses, invalid, -1));
                }
                return profiles.toArray(Profile[]::new);
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getProfilesByNick", e);
        }
        return null;
    }

    /**
     * Sets status of created games to dropped after a server restart (aka when the matchmaker
     * initializes)
     */
    public static void initDropGames() {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE games G SET status = ? WHERE G.status = ?")) {
            stmt.setString(1, "dropped");
            stmt.setString(2, "created");
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "initDropGames", e);
        }
    }

    public static void dropGame(String nick) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE games G inner join game_players GP on G.id = GP.id SET G.status" + " = ? WHERE GP.nick = ? AND G.status = ?")) {
            stmt.setString(1, "dropped");
            stmt.setString(2, nick);
            stmt.setString(3, "created");
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "dropGame", e);
        }
    }

    public static void startGame(TimestampedGameSession tgs, MatchmakingServer server) {
        GameSession session = tgs.getSession();
        GamePlayer[] playerInfo = session.getPlayerInfo();

        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE games G SET G.time_start = ?, G.status = ? WHERE G.id = ?")) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, "started");
            stmt.setInt(3, tgs.getDatabaseID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception (startGame): " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "startGame", e);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < playerInfo.length; i++) {
            if (i > 0) builder.append(", ");
            builder.append("(?, ?, ?, ?)");
        }
        String sql = "INSERT INTO game_players (game_id, nick, team, race) VALUES " + builder;

        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement playerStmt = conn.prepareStatement(
                sql)) {
            int parameter_index = 1;
            for (GamePlayer player : playerInfo) {
                playerStmt.setInt(parameter_index++, tgs.getDatabaseID());
                playerStmt.setString(parameter_index++, player.getNick());
                playerStmt.setInt(parameter_index++, player.getTeam());
                playerStmt.setInt(parameter_index++, player.getRace());
            }
            playerStmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "startGame", e);
        }
    }

    /**
     * Ends a game session and updates the database.
     *
     * @param tgs      The timestamped game session to end.
     * @param end_time The time at which the game ended.
     * @param winner   The ID of the winning team. When -1 then the player lost against the AI or the
     *                 game state was determined to be invalid and someone cheated. NULL if the game does not
     *                 end naturally (dropped for example)
     */
    public static void endGame(TimestampedGameSession tgs, long end_time, int winner) {
        GameSession session = tgs.getSession();
        Participant[] participants = session.getParticipants();

        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE games G SET G.time_stop = ?, G.status = ?, G.winner = ? WHERE G.id = ?")) {
            stmt.setTimestamp(1, new Timestamp(end_time));
            stmt.setString(2, "completed");
            stmt.setInt(3, winner);
            stmt.setInt(4, tgs.getDatabaseID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "endGame", e);
        }
    }

    public static boolean isProfileRegisteredToDiscord(String nick) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM discord_to_profiles WHERE nick = ?")) {
            stmt.setString(1, nick);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "isProfileRegisteredToDiscord", e);
        }
        return false;
    }

    public static long getDiscordUserIdForProfile(String nick) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT discord_id FROM discord_to_profiles WHERE nick = ?")) {
            stmt.setString(1, nick);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("discord_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getDiscordUserIdForProfile", e);
        }
        return -1L;
    }

    public static String[] getProfilesRegisteredToDiscordUser(long discord_user_id) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT nick FROM discord_to_profiles WHERE discord_id = ?")) {
            stmt.setLong(1, discord_user_id);
            try (ResultSet result = stmt.executeQuery()) {
                List<String> nicks = new ArrayList<>();
                while (result.next()) {
                    String nick = result.getString("nick").trim();
                    nicks.add(nick);
                }
                return nicks.toArray(String[]::new);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getProfilesRegisteredToDiscordUser",
                    e);
        }
        return new String[0];
    }

    public static void registerProfileToDiscordUser(String nick, long discord_user_id) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO discord_to_profiles (nick, discord_id) VALUES (?, ?)")) {
            stmt.setString(1, nick);
            stmt.setLong(2, discord_user_id);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "registerProfileToDiscordUser", e);
        }
    }

    public static void profileOnline(String nick) {
        MatchmakingServer.getLogger().info("profileOnline '" + nick + "'");
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO online_profiles (nick) VALUES (?)")) {
            stmt.setString(1, nick);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "profileOnline", e);
        }
    }

    public static void profileOffline(String nick) {
        MatchmakingServer.getLogger().info("profileOffline '" + nick + "'");
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM online_profiles WHERE nick = ?")) {
            stmt.setString(1, nick);
            int row_count = stmt.executeUpdate();
            assert row_count == 1 : row_count;
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "profileOffline", e);
        }
    }

    public static void profileSetGame(String nick, int game_id) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE online_profiles O SET O.game_id = ? WHERE O.nick = ?")) {
            stmt.setInt(1, game_id);
            stmt.setString(2, nick);
            int row_count = stmt.executeUpdate();
            assert row_count == 1 : row_count + " nick = '" + nick + "' | game_id = " + game_id;
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "profileSetGame", e);
        }
    }

    public static void clearOnlineProfiles() {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "TRUNCATE TABLE online_profiles")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "clearOnlineProfiles", e);
        }
    }

    public static String[] getOnlineProfiles() {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT nick FROM online_profiles ORDER BY nick")) {
            try (ResultSet result = stmt.executeQuery()) {
                List<String> nicks = new ArrayList<>();
                while (result.next()) {
                    String nick = result.getString("nick").trim();
                    nicks.add(nick);
                }
                return nicks.toArray(String[]::new);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getOnlineProfiles", e);
        }
        return new String[0];
    }

    /**
     * Only games an online profile is still in count, since a server restart or abandoned game
     * can leave the games row at started.
     */
    public static List<GameDataModel> getLiveGames() {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT G.id, G.name, G.rated, G.time_start, GP.nick, GP.race, GP.team FROM games G INNER JOIN" + " game_players GP ON GP.game_id = G.id WHERE G.status = ? AND G.id IN (SELECT game_id FROM" + " online_profiles) ORDER BY G.time_start, G.id, GP.team")) {
            stmt.setString(1, "started");
            try (ResultSet result = stmt.executeQuery()) {
                Map<Integer, GameDataModel> games = new LinkedHashMap<>();
                while (result.next()) {
                    int id = result.getInt("id");
                    GameDataModel game = games.get(id);
                    if (game == null) {
                        game = new GameDataModel();
                        game.setId(id);
                        game.setName(result.getString("name"));
                        game.setRated(result.getString("rated"));
                        game.setTimeStart(result.getTimestamp("time_start"));
                        games.put(id, game);
                    }
                    game.getPlayers().add(
                            new GamePlayerModel(
                                    result.getString("nick"), result.getString("race"), result.getInt("team")));
                }
                return new ArrayList<>(games.values());
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getLiveGames", e);
        }
        return new ArrayList<>();
    }

    /**
     * Games played per race, most played first.
     */
    public static Map<Integer, Integer> getRaceGameCounts(String nick) {
        try (Connection conn = DBUtils.createDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(
                // Older games stored the race as N or V instead of its number.
                "SELECT CASE race WHEN 'N' THEN '0' WHEN 'V' THEN '1' ELSE race END AS race_id, COUNT(*) AS games FROM game_players WHERE nick = ? GROUP BY race_id ORDER BY games DESC")) {
            stmt.setString(1, nick);
            try (ResultSet result = stmt.executeQuery()) {
                Map<Integer, Integer> counts = new LinkedHashMap<>();
                while (result.next()) {
                    counts.put(result.getInt("race_id"), result.getInt("games"));
                }
                return counts;
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
            MatchmakingServer.getLogger().throwing(DBInterface.class.getName(), "getRaceGameCounts", e);
        }
        return new LinkedHashMap<>();
    }
}
