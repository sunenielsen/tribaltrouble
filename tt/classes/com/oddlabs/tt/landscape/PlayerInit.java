package com.oddlabs.tt.landscape;

public final strictfp class PlayerInit {
	final int ai_difficulty;
	final PlayerInitAction init_action;

	public PlayerInit(int ai_difficulty, PlayerInitAction init_action) {
		this.ai_difficulty = ai_difficulty;
		this.init_action = init_action;
	}
}
