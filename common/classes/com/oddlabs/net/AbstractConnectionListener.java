package com.oddlabs.net;

import java.io.IOException;

public abstract strictfp class AbstractConnectionListener {
	private final ConnectionListenerInterface listener_interface;
	
	protected AbstractConnectionListener(ConnectionListenerInterface connection_listener) {
		this.listener_interface = connection_listener;
	}
	
	public abstract void close();

	public AbstractConnection acceptConnection(ConnectionInterface connection_interface) {
		return doAcceptConnection(connection_interface);
	}
	
	protected abstract AbstractConnection doAcceptConnection(ConnectionInterface conn_interface);

	public abstract void rejectConnection();
	
	protected final void notifyIncomingConnection(Object address) {
		listener_interface.incomingConnection(this, address);
	}

	protected final void notifyError(IOException e) {
		close();
		listener_interface.error(this, e);
	}
}
