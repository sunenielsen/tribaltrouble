package com.oddlabs.tt.model.behaviour;

public strictfp abstract class Controller {
	private final static int MAX_TRIES = 1;
	private int[] give_up_counters;

	protected Controller(int num_states) {
		give_up_counters = new int[num_states];
	}

	public final void resetGiveUpCounters() {
		for (int i = 0; i < give_up_counters.length; i++)
			give_up_counters[i] = 0;
	}
	
	public final void resetGiveUpCounter(int state_index) {
		give_up_counters[state_index] = 0;
	}

	protected final boolean shouldGiveUp(int state_index) {
		if (give_up_counters[state_index] != MAX_TRIES)  {
			give_up_counters[state_index]++;
			return false;
		} else {
			return true;
		}
	}

	public String getKey() {
		return "" + getClass().hashCode();
	}

	public abstract void decide();
}
