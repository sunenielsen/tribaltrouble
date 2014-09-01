package com.oddlabs.tt.net;

public strictfp interface PeerHubInterface {
	public void chat(String text, boolean team);
	public void beacon(float x, float y);
}
