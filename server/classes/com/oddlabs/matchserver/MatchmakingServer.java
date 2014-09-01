package com.oddlabs.matchserver;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.*;

import com.oddlabs.util.KeyManager;
import com.oddlabs.util.DBUtils;
import com.oddlabs.net.AbstractConnectionListener;
import com.oddlabs.net.ConnectionListener;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.net.SecureConnection;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.net.ConnectionListenerInterface;
import com.oddlabs.matchmaking.MatchmakingServerInterface;
import com.oddlabs.matchmaking.MatchmakingServerLoginInterface;
import com.oddlabs.registration.RegistrationInfo;
import com.oddlabs.registration.RegistrationKey;

import com.oddlabs.event.Deterministic;
import com.oddlabs.event.NotDeterministic;

import java.security.spec.AlgorithmParameterSpec;
import java.security.PublicKey;
import java.net.InetAddress;

public final class MatchmakingServer implements ConnectionListenerInterface {
	private final static Map online_users = new HashMap();
	private static int current_id = 1;

	private final static Logger logger = Logger.getLogger("com.oddlabs.matchserver");

	private final Logger chat_logger = Logger.getLogger("chatlog");

	private final AbstractConnectionListener connection_listener;
	private final AlgorithmParameterSpec param_spec;
	private final PublicKey public_reg_key;
	private final NetworkSelector network;
	private final Map client_map = new HashMap();

	static {
		try {
			Handler fh = new FileHandler("logs/matchserver.%g.log", 10*1024*1024, 50);
			fh.setFormatter(new SimpleFormatter());
			logger.addHandler(fh);
			logger.setLevel(Level.ALL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private MatchmakingServer() throws Exception {
		Deterministic deterministic = new NotDeterministic();
		this.network = new NetworkSelector(deterministic);
		Handler fh = new FileHandler("logs/chatlog.%g.log", 10*1024*1024, 50);
		fh.setFormatter(new SimpleFormatter());
		chat_logger.addHandler(fh);
		chat_logger.setLevel(Level.ALL);

		this.public_reg_key = RegistrationKey.loadPublicKey();
		DBUtils.initConnection("jdbc:mysql://localhost/oddlabs", "matchmaker", "U46TawOp");
		logger.info("Generating encryption keys.");
		this.param_spec = KeyManager.generateParameterSpec();
		connection_listener = new ConnectionListener(network, null, MatchmakingServerInterface.MATCHMAKING_SERVER_PORT, this);
		DBInterface.initDropGames();
		DBInterface.clearOnlineProfiles();
		logger.info("Matchmaking server started.");
		while (true)
			network.tickBlocking();
	}

	public final static Logger getLogger() {
		return logger;
	}

	public final Logger getChatLogger() {
		return chat_logger;
	}

	public final PublicKey getPublicRegKey() {
		return public_reg_key;
	}
	
	public final AlgorithmParameterSpec getSpec() {
		return param_spec;
	}

	public final void incomingConnection(AbstractConnectionListener connection_listener, Object remote_address) {
		int id = current_id++;
		AbstractConnection conn = connection_listener.acceptConnection(null);
		SecureConnection secure_conn = new SecureConnection(network.getDeterministic(), conn, param_spec);
		Authenticator client = new Authenticator(this, secure_conn, (InetAddress)remote_address, id);
	}

//	public final boolean isKeyOnline(String key_encoded) {
//		return online_keys.contains(key_encoded);
//	}

	public final void loginClient(InetAddress remote_address, InetAddress local_remote_address, String username, AbstractConnection conn, String key_code_encoded, int revision, int host_id) {
//		online_keys.add(key_code_encoded);
		Client old_logged_in = (Client)online_users.remove(username.toLowerCase());
		if (old_logged_in != null) {
			old_logged_in.close();
			logger.info(username + " overtaked old login");
		}
		Client client = new Client(this, conn, remote_address, local_remote_address, username, key_code_encoded == null, revision, host_id);
		online_users.put(username.toLowerCase(), client);
		client_map.put(new Integer(client.getHostID()), client);
		logger.info(username + " logged in, with key " + key_code_encoded);
	}

	public final Client getClientFromID(int host_id) {
		return (Client)client_map.get(new Integer(host_id));
	}
	
	public final void error(AbstractConnectionListener conn_id, IOException e) {
		logger.severe("Server socket failed!");
		throw new RuntimeException(e);
	}

	public final void logoutClient(Client client) {
		online_users.remove(client.getUsername().toLowerCase());
		removeInstance(client.getHostID());
	}

	public final void removeInstance(int instance_id) {
		client_map.remove(new Integer(instance_id));
	}
	
	private final static void postPanic() {
		try {
			DBUtils.postHermesMessage("elias, xar, jacob, thufir: Matchmaking service crashed!");
		} catch (Throwable t) {
			logger.throwing("MatchmakingServer", "postPanic", t);
		}
	}
	
	public final static void main(String[] args) {
		try {
			new MatchmakingServer();
		} catch (Throwable t) {
			logger.throwing("MatchmakingServer", "main", t);
			postPanic();
			System.exit(1);
		}
	}
}
