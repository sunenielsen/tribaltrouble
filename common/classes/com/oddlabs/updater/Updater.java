package com.oddlabs.updater;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.oddlabs.event.Deterministic;

public final strictfp class Updater {
	private final static int THREAD_SLEEP = 10;
	
	private final UpdaterStatusThread status;
	private final UpdateInfo update_info;
	private final UpdateHandler handler;
	private final Deterministic deterministic;
	private boolean done;
	
	private class UpdaterStatusThread implements Runnable {
		private final Thread thread;
		private final Process updater_process;
		private final List status_list = new ArrayList();

		public UpdaterStatusThread() {
			this.thread = new Thread(this);
			thread.setName("Update thread");
			File workspace_path = update_info.getDataDir();
			File root = workspace_path.getParentFile();
			String executable = update_info.getJavaCommand();
			File relative_path = new File(System.getProperty("user.dir"), executable);
			if (relative_path.exists())
				executable = relative_path.getAbsolutePath();
System.out.println("executable = " + executable);
			String[] cmd_line = {
				executable,
				"-cp", update_info.getClasspath(), 
				"-Xmx128000000",
				"com.oddlabs.loader.Loader",
				update_info.getJavaCommand(),
				update_info.getClasspath(),
				"com.oddlabs.updater.UpdaterProcess", 
				workspace_path.getAbsolutePath(), root.getAbsolutePath()};
System.out.println("workspace_path.getAbsolutePath() = " + workspace_path.getAbsolutePath() + " | root.getAbsolutePath() = " + root.getAbsolutePath());
			try {
				this.updater_process = Runtime.getRuntime().exec(cmd_line);
				System.out.println("Started update process");
				thread.start();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public final List getStatusList() {
			synchronized (status_list) {
				return new ArrayList(status_list);
			}
		}

		private final void addStatus(UpdateStatus status) {
			synchronized (status_list) {
				status_list.add(status);
			}
		}

		public final void cancel() {
			updater_process.destroy();
			try {
				thread.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		public final void run() {
			InputStream process_in = updater_process.getInputStream();
			try {
				ObjectInputStream status_in = new ObjectInputStream(process_in);
				while (true) {
					try {
						UpdateStatus status = (UpdateStatus)status_in.readObject();
						if (status.getKind() == UpdateStatus.UPDATE_COMPLETE) {
							File root = update_info.getDataDir().getParentFile();
							File temp_data = new File(root, UpdaterProcess.TEMP_DATA_DIR);
							File data_dir = new File(root, UpdateInfo.DATA_DIR_PREFIX + System.currentTimeMillis());
							if (temp_data.renameTo(data_dir))
								addStatus(status);
							else
								throw new IOException("Could not rename temporary update directory");
						} else
							addStatus(status);
					} catch (EOFException e) {
						break;
					}
				}
			} catch (Exception e) {
				addStatus(new UpdateStatus(e));
			}
			addStatus(new UpdateStatus(UpdateStatus.EOF, null));
			try {
				updater_process.waitFor();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private final void processStatus(UpdateStatus status) {
		if (done)
			return;
		switch (status.getKind()) {
			case UpdateStatus.LOG:
				handler.statusLog(status.getSubType(), status.getMessage());
				break;
			case UpdateStatus.NO_UPDATES:
				handler.statusNoUpdates();
				done = true;
				break;
			case UpdateStatus.ERROR:
				handler.statusError(status.getException());
				done = true;
				break;
			case UpdateStatus.UPDATE_COMPLETE:
				handler.statusUpdated(status.getMessage());
				done = true;
				break;
			case UpdateStatus.EOF:
				handler.statusError(new IOException("The update process exited prematurely"));
				done = true;
				break;
			default:
				throw new RuntimeException("Unknown update status kind: " + status.getKind());
		}
	}
	
	public final void updateStatus() {
		List status_list = (List)deterministic.log(deterministic.isPlayback() ? null : status.getStatusList());
		while (status_list.size() > 0) {
			UpdateStatus status = (UpdateStatus)status_list.remove(0);
			processStatus(status);
		}
		try {
			Thread.sleep(THREAD_SLEEP);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public final void cancel() {
		if (!deterministic.isPlayback())
			if (!done)
				status.cancel();
	}
	
	public Updater(Deterministic deterministic, UpdateInfo update_info, UpdateHandler handler) {
		this.deterministic = deterministic;
		this.handler = handler;
		this.update_info = update_info;
		if (!deterministic.isPlayback())
			this.status = new UpdaterStatusThread();
		else
			this.status = null;
	}
}
