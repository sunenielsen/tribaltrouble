package com.oddlabs.regclient;

public strictfp interface RegistrationListener {
	public void registrationCompleted();
	public void registrationFailed(int reason, Exception e);
}
