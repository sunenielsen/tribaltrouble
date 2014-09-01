package com.oddlabs.net;

public strictfp class IllegalARMIEventException extends Exception {
	private static final long serialVersionUID = 6874182030169648695L;

	public IllegalARMIEventException(String message) {
		super(message);
	}

	public IllegalARMIEventException(Throwable e) {
		super(e);
	}
}
