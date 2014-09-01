package com.oddlabs.tt.net;

import com.oddlabs.util.Utils;
import com.oddlabs.net.*;
import com.oddlabs.router.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public final strictfp class RouterClient implements ConnectionInterface {
	private final ARMIInterfaceMethods interface_methods = new ARMIInterfaceMethods(RouterClientInterface.class);
	private final AbstractConnection connection;
	private final GameInterface game_interface;
	private final RouterHandler router_handler;

	public RouterClient(NetworkSelector network, RouterHandler router_handler, int port) {
		this.router_handler = router_handler;
		this.connection = new Connection(network, new InetSocketAddress(Utils.getLoopbackAddress(), port), this);
		this.game_interface = (GameInterface)ARMIEvent.createProxy(connection, GameInterface.class);
	}

	public RouterClient(NetworkSelector network, String address, RouterHandler router_handler) {
		this.router_handler = router_handler;
		this.connection = new Connection(network, address, RouterInterface.PORT, this);
		this.game_interface = (GameInterface)ARMIEvent.createProxy(connection, GameInterface.class);
	}

	public final void connect(SessionID session_id, SessionInfo session_info, int client_id) {
		RouterInterface router_interface = (RouterInterface)ARMIEvent.createProxy(connection, RouterInterface.class);
		router_interface.login(session_id, session_info, client_id);
	}

	public final GameInterface getInterface() {
		return game_interface;
	}
	
	public final void handle(Object sender, ARMIEvent armi_event) {
		try {
			armi_event.execute(interface_methods, router_handler);
		} catch (IllegalARMIEventException e) {
			close();
			router_handler.routerFailed(e);
		}
	}

	public final void writeBufferDrained(AbstractConnection conn) {
	}

	public final void connected(AbstractConnection conn) {
	}

	public final void error(AbstractConnection conn, IOException e) {
		router_handler.routerFailed(e);
	}

	public final void close() {
		connection.close();
	}
}
