package com.oddlabs.router;

import java.io.Serializable;

public final strictfp class SessionID implements Serializable {
	private final static long serialVersionUID = 1;

	private final long session_id;

	public SessionID(long session_id) {
		this.session_id = session_id;
	}

	public final boolean equals(Object other) {
		return ((SessionID)other).session_id == session_id;
	}

	public final int hashCode() {
		return (int)session_id;
	}

	public final String toString() {
		return "(SessionID: session_id = " + session_id + ")";
	}
}
