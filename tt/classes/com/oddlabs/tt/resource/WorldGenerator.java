package com.oddlabs.tt.resource;

import java.io.Serializable;

public strictfp interface WorldGenerator extends Serializable {
	WorldInfo generate(int num_players, int initial_unit_count, float random_start_pos);
	int getTerrainType();
	int getMetersPerWorld();
}
