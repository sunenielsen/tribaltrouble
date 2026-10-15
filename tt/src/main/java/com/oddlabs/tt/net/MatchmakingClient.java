package com.oddlabs.tt.net;

import com.oddlabs.matchmaking.ChatRoomUser;
import com.oddlabs.matchmaking.Login;
import com.oddlabs.matchmaking.LoginDetails;
import com.oddlabs.matchmaking.MatchmakingClientInterface;
import com.oddlabs.matchmaking.MatchmakingServerInterface;
import com.oddlabs.matchmaking.MatchmakingServerLoginInterface;
import com.oddlabs.matchmaking.Profile;
import com.oddlabs.matchmaking.TunnelAddress;
import com.oddlabs.event.Deterministic;
import com.oddlabs.net.ARMIEvent;
import com.oddlabs.net.ARMIInterfaceMethods;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.net.Connection;
import com.oddlabs.net.ConnectionInterface;
import com.oddlabs.net.HostSequenceID;
import com.oddlabs.net.IllegalARMIEventException;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.net.SecureConnection;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.form.ChatErrorForm;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.form.InfoForm;
import com.oddlabs.tt.gui.ChatRoomInfo;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.steam.SteamManager;
import com.oddlabs.tt.util.Utils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public final class MatchmakingClient implements MatchmakingClientInterface, ConnectionInterface {
    private static final int STATE_NOT_CONNECTED = 1;
    private static final int STATE_AWAITING_OK = 2;
    private static final int STATE_LOGGED_IN = 4;

    private final Map<HostSequenceID, TunnelledConnection> tunnels = new HashMap<>();
    private final ARMIInterfaceMethods interface_methods = new ARMIInterfaceMethods(MatchmakingClientInterface.class);
    private final @NonNull ChatRoomHistory chat_room_history;
    private final @NonNull InGameChatHistory in_game_chat_history;
    private @Nullable GUIRoot chat_gui_root;
    private @Nullable String spectate_nick;
    private @Nullable NetworkSelector network;
    private int current_seq_id = 1;
    private @Nullable SecureConnection conn;
    private @Nullable MatchmakingServerInterface matchmaking_interface;
    private @Nullable MatchmakingServerLoginInterface matchmaking_login_interface;
    private @Nullable TunnelledConnectionListener tunnelled_listener;
    private TunnelAddress local_address;
    private String username = Utils.getBundleString(ResourceBundle.getBundle(MatchmakingClient.class.getName()),
            "player");
    private @Nullable Profile active_profile = null;
    private int state = STATE_NOT_CONNECTED;
    private boolean update_allowed;
    private final Set<@NonNull Integer> update_requested_types = new LinkedHashSet<>();
    private int update_key = 0;
    private @Nullable ProfileListener create_profile_listener;
    private @Nullable ChatRoomInfo chat_room_info;

    private Login login;
    private LoginDetails login_details;
    private boolean steamLogin;

    MatchmakingClient() {
        this.chat_room_history = new ChatRoomHistory();
        this.in_game_chat_history = new InGameChatHistory();
        Network.getChatHub().addListener(chat_room_history);
        Network.getChatHub().addListener(in_game_chat_history);
    }

    public @NonNull List<@NonNull String> getChatRoomHistory() {
        return chat_room_history.getMessages();
    }

    public @NonNull List<@NonNull String> getInGameChatHistory() {
        return in_game_chat_history.getMessages();
    }

    public void clearInGameChatHistory() {
        in_game_chat_history.clear();
    }

    @Override
    public void writeBufferDrained(AbstractConnection conn) {
    }

    @Override
    public void loginOK(String username, TunnelAddress address) {
        if (state == STATE_AWAITING_OK) {
            this.update_allowed = true;
            this.username = username;
            this.local_address = address;
            this.matchmaking_login_interface = null;
            this.matchmaking_interface = (MatchmakingServerInterface) ARMIEvent.createProxy(
                    conn.getWrappedConnectionAndShutdown(), MatchmakingServerInterface.class);
            state = STATE_LOGGED_IN;
            MatchmakingListener listener = Network.getMatchmakingListener();
            listener.loggedIn();
        }
    }

    public void requestProfiles() {
        matchmaking_interface.requestProfiles();

    }

    public void requestList(int type) {
        if (update_allowed) {
            update_allowed = false;
            matchmaking_interface.requestList(type, update_key);
        } else
            update_requested_types.add(type);
    }

    public @Nullable Profile getProfile() {
        return active_profile;
    }

    /*	public final String getNick() {
            if (active_profile == null)
                return username;
            else
                return active_profile.getNick();
        }
    */
    public void setProfile(String nick) {
        matchmaking_interface.setProfile(nick);
    }

    @Override
    public void updateProfile(Profile profile) {
        active_profile = profile;
        MatchmakingListener listener = Network.getMatchmakingListener();
        if (listener != null) {
            listener.profileUpdated();
        }
    }

    public void setCreatingProfileListener(ProfileListener listener) {
        create_profile_listener = listener;
    }

    @Override
    public void createProfileSuccess() {
        if (create_profile_listener != null) {
            create_profile_listener.success();
            create_profile_listener = null;
        }
    }

    @Override
    public void createProfileError(int error_code) {
        if (create_profile_listener != null) {
            create_profile_listener.error(error_code);
            create_profile_listener = null;
        }
    }

    public void createProfile(String nick) {
        matchmaking_interface.createProfile(nick);
    }

    public void deleteProfile(String nick) {
        matchmaking_interface.deleteProfile(nick);
    }

    public void logPriority(String nick, int priority) {
        matchmaking_interface.logPriority(nick, priority);
    }

    @Override
    public void gameWonAck() {
        PeerHub.receivedAck();
    }

    @Override
    public void updateStart(int type) {
        MatchmakingListener listener = Network.getMatchmakingListener();
        if (listener != null) {
            listener.clearList(type);
        }
    }

    @Override
    public void updateList(int type, /*GameHost[]*/Object[] names) {
        MatchmakingListener listener = Network.getMatchmakingListener();
        if (listener != null)
            listener.receivedList(type, names);
    }

    @Override
    public void updateComplete(int next_update_key) {
        this.update_key = next_update_key;
        update_allowed = true;
        if (!update_requested_types.isEmpty()) {
            Iterator<Integer> it = update_requested_types.iterator();
            int type = it.next();
            it.remove();
            requestList(type);
        }
    }

    @Override
    public void updateProfileList(Profile[] profiles, String last_profile_nick) {
        MatchmakingListener listener = Network.getMatchmakingListener();
        if (listener != null)
            listener.receivedProfiles(profiles, last_profile_nick);
    }

    @Override
    public void joiningChatRoom(@NonNull String room_name) {
        assert chat_room_info == null;
        chat_room_info = new ChatRoomInfo(room_name, null);

        chat_room_history.clear();
        SteamManager.setLobbyRichPresence(room_name);
        MatchmakingListener listener = Network.getMatchmakingListener();
        if (listener != null)
            listener.joinedChat(chat_room_info);
    }

    @Override
    public void receiveChatRoomUsers(ChatRoomUser[] users) {
        if (chat_room_info != null) {
            chat_room_info = new ChatRoomInfo(chat_room_info.name(), users);

            MatchmakingListener listener = Network.getMatchmakingListener();
            chat_room_history.update(chat_room_info.users());
            if (listener != null)
                listener.updateChatRoom(chat_room_info);
        }
    }

    @Override
    public void receiveChatRoomMessage(@NonNull String owner, @NonNull String msg) {
        Network.getChatHub().chat(new ChatMessage(owner, msg, ChatMessage.Type.CHATROOM));
    }

    @Override
    public void receivePrivateMessage(@NonNull String nick, @NonNull String msg) {
        Network.getChatHub().chat(new ChatMessage(nick, msg, ChatMessage.Type.PRIVATE));
    }

    public void sendPrivateMessage(GUIRoot gui_root, String nick, String message) {
        this.chat_gui_root = gui_root;
        getInterface().sendPrivateMessage(nick, message);
    }

    public void joinRoom(GUIRoot gui_root, String name) {
        this.chat_gui_root = gui_root;
        getInterface().joinRoom(name);
    }

    public void requestInfo(GUIRoot gui_root, String nick) {
        this.chat_gui_root = gui_root;
        getInterface().requestInfo(nick);
    }

    @Override
    public void receiveInfo(@NonNull Profile profile) {
        if (chat_gui_root != null) {
            chat_gui_root.addModalForm(new InfoForm(profile));
            chat_gui_root = null;
        }
    }

    public void leaveChatRoom() {
        if (isConnected())
            getInterface().leaveRoom();
        chat_room_info = null;
        SteamManager.clearRichPresence();
    }

    public @Nullable ChatRoomInfo getChatRoomInfo() {
        return chat_room_info;
    }

    @Override
    public void error(int error_code) {
        if (chat_gui_root != null) {
            chat_gui_root.addModalForm(new ChatErrorForm(error_code));
            chat_gui_root = null;
        }
    }

    public boolean isConnected() {
        return state == STATE_LOGGED_IN;
    }

    private void open(@NonNull NetworkSelector network) {
        close();
        this.network = network;
        this.conn = new SecureConnection(network.getDeterministic(), new Connection(network,
                Settings.getSettings().getMatchmakingAddress(), MatchmakingServerInterface.MATCHMAKING_SERVER_PORT,
                this), null);
        this.matchmaking_login_interface = (MatchmakingServerLoginInterface) ARMIEvent.createProxy(conn,
                MatchmakingServerLoginInterface.class);
    }

    public void login(@NonNull NetworkSelector network, Login login, LoginDetails login_details) {
        this.login = login;
        this.login_details = login_details;
        this.steamLogin = false;
        open(network);
        state = STATE_AWAITING_OK;
    }

    public void loginWithSteam(@NonNull NetworkSelector network) {
        this.login = null;
        this.login_details = null;
        this.steamLogin = true;
        SteamManager steam = SteamManager.getInstance();
        if (steam != null) {
            steam.requestWebApiTicket();
        }
        open(network);
        state = STATE_AWAITING_OK;
    }

    @Override
    public void loginError(int error_code) {
        close();
        MatchmakingListener listener = Network.getMatchmakingListener();
        listener.loginError(error_code);
    }

    public void requestSpectate(GUIRoot gui_root, String nick) {
        this.chat_gui_root = gui_root;
        this.spectate_nick = nick;
        getInterface().requestSpectate(nick);
    }

    @Override
    public void receiveSpectatorData(byte[] world_params_data) {
        if (chat_gui_root == null || network == null) return;
        try {
            var bais = new java.io.ByteArrayInputStream(world_params_data);
            var ois = new java.io.ObjectInputStream(bais);
            ois.setObjectInputFilter(java.io.ObjectInputFilter.Config.createFilter(
                    "com.oddlabs.**;" + "java.lang.*;java.net.*;" + "!*"));
            var generator = (com.oddlabs.tt.resource.WorldGenerator) ois.readObject();
            var world_params = (com.oddlabs.tt.landscape.WorldParameters) ois.readObject();
            var player_slots = (PlayerSlot[]) ois.readObject();
            var unit_infos = (com.oddlabs.tt.player.UnitInfo[]) ois.readObject();
            float random_start_position = ois.readFloat();
            int session_id = ois.readInt();
            ois.close();
            int followed_slot = 0;
            for (int i = 0; i < player_slots.length; i++) {
                if (player_slots[i].getInfo() != null && player_slots[i].getInfo().getName().equalsIgnoreCase(
                        spectate_nick))
                    followed_slot = i;
            }

            var gui = chat_gui_root.getGUI();
            com.oddlabs.tt.form.ProgressForm.setProgressForm(network, gui,
                    new ReplayWorldStarter(network, session_id, generator, world_params,
                            player_slots, unit_infos, (short) 0,
                            new com.oddlabs.tt.viewer.SpectatorInGameInfo(random_start_position),
                            new SpectatorWorldInitAction(followed_slot)));
            chat_gui_root = null;
        } catch (Exception e) {
            IO.println("Failed to deserialize spectator data: " + e);
            error(MatchmakingClientInterface.CHAT_ERROR_SPECTATE_FAILED);
        }
    }

    private @Nullable ByteArrayOutputStream pendingSpectatorEventLog;
    private int pendingSpectatorEventLogTotal;

    @Override
    public void receiveSpectatorEventLog(byte[] chunk, int chunk_index, int total_chunks, int current_tick) {
        if (chunk_index == 0) {
            pendingSpectatorEventLog = new java.io.ByteArrayOutputStream();
            pendingSpectatorEventLogTotal = total_chunks;
        }
        if (pendingSpectatorEventLog == null) return;  // missed chunk 0; can't reassemble
        pendingSpectatorEventLog.write(chunk, 0, chunk.length);
        if (chunk_index == pendingSpectatorEventLogTotal - 1) {
            byte[] full = pendingSpectatorEventLog.toByteArray();
            pendingSpectatorEventLog = null;
            PeerHubSpectatorController controller = PeerHubSpectatorController.getInstance();
            if (controller == null) return;
            controller.fastForward(full, current_tick);
        }
    }

    public @Nullable MatchmakingServerLoginInterface getLoginInterface() {
        assert !isConnected();
        return matchmaking_login_interface;
    }

    public @Nullable MatchmakingServerInterface getInterface() {
        assert isConnected();
        return matchmaking_interface;
    }

    public TunnelAddress getLocalAddress() {
        return local_address;
    }

    public String getUsername() {
        return username;
    }

    public void registerTunnel(HostSequenceID host_seq, TunnelledConnection conn) {
        Object old = tunnels.put(host_seq, conn);
        assert old == null;
    }

    public @NonNull HostSequenceID registerTunnel(int address, TunnelledConnection conn) {
        int seq_id = current_seq_id++;
        HostSequenceID host_seq = new HostSequenceID(local_address.getHostID(), seq_id);
        matchmaking_interface.openTunnel(address, seq_id);
        registerTunnel(host_seq, conn);
        return host_seq;
    }

    public void unregisterTunnel(HostSequenceID host_seq, TunnelledConnection conn) {
        if (isConnected()) {
            Object old = tunnels.remove(host_seq);
            assert conn == old;
            closeTunnel(host_seq);
        }
    }

    public void closeTunnel(HostSequenceID host_seq) {
        matchmaking_interface.closeTunnel(host_seq);
    }

    public void registerTunnelledListener(TunnelledConnectionListener listener) {
        assert tunnelled_listener == null;
        this.tunnelled_listener = listener;
    }

    public void unregisterTunnelledListener(TunnelledConnectionListener listener) {
        assert tunnelled_listener == listener;
        tunnelled_listener = null;
    }

    private TunnelledConnection removeTunnel(HostSequenceID from) {
        return tunnels.remove(from);
    }

    private TunnelledConnection getTunnel(HostSequenceID from) {
        return tunnels.get(from);
    }

    @Override
    public void tunnelClosed(HostSequenceID from) {
        TunnelledConnection tunnel = removeTunnel(from);
        if (tunnel != null)
            tunnel.tunnelClosed();
    }

    @Override
    public void tunnelAccepted(HostSequenceID from) {
        TunnelledConnection tunnel = getTunnel(from);
        if (tunnel != null)
            tunnel.connected();
        else
            closeTunnel(from);
    }

    @Override
    public void tunnelOpened(@NonNull HostSequenceID from, InetAddress inet_from, InetAddress local_inet_from,
            Profile other) {
        if (tunnelled_listener != null) {
            tunnelled_listener.requestTunnelledConnection(from, inet_from, local_inet_from, other);
        } else
            closeTunnel(from);
    }

    @Override
    public void receiveRoutedEvent(HostSequenceID from, ARMIEvent event) {
        TunnelledConnection tunnel = getTunnel(from);
        if (tunnel != null)
            tunnel.receiveEvent(event);
        else
            closeTunnel(from);
    }

    @Override
    public void handle(Object sender, @NonNull ARMIEvent event) {
        try {
            event.execute(interface_methods, this);
        } catch (IllegalARMIEventException e) {
            handleError(e);
        }
    }

    private void handleError(Exception e) {
        ErrorListener listener = Network.getMatchmakingListener();
        close();
        if (listener != null)
            listener.connectionLost();
    }

    @Override
    public void connected(AbstractConnection connection) {
        Connection wrapped_connection = (Connection) conn.getWrappedConnection();
        matchmaking_login_interface.setLocalRemoteAddress(wrapped_connection.getLocalAddress());
        IO.println("wrapped_connection.getLocalAddress() = " + wrapped_connection.getLocalAddress());
        int revision = com.oddlabs.util.Compatibility.API_VERSION;
        matchmaking_login_interface.setSimVersion(com.oddlabs.util.Compatibility.SIM_VERSION);

        if (steamLogin) {
            Deterministic deterministic = LocalEventQueue.getQueue().getDeterministic();
            SteamManager steam = SteamManager.getInstance();
            if (!SteamManager.isAvailable()) {
                loginError(MatchmakingClientInterface.USER_ERROR_NO_SUCH_USER);
                return;
            }
            if (!deterministic.log(steam != null && steam.awaitWebApiTicket(3000))) {
                handleError(new IOException("Steam Web API ticket not ready — Steam may not be running properly"));
                return;
            }
            long accountId = deterministic.log(steam != null ? steam.getAccountID() : 0L);
            String personaName = deterministic.log(steam != null ? steam.getPersonaName() : null);
            int appId = deterministic.log(steam != null ? steam.getAppID() : 0);
            byte[] authTicket = steam != null ? steam.getWebApiTicket() : null;
            if (!deterministic.log(authTicket != null)) {
                handleError(new IOException("Steam Web API ticket was null after awaiting"));
                return;
            }
            int ticketLength = deterministic.log(authTicket != null ? authTicket.length : 0);
            if (authTicket == null)
                authTicket = new byte[ticketLength];
            matchmaking_login_interface.loginWithSteam(accountId, personaName, authTicket, appId, revision);
        } else if (!Renderer.isRegistered()) {
            matchmaking_login_interface.loginAsGuest(revision);
        } else if (login_details != null) {
            matchmaking_login_interface.createUser(login, login_details, null, revision);
        } else {
            matchmaking_login_interface.login(login, null, revision);
        }
    }

    @Override
    public void error(AbstractConnection conn, IOException e) {
        handleError(e);
    }

    public void close() {
        //if (state == STATE_NOT_CONNECTED)
        //	return;
        if (tunnelled_listener != null) {
            tunnelled_listener.connectionClosed();
            tunnelled_listener = null;
        }
        for (TunnelledConnection tunnel : tunnels.values()) {
            tunnel.tunnelClosed();
        }
        tunnels.clear();
        if (conn != null) {
            conn.close();
            conn = null;
        }

        if (!steamLogin) {
            SteamManager steam = SteamManager.getInstance();
            if (steam != null) {
                steam.cancelAuthTicket();
            }
        }

        state = STATE_NOT_CONNECTED;
        matchmaking_interface = null;
        active_profile = null;
        chat_room_info = null;
        SteamManager.clearRichPresence();
    }
}
