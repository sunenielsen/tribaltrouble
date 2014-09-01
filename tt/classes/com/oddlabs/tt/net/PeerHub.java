package com.oddlabs.tt.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.matchmaking.GameSession;
import com.oddlabs.matchmaking.Participant;
import com.oddlabs.matchmaking.TunnelAddress;
import com.oddlabs.router.Router;
import com.oddlabs.router.RouterListener;
import com.oddlabs.net.ARMIEvent;
import com.oddlabs.net.ARMIEventWriter;
import com.oddlabs.net.ARMIInterfaceMethods;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.net.AbstractConnectionListener;
import com.oddlabs.net.ConnectionListenerInterface;
import com.oddlabs.net.IllegalARMIEventException;
import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.form.MessageForm;
import com.oddlabs.tt.form.WaitingForPlayersForm;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.delegate.CameraDelegate;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.InfoPrinter;
import com.oddlabs.tt.delegate.SelectionDelegate;
import com.oddlabs.tt.trigger.GameOverTrigger;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.landscape.WorldParameters;
import com.oddlabs.tt.viewer.NotificationManager;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.player.PlayerInterface;
import com.oddlabs.tt.player.UnitInfo;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.resource.WorldGenerator;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.router.RouterInterface;
import com.oddlabs.router.SessionID;
import com.oddlabs.router.SessionInfo;
import com.oddlabs.tt.util.StatCounter;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.util.Utils;

