package com.oddlabs.net;

import java.io.IOException;

public strictfp interface ConnectionListenerInterface {
	public void error(AbstractConnectionListener listener, IOException e);
	public void incomingConnection(AbstractConnectionListener listener, Object address);
}
