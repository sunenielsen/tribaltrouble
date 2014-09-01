package com.oddlabs.updater;

import java.io.Serializable;

public final strictfp class UpdateStatus implements Serializable {
	private final static long serialVersionUID = 3;

	public final static int LOG = 1;
	public final static int NO_UPDATES = 2;
	public final static int ERROR = 3;
	public final static int UPDATE_COMPLETE = 4;
	public final static int EOF = 5;

	// subtypes
	public final static int DELETING = 10;
	public final static int COPYING = 11;
	public final static int UPDATED = 12;
	public final static int UPDATING = 13;
	public final static int CHECKED = 14;
	public final static int INIT = 15;
	public final static int CHECKING = 16;
	public final static int COPIED = 17;
	public final static int NONE = 18;

	private final String message;
	private final Throwable exception;
	private final int kind;
	private final int sub_type;
	
	public UpdateStatus(Throwable t) {
		this(ERROR, NONE, null, t);
	}
	
	public UpdateStatus(int kind, String message) {
		this(kind, NONE, message);
	}

	public UpdateStatus(int kind, int sub_type, String message) {
		this(kind, sub_type, message, null);
	}
	
	public UpdateStatus(int kind, int sub_type, String message, Throwable t) {
		assert kind != LOG || sub_type != NONE;
		this.kind = kind;
		this.sub_type = sub_type;
		this.message = message;
		this.exception = t;
	}

	public int getSubType() {
		return sub_type;
	}
	
	public int getKind() {
		return kind;
	}
	
	public String getMessage() {
		return message;
	}

	public Throwable getException() {
		return exception;
	}
}