public final strictfp class PeerHub implements Animated, RouterHandler {
	public final static ResourceBundle bundle = ResourceBundle.getBundle(PeerHub.class.getName());
	public final static String SYSTEM_NAME;

	private final static int MILLISECONDS_PER_HEARTBEAT = 60;
	private final static int CLIENT_MAX_DELAY_MILLIS = 60;
	private final static float FREE_QUIT_TIME = 120f;
	private final static int TICKS_PER_STATUS_UPDATE = (int)(20/AnimationManager.ANIMATION_SECONDS_PER_TICK);
	private final static int TICKS_PER_CHECKSUM = (int)(10/AnimationManager.ANIMATION_SECONDS_PER_TICK);

	private static boolean waiting_for_ack = false;
	
	private final ARMIInterfaceMethods interface_methods = new ARMIInterfaceMethods(PeerHubInterface.class);
	private final StateChecksum checksum = new StateChecksum();
	private final PeerHubInterface peerhubs_interface;
	private final PlayerInterface player_interface;
	private final int num_participants;
	private final Peer[] peer_index_to_peer;
	private final Map player_to_peer = new LinkedHashMap();
	private final Map peer_to_player = new LinkedHashMap();
	private final Set nonhuman_players = new HashSet();
	private final GUIRoot gui_root;
	private final NetworkSelector network;
	private final RouterClient router_client;
	private final Router router;
	private final NotificationManager notification_manager;
	private final Player local_player;
	private final AnimationManager manager;
	private final boolean is_multiplayer;
	private final boolean is_rated;
	private final StallHandler stall_handler;

	private int pause_ticks;
	private int server_millis;
	private int paused;
	private boolean is_synchronized;

//private int ignore_peer = -1;

	static {
		SYSTEM_NAME = Utils.getBundleString(bundle, "system_name");
	}

	public PeerHub(AnimationManager manager, boolean is_multiplayer, boolean is_rated, Player local_player, PlayerSlot[] player_slots, NetworkSelector network, GUIRoot gui_root, NotificationManager notification_manager, DistributableTable distributable_table, SessionID session_id, StallHandler stall_handler) {
		this.stall_handler = stall_handler;
		this.is_rated = is_rated;
		this.local_player = local_player;
		this.network = network;
		this.gui_root = gui_root;
		this.notification_manager = notification_manager;
		this.is_multiplayer = is_multiplayer;
		this.manager = manager;

		GameArgumentReader argument_reader = new GameArgumentReader(distributable_table);
		List peer_index_to_peer_list = new ArrayList();
		Player[] players = local_player.getWorld().getPlayers();
		int local_peer_index = -1;
		if (!is_multiplayer) {
			this.router = new Router(network, com.oddlabs.util.Utils.getLoopbackAddress(), 0, Logger.getAnonymousLogger(), new RouterListener() {
				public final void routerFailed(IOException e) {
//					PeerHub.this.routerFailed(e);
					throw new RuntimeException(e);
				}
			});
			this.router_client = new RouterClient(network, this, router.getPort());
		} else {
			this.router = null;
			this.router_client = new RouterClient(network, Settings.getSettings().router_address, this);
		}
		for (short i = 0; i < players.length; i++) {
			Player player = players[i];
			if (player_slots[i].getType() != PlayerSlot.HUMAN) {
				nonhuman_players.add(player);
				continue;
			}
			System.out.println("index " + i + " contains player " + player);
			final int peer_index = peer_index_to_peer_list.size();
			ARMIEventWriter router_handler = new ARMIEventWriter() {
				public final void handle(ARMIEvent event) {
					router_client.getInterface().relayEventTo(peer_index, event);
				}
			};
			PeerHubInterface peer_interface = (PeerHubInterface)ARMIEvent.createProxy(router_handler, PeerHubInterface.class);
			Peer peer = new Peer(this, peer_index, player, argument_reader, peer_interface);
			ARMIEventWriter peer_broker;
			if (player == local_player) {
				local_peer_index = peer_index;
			}
			peer_index_to_peer_list.add(peer);
			peer_to_player.put(peer, player);
			player_to_peer.put(player, peer);
		}
		this.peer_index_to_peer = new Peer[peer_index_to_peer_list.size()];
		peer_index_to_peer_list.toArray(peer_index_to_peer);
		this.num_participants = peer_index_to_peer.length;
		ARMIEventWriter game_router_handler = new ARMIEventWriter() {
			public final void handle(ARMIEvent event) {
				router_client.getInterface().relayGameStateEvent(event);
			}
		};
		this.player_interface = (PlayerInterface)ARMIEvent.createProxy(game_router_handler, new GameArgumentWriter(distributable_table), PlayerInterface.class);
		ARMIEventWriter hub_router_handler = new ARMIEventWriter() {
			public final void handle(ARMIEvent event) {
				router_client.getInterface().relayEvent(event);
			}
		};
		this.peerhubs_interface = (PeerHubInterface)ARMIEvent.createProxy(hub_router_handler, PeerHubInterface.class);
		manager.registerAnimation(this);
		router_client.connect(session_id, new SessionInfo(num_participants, MILLISECONDS_PER_HEARTBEAT), local_peer_index);
	}

	public final void routerFailed(Exception e) {
		System.out.println("Router failed with exception: " + e);
		closeNetwork();
		stall_handler.peerhubFailed();
	}

	public final void heartbeat(int millis) {
		if (millis < server_millis) {
			routerFailed(new IOException("Invalid time received: " + millis + " (tick currently at " + getTick() + ")"));
			return;
		}
		server_millis = millis;
	}

	public final void receiveEvent(int client_id, ARMIEvent event) {
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

	public final void receiveGameStateEvent(int client_id, int millis, ARMIEvent event) {
		if (millis < server_millis) {
			routerFailed(new IOException("Invalid time received for event: " + millis + " (tick currently at " + getTick() + ")"));
			return;
		}
		Peer peer = getPeerFromClientID(client_id);
		if (peer == null) {
			routerFailed(new IOException("Invalid client_id received: " + client_id));
			return;
		}
		server_millis = millis;
		peer.addEvent(millisToTickCeil(millis), event);
	}

	public final void playerDisconnected(int client_id, boolean checksum_error) {
		Peer peer = getPeerFromClientID(client_id);
		if (checksum_error)
			peerChecksumError(peer);
		else
			peerDisconnected(peer, "Peer disconnected");
	}

	private final void peerChecksumError(Peer peer) {
		System.out.println("Disconnecting peer because of checksum mismatch: " + peer.getPlayerInfo().getName());
		peerDisconnected(peer, "Checksum error");
		Globals.checksum_error_in_last_game = true;
	}

	private Peer getPeerFromClientID(int client_id) {
		if (client_id >= 0 && client_id < peer_index_to_peer.length)
			return unsafeGetPeerFromClientID(client_id);
		else
			return null;
	}

	private Peer unsafeGetPeerFromClientID(int client_id) {
		return peer_index_to_peer[client_id];
	}

	public final void start() {
		is_synchronized = true;
	}
	
	private final Peer locatePeerFromPlayer(Player player) {
		return (Peer)player_to_peer.get(player);
	}

	private final boolean isDisconnected(Peer peer) {
		return getPlayerFromPeer(peer) == null;
	}

	private Player getPlayerFromPeer(Peer peer) {
		return (Player)peer_to_player.get(peer);
	}

	public final boolean isAlive(Player player) {
		return (nonhuman_players.contains(player) || locatePeerFromPlayer(player) != null) && player.isAlive();
	}

	public final PlayerInterface getPlayerInterface() {
		return player_interface;
	}

	private final int millisToTickCeil(int millis) {
		return millisToTick(millis + AnimationManager.ANIMATION_MILLISECONDS_PER_TICK - 1);
	}

	private final int millisToTick(int millis) {
		return millis/AnimationManager.ANIMATION_MILLISECONDS_PER_TICK - pause_ticks;
	}

	public final void animate(float t) {
		if (router != null)
			router.process();
		int server_tick = millisToTick(server_millis);
		if (!is_synchronized || getTick() == server_tick) {
			processStall();
		} else {
			if (!isPaused()) {
				doTick(t);
				int min_tick = millisToTick(server_millis - MILLISECONDS_PER_HEARTBEAT - CLIENT_MAX_DELAY_MILLIS);
				//System.out.println("min_tick-getTick() = " + (min_tick-getTick()));
				while (getTick() < min_tick)
					doTick(t);
			} else
				pause_ticks++;
		}
	}

	private int getTick() {
		return local_player.getWorld().getTick();
	}

	private final void doTick(float t) {
		stall_handler.stopStall();
		if (getFreeQuitTicksLeft(local_player.getWorld()) == 0 && Network.getMatchmakingClient().isConnected())
			Network.getMatchmakingClient().getInterface().freeQuitStopNotify();

		if (getTick()%TICKS_PER_STATUS_UPDATE == 0 && Network.getMatchmakingClient().isConnected())
			sendStatusUpdate();

		for (int i = 0; i < peer_index_to_peer.length; i++) {
			Peer peer = peer_index_to_peer[i];
			if (peer != null) {
				try {
					peer.executeEvents(getTick());
				} catch (IllegalARMIEventException e) {
					peerDisconnected(peer, e.getMessage());
				}
			}
		}
		local_player.getWorld().tick(t);
		if (getTick() % TICKS_PER_CHECKSUM == 0)
			sendChecksum();
	}

	private void sendChecksum() {
		checksum.update(getTick());
		checksum.update(local_player.getWorld().getChecksum());
		local_player.getWorld().getAnimationManagerGameTime().updateChecksum(checksum);
		local_player.getWorld().getAnimationManagerRealTime().updateChecksum(checksum);
		router_client.getInterface().checksum(checksum.getValue());
	}

	public void setPaused(boolean p) {
		if (!is_multiplayer && p)
			paused++;
		else if (!is_multiplayer && !p)
			paused--;
			// FIXME er ikke et problem naar hver gui_root har sin egen World
			//      assert world_singleton.paused >= 0;
	}

	private boolean isPaused() {
		return paused > 0;
	}

	private final void sendStatusUpdate() {
		int[] status = new int[num_participants];
		for (int i = 0; i < status.length; i++) {
			Peer peer = getPeerFromClientID(i);
			if (peer != null)
				status[i] = peer.getPlayer().getStatus();
		}
		Network.getMatchmakingClient().getInterface().updateGameStatus(getTick(), status);
	}

	private final Iterator getPeerIterator()  {
		return peer_to_player.keySet().iterator();
	}

	public final PeerHubInterface getInterface() {
		return peerhubs_interface;
	}

	private final void processStall() {
		stall_handler.processStall(getTick());
	}

	private final void removePeerFromActiveList(Peer peer) {
		System.out.println("Removing from active list:" + peer);
		peer_index_to_peer[peer.getPeerIndex()] = null;
		peer.getPlayer().setPreferredGamespeed(World.GAMESPEED_DONTCARE);
	}

	public final void updateChecksum(StateChecksum sum) {
		sum.update(getTick());
		sum.update(checksum.getValue());
	}

	public final void peerDisconnected(Peer peer, String reason) {
		Player player = getPlayerFromPeer(peer);
		if (player == null)
			return;
		peer_to_player.remove(peer);
		player_to_peer.remove(player);
		int peer_index = peer.getPeerIndex();
		removePeerFromActiveList(peer);
		String left_game_message = Utils.getBundleString(bundle, "left_game", new Object[]{peer.getPlayerInfo().getName(), reason});
		receiveChat(SYSTEM_NAME, left_game_message, false);
		if (getFreeQuitTicksLeft(local_player.getWorld()) >= 0 && Network.getMatchmakingClient().isConnected())
			Network.getMatchmakingClient().getInterface().gameQuitNotify(peer.getPlayerInfo().getName());
	}

	public final void sendChat(String text, boolean team_only) {
		Iterator it = getPeerIterator();
		int local_team = local_player.getPlayerInfo().getTeam();
		while (it.hasNext()) {
			Peer peer = (Peer)it.next();
			int peer_team = peer.getPlayerInfo().getTeam();
			if (!team_only || local_team == peer_team)
				peer.getPeerHubInterface().chat(text, team_only);
		}
	}

	public final void sendBeacon(float x, float y) {
		Iterator it = getPeerIterator();
		int local_team = local_player.getPlayerInfo().getTeam();
		while (it.hasNext()) {
			Peer peer = (Peer)it.next();
			int peer_team = peer.getPlayerInfo().getTeam();
			if (local_team == peer_team)
				peer.getPeerHubInterface().beacon(x, y);
		}
	}

	public final void receiveChat(String name, String text, boolean team) {
		if (team)
			Network.getChatHub().chat(new ChatMessage(name, text, ChatMessage.CHAT_TEAM));
		else
			Network.getChatHub().chat(new ChatMessage(name, text, ChatMessage.CHAT_NORMAL));
	}

	public final void receiveBeacon(float x, float y, String owner) {
		if (!ChatCommand.isIgnoring(owner))
			notification_manager.newBeacon(manager, local_player, x, y);
	}

	private void closeNetwork() {
		router_client.close();
		if (router != null)
			router.close();
		Object[] peers = peer_to_player.keySet().toArray();
		for (int i = 0; i < peers.length; i++) {
			Peer peer = (Peer)peers[i];
			if (peer.getPlayer() != local_player) {
				peerDisconnected(peer, "Synchronization failed");
			}
		}
		stall_handler.stopStall();
		leaveGame();
	}

	public final void close() {
		closeNetwork();
		LocalEventQueue.getQueue().getManager().removeAnimation(this);
System.out.println("PeerHub closed");
	}

	private static int getFreeQuitTicksLeft(World world) {
		return (int)(FREE_QUIT_TIME/world.getSecondsPerTick()) - world.getTick();
	}

	public static float getFreeQuitTimeLeft(World world) {
		int left = getFreeQuitTicksLeft(world);
		return left*AnimationManager.ANIMATION_SECONDS_PER_TICK;
	}

	public final void leaveGame() {
		if (Network.getMatchmakingClient().isConnected()) {
			if (getFreeQuitTicksLeft(local_player.getWorld()) >= 0) {
				Network.getMatchmakingClient().getInterface().gameQuitNotify(local_player.getPlayerInfo().getName());
			} else {
				Network.getMatchmakingClient().getInterface().gameLostNotify();
			}
		}
	}

	public final void gameWon() {
		if (Network.getMatchmakingClient().isConnected()) {
			Network.getMatchmakingClient().getInterface().gameWonNotify();
			waiting_for_ack = true;
		}
	}

	public final static void receivedAck() {
		waiting_for_ack = false;
	}

	public final static boolean isWaitingForAck() {
		return waiting_for_ack;
	}
}
