package com.oddlabs.net;

import java.util.*;
import java.io.Serializable;

import com.oddlabs.event.Deterministic;

public final strictfp class TaskThread {
	private final Map id_to_callable = new HashMap();
	private final List tasks = new ArrayList();
	private final List finished_tasks = new ArrayList();
	private final Object lock = new Object();
	private final Runnable notification_action;
	private int current_id = 0;
	private Thread thread;
	private volatile boolean finished;

	private final Deterministic deterministic;

	public TaskThread(Deterministic deterministic, Runnable notification_action) {
		this.deterministic = deterministic;
		this.notification_action = notification_action;
	}

	static strictfp interface TaskResult extends Serializable {
		void deliverResult(TaskExecutorLoopbackInterface callback);
	}

	final static strictfp class TaskFailed implements TaskResult {
		private final Exception result;

		TaskFailed(Exception e) {
			this.result = e;
		}

		public final void deliverResult(TaskExecutorLoopbackInterface callback) {
			callback.taskFailed(result);
		}
	}

	final static strictfp class TaskSucceeded implements TaskResult {
		private final Object result;

		TaskSucceeded(Object result) {
			this.result = result;
		}

		public final void deliverResult(TaskExecutorLoopbackInterface callback) {
			callback.taskCompleted(result);
		}
	}

	private void processTasks() {
		while (!finished) {
			BlockingTask task;
			Callable callable;
			synchronized (lock) {
				while (tasks.isEmpty()) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						// ignore
					}
				}
				task = (BlockingTask)tasks.get(0);
				callable = lookupCallable(task);
			}
			TaskResult result;
			try {
				Object callable_result = callable.call();
				result = new TaskSucceeded(callable_result);
			} catch (Exception e) {
				result = new TaskFailed(e);
			}
			synchronized (lock) {
				task.result = result;
				tasks.remove(0);
				finished_tasks.add(task);
			}
		}
		if (notification_action != null)
			notification_action.run();
	}

	public final Deterministic getDeterministic() {
		return deterministic;
	}
	
	public final Task addTask(Callable callable) {
		BlockingTask task;
		synchronized (lock) {
			int task_id = current_id++;
			id_to_callable.put(new Integer(task_id), callable);
			task = new BlockingTask(task_id);
			tasks.add(task);
			lock.notify();
		}
		if (!deterministic.isPlayback() && thread == null) {
			this.thread = new Thread(new Runnable() {
				public final void run() {
					processTasks();
				}
			});
			this.thread.setName("Task executor thread");
			this.thread.setDaemon(true);
			this.thread.start();
		}
		return task;
	}

	public final void poll() {
		while (true) {
			BlockingTask task;
			Callable callable;
			synchronized (lock) {
				if (!deterministic.log(!finished_tasks.isEmpty())) {
					// Check for cancelled task blocking thread
					if (tasks.size() > 0) {
						BlockingTask current_task = (BlockingTask)tasks.get(0);
						if (current_task.cancelled && thread != null)
							thread.interrupt();
					}
					return;
				}
				task = (BlockingTask)deterministic.log(deterministic.isPlayback() ? null : finished_tasks.remove(0));
				callable = lookupCallable(task);
			}
			if (!task.cancelled)
				task.result.deliverResult(callable);
		}
	}

	private Callable lookupCallable(BlockingTask task) {
		return (Callable)id_to_callable.get(new Integer(task.id));
	}

	public final void close() {
		finished = true;
		if (!deterministic.isPlayback())
			thread.interrupt();
	}
}
