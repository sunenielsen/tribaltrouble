package com.oddlabs.net;

public strictfp interface Callable extends TaskExecutorLoopbackInterface {
	/*
	 * This method is called by instances of the AbstractTaskExecutor
	 * subclasses. It is threaded  and, is not deterministic, 
	 * so it must _not_ have any side effects!
	 */
	public Object call() throws Exception;
}
