package com.oddlabs.tt.net;

import java.net.InetAddress;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;

import com.oddlabs.matchmaking.Profile;
import com.oddlabs.matchmaking.TunnelAddress;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.net.AbstractConnectionListener;
import com.oddlabs.net.ConnectionInterface;
import com.oddlabs.net.ConnectionListenerInterface;
import com.oddlabs.net.HostSequenceID;

public final strictfp class TunnelledConnectionListener extends AbstractConnectionListener {
	private final List incoming_connections = new LinkedList();
	private boolean open = true;

	public TunnelledConnectionListener(ConnectionListenerInterface listener_interface) {
		super(listener_interface);
		Network.getMatchmakingClient().registerTunnelledListener(this);
	}

	public final void requestTunnelledConnection(HostSequenceID address, InetAddress inet_address, InetAddress local_address, Profile profile) {
		TunnelledConnection conn = new TunnelledConnection(address, null);
		incoming_connections.add(conn);
		notifyIncomingConnection(new TunnelIdentifier(profile, new TunnelAddress(address.getHostID(), inet_address, local_address)));
	}
	
	private final TunnelledConnection getNextTunnel() {
		return (TunnelledConnection)incoming_connections.remove(0);
	}

	protected final AbstractConnection doAcceptConnection(ConnectionInterface connection_interface) {
		TunnelledConnection conn = getNextTunnel();
		conn.setConnectionInterface(connection_interface);
		conn.accept();
		return conn;
	}
	
	public final void rejectConnection() {
		getNextTunnel().close();
	}

	public final void connectionClosed() {
		open = false;
		notifyError(new ClosedChannelException());
	}

	public final void close() {
		if (open) {
			Network.getMatchmakingClient().unregisterTunnelledListener(this);
			open = false;
		}
	}
}
