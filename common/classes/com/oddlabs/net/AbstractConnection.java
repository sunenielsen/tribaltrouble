package com.oddlabs.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract strictfp class AbstractConnection implements ARMIEventWriter {
	private final List event_backlog = new ArrayList();
	private ConnectionInterface connection_interface;
	private IOException error_flag;
	private boolean connected_flag;
	private boolean connected_signaled;

	public final void setConnectionInterface(ConnectionInterface connection_interface) {
		this.connection_interface = connection_interface;
		if (connection_interface != null) {
			if (connected_flag)
				signalConnected();
			for (int i = 0; i < event_backlog.size(); i++)
				connection_interface.handle(this, (ARMIEvent)event_backlog.get(i));
			event_backlog.clear();
			if (error_flag != null)
				connection_interface.error(this, error_flag);
		}
	}

	protected final void writeBufferDrained() {
		if (connection_interface != null)
			connection_interface.writeBufferDrained(this);
	}

	protected final ConnectionInterface getConnectionInterface() {
		return connection_interface;
	}

	private final void signalConnected() {
		if (!connected_signaled) {
			connection_interface.connected(this);
			connected_signaled = true;
		}
	}

	public final void close() {
		connected_flag = false;
		doClose();
		connection_interface = null;
	}
	
	protected abstract void doClose();

	public final void receiveEvent(ARMIEvent event) {
		if (connection_interface != null) {
			connection_interface.handle(this, event);
		} else
			event_backlog.add(event);
	}

	public final boolean isConnected() {
		return connected_flag;
	}

	protected final void notifyConnected() {
		if (!connected_flag) {
			connected_flag = true;
			if (connection_interface != null)
				signalConnected();
		}
	}
	
	protected final void notifyError(IOException e) {
		if (error_flag == null) {
			error_flag = e;
			if (connection_interface != null)
				connection_interface.error(this, e);
		}
		close();
	}
}
