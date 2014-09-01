package com.oddlabs.tt.net;

public strictfp interface StallHandler {
	void stopStall();
	void processStall(int tick);
	void peerhubFailed();
}
