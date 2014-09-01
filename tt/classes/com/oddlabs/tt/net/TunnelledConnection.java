package com.oddlabs.tt.net;

import java.io.IOException;

import com.oddlabs.net.ARMIEvent;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.net.ConnectionInterface;
import com.oddlabs.net.HostSequenceID;

public final strictfp class TunnelledConnection extends AbstractConnection {
	private final HostSequenceID address;
	private boolean open = true;
	
	public TunnelledConnection(HostSequenceID address, ConnectionInterface conn_interface) {
		setConnectionInterface(conn_interface);
		this.address = address;
		Network.getMatchmakingClient().registerTunnel(this.address, this);
		notifyConnected();
	}

	public TunnelledConnection(int address, ConnectionInterface conn_interface) {
		setConnectionInterface(conn_interface);
		this.address = Network.getMatchmakingClient().registerTunnel(address, this);
	}
	
	public final void tunnelClosed() {
		open = false;
		notifyError(new IOException("Connection closed"));
	}
	
	public final void connected() {
		notifyConnected();
	}

	public final void accept() {
		Network.getMatchmakingClient().getInterface().acceptTunnel(address);
	}

	public final void handle(ARMIEvent event) {
		Network.getMatchmakingClient().getInterface().routeEvent(address, event);
		writeBufferDrained();
	}

	public final HostSequenceID getAddress() {
		return address;
	}

	protected final void doClose() {
		if (open) {
			Network.getMatchmakingClient().unregisterTunnel(address, this);
			open = false;
		}
	}
}
