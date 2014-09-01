package com.oddlabs.matchserver;

import com.oddlabs.util.KeyManager;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.net.ARMIEvent;
import com.oddlabs.net.HostSequenceID;
import com.oddlabs.net.ConnectionInterface;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.net.IllegalARMIEventException;
import com.oddlabs.net.ARMIInterfaceMethods;
import com.oddlabs.matchmaking.MatchmakingServerInterface;
import com.oddlabs.matchmaking.MatchmakingServerLoginInterface;
import com.oddlabs.matchmaking.MatchmakingClientInterface;
import com.oddlabs.matchmaking.TunnelAddress;
import com.oddlabs.matchmaking.GameHost;
import com.oddlabs.matchmaking.RankingEntry;
import com.oddlabs.matchmaking.Game;
import com.oddlabs.matchmaking.ChatRoomEntry;
import com.oddlabs.matchmaking.Participant;
import com.oddlabs.matchmaking.GameSession;
import com.oddlabs.matchmaking.Profile;
import com.oddlabs.matchmaking.Login;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.net.InetAddress;

public final strictfp class Client implements MatchmakingServerInterface, ConnectionInterface {
	private final static int CHUNK_SIZE = 10;
	private final static Set game_hosts = new HashSet();
	private final static Map active_clients = new HashMap();

	private static int current_random_seed = 1;

	private final ARMIInterfaceMethods interface_methods = new ARMIInterfaceMethods(MatchmakingServerInterface.class);
	private final MatchmakingServer server;
	private final MatchmakingClientInterface client_interface;
	
	private final AbstractConnection conn;
	private final Map tunnels = new HashMap();
	private final InetAddress remote_address;
	private final InetAddress local_remote_address;
	private final int host_id;
	private HostSequenceID[] multicast_addresses;
	
	private final Random random = new Random(current_random_seed++);
	private int update_key = 0;
	
	private final int revision;
	private final String username;
	private final boolean guest;
	private Profile active_profile;

	private Game current_game;
	private TimestampedGameSession current_session;
	private ChatRoom current_room;

	public Client(MatchmakingServer server, AbstractConnection conn, InetAddress remote_address, InetAddress local_remote_address, String username, boolean guest, int revision, int host_id) {
		this.conn = conn;
		this.server = server;
		this.remote_address = remote_address;
		this.local_remote_address = local_remote_address;
		this.client_interface = (MatchmakingClientInterface)ARMIEvent.createProxy(conn, MatchmakingClientInterface.class);
		this.username = username;
		this.guest = guest;
		this.revision = revision;
		this.host_id = host_id;
		conn.setConnectionInterface(this);
	}

	public final void writeBufferDrained(AbstractConnection conn) {
	}

	public final void requestProfiles() {
		if (!guest)
			client_interface.updateProfileList(DBInterface.getProfiles(username, revision), DBInterface.getLastUsedProfile(username));
	}

	public final void setProfile(String nick) {
		closeProfile();
		if (!guest) {
			if (nick != null) 
				updateProfile(nick);
		} else {
			updateProfile(new Profile(username, 0, 0, 0, 0, revision));
		}
		if (active_profile != null) {
			active_clients.put(active_profile.getNick().toLowerCase(), this);
			DBInterface.profileOnline(active_profile.getNick());
			ChatRoom.joinStandardChatRoom(this);
		}
	}

	public final void createProfile(String nick) {
		if (!guest) {
			Profile[] profiles = DBInterface.getProfiles(username, revision);
			if (profiles.length >= DBInterface.getSettingsInt("max_profiles")) {
				client_interface.createProfileError(MatchmakingClientInterface.USERNAME_ERROR_TOO_MANY);
				return;
			}

			try {
				Authenticator.checkUsername(nick);
			} catch (InvalidUsernameException e) {
				client_interface.createProfileError(e.getErrorCode());
				return;
			}

			if (nick.toLowerCase().startsWith("guest")) {
				client_interface.createProfileError(MatchmakingClientInterface.PROFILE_ERROR_GUEST);
				return;
			}

			if (DBInterface.nickExists(nick)) {
				client_interface.createProfileError(MatchmakingClientInterface.USERNAME_ERROR_ALREADY_EXISTS);
				return;
			}

			DBInterface.createProfile(username, nick);
			client_interface.createProfileSuccess();
		}
	}

	public final void logPriority(String other_nick, int priority) {
		if (current_session != null && active_profile != null && active_clients.get(other_nick.toLowerCase()) != null) {
			DBInterface.logPriority(current_session.getDatabaseID(), active_profile.getNick(), other_nick, priority);
		}
	}

	public final void deleteProfile(String nick) {
		if (!guest) {
			DBInterface.deleteProfile(username, nick);
		}
	}

	public final void updateProfile() {
		if (!guest) {
			if (active_profile != null)
				updateProfile(active_profile.getNick());
		}
	}

	private final void updateProfile(String nick) {
		if (!guest) {
			Profile profile = DBInterface.getProfile(username, nick, revision);
			if (profile != null) {
				updateProfile(profile);
				DBInterface.setLastUsedProfile(username, nick);
			}
		}
	}
	
	private final void updateProfile(Profile profile) {
		active_profile = profile;
		client_interface.updateProfile(active_profile);
	}
	
	public final Profile getProfile() {
		return active_profile;
	}

	public final void freeQuitStopNotify() {
		if (getGameSession() == null)
			return;
		getGameSession().freeQuitStop();
	}

	public final void updateGameStatus(int tick, int[] status) {
		if (getGameSession() == null || status == null || tick < 0)
			return;
		getGameSession().updateGameStatus(tick, status);
	}

	public final void gameQuitNotify(String nick) {
		if (getGameSession() == null)
			return;

		Client client = (Client)active_clients.get(nick.toLowerCase());
		if (client == null)
			return;

		if (client == this) {
			getGameSession().gameQuit(server, this);
			setGameSession(null);
		} else
			getGameSession().participantQuit(server, client);
	}

	public final void gameLostNotify() {
		if (getGameSession() == null)
			return;
		getGameSession().gameLost(server, this);
		setGameSession(null);
	}

	public final void gameWonNotify() {
		if (getGameSession() == null)
			return;
		client_interface.gameWonAck();
		getGameSession().gameWon(server, this);
		setGameSession(null);
	}

	public final void gameStartedNotify(GameSession game_session) {
		if (game_session == null || game_session.getParticipants() == null || game_session.getParticipants().length == 0) {
			MatchmakingServer.getLogger().warning("Invalid GameSession received from " + getUsername());
			return;
		}
		Participant[] participants = game_session.getParticipants();
		int database_id = -1;
		for (int i = 0; i < participants.length; i++) {
			Client client = server.getClientFromID(participants[i].getMatchID());
			if (client == null) {
				MatchmakingServer.getLogger().warning("Invalid participant in GameSession from " + getUsername());
				break;
			}
			Profile p = client.getProfile();
			if (p == null || !p.getNick().equals(participants[i].getNick())) {
				MatchmakingServer.getLogger().warning("Invalid nickparticipant in GameSession from " + getUsername() + " or " + client.getUsername() + " has given wrong nick");
				break;
			}
			if (i == 0)
				database_id = client.getCurrentGame().getDatabaseID();
			// Check if one of the others already established the session
			TimestampedGameSession client_session = client.getGameSession();
			if (client_session != null && client_session.getSession().getID() == game_session.getID()) {
				// If the session ids match, it must be the same game
				if (!client_session.getSession().equals(game_session)) {
					MatchmakingServer.getLogger().warning("GameSession from " + getUsername() + " does not match the one from " + client.getUsername());
					break;
				}
				if (!client_session.join(server, this)) {
					MatchmakingServer.getLogger().warning(getUsername() + " joined session " + Integer.toHexString(game_session.getID()) + " too late or seat already taken");
					break;
				}
				MatchmakingServer.getLogger().info("GameSession " + Integer.toHexString(game_session.getID()) + " joined by " + getUsername());
				setGameSession(client_session);
				return;
			}
		}
		MatchmakingServer.getLogger().info("Game " + database_id + ": New GameSession " + Integer.toHexString(game_session.getID()) + " started by " + getUsername());
		TimestampedGameSession new_session = new TimestampedGameSession(game_session, database_id);
		if (new_session.join(server, this)) {
			setGameSession(new_session);
			DBInterface.startGame(new_session, server);
		} else {
			MatchmakingServer.getLogger().warning("Game " + database_id + ": " + getUsername() + " could not join own game");
		}
	}

	private final void setGameSession(TimestampedGameSession t) {
		if (t != null && current_session != null)
			gameLostNotify();
		current_session = t;

		int database_id = -1;
		if (t != null)
			database_id = t.getDatabaseID();
		DBInterface.profileSetGame(active_profile.getNick(), database_id);
		if (current_room != null)
			current_room.sendUsers();
	}

	private final TimestampedGameSession getGameSession() {
		return current_session;
	}
	
	public final boolean isPlaying() {
		return current_session != null;
	}
	
	public final void handle(Object sender, ARMIEvent event) {
		try {
			event.execute(interface_methods, this);
		} catch (IllegalARMIEventException e) {
			error(e);
		}
	}

	public final String getUsername() {
		return username;
	}

	public final void error(AbstractConnection conn, IOException e) {
		error(e);
	}
	
	private final void error(Exception e) {
		MatchmakingServer.getLogger().info(username + " logged out. Caused by: " + e.getMessage());
		MatchmakingServer.getLogger().throwing("Client", "error", e);
		close();
	}

	public final void connected(AbstractConnection conn) {
	}

	public final int getHostID() {
		return host_id;
	}

	private final Game getCurrentGame() {
		return current_game;
	}
	
	public final InetAddress getRemoteAddress() {
		return remote_address;
	}

	private final int getRevision() {
		return revision;
	}
		
	public final void requestList(int type, int update_key) {
		if (update_key != this.update_key) {
			client_interface.updateComplete(this.update_key);
			return;
		}
		client_interface.updateStart(type);
		Iterator it;
		int chunk_index = 0;
		switch (type) {
			case TYPE_GAME:
				it = game_hosts.iterator();
				GameHost[] game_hosts_chunk = new GameHost[CHUNK_SIZE];
				while (it.hasNext()) {
					Client client = (Client)it.next();
					Game game = client.getCurrentGame();
					int host_id = client.getHostID();
					int game_revision = client.getRevision();
					game_hosts_chunk[chunk_index++] = new GameHost(game, host_id, game_revision);
					if (chunk_index == game_hosts_chunk.length) {
						client_interface.updateList(type, game_hosts_chunk);
						chunk_index = 0;
					}
				}
				if (chunk_index > 0) {
					GameHost[] capped_game_hosts_chunk = new GameHost[chunk_index];
					for (int i = 0; i < capped_game_hosts_chunk.length; i++)
						capped_game_hosts_chunk[i] = game_hosts_chunk[i];
					client_interface.updateList(type, capped_game_hosts_chunk);
				}
				break;
			case TYPE_CHAT_ROOM_LIST:
				it = ChatRoom.getChatRooms().values().iterator();
				ChatRoomEntry[] chat_rooms_chunk = new ChatRoomEntry[CHUNK_SIZE];
				while (it.hasNext()) {
					ChatRoom chat_room = (ChatRoom)it.next();
					chat_rooms_chunk[chunk_index++] = new ChatRoomEntry(chat_room.getName(), chat_room.getUsers().size());
					if (chunk_index == chat_rooms_chunk.length) {
						client_interface.updateList(type, chat_rooms_chunk);
						chunk_index = 0;
					}
				}
				if (chunk_index > 0) {
					ChatRoomEntry[] capped_chat_rooms_chunk = new ChatRoomEntry[chunk_index];
					for (int i = 0; i < capped_chat_rooms_chunk.length; i++)
						capped_chat_rooms_chunk[i] = chat_rooms_chunk[i];
					client_interface.updateList(type, capped_chat_rooms_chunk);
				}
				break;
			case TYPE_RANKING_LIST:
				RankingEntry[] all = DBInterface.getTopRankings(50);
				RankingEntry[] ranking_chunk = new RankingEntry[CHUNK_SIZE];
				for (int i = 0; i < all.length; i++) {
					ranking_chunk[chunk_index++] = all[i];
					if (chunk_index == ranking_chunk.length) {
						client_interface.updateList(type, ranking_chunk);
						chunk_index = 0;
					}
				}
				if (chunk_index > 0) {
					RankingEntry[] capped_ranking_chunk = new RankingEntry[chunk_index];
					for (int i = 0; i < capped_ranking_chunk.length; i++)
						capped_ranking_chunk[i] = ranking_chunk[i];
					client_interface.updateList(type, capped_ranking_chunk);
				}
				break;
			default:
				MatchmakingServer.getLogger().warning("Unexpected type requested");
				break;
		}
		this.update_key = random.nextInt();
		client_interface.updateComplete(this.update_key);
	}

	public final void closeTunnel(HostSequenceID address_to) {
		Client client = (Client)tunnels.remove(address_to);
		if (client != null)
			client.tunnelClosed(address_to);
	}
	
	public final void openTunnel(int address_to, int seq) {
		HostSequenceID host_seq_id = new HostSequenceID(getHostID(), seq);
		Client client = server.getClientFromID(address_to);
		tunnels.put(host_seq_id, client);
		if (client != null) {
			client.tunnelOpened(host_seq_id, remote_address, local_remote_address, active_profile, this);
		} else
			tunnelClosed(host_seq_id);
	}

	private final void tunnelClosed(HostSequenceID address_from) {
		if (tunnels.remove(address_from) != null)
			client_interface.tunnelClosed(address_from);
	}

	public final void close() {
		Iterator it = tunnels.keySet().iterator();
		while (it.hasNext()) {
			HostSequenceID tunnel_address = (HostSequenceID)it.next();
			Client client = (Client)tunnels.get(tunnel_address);
			if (client != null && client != this)
				client.tunnelClosed(tunnel_address);
		}
		conn.close();
		closeProfile();
		server.logoutClient(this);
	}

	private final void closeProfile() {
		gameLostNotify();
		leaveRoom();
		unregisterGame();
		if (active_profile != null) {
			active_clients.remove(active_profile.getNick().toLowerCase());
			DBInterface.profileOffline(active_profile.getNick());
			active_profile = null;
		}
	}

	private final void tunnelOpened(HostSequenceID address_to, InetAddress inet_address_to, InetAddress local_inet_address_to, Profile profile, Client remote_client) {
		tunnels.put(address_to, remote_client);
		client_interface.tunnelOpened(address_to, inet_address_to, local_inet_address_to, profile);
	}
	
	private final void receiveRoutedEvent(HostSequenceID address, ARMIEvent event) {
		client_interface.receiveRoutedEvent(address, event);
	}
	
	public final void setMulticast(HostSequenceID[] addresses) {
		this.multicast_addresses = addresses;
	}
	
	public final void multicastEvent(ARMIEvent event) {
		for (int i = 0; i < multicast_addresses.length; i++)
			routeEvent(multicast_addresses[i], event);
	}

	public final void routeEvent(HostSequenceID address_to, ARMIEvent event) {
		Client client = (Client)tunnels.get(address_to);
		if (client != null) {
			client.receiveRoutedEvent(address_to, event);
		} else
			tunnelClosed(address_to);
	}
	
	private final void tunnelAccepted(HostSequenceID host_seq) {
		client_interface.tunnelAccepted(host_seq);
	}

	public final void acceptTunnel(HostSequenceID address_to) {
		Client client = (Client)tunnels.get(address_to);
		if (client != null) {
			client.tunnelAccepted(address_to);
		} else
			tunnelClosed(address_to);
	}

	public final void registerGame(Game game) {
		if (game != null && game.isValid() && getProfile() != null) {
			current_game = game;
			game_hosts.add(this);
			MatchmakingServer.getLogger().info("Game registered, name = " + current_game.getName());
			DBInterface.createGame(game, getProfile().getNick());

			if (current_room != null) {
				String formatted_message = getProfile().getNick() + " has created a game called \"" + current_game.getName() + "\".";
				server.getChatLogger().info(formatted_message);
				current_room.sendMessage("Server", formatted_message);
			}
		}
	}

	public final void unregisterGame() {
		if (game_hosts.contains(this)) {
			MatchmakingServer.getLogger().info("Game unregistered, name = " + current_game.getName());
			game_hosts.remove(this);
			DBInterface.dropGame(getProfile().getNick());
		}
	}

	public final MatchmakingClientInterface getClientInterface() {
		return client_interface;
	}

	public final void joinRoom(String room_name) {
		if (getProfile() != null) {
			if (current_room == null && ChatRoom.isNameValid(room_name)) {
				ChatRoom room = ChatRoom.getChatRoom(room_name);
				if (room.getUsers().size() < MAX_ROOM_USERS) {
					MatchmakingServer.getLogger().info(getProfile().getNick() + " joined chat room, name = " + room.getName());
					current_room = room;
					client_interface.joiningChatRoom(current_room.getName());
					current_room.join(this);
					client_interface.receiveChatRoomMessage("Server", "Welcome to the Tribal Trouble multiplayer server. Please keep a proper tone while playing online: All activity in the chatrooms and the game is logged and any abusive behavior will result in the immediate banning from the multiplayer server at Oddlabs' discretion.");
					return;
				} else {
					client_interface.error(MatchmakingClientInterface.CHAT_ERROR_TOO_MANY_USERS);
				}
			} else {
				client_interface.error(MatchmakingClientInterface.CHAT_ERROR_INVALID_NAME);
			}
		}
	}

	public final void sendPrivateMessage(String nick, String msg) {
		if (nick == null || msg == null)
			return;
		if (getProfile() != null) {
			if (guest) {
				 client_interface.receivePrivateMessage("Server", "Sorry, only registered users are able to chat.");
				 return;
			}
			Client client = (Client)active_clients.get(nick.toLowerCase());
			if (client != null) {
				server.getChatLogger().info("To " + nick + ": " + formatChat(msg));
				client.getClientInterface().receivePrivateMessage(getProfile().getNick(), msg);
				if (client != this)
					getClientInterface().receivePrivateMessage(getProfile().getNick(), msg);
			} else
				getClientInterface().error(MatchmakingClientInterface.CHAT_ERROR_NO_SUCH_NICK);
		}
	}

	public final void requestInfo(String nick) {
		Client client = (Client)active_clients.get(nick.toLowerCase());
		if (client != null) {
			Profile profile = client.getProfile();
			if (profile != null)
				getClientInterface().receiveInfo(profile);
			else
				getClientInterface().error(MatchmakingClientInterface.CHAT_ERROR_NO_SUCH_NICK);
		}
	}

	private final String formatChat(String message) {
		return "<" + getProfile().getNick() + "> " + message;
	}

	public final void sendMessageToRoom(String msg) {
		if (current_room != null) {
			if (guest) {
				 client_interface.receivePrivateMessage("Server", "Sorry, only registered users are able to chat.");
				 return;
			}
			String formatted_message = formatChat(msg);
			server.getChatLogger().info(formatted_message);
			current_room.sendMessage(getProfile().getNick(), msg);
		}
	}

	public final void leaveRoom() {
		if (current_room != null) {
			current_room.leave(this);
			current_room = null;
		}
	}
}
