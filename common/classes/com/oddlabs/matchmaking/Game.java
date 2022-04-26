package com.oddlabs.matchmaking;

import java.io.Serializable;

public final strictfp class Game implements Serializable {
	private final static long serialVersionUID = 3;

	public final static int SIZE_SMALL = 0;
	public final static int SIZE_MEDIUM = 1;
	public final static int SIZE_LARGE = 2;
	public final static int SIZE_HUGE = 3;

	public final static int TERRAIN_TYPE_NATIVE = 0;
	public final static int TERRAIN_TYPE_VIKING = 1;

	public final static int GAMESPEED_PAUSE = 0;
	public final static int GAMESPEED_SLOW = 1;
	public final static int GAMESPEED_NORMAL = 2;
	public final static int GAMESPEED_FAST = 3;
	public final static int GAMESPEED_LUDICROUS = 4;
	
	public final static int MIN_LENGTH = 2;
	public final static int MAX_LENGTH = 30;
	
	private final String game_name;
	private final byte size;
	private final byte terrain_type;
	private final byte hills;
	private final byte trees;
	private final byte supplies;
	private final boolean rated;
	private final byte gamespeed;
	private final String mapcode;
	private final float random_start_pos;
	private final int max_unit_count;
	
	private int database_id;

	public Game(String game_name, byte size, byte terrain_type, byte hills, byte trees, byte supplies, boolean rated, byte gamespeed, String mapcode, float random_start_pos, int max_unit_count) {
		this.game_name = game_name;
		this.size = size;
		this.terrain_type = terrain_type;
		this.hills = hills;
		this.trees = trees;
		this.supplies = supplies;
		this.rated = rated;
		this.gamespeed = gamespeed;
		this.mapcode = mapcode;
		this.random_start_pos = random_start_pos;
		this.max_unit_count = max_unit_count;
		assert isValid(): game_name.length();
	}

	public final boolean isValid() {
		return game_name != null && game_name.length() >= MIN_LENGTH && game_name.length() <= MAX_LENGTH;
	}

	public final boolean isValidGuestGame() {
		return true;
	}
	
	public final String getName() {
		return game_name;
	}

	public final byte getSize() {
		return size;
	}

	public final byte getTerrainType() {
		return terrain_type;
	}
	
	public final byte getHills() {
		return hills;
	}
	
	public final byte getTrees() {
		return trees;
	}
	
	public final byte getSupplies() {
		return supplies;
	}

	public final boolean isRated() {
		return rated;
	}

	public final byte getGamespeed() {
		return gamespeed;
	}
	
	public final String getMapcode() {
		return mapcode;
	}

	public final float getRandomStartPos() {
		return random_start_pos;
	}

	public final int getMaxUnitCount() {
		return max_unit_count;
	}

	public final void setDatabaseID(int database_id) {
		this.database_id = database_id;
	}

	public final int getDatabaseID() {
		return database_id;
	}
}
