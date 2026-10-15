package com.oddlabs.tt.steam;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAuth;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamLeaderboardEntriesHandle;
import com.codedisaster.steamworks.SteamLeaderboardHandle;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;
import com.codedisaster.steamworks.SteamUserStats;
import com.codedisaster.steamworks.SteamUserStatsCallback;
import com.codedisaster.steamworks.SteamUtils;
import com.codedisaster.steamworks.SteamUtilsCallback;
import com.oddlabs.tt.event.LocalEventQueue;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SteamManager implements SteamUserCallback, SteamFriendsCallback, SteamUserStatsCallback, SteamUtilsCallback {
    private static final Logger logger = Logger.getLogger(SteamManager.class.getName());
    private static final Duration TICKET_MAX_AGE = Duration.ofHours(1);
    private static final String WEB_API_IDENTITY = "tribaltrouble.org";

    private static @Nullable SteamManager instance;
    private static volatile boolean inActiveWorld = false;

    private final SteamUser steamUser;
    private final SteamFriends steamFriends;
    private final SteamUserStats steamUserStats;
    private final SteamUtils steamUtils;

    private @Nullable SteamAuthTicket currentAuthTicket;
    private byte @Nullable [] cachedTicketData;
    private boolean ticketReady;
    private @Nullable Instant ticketTimestamp;

    private SteamManager() {
        steamUser = new SteamUser(this);
        steamFriends = new SteamFriends(this);
        steamUserStats = new SteamUserStats(this);
        steamUtils = new SteamUtils(this);
    }

    public static @Nullable SteamManager getInstance() {
        return instance;
    }

    public static boolean isAvailable() {
        return LocalEventQueue.getQueue().getDeterministic().log(instance != null);
    }

    public static boolean init() {
        try {
            SteamAPI.loadLibraries(new SteamLibraryLoaderLwjgl3());
            if (!SteamAPI.init()) {
                logger.warning("Steam API init returned false — is the Steam client running?");
                return false;
            }
            instance = new SteamManager();
            logger.info("Steam API initialized (appId running via Steam overlay)");
            return true;
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Failed to initialize Steam API", t);
            return false;
        }
    }

    public static void setLobbyRichPresence(String roomName) {
        if (instance == null) return;
        instance.steamFriends.setRichPresence("steam_display", "#Status_InLobby");
    }

    /**
     * Sets rich presence for an active multiplayer game. Clears the {@code connect}
     * key so the friend list shows no "Join Game" button — joining a started match
     * isn't supported (spectate would be a future addition).
     */
    public static void setMultiplayerInGameRichPresence() {
        if (instance == null) return;
        instance.steamFriends.clearRichPresence();
        instance.steamFriends.setRichPresence("steam_display", "#Status_InGame");
    }

    public static void clearRichPresence() {
        if (instance == null) return;
        instance.steamFriends.clearRichPresence();
    }

    /**
     * Marks the local user as being inside an active world (multiplayer game,
     * skirmish, or campaign mission). While set, an inbound Steam join is
     * dropped — yanking someone out of an active match would lose progress
     * and drop teammates. The friend can re-invite once the match ends.
     */
    public static void setInActiveWorld(boolean active) {
        inActiveWorld = active;
    }

    /**
     * Sets rich presence for a single-player campaign mission. Intentionally clears
     * any previous keys first so friends don't see leftover lobby/in-game state.
     *
     * @param race "Vikings" or "Natives" — used to pick a localization token
     *             {@code #Status_CampaignVikings} / {@code #Status_CampaignNatives}.
     */
    public static void setCampaignRichPresence(String race) {
        if (instance == null) return;
        instance.steamFriends.clearRichPresence();
        instance.steamFriends.setRichPresence("steam_display", "#Status_Campaign" + race);
        instance.steamFriends.setRichPresence("race", race);
    }

    public static void unlockAchievement(String achievementId) {
        if (instance == null) return;
        if (instance.steamUserStats.isAchieved(achievementId, false)) return;

        if (instance.steamUserStats.setAchievement(achievementId)) {
            logger.info("Achievement unlocked: " + achievementId);
            instance.steamUserStats.storeStats();
        } else {
            logger.warning("Failed to unlock achievement: " + achievementId);
        }
    }

    public static void detach() {
        if (instance != null) {
            instance.cancelAuthTicket();
            instance.steamUserStats.dispose();
            instance.steamUser.dispose();
            instance.steamFriends.dispose();
            instance.steamUtils.dispose();
            instance = null;
        }
    }

    public static void shutdown() {
        detach();
        SteamAPI.shutdown();
    }

    public static void runCallbacks() {
        if (instance != null) {
            SteamAPI.runCallbacks();
        }
    }

    public long getAccountID() {
        return steamUser.getSteamID().getAccountID();
    }

    public int getAppID() {
        return steamUtils.getAppID();
    }

    public String getPersonaName() {
        return steamFriends.getPersonaName();
    }

    /**
     * Request a Web API auth ticket asynchronously. The ticket arrives via
     * {@link #onGetTicketForWebApi}. Reuses a cached ticket if it's less than 1 hour old.
     */
    public void requestWebApiTicket() {
        if (ticketReady && ticketTimestamp != null && Duration.between(ticketTimestamp, Instant.now()).compareTo(
                TICKET_MAX_AGE) < 0) {
            logger.info("Web API ticket still fresh, reusing");
            return;
        }

        if (ticketReady && ticketTimestamp != null) {
            logger.info("Web API ticket expired, requesting fresh ticket");
            cancelAuthTicket();
        }

        if (currentAuthTicket != null && !ticketReady) {
            logger.info("Web API ticket request already pending");
            return;
        }

        ticketReady = false;
        cachedTicketData = null;
        currentAuthTicket = null;

        logger.info("Requesting Steam Web API ticket...");
        currentAuthTicket = steamUser.getAuthTicketForWebApi(WEB_API_IDENTITY);
        logger.info("getAuthTicketForWebApi returned: " + (currentAuthTicket != null ? "ticket created" : "null"));
    }

    public byte @Nullable [] getWebApiTicket() {
        return ticketReady ? cachedTicketData : null;
    }

    public boolean isWebApiTicketReady() {
        return ticketReady;
    }

    /**
     * Wait up to {@code maxWaitMs} for the Web API ticket callback to fire,
     * pumping Steam callbacks while waiting.
     *
     * @return true if the ticket is ready
     */
    public boolean awaitWebApiTicket(int maxWaitMs) {
        int elapsed = 0;
        int pollInterval = 50;
        while (!ticketReady && elapsed < maxWaitMs) {
            if (SteamAPI.isSteamRunning()) {
                SteamAPI.runCallbacks();
            }
            try {
                Thread.sleep(pollInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            elapsed += pollInterval;
        }
        if (elapsed > 0 && ticketReady) {
            logger.info("Web API ticket ready after " + elapsed + "ms");
        }
        return ticketReady;
    }

    public void cancelAuthTicket() {
        if (currentAuthTicket != null) {
            steamUser.cancelAuthTicket(currentAuthTicket);
            currentAuthTicket = null;
        }
        cachedTicketData = null;
        ticketReady = false;
        ticketTimestamp = null;
    }

    // SteamUserCallback
    @Override
    public void onGetTicketForWebApi(SteamAuthTicket authTicket, SteamResult result, byte[] ticketData) {
        logger.info("Web API ticket callback — result: " + result);
        if (result == SteamResult.OK && ticketData != null && ticketData.length > 0) {
            cachedTicketData = ticketData.clone();
            ticketReady = true;
            ticketTimestamp = Instant.now();
            logger.info("Web API ticket ready: " + ticketData.length + " bytes");
        } else {
            cachedTicketData = null;
            ticketReady = false;
            ticketTimestamp = null;
            logger.warning("Failed to get Web API ticket: " + result);
        }
    }

    @Override
    public void onValidateAuthTicket(SteamID steamID, SteamAuth.AuthSessionResponse authSessionResponse,
            SteamID ownerSteamID) {
    }

    @Override
    public void onMicroTxnAuthorization(int appID, long orderID, boolean authorized) {
    }

    // SteamFriendsCallback
    @Override
    public void onPersonaStateChange(SteamID steamID, SteamFriends.PersonaChange change) {
    }

    @Override
    public void onGameLobbyJoinRequested(SteamID steamIDLobby, SteamID steamIDFriend) {
    }

    @Override
    public void onAvatarImageLoaded(SteamID steamID, int image, int width, int height) {
    }

    @Override
    public void onFriendRichPresenceUpdate(SteamID steamIDFriend, int appID) {
    }

    @Override
    public void onGameRichPresenceJoinRequested(SteamID steamIDFriend, String connect) {
    }

    @Override
    public void onGameServerChangeRequested(String server, String password) {
    }

    // SteamUserStatsCallback
    @Override
    public void onUserStatsReceived(long gameId, SteamID steamIDUser, SteamResult result) {
    }

    @Override
    public void onUserStatsStored(long gameId, SteamResult result) {
    }

    @Override
    public void onUserStatsUnloaded(SteamID steamIDUser) {
    }

    @Override
    public void onUserAchievementStored(long gameId, boolean groupAchievement, String achievementName, int curProgress,
            int maxProgress) {
    }

    @Override
    public void onLeaderboardFindResult(SteamLeaderboardHandle leaderboard, boolean found) {
    }

    @Override
    public void onLeaderboardScoresDownloaded(SteamLeaderboardHandle leaderboard, SteamLeaderboardEntriesHandle entries,
            int numEntries) {
    }

    @Override
    public void onLeaderboardScoreUploaded(boolean success, SteamLeaderboardHandle leaderboard, int score,
            boolean scoreChanged, int globalRankNew, int globalRankPrevious) {
    }

    @Override
    public void onNumberOfCurrentPlayersReceived(boolean success, int players) {
    }

    @Override
    public void onGlobalStatsReceived(long gameId, SteamResult result) {
    }

    // SteamUtilsCallback
    @Override
    public void onSteamShutdown() {
    }
}
