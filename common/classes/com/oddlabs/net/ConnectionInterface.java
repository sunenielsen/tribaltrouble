package com.oddlabs.net;

import java.io.IOException;

public strictfp interface ConnectionInterface extends ARMIEventBroker {
	void error(AbstractConnection conn, IOException e);
	void connected(AbstractConnection conn);
	void writeBufferDrained(AbstractConnection conn);
}
