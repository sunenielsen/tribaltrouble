package com.oddlabs.util;

public strictfp interface DeterministicSerializerLoopbackInterface {
	public void saveSucceeded();
	public void loadSucceeded(Object object);
	public void failed(Exception e);
}
