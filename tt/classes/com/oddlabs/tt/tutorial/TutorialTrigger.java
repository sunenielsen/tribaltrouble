package com.oddlabs.tt.tutorial;

public abstract strictfp class TutorialTrigger {
	private final float check_interval;
	private final float after_done_time;
	private final String text_key;
	private final Object[] format_args;

	public TutorialTrigger(float check_interval, float after_done_time, String text_key) {
		this(check_interval, after_done_time, text_key, null);
	}

	public TutorialTrigger(float check_interval, float after_done_time, String text_key, Object[] format_args) {
		this.check_interval = check_interval;
		this.after_done_time = after_done_time;
		this.text_key = text_key;
		this.format_args = format_args;
	}

	final float getCheckInterval() {
		return check_interval;
	}

	final float getAfterDoneTime() {
		return after_done_time;
	}

	final String getTextKey() {
		return text_key;
	}

	final Object[] getFormatArgs() {
		return format_args;
	}

	protected abstract void run(Tutorial tutorial);
}
