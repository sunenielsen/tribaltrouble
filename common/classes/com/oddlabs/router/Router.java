package com.oddlabs.router;

import com.oddlabs.net.ConnectionListenerInterface;
import com.oddlabs.net.AbstractConnectionListener;
import com.oddlabs.net.ConnectionListener;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.net.TimeManager;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;
import java.util.Iterator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;

public final strictfp class Router implements ConnectionListenerInterface {
	private final Logger logger;
	private final SessionManager manager;
	private final AbstractConnectionListener listener;
	private final Set clients = new LinkedHashSet();
	private final RouterListener router_listener;
	private final int port;

	public Router(NetworkSelector network, Logger logger) {
		this(network, null, RouterInterface.PORT, logger, null);
	}

	public Router(NetworkSelector network, InetAddress address, int port, Logger logger, RouterListener router_listener) {
		this.manager = new SessionManager(network.getTimeManager(), logger);
		this.logger = logger;
		this.router_listener = router_listener;
		ConnectionListener tmp_listener = new ConnectionListener(network, address, port, this);
		this.listener = tmp_listener;
		this.port = tmp_listener.getPort();
	}

	public final int getPort() {
		return port;
	}

	public final long getNextTimeout() {
		return manager.getNextTimeout();
	}

	public final void process() {
		manager.process();
	}

	public final void incomingConnection(AbstractConnectionListener connection_listener, Object remote_address) {
		logger.info("Incoming connection from " + remote_address);
		AbstractConnection conn = connection_listener.acceptConnection(null);
		RouterClient client = new RouterClient(manager, conn, logger, this);
		clients.add(client);
		conn.setConnectionInterface(client);
	}

	final void removeClient(RouterClient client) {
		clients.remove(client);
	}

	public final void error(AbstractConnectionListener conn_id, IOException e) {
		close();
		logger.severe("Server socket failed: " + e);
		if (router_listener != null)
			router_listener.routerFailed(e);
	}

	public final void close() {
		if (listener != null)
			listener.close();
		Iterator it = clients.iterator();
		while (it.hasNext()) {
			RouterClient client = (RouterClient)it.next();
			it.remove();
			client.close(false);
		}
	}
}
