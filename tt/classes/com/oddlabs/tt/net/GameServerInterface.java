package com.oddlabs.tt.net;

public strictfp interface GameServerInterface {
	void resetSlotState(int slot, boolean open);
	void setPlayerSlot(int slot, int type, int race, int team, boolean ready, int ai_difficulty);
	void startServer();
	void chat(String chat);
}
