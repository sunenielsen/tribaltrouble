package com.oddlabs.net;

public final strictfp class MonotoneTimeManager implements TimeManager {
	private final TimeManager source;
	private long last_time;

	public MonotoneTimeManager(TimeManager source) {
		this.source = source;
		this.last_time = source.getMillis();
	}

	public final long getMillis() {
		long new_time = source.getMillis();
		this.last_time = StrictMath.max(last_time, new_time);
		return last_time;
	}
}
