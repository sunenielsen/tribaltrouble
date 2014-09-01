package com.oddlabs.tt.landscape;

import com.oddlabs.tt.trigger.GameOverTrigger;

public final strictfp class WorldParameters {
	private final String map_code;
	private final int initial_unit_count;
	private final int max_unit_count;
	private final int initial_game_speed;

	public WorldParameters(int initial_game_speed, String map_code, int initial_unit_count, int max_unit_count) {
		this.map_code = map_code;
		this.initial_unit_count = initial_unit_count;
		this.max_unit_count = max_unit_count;
		this.initial_game_speed = initial_game_speed;
	}

	public final String getMapcode() {
		return map_code;
	}

	public final int getInitialUnitCount() {
		return initial_unit_count;
	}

	public final int getMaxUnitCount() {
		return max_unit_count;
	}

	public final int getInitialGameSpeed() {
		return initial_game_speed;
	}
}
