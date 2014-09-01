package com.oddlabs.tt.model.behaviour;

public strictfp interface Behaviour {
	public int animate(float t);
	public boolean isBlocking();
	public void forceInterrupted();
}
