package com.oddlabs.tt.net;

import java.io.IOException;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.net.ConnectionInterface;
import com.oddlabs.net.ARMIEvent;
import com.oddlabs.net.IllegalARMIEventException;
import com.oddlabs.net.ARMIInterfaceMethods;
import com.oddlabs.net.ConnectionListener;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.tt.player.PlayerInfo;

final strictfp class ClientConnection {
	private final AbstractConnection connection;
	private final GameClientInterface gameclient_interface;
	private final ClientInfo client;

	public ClientConnection(AbstractConnection conn, ClientInfo client) {
		this.connection = conn;
		this.gameclient_interface = (GameClientInterface)ARMIEvent.createProxy(connection, GameClientInterface.class);
		this.client = client;
	}

	public final GameClientInterface getClientInterface() {
		return gameclient_interface;
	}

	public final AbstractConnection getConnection() {
		return connection;
	}

	public final ClientInfo getClient() {
		return client;
	}
}
