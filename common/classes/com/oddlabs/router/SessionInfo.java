package com.oddlabs.router;

import java.io.Serializable;

public strictfp class SessionInfo implements Serializable {
	private final static long serialVersionUID = 1;

	public final int milliseconds_per_heartbeat;
	public final int num_participants;

	public SessionInfo(int num_participants, int milliseconds_per_heartbeat) {
		this.num_participants = num_participants;
		this.milliseconds_per_heartbeat = milliseconds_per_heartbeat;
	}

	public final boolean equals(Object other) {
		return ((SessionInfo)other).num_participants == num_participants;
	}

	public final int hashCode() {
		return num_participants;
	}

	public final String toString() {
		return "(SessionInfo: num_participants = " + num_participants + ")";
	}
}
