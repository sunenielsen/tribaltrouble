package com.oddlabs.net;

import java.io.Serializable;
  
final strictfp class BlockingTask implements Task, Serializable {
	 final int id;
	 boolean cancelled;
	 TaskThread.TaskResult result; // Set by task thread

	 BlockingTask(int id) {
		 this.id = id;
	 }

	 public final void cancel() {
		 cancelled = true;
	 }
 }
