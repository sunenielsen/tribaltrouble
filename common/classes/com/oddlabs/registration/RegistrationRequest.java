package com.oddlabs.registration;

import java.io.Serializable;

public final strictfp class RegistrationRequest implements Serializable {
	private final static long serialVersionUID = 1;

	private final long key;
	private final String affiliate;

	// demo restriction vars
	private final int demo_restrictions_version;
	private final boolean use_time_limit;
	private final int max_time;
	private final boolean force_quit;
	private final int max_num_games;
	
	public RegistrationRequest(long key, String affiliate) {
		this(key, affiliate, 0, false, 0, false, 0);
	}
	
	public RegistrationRequest(long key, String affiliate, int demo_restrictions_version, boolean use_time_limit, int max_time, boolean force_quit, int max_num_games) {
		this.key = key;
		this.affiliate = affiliate;
		this.demo_restrictions_version = demo_restrictions_version;
		this.use_time_limit = use_time_limit;
		this.max_time = max_time;
		this.force_quit = force_quit;
		this.max_num_games = max_num_games;
	}

	public final long getKey() {
		return key;
	}

	public final String getAffiliate() {
		return affiliate;
	}

	public final int getDemoRestrictionsVersion() {
		return demo_restrictions_version;
	}

	public final boolean getUseTimeLimit() {
		return use_time_limit;
	}

	public final int getMaxTime() {
		return max_time;
	}

	public final boolean getForceQuit() {
		return force_quit;
	}

	public final int getMaxNumGames() {
		return max_num_games;
	}
}
