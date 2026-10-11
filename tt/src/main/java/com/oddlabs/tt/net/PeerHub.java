package com.oddlabs.tt.net;

import com.oddlabs.matchmaking.Profile;
import com.oddlabs.net.ARMIEvent;
import com.oddlabs.net.ARMIEventWriter;
import com.oddlabs.net.ARMIInterfaceMethods;
import com.oddlabs.net.IllegalARMIEventException;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.router.GameInterface;
import com.oddlabs.router.Router;
import com.oddlabs.router.SessionID;
import com.oddlabs.router.SessionInfo;
import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.landscape.HeightMap;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInterface;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.viewer.NotificationManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PeerHub implements Animated, RouterHandler {
    private static final Logger logger = Logger.getLogger(PeerHub.class.getName());

    public static final ResourceBundle bundle = ResourceBundle.getBundle(PeerHub.class.getName());

    private static @NonNull String i18n(@NonNull String key, @NonNull Object @NonNull... args) {
        return Utils.getBundleString(bundle, key, args);
    }

    public static final @NonNull String SYSTEM_NAME = i18n("system_name");

    private static final int MILLISECONDS_PER_HEARTBEAT = 60;
    private static final int CLIENT_MAX_DELAY_MILLIS = 60;
    private static final float FREE_QUIT_TIME = 120f;
    private static final int TICKS_PER_STATUS_UPDATE = (int) (20 / AnimationManager.ANIMATION_SECONDS_PER_TICK);
    private static final int TICKS_PER_SPECTATOR_UPDATE = 5;
    private static final int SPECTATOR_KEY_TREES = -20000;
    private static final int MAX_SPECTATOR_INFO_CHARS = 30000;
    private static final char SPECTATOR_CHAT_SEPARATOR = '\u001f';
    // One relayed event reaches the whole session: for a player every player and spectator, for a spectator the
    // other spectators only.
    private final @NonNull PeerHubInterface everyone;
    private static final int TICKS_PER_CHECKSUM = (int) (10 / AnimationManager.ANIMATION_SECONDS_PER_TICK);
    // Spectator controller is non-null only for spectator instances

    private static boolean waiting_for_ack = false;

    private final ARMIInterfaceMethods interface_methods = new ARMIInterfaceMethods(PeerHubInterface.class);
    private final StateChecksum checksum = new StateChecksum();
    private final @NonNull PeerHubInterface peerhubs_interface;
    private final @NonNull PlayerInterface player_interface;
    private final int num_participants;
    private final Peer @NonNull [] peer_index_to_peer;
    private final Map<Player, Peer> player_to_peer = new LinkedHashMap<>();
    private final Map<Peer, Player> peer_to_player = new LinkedHashMap<>();
    private final Set<Player> nonhuman_players = new HashSet<>();
    private final @NonNull NetworkSelector network;
    private final @NonNull RouterClient router_client;
    private final @Nullable Router router;
    private final NotificationManager notification_manager;
    private final @NonNull Player local_player;
    private final @NonNull AnimationManager manager;
    private final boolean is_multiplayer;
    private final boolean is_rated;
    private final boolean is_spectator;
    private final @Nullable PeerHubSpectatorController spectatorController;
    private final StallHandler stall_handler;
    private int local_peer_index;

    private int pause_ticks;
    private int server_millis;
    private int paused;
    private boolean is_synchronized;

    // Web spectator streaming state
    private boolean sentMap;
    private boolean sentInitInfo;
    private boolean sentTrees;
    private boolean spectator_upload_failed;
    private int mapRowsSent;

    public boolean isSynchronized() {
        return is_synchronized;
    }

//private int ignore_peer = -1;

    public PeerHub(@NonNull AnimationManager manager, boolean is_multiplayer, boolean is_rated,
            @NonNull Player local_player, PlayerSlot[] player_slots, @NonNull NetworkSelector network, GUIRoot gui_root,
            NotificationManager notification_manager, DistributableTable distributable_table, SessionID session_id,
            StallHandler stall_handler) {
        this(manager, is_multiplayer, is_rated, false, local_player, player_slots, network, gui_root, notification_manager, distributable_table, session_id, stall_handler);
    }

    public PeerHub(@NonNull AnimationManager manager, boolean is_multiplayer, boolean is_rated, boolean is_spectator,
            @NonNull Player local_player, PlayerSlot[] player_slots, @NonNull NetworkSelector network, GUIRoot gui_root,
            NotificationManager notification_manager, DistributableTable distributable_table, SessionID session_id,
            StallHandler stall_handler) {
        this.stall_handler = stall_handler;
        this.is_rated = is_rated;
        this.is_spectator = is_spectator;
        this.local_player = local_player;
        this.network = network;
        this.notification_manager = notification_manager;
        this.is_multiplayer = is_multiplayer;
        this.manager = manager;

        GameArgumentReader argument_reader = new GameArgumentReader(distributable_table);
        List<Peer> peer_index_to_peer_list = new ArrayList<>();
        Player[] players = local_player.getWorld().getPlayers();
        this.local_peer_index = -1;
        if (!is_multiplayer) {
            this.router = new Router(network, com.oddlabs.util.Utils.getLoopbackAddress(), 0,
                    Logger.getAnonymousLogger(), (IOException e) -> {
                        //					PeerHub.this.routerFailed(e);
                        throw new RuntimeException(e);
                    });
            this.router_client = new RouterClient(network, this, router.getPort());
        } else {
            this.router = null;
            this.router_client = new RouterClient(network, Settings.getSettings().getRouterAddress(), this);
        }
        this.everyone = (PeerHubInterface) ARMIEvent.createProxy(
                (ARMIEvent event) -> router_client.getInterface().relayEvent(event), PeerHubInterface.class);
        for (short i = 0; i < players.length; i++) {
            Player player = players[i];
            if (player_slots[i].getType() != PlayerSlot.HUMAN) {
                nonhuman_players.add(player);
                continue;
            }
            IO.println("index " + i + " contains player " + player);
            final int peer_index = peer_index_to_peer_list.size();
            ARMIEventWriter router_handler = (ARMIEvent event) -> router_client.getInterface().relayEventTo(peer_index,
                    event);
            PeerHubInterface peer_interface = (PeerHubInterface) ARMIEvent.createProxy(router_handler,
                    PeerHubInterface.class);
            Peer peer = new Peer(this, peer_index, player, argument_reader, peer_interface);
            ARMIEventWriter peer_broker;
            if (player == local_player) {
                this.local_peer_index = peer_index;
            }
            peer_index_to_peer_list.add(peer);
            peer_to_player.put(peer, player);
            player_to_peer.put(player, peer);
        }
        this.peer_index_to_peer = new Peer[peer_index_to_peer_list.size()];
        peer_index_to_peer_list.toArray(peer_index_to_peer);
        this.num_participants = peer_index_to_peer.length;
        if (is_spectator) {
            this.player_interface = new NoOpPlayerInterface();
        } else {
            ARMIEventWriter game_router_handler = router_client.getInterface()::relayGameStateEvent;
            this.player_interface = (PlayerInterface) ARMIEvent.createProxy(game_router_handler, new GameArgumentWriter(
                    distributable_table), PlayerInterface.class);
        }
        ARMIEventWriter hub_router_handler = router_client.getInterface()::relayEvent;
        this.peerhubs_interface = (PeerHubInterface) ARMIEvent.createProxy(hub_router_handler, PeerHubInterface.class);
        this.spectatorController = is_spectator ? new PeerHubSpectatorController(this) : null;
        manager.registerAnimation(this);
        if (is_spectator) {
            router_client.connectSpectator(session_id);
        } else {
            router_client.connect(session_id, new SessionInfo(num_participants, MILLISECONDS_PER_HEARTBEAT),
                    local_peer_index);
        }
    }

    @Override
    public void routerFailed(Exception e) {
        logger.log(Level.WARNING, "Router failed", e);
        closeNetwork();
        stall_handler.peerhubFailed();
    }

    @Override
    public void heartbeat(int millis) {
        if (millis < server_millis) {
            routerFailed(new IOException(
                    "Invalid time received: " + millis + " (tick currently at " + getTick() + ")"));
            return;
        }
        server_millis = millis;
    }

    @Override
    public void receiveEvent(int client_id, @NonNull ARMIEvent event) {
        if (client_id == GameInterface.SPECTATOR_CLIENT_ID) {
            if (is_spectator) {
                try {
                    event.execute(interface_methods, spectator_chat);
                } catch (IllegalARMIEventException e) {
                    IO.println("Ignoring bad spectator chat event: " + e.getMessage());
                }
            }
            return;
        }
        Peer peer = getPeerFromClientID(client_id);
        if (peer == null) {
            routerFailed(new IOException("Invalid client_id received: " + client_id));
            return;
        }
        try {
            event.execute(interface_methods, peer);
        } catch (IllegalARMIEventException e) {
            peerDisconnected(peer, e.getMessage());
        }
    }

    @Override
    public void receiveGameStateEvent(int client_id, int millis, ARMIEvent event) {
        if (millis < server_millis) {
            routerFailed(new IOException(
                    "Invalid time received for event: " + millis + " (tick currently at " + getTick() + ")"));
            return;
        }
        Peer peer = getPeerFromClientID(client_id);
        if (peer == null) {
            routerFailed(new IOException("Invalid client_id received: " + client_id));
            return;
        }
        server_millis = millis;
        int event_tick = millisToTickCeil(millis);
        if (spectatorController != null && spectatorController.holdLiveEvent(peer, event_tick, event))
            return;
        peer.addEvent(event_tick, event);
        if (!is_spectator && isFirstActivePeer() && Network.getMatchmakingClient().isConnected()) {
            PeerHubSpectatorController.sendCommandEvent(event_tick, client_id, event);
        }
    }

    private boolean isFirstActivePeer() {
        for (int i = 0; i < peer_index_to_peer.length; i++) {
            if (peer_index_to_peer[i] != null) return i == local_peer_index;
        }
        return false;
    }

    @Override
    public void playerDisconnected(int client_id, boolean checksum_error) {
        Peer peer = getPeerFromClientID(client_id);
        if (checksum_error)
            peerChecksumError(peer);
        else
            peerDisconnected(peer, "Peer disconnected");
    }

    private void peerChecksumError(@NonNull Peer peer) {
        IO.println("Disconnecting peer because of checksum mismatch: " + peer.getPlayerInfo().getName());
        peerDisconnected(peer, "Checksum error");
        Globals.checksum_error_in_last_game = true;
    }

    @Nullable
    Peer getPeerFromClientID(int client_id) {
        if (client_id >= 0 && client_id < peer_index_to_peer.length)
            return unsafeGetPeerFromClientID(client_id);
        else
            return null;
    }

    private Peer unsafeGetPeerFromClientID(int client_id) {
        return peer_index_to_peer[client_id];
    }

    @Override
    public void start() {
        if (spectatorController != null) {
            spectatorController.onStart();
        } else {
            is_synchronized = true;
        }
    }

    private Peer locatePeerFromPlayer(Player player) {
        return player_to_peer.get(player);
    }

    private boolean isDisconnected(Peer peer) {
        return getPlayerFromPeer(peer) == null;
    }

    private Player getPlayerFromPeer(Peer peer) {
        return peer_to_player.get(peer);
    }

    public boolean isAlive(@NonNull Player player) {
        return (nonhuman_players.contains(player) || locatePeerFromPlayer(player) != null) && player.isAlive();
    }

    public @NonNull PlayerInterface getPlayerInterface() {
        return player_interface;
    }

    private int millisToTickCeil(int millis) {
        return millisToTick(millis + (int) AnimationManager.ANIMATION_MILLISECONDS_PER_TICK - 1);
    }

    private int millisToTick(int millis) {
        return (int) (millis / AnimationManager.ANIMATION_MILLISECONDS_PER_TICK) - pause_ticks;
    }

    @Override
    public void animate(float t) {
        if (router != null)
            router.process();

        if (spectatorController != null && spectatorController.animateCatchUp(t))
            return;

        int server_tick = millisToTick(server_millis);
        if (!is_synchronized || getTick() == server_tick) {
            processStall();
        } else {
            if (!isPaused()) {
                doTick(t);
                int min_tick = millisToTick(server_millis - MILLISECONDS_PER_HEARTBEAT - CLIENT_MAX_DELAY_MILLIS);
                while (getTick() < min_tick)
                    doTick(t);
            } else
                pause_ticks++;
        }
    }

    int getTick() {
        return local_player.getWorld().getTick();
    }

    void setSynchronized(boolean value) {
        this.is_synchronized = value;
    }

    /**
     * Package-private entry point for SpectatorController catch-up ticks.
     */
    void doTickInternal(float t) {
        doTick(t);
    }

    private void doTick(float t) {
        stall_handler.stopStall();

        if (!is_spectator) {
            if (getFreeQuitTicksLeft(local_player.getWorld()) == 0 && Network.getMatchmakingClient().isConnected())
                Network.getMatchmakingClient().getInterface().freeQuitStopNotify();

            if (getTick() % TICKS_PER_STATUS_UPDATE == 0 && Network.getMatchmakingClient().isConnected())
                sendStatusUpdate();

            if (is_multiplayer && !spectator_upload_failed && Network.getMatchmakingClient().isConnected()) {
                // The web spectator feed is not worth the game: on any failure stop feeding it and play on.
                try {
                    if (!sentMap) sendMap();
                    if (!sentInitInfo) sendInitInfo();
                    if (!sentTrees) sendTrees();
                    if (getTick() % TICKS_PER_SPECTATOR_UPDATE == 0) sendSpectatorInfo();
                } catch (RuntimeException e) {
                    spectator_upload_failed = true;
                    IO.println("Web spectator feed stopped: " + e);
                }
            }
        }

        for (Peer peer : peer_index_to_peer) {
            if (peer != null) {
                try {
                    peer.executeEvents(getTick());
                } catch (IllegalARMIEventException e) {
                    peerDisconnected(peer, e.getMessage());
                }
            }
        }
        local_player.getWorld().tick(t);
        if (!is_spectator && getTick() % TICKS_PER_CHECKSUM == 0)
            sendChecksum();
    }

    private void sendChecksum() {
        checksum.update(getTick());
        checksum.update(local_player.getWorld().getChecksum());
        local_player.getWorld().getAnimationManagerGameTime().updateChecksum(checksum);
        local_player.getWorld().getAnimationManagerRealTime().updateChecksum(checksum);
        router_client.getInterface().checksum(checksum.getValue());
    }

    private void sendMap() {
        HeightMap map = local_player.getWorld().getHeightMap();
        int size = map.getGridUnitsPerWorld();
        if (mapRowsSent < size) {
            StringBuilder info = new StringBuilder("M ").append(mapRowsSent).append(' ');
            for (int x = 0; x < size; x++) {
                info.append(map.getHeight(x, mapRowsSent)).append(' ');
            }
            info.append('\n');
            mapRowsSent++;
            Network.getMatchmakingClient().getInterface().updateSpectatorInfo(-mapRowsSent, info.toString());
        } else {
            sentMap = true;
        }
    }

    private void sendInitInfo() {
        Player[] players = local_player.getWorld().getPlayers();
        StringBuilder info = new StringBuilder("I ");
        for (Player player : players) {
            var color = player.getColor();
            String name = player.getPlayerInfo().getName();
            int race = player.getPlayerInfo().getRace();
            int team = player.getPlayerInfo().getTeam();
            info.append("NAME ").append(name).append(' ');
            info.append("RACE ").append(race).append(' ');
            info.append("TEAM ").append(team).append(' ');
            info.append("COLOR ").append(color.x()).append(' ').append(color.y()).append(' ').append(color.z()).append(
                    ' ');
        }
        info.append('\n');
        Network.getMatchmakingClient().getInterface().updateSpectatorInfo(-10001, info.toString());
        sentInitInfo = true;
    }

    private void sendTrees() {
        List<int[]> trees = local_player.getWorld().getTreePositions();
        StringBuilder info = new StringBuilder("T ");
        for (int[] pos : trees) {
            info.append(pos[0]).append(' ').append(pos[1]).append(' ');
        }
        info.append('\n');
        sendSpectatorChunks(SPECTATOR_KEY_TREES, -1, info.toString());
        sentTrees = true;
    }

    private void sendSpectatorInfo() {
        int tick = getTick();
        StringBuilder info = new StringBuilder().append(tick).append(' ');
        Player[] players = local_player.getWorld().getPlayers();
        for (int i = 0; i < players.length; i++) {
            info.append("P ").append(i).append(' ');
            for (var s : players[i].getUnits().getSet()) {
                if (s instanceof Unit u) {
                    info.append("U ").append(u.getGridX()).append(' ').append(u.getGridY()).append(' ');
                } else if (s instanceof Building b) {
                    info.append("B ").append(b.getGridX()).append(' ').append(b.getGridY()).append(' ').append(
                            b.getTemplate().getPlacingSize()).append(' ').append(b.getHitPoints()).append(' ');
                }
            }
        }
        info.append('\n');
        sendSpectatorChunks(tick, 1, info.toString());
    }

    private void sendSpectatorChunks(int first_key, int key_step, @NonNull String info) {
        var server = Network.getMatchmakingClient().getInterface();
        int key = first_key;
        for (int start = 0; start < info.length(); start += MAX_SPECTATOR_INFO_CHARS) {
            server.updateSpectatorInfo(key, info.substring(start, Math.min(info.length(),
                    start + MAX_SPECTATOR_INFO_CHARS)));
            key += key_step;
        }
    }

    public void setPaused(boolean p) {
        if (!is_multiplayer && p)
            paused++;
        else if (!is_multiplayer && !p)
            paused--;
        // FIXME er ikke et problem naar hver gui_root har sin egen World
        //      assert world_singleton.paused >= 0;
    }

    public boolean isPaused() {
        return paused > 0;
    }

    private void sendStatusUpdate() {
        int[] status = new int[num_participants];
        for (int i = 0; i < status.length; i++) {
            Peer peer = getPeerFromClientID(i);
            if (peer != null)
                status[i] = peer.getPlayer().getStatus();
        }
        Network.getMatchmakingClient().getInterface().updateGameStatus(getTick(), status);
    }

    private @NonNull Iterator<Peer> getPeerIterator() {
        return peer_to_player.keySet().iterator();
    }

    public @NonNull PeerHubInterface getInterface() {
        return peerhubs_interface;
    }

    private void processStall() {
        stall_handler.processStall(getTick());
    }

    private void removePeerFromActiveList(@NonNull Peer peer) {
        IO.println("Removing from active list:" + peer);
        peer_index_to_peer[peer.getPeerIndex()] = null;
        peer.getPlayer().setPreferredGamespeed(World.GAMESPEED_DONTCARE);
    }

    @Override
    public void updateChecksum(@NonNull StateChecksum sum) {
        sum.update(getTick());
        sum.update(checksum.getValue());
    }

    public void peerDisconnected(@NonNull Peer peer, String reason) {
        Player player = getPlayerFromPeer(peer);
        if (player == null)
            return;
        peer_to_player.remove(peer);
        player_to_peer.remove(player);
        int peer_index = peer.getPeerIndex();
        removePeerFromActiveList(peer);
        player.getWorld().getNotificationListener().playerLeft(player);
        String left_game_message = i18n("left_game", peer.getPlayerInfo().getName(), reason);
        receiveChat(SYSTEM_NAME, left_game_message, false);
        if (getFreeQuitTicksLeft(local_player.getWorld()) >= 0 && Network.getMatchmakingClient().isConnected())
            Network.getMatchmakingClient().getInterface().gameQuitNotify(peer.getPlayerInfo().getName());
    }

    public void sendChat(String text, boolean team_only) {
        if (is_spectator) {
            // Spectators talk only to each other: the router relays this to the session's other spectators.
            String nick = spectatorNick();
            Network.getChatHub().chat(new ChatMessage(nick, text, ChatMessage.Type.SPECTATOR_CHAT));
            everyone.chat(nick + SPECTATOR_CHAT_SEPARATOR + text, false);
            return;
        }
        if (!team_only) {
            everyone.chat(text, false);
            return;
        }
        Iterator<Peer> it = getPeerIterator();
        int local_team = local_player.getPlayerInfo().getTeam();
        while (it.hasNext()) {
            Peer peer = it.next();
            int peer_team = peer.getPlayerInfo().getTeam();
            if (!team_only || local_team == peer_team)
                peer.getPeerHubInterface().chat(text, team_only);
        }
    }

    public void sendBeacon(float x, float y) {
        player_interface.viewBeacon(x, y);
        Iterator<Peer> it = getPeerIterator();
        int local_team = local_player.getPlayerInfo().getTeam();
        while (it.hasNext()) {
            Peer peer = it.next();
            int peer_team = peer.getPlayerInfo().getTeam();
            if (local_team == peer_team)
                peer.getPeerHubInterface().beacon(x, y);
        }
    }

    private static @NonNull String spectatorNick() {
        Profile profile = Network.getMatchmakingClient().getProfile();
        return profile != null ? profile.getNick() : "Spectator";
    }

    // Chat relayed from another spectator; the sender's nick travels in the text since spectators have no slot.
    private final @NonNull PeerHubInterface spectator_chat = new PeerHubInterface() {
        @Override
        public void chat(String text, boolean team) {
            int split = text.indexOf(SPECTATOR_CHAT_SEPARATOR);
            String nick = split > 0 ? text.substring(0, split) : "Spectator";
            String message = split > 0 ? text.substring(split + 1) : text;
            Network.getChatHub().chat(new ChatMessage(nick, message, ChatMessage.Type.SPECTATOR_CHAT));
        }

        @Override
        public void beacon(float x, float y) {
        }
    };

    public void receiveChat(@NonNull String name, @NonNull String text, boolean team) {
        if (team)
            Network.getChatHub().chat(new ChatMessage(name, text, ChatMessage.Type.TEAM));
        else
            Network.getChatHub().chat(new ChatMessage(name, text, ChatMessage.Type.NORMAL));
    }

    public void receiveBeacon(float x, float y, @NonNull String owner) {
        if (!ChatCommand.isIgnoring(owner))
            notification_manager.newBeacon(manager, local_player, x, y);
    }

    private void closeNetwork() {
        router_client.close();
        if (router != null)
            router.close();
        Object[] peers = peer_to_player.keySet().toArray();
        for (Object peer1 : peers) {
            Peer peer = (Peer) peer1;
            if (peer.getPlayer() != local_player) {
                peerDisconnected(peer, "Synchronization failed");
            }
        }
        stall_handler.stopStall();
        leaveGame();
    }

    public void close() {
        if (spectatorController != null) PeerHubSpectatorController.clearInstance(spectatorController);
        closeNetwork();
        LocalEventQueue.getQueue().getManager().removeAnimation(this);
        IO.println("PeerHub closed");
    }

    private static int getFreeQuitTicksLeft(@NonNull World world) {
        return (int) (FREE_QUIT_TIME / world.getSecondsPerTick()) - world.getTick();
    }

    public static float getFreeQuitTimeLeft(@NonNull World world) {
        int left = getFreeQuitTicksLeft(world);
        return left * AnimationManager.ANIMATION_SECONDS_PER_TICK;
    }

    public void leaveGame() {
        if (Network.getMatchmakingClient().isConnected()) {
            if (getFreeQuitTicksLeft(local_player.getWorld()) >= 0) {
                Network.getMatchmakingClient().getInterface().gameQuitNotify(local_player.getPlayerInfo().getName());
            } else {
                Network.getMatchmakingClient().getInterface().gameLostNotify();
            }
        }
    }

    public void gameWon() {
        if (Network.getMatchmakingClient().isConnected()) {
            Network.getMatchmakingClient().getInterface().gameWonNotify();
            waiting_for_ack = true;
        }
    }

    public static void receivedAck() {
        waiting_for_ack = false;
    }

    public static boolean isWaitingForAck() {
        return waiting_for_ack;
    }
}
