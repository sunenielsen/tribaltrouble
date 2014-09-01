package com.oddlabs.updater;

public strictfp interface UpdateHandler {
	public void statusLog(int subtype, String message);
	public void statusError(Throwable exception);
	public void statusUpdated(String changelog);
	public void statusNoUpdates();
}
