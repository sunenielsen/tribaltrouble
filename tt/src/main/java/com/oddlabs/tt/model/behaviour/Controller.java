package com.oddlabs.tt.model.behaviour;

import org.jspecify.annotations.NonNull;

import java.util.Arrays;

public abstract class Controller {
    private static final int MAX_TRIES = 1;
    private final int @NonNull [] give_up_counters;

    protected Controller(int num_states) {
        give_up_counters = new int[num_states];
    }

    public final void resetGiveUpCounters() {
        Arrays.fill(give_up_counters, 0);
    }

    public final void resetGiveUpCounter(int state_index) {
        give_up_counters[state_index] = 0;
    }

    protected final boolean shouldGiveUp(int state_index) {
        if (give_up_counters[state_index] != MAX_TRIES) {
            give_up_counters[state_index]++;
            return false;
        } else {
            return true;
        }
    }

    public @NonNull String getKey() {
        return getClass().getName();
    }

    public abstract void decide();
}
