package com.oddlabs.tt.net;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.matchmaking.MatchmakingServerInterface;
import com.oddlabs.matchmaking.Profile;
import com.oddlabs.matchmaking.GameSession;
import com.oddlabs.net.ARMIEvent;
import com.oddlabs.net.ARMIEventBroker;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.net.AbstractConnectionListener;
import com.oddlabs.net.ConnectionInterface;
import com.oddlabs.net.ConnectionListener;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.net.ConnectionListenerInterface;
import com.oddlabs.net.IllegalARMIEventException;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.resource.WorldGenerator;
import com.oddlabs.tt.net.MatchmakingClient;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.matchmaking.TunnelAddress;

public final strictfp class Server implements ConnectionListenerInterface {
	private final static int NEGOTIATING = 1;
	private final static int SYNCHRONIZING = 2;
	private final static int CLOSED = 3;

	private final PlayerSlot[] players;
	private final String[] ai_names;
	private final WorldGenerator generator;
	private final Game game;
	private final AbstractConnectionListener local_listener;
	private final Map connection_to_client = new LinkedHashMap();
	private final Random random;
	private AbstractConnectionListener tunnelled_listener;

	private int state = NEGOTIATING;
	private boolean register_server;

	public Server(NetworkSelector network, Game game, InetAddress ip, WorldGenerator generator, boolean register_server, String[] ai_names) {
		this.local_listener = new ConnectionListener(network, ip, Globals.NET_PORT, this);
		this.game = game;
		this.generator = generator;
		this.register_server = register_server;
		this.ai_names = ai_names;
		this.random = new Random(LocalEventQueue.getQueue().getHighPrecisionManager().getTick());
		players = new PlayerSlot[MatchmakingServerInterface.MAX_PLAYERS];
		for (short i = 0; i < players.length; i++) {
			players[i] = new PlayerSlot(i);
			players[i].setReady(i != 0);
/*			players[i] = new PlayerInfo(i%max_teams);
			players[i].setType(PlayerSlot.OPEN);
			players[i].setRace((int)(random.nextFloat()*(RacesResources.getNumRaces() - 1) + .5f));
			players[i].setReady(i != 0);*/
		}
	}

	private final Iterator getClientIterator() {
		return connection_to_client.values().iterator();
	}
	
	private final int getNumClients() {
		return connection_to_client.size();
	}

	private final ClientConnection getClientFromConnection(AbstractConnection conn) {
		return (ClientConnection)connection_to_client.get(conn);
	}

	private final void unregisterGame() {
		local_listener.close();
		if (tunnelled_listener != null)
			tunnelled_listener.close();
		if (register_server && Network.getMatchmakingClient().isConnected()) {
			Network.getMatchmakingClient().getInterface().unregisterGame();
		}
	}

	private final void unregister() {
		state = CLOSED;
	}

	private final void closeConnections() {
		Iterator it = connection_to_client.keySet().iterator();
		while (it.hasNext()) {
			AbstractConnection conn = (AbstractConnection)it.next();
			conn.close();
		}
		connection_to_client.clear();
		unregister();
	}

	public final void close() {
		unregisterGame();
		closeConnections();
	}

	private final int getNumReady() {
		int count = 0;
		Iterator it = getClientIterator();
		while (it.hasNext()) {
			ClientConnection client = (ClientConnection)it.next();
			if (client.getClient().getPlayerSlot().isReady())
				count++;
		}
		return count;
	}

	public final void error(AbstractConnectionListener listener, IOException e) {
		System.out.println("Listener failed: " + e);
		close();
	}

	public final void handleError(AbstractConnection conn, Exception e) {
		System.out.println("Disconnecting client because of exception: " + e);
		ClientConnection client = getClientFromConnection(conn);
		if (client != null) {
			disconnectClient(client);
			if (state == NEGOTIATING) {
				resetSlotState(client.getClient().getPlayerSlot(), true);
			}
		}
	}

	private final void disconnectClient(ClientConnection client) {
		assert client != null;
		client.getConnection().close();
		connection_to_client.remove(client.getConnection());
	}

	private final ClientConnection locateClientForSlot(PlayerSlot player_slot) {
		Iterator it = getClientIterator();
		while (it.hasNext()) {
			ClientConnection client = (ClientConnection)it.next();
			if (client.getClient().getPlayerSlot() == player_slot)
				return client;
		}
		return null;
	}

	public final void resetSlotState(PlayerSlot client_slot, int slot, boolean open) {
		if (!canControlSlot(client_slot, slot))
			return;
		resetSlotState(players[slot], open);
	}

	private void resetSlotState(PlayerSlot client_slot, boolean open) {
		client_slot.setType(open ? PlayerSlot.OPEN : PlayerSlot.CLOSED);
		client_slot.setInfo(null);
		client_slot.setAddress(null);
		client_slot.setReady(true);
		client_slot.setAIDifficulty(PlayerSlot.AI_NONE);
		ClientConnection player_client = locateClientForSlot(client_slot);
		if (player_client != null)
			disconnectClient(player_client);
		broadcastPlayers(true);
	}

	private boolean canControlSlot(PlayerSlot client_slot, int slot) {
		return slot >= 0 && slot < players.length && state == NEGOTIATING &&
			((client_slot.getSlot() == 0 || client_slot.getSlot() == slot));
	}

	public final void startServer(PlayerSlot slot) {
		if (!canControlSlot(slot, 0) || getNumReady() != getNumClients())// || PlayerSlot.getNumTeams(players) < 2)
			return;
		state = SYNCHRONIZING;
		unregisterGame();
		broadcastInits();
	}

	public final void setPlayerSlot(PlayerSlot client_slot, int slot, int type, int race, int team, boolean ready, int ai_difficulty) {
		if (!PlayerSlot.isValidType(type) || !RacesResources.isValidRace(race))
			return;
		if (!canControlSlot(client_slot, slot) || (client_slot.getSlot() == slot && type != PlayerSlot.HUMAN))
			return;
		PlayerSlot player_slot = players[slot];
//		PlayerInfo player_info = players[slot];
		ClientConnection player_client = locateClientForSlot(player_slot);
		if (player_client != null && type != PlayerSlot.HUMAN)
			disconnectClient(player_client);
		String name;
		if (type == PlayerSlot.AI) {
			name = ai_names[slot];
		} else {
			name = player_slot.getInfo().getName();
		}
		PlayerInfo player_info = new PlayerInfo(team, race, name);
		boolean reset_ready = player_slot.getInfo() == null || type != player_slot.getType() || ai_difficulty != player_slot.getAIDifficulty() || !player_info.equals(player_slot.getInfo());
		player_slot.setType(type);
		player_slot.setAIDifficulty(ai_difficulty);
		player_slot.setInfo(player_info);
		player_slot.setReady(type != PlayerSlot.HUMAN || ready);
		broadcastPlayers(reset_ready);
	}

	private final void resetReady() {
		int num_humans = 0;
		for (int i = 0; i < players.length; i++) {
			PlayerSlot player_slot = players[i];
			if (player_slot.getType() == PlayerSlot.HUMAN)
				num_humans++;
		}
		if (num_humans > 1) {
			for (int i = 0; i < players.length; i++) {
				PlayerSlot player_slot = players[i];
				if (player_slot.getType() == PlayerSlot.HUMAN)
					player_slot.setReady(false);
			}
		}
	}

	private final void broadcastPlayers(boolean reset_ready) {
		if (reset_ready)
			resetReady();
		Iterator it = getClientIterator();
		while (it.hasNext()) {
			ClientConnection client = (ClientConnection)it.next();
			client.getClientInterface().setPlayers(players);
		}
	}

	public final void chat(PlayerSlot player_slot, String chat) {
		Iterator it = getClientIterator();
		while (it.hasNext()) {
			ClientConnection client = (ClientConnection)it.next();
			client.getClientInterface().chat(player_slot.getSlot(), chat);
		}
	}
	
	private final void broadcastInits() {
		Iterator it = getClientIterator();
		while (it.hasNext()) {
			ClientConnection client = (ClientConnection)it.next();
			int session_id = new Random(LocalEventQueue.getQueue().getHighPrecisionManager().getTick()).nextInt();
			client.getClientInterface().startGame(session_id);
		}
	}

	private final short locateAvailableSlot() {
		for (short i = 0; i < players.length; i++) {
			if (players[i].getType() == PlayerSlot.OPEN)
				return i;
		}
		return (short)-1;
	}

	public final void incomingConnection(AbstractConnectionListener connection_listener, Object remote_address) {
System.out.println("Incoming host connection from " + remote_address);
		short available_slot = locateAvailableSlot();
		if (state != NEGOTIATING || available_slot == -1 ||
			(remote_address instanceof InetAddress && !((InetAddress)remote_address).isLoopbackAddress()) ||
			(remote_address instanceof TunnelIdentifier && game != null && game.isRated() && 
			((TunnelIdentifier)remote_address).getProfile().getWins() < GameSession.MIN_WINS_FOR_RANKING)) {
			System.out.println("rejecting incoming connection since state = " + state + " | locateAvailableSlot() = " + available_slot + " remote_address = " + remote_address);
			connection_listener.rejectConnection();
			return;
		}
		PlayerSlot player_slot = players[available_slot];
		int rating = 0;
		String name;
		TunnelAddress address;
		if (remote_address instanceof InetAddress) {
			address = Network.getMatchmakingClient().getLocalAddress();
			if (register_server) {
				tunnelled_listener = new TunnelledConnectionListener(this);
				Network.getMatchmakingClient().getInterface().registerGame(game);
			}
			Profile profile = Network.getMatchmakingClient().getProfile();
			if (profile != null) {
				name = profile.getNick();
				rating = profile.getRating();
			} else
				name = Utils.getBundleString(ResourceBundle.getBundle(MatchmakingClient.class.getName()), "player");
		} else {
			TunnelIdentifier tunnel_id = (TunnelIdentifier)remote_address;
			name = tunnel_id.getProfile().getNick();
			rating = tunnel_id.getProfile().getRating();
			address = tunnel_id.getAddress();
		}
		player_slot.setReady(false);
		int max_teams = MatchmakingServerInterface.MAX_PLAYERS;
		if (game != null && game.isRated())
			max_teams = 2;
		PlayerInfo player_info = new PlayerInfo(available_slot%max_teams, random.nextInt(RacesResources.getNumRaces()), name);
		player_slot.setRating(rating);
		player_slot.setType(PlayerSlot.HUMAN);
		player_slot.setAddress(address);
		player_slot.setInfo(player_info);
		ClientInfo client = new ClientInfo(this, player_slot);
		AbstractConnection conn = connection_listener.acceptConnection(client);
		ClientConnection client_conn = new ClientConnection(conn, client);
		connection_to_client.put(conn, client_conn);
		client_conn.getClientInterface().setWorldGeneratorAndPlayerSlot(game, generator, available_slot);
		broadcastPlayers(true);
	}
}
