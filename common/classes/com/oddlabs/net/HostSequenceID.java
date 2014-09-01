package com.oddlabs.net;

import java.io.Serializable;

public final strictfp class HostSequenceID implements Serializable {
	private static final long serialVersionUID = -8670168662221748395L;

	private final int host_id;
	private final int seq_id;

	public HostSequenceID(int host_id, int seq_id) {
		this.host_id = host_id;
		this.seq_id = seq_id;
	}

	public final int getHostID() {
		return host_id;
	}

	public final int getSequenceID() {
		return seq_id;
	}

	public final int hashCode() {
		return host_id ^ seq_id;
	}

	public final String toString() {
		return host_id + " " + seq_id;
	}

	public final boolean equals(Object other) {
		if (!(other instanceof HostSequenceID))
			return false;
		HostSequenceID other_id = (HostSequenceID)other;
		return host_id == other_id.host_id && seq_id == other_id.seq_id;
	}
}
