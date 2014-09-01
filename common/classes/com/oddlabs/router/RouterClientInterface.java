package com.oddlabs.router;

import com.oddlabs.net.ARMIEvent;

public strictfp interface RouterClientInterface {
	void start();
	void heartbeat(int tick);
	void receiveEvent(int client_id, ARMIEvent event);
	void receiveGameStateEvent(int client_id, int tick, ARMIEvent event);
	void playerDisconnected(int client_id, boolean checksum_error);
}
