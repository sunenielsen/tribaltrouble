package com.oddlabs.tt.audio;

import com.oddlabs.tt.Main;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.openal.AL;

public final strictfp class RefillerList {
	private final static int THREAD_SLEEP_MILLIS = 50;

	private boolean finished = false;
	private final Thread refill_thread;
	private final List players = new ArrayList();

	public final synchronized void registerQueuedPlayer(QueuedAudioPlayer q) {
		assert !players.contains(q);
		players.add(q);
		refill_thread.interrupt();
	}

	public final synchronized void removeQueuedPlayer(QueuedAudioPlayer q) {
		players.remove(q);
		assert !players.contains(q);
	}

	public RefillerList() {
		Refiller refiller = new Refiller();
		refill_thread = new Thread(refiller, "Refiller");
		refill_thread.setDaemon(true);
		refill_thread.start();
	}

	public final void destroy() {
		synchronized (this) {
			finished = true;
			players.clear();
			refill_thread.interrupt();
		}
		try {
			refill_thread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private class Refiller implements Runnable {
		public void run() {
			try {
				while (!finished) {
					synchronized (RefillerList.this) {
						if (AL.isCreated()) {
							for (int i = 0; i < players.size(); i++) {
								((QueuedAudioPlayer)players.get(i)).refill();
							}
						}
						while (players.size() == 0 && !finished) {
							try {
								RefillerList.this.wait();
							} catch (InterruptedException e) {
								// ignore
							}
						}
					}
					try {
						Thread.sleep(THREAD_SLEEP_MILLIS);
					} catch (InterruptedException e) {
						// ignore
					}
				}
			} catch (Throwable t) {
				Main.fail(t);
			}
		}
	}
}
