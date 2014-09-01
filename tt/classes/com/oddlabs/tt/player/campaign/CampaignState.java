package com.oddlabs.tt.player.campaign;

import java.io.Serializable;

public final strictfp class CampaignState implements Serializable {
	private final static long serialVersionUID = 1;

	public final static int RACE_VIKINGS = 0; // DON'T CHANGE! will ruin serializability
	public final static int RACE_NATIVES = 1;

	public final static int DIFFICULTY_EASY = 1;
	public final static int DIFFICULTY_NORMAL = 0; //Serializable defaults to 0
	public final static int DIFFICULTY_HARD = 2;

	public final static int ISLAND_AVAILABLE = 1;
	public final static int ISLAND_UNAVAILABLE = 2;
	public final static int ISLAND_COMPLETED = 3;
	public final static int ISLAND_SEMI_AVAILABLE = 4;
	public final static int ISLAND_HIDDEN = 5;

	private final int[] island_states;

	private int prev_island;
	private int current_island = -1;
	private int num_peons = 10;
	private int num_rock_warriors = 0;
	private int num_iron_warriors = 0;
	private int num_rubber_warriors = 0;
	private boolean has_rubber_weapons = false;
	private boolean has_magic0 = false;
	private boolean has_magic1 = false;

	private String name;
	private long date;
	private int race;
	private int difficulty;

	public CampaignState(int[] initial_states) {
		island_states = new int[initial_states.length];
		for (int i = 0; i < island_states.length; i++) {
			island_states[i] = initial_states[i];
		}
	}

	public final void setIslandState(int index, int state) {
		if (island_states[index] == ISLAND_SEMI_AVAILABLE && state == ISLAND_SEMI_AVAILABLE)
			island_states[index] = ISLAND_AVAILABLE;
		else if (island_states[index] != ISLAND_COMPLETED)
			island_states[index] = state;
	}

	public final int getIslandState(int index) {
/*		for (int i = 0; i < island_states.length; i++)
			island_states[i] = ISLAND_AVAILABLE;
		island_states[0] = ISLAND_COMPLETED;*/
		return island_states[index];
	}

	public final void setCurrentIsland(int current_island) {
		prev_island = this.current_island;
		this.current_island = current_island;
	}

	public final int getCurrentIsland() {
		return current_island;
	}

	public final void setPrevIsland(int prev_island) {
		this.prev_island = prev_island;
	}

	public final int getPrevIsland() {
		return prev_island;
	}

	public final void setNumPeons(int num_peons) {
		this.num_peons = num_peons;
	}

	public final int getNumPeons() {
		return num_peons;
	}

	public final void setNumRockWarriors(int num_rock_warriors) {
		this.num_rock_warriors = num_rock_warriors;
	}

	public final int getNumRockWarriors() {
		return num_rock_warriors;
	}

	public final void setNumIronWarriors(int num_iron_warriors) {
		this.num_iron_warriors = num_iron_warriors;
	}

	public final int getNumIronWarriors() {
		return num_iron_warriors;
	}

	public final void setNumRubberWarriors(int num_rubber_warriors) {
		this.num_rubber_warriors = num_rubber_warriors;
	}

	public final int getNumRubberWarriors() {
		return num_rubber_warriors;
	}

	public final void setHasRubberWeapons(boolean has_rubber_weapons) {
		this.has_rubber_weapons = has_rubber_weapons;
	}

	public final boolean hasRubberWeapons() {
		return has_rubber_weapons;
	}

	public final void setHasMagic0(boolean has_magic0) {
		this.has_magic0 = has_magic0;
	}

	public final boolean hasMagic0() {
		return has_magic0;
	}

	public final void setHasMagic1(boolean has_magic1) {
		this.has_magic1 = has_magic1;
	}

	public final boolean hasMagic1() {
		return has_magic1;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final long getDate() {
		return date;
	}

	public final void setDate(long date) {
		this.date = date;
	}

	public final void setRace(int race) {
		this.race = race;
	}
	
	public final int getRace() {
		return race;
	}

	public final void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}
	
	public final int getDifficulty() {
		return difficulty;
	}
}
