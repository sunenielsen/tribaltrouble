package com.oddlabs.router;

import com.oddlabs.net.ARMIEvent;

public strictfp interface GameInterface {
	void relayEventTo(int client_id, ARMIEvent event);
	void relayEvent(ARMIEvent event);
	void relayGameStateEvent(ARMIEvent event);
	void checksum(int checksum);
}
