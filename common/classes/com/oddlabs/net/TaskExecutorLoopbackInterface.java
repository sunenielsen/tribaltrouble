package com.oddlabs.net;

public strictfp interface TaskExecutorLoopbackInterface {
	public void taskCompleted(Object result);
	public void taskFailed(Exception e);
}
