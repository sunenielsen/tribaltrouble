package com.oddlabs.net;

strictfp interface ConnectionPeerInterface {
	public void ping();
	public void receiveEvent(ARMIEvent event);
}
