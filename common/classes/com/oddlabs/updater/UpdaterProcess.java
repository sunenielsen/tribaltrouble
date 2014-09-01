package com.oddlabs.updater;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.tmatesoft.svn.core.ISVNStatusHandler;
import org.tmatesoft.svn.core.ISVNWorkspace;
import org.tmatesoft.svn.core.SVNStatus;
import org.tmatesoft.svn.core.SVNWorkspaceAdapter;
import org.tmatesoft.svn.core.SVNWorkspaceManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.io.SVNException;
import org.tmatesoft.svn.core.io.SVNLogEntry;

import com.oddlabs.util.FileUtils;

final strictfp class Status {
	private final long revision;
	private final boolean has_updates;

	public Status(long revision, boolean has_updates) {
		this.revision = revision;
		this.has_updates = has_updates;
	}
	
	public final long getRevision() {
		return revision;
	}

	public final boolean hasUpdates() {
		return has_updates;
	}
}

final strictfp class LogHandler implements ISVNLogEntryHandler {
	private final static int MAX_LOG_LENGTH = 4096;
	
	private final StringBuffer log = new StringBuffer();

	public LogHandler(String header) {
		log.append(header + "\n\n");
	}

	public final void handleLogEntry(SVNLogEntry entry) {
		if (log.length() >= MAX_LOG_LENGTH)
			return;
		log.append("Update " + entry.getRevision() + ":\n");
		log.append(entry.getMessage() + "\n");
	}

	public final String getLog() {
		return log.toString();
	}
}

public final strictfp class UpdaterProcess {
	public final static String TEMP_DATA_DIR = "temp";
	public final static String FS_TYPE = "update";

	static {
		DAVRepositoryFactory.setup();
//		FSEntryFactory.setup();
		UpdateEntryFactory.setup();
	}

	private static class HasUpdatesHandler implements ISVNStatusHandler {
		private final ObjectOutputStream status_out;
		private boolean has_updates;
		private boolean revert_needed;
		private long latest_revision = 0;

		public HasUpdatesHandler(ObjectOutputStream status_out) {
			this.status_out = status_out;
		}

		public final boolean hasUpdates() {
			return has_updates;
		}

		public final boolean isRevertNeeded() {
			return revert_needed;
		}
		
		public final long getLatestRevision() {
			return latest_revision;
		}
		
		public final void handleStatus(String path, SVNStatus status) {
			has_updates = has_updates || status.getRepositoryContentsStatus() != SVNStatus.NOT_MODIFIED ||
							status.getRepositoryPropertiesStatus() != SVNStatus.NOT_MODIFIED;
			revert_needed = revert_needed || (status.getContentsStatus() == SVNStatus.MISSING) || (status.getPropertiesStatus() == SVNStatus.MISSING);
			latest_revision = StrictMath.max(latest_revision, status.getWorkingCopyRevision());
			try {
				status_out.writeObject(new UpdateStatus(UpdateStatus.LOG, UpdateStatus.CHECKED, path));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private final static void revert(ObjectOutputStream status_out, File workspace_file) throws SVNException, IOException {
		ISVNWorkspace workspace = SVNWorkspaceManager.createWorkspace(FS_TYPE, workspace_file.getAbsolutePath());
		status_out.writeObject(new UpdateStatus(UpdateStatus.LOG, UpdateStatus.INIT, workspace_file.getName()));
		workspace.revert("", true);
	}
	
	private final static Status revertAndCheck(ObjectOutputStream status_out, File workspace_file) throws SVNException, IOException {
		ISVNWorkspace workspace = SVNWorkspaceManager.createWorkspace(FS_TYPE, workspace_file.getAbsolutePath());
		status_out.writeObject(new UpdateStatus(UpdateStatus.LOG, UpdateStatus.CHECKING, workspace_file.getName()));
		HasUpdatesHandler handler = new HasUpdatesHandler(status_out);
		workspace.status("", true, handler, true, true, false);
		if (handler.isRevertNeeded()) {
			revert(status_out, workspace_file);
			// Check again
			handler = new HasUpdatesHandler(status_out);
			workspace.status("", true, handler, true, true, false);
		}
		return new Status(handler.getLatestRevision(), handler.hasUpdates());
	}
	
	public final static void main(String[] args) {
		new UpdaterProcess(args);
	}

	private UpdaterProcess(String[] args) {
		try {
			final ObjectOutputStream status_out = new ObjectOutputStream(System.out);
			try {
				String workspace_path_name = args[0];
				String root_name = args[1];
				File workspace_file = new File(workspace_path_name);
				File root = new File(root_name);
				File common_dir = new File(workspace_file, UpdateInfo.COMMON_DIR_NAME);
				File native_dir = new File(workspace_file, UpdateInfo.NATIVE_DIR_NAME);
				Status common_status = revertAndCheck(status_out, common_dir);
				Status native_status = revertAndCheck(status_out, native_dir);
				if (common_status.hasUpdates() || native_status.hasUpdates()) {
					deleteOldDataDirs(status_out, root, workspace_file);
					File temp_data_dir = new File(root, TEMP_DATA_DIR);
					if (temp_data_dir.exists()) {
						status_out.writeObject(new UpdateStatus(UpdateStatus.LOG, UpdateStatus.DELETING, temp_data_dir.getName()));
						deleteFile(true, temp_data_dir);
					}
					status_out.writeObject(new UpdateStatus(UpdateStatus.LOG, UpdateStatus.COPYING, temp_data_dir.getName()));
					copy(status_out, true, workspace_file, temp_data_dir);
					File common_copy = new File(temp_data_dir, UpdateInfo.COMMON_DIR_NAME);
					File native_copy = new File(temp_data_dir, UpdateInfo.NATIVE_DIR_NAME);
					long common_revision = update(status_out, common_copy);
					update(status_out, native_copy);
					long previous_revision = StrictMath.min(common_status.getRevision() + 1, common_revision);
					String log = log(common_copy, previous_revision, common_revision);
					status_out.writeObject(new UpdateStatus(UpdateStatus.UPDATE_COMPLETE, log));
				} else
					status_out.writeObject(new UpdateStatus(UpdateStatus.NO_UPDATES, null));
			} catch (Throwable t) {
				status_out.writeObject(new UpdateStatus(t));
			}
		} catch (IOException e) {
			// there's nothing we can do, really
			throw new RuntimeException(e);
		}
	}

	private final static String log(File workspace_directory, long rev_start, long rev_end) throws SVNException {
		ISVNWorkspace workspace = SVNWorkspaceManager.createWorkspace(FS_TYPE, workspace_directory.getAbsolutePath());
		LogHandler handler = new LogHandler("Changes:");
		workspace.log("", rev_start, rev_end, false, true, handler);
		return handler.getLog();
	}

	private final static long update(final ObjectOutputStream status_out, File workspace_directory) throws IOException, SVNException {
		ISVNWorkspace workspace = SVNWorkspaceManager.createWorkspace(FS_TYPE, workspace_directory.getAbsolutePath());
		workspace.addWorkspaceListener(new SVNWorkspaceAdapter() {
			public final void updated(String path, int content_status, int properties_status, long revision) {
				try {
					status_out.writeObject(new UpdateStatus(UpdateStatus.LOG, UpdateStatus.UPDATED, path));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		status_out.writeObject(new UpdateStatus(UpdateStatus.LOG, UpdateStatus.UPDATING, null));
		return workspace.update(ISVNWorkspace.HEAD);
	}

	private final static void deleteOldDataDirs(final ObjectOutputStream status_out, File root, final File current_data_dir) throws IOException {
		File[] datadirs = root.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().startsWith(UpdateInfo.DATA_DIR_PREFIX) &&
						file.isDirectory() && !file.equals(current_data_dir);
			}
		});
		for (int i = 0; i < datadirs.length; i++) {
			status_out.writeObject(new UpdateStatus(UpdateStatus.LOG, UpdateStatus.DELETING, datadirs[i].getName()));
			deleteFile(true, datadirs[i]);
		}
	}

	private final static void deleteFile(boolean recursive, File file) throws IOException {
		if (file.isDirectory()) {
			if (recursive) {
				File[] subfiles = file.listFiles();
				for (int i = 0; i < subfiles.length; i++)
					deleteFile(recursive, subfiles[i]);
			}
		}
		if (!file.delete())
			throw new IOException("Could not delete file " + file);
	}
	
	private final static void copy(ObjectOutputStream status_out, boolean recursive, File src, File dst) throws IOException {
		if (!dst.mkdir())
			throw new IOException("Could not create directory " + dst);
/*		File[] files = src.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory() ||
					(file.getName().endsWith(".svn-base") && 
					file.getParentFile() != null && file.getParentFile().getName().equals("text-base") &&
					file.getParentFile().getParentFile() != null && file.getParentFile().getParentFile().getName().equals(".svn"));
			}
		});*/
		File[] files = src.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			File dst_file = new File(dst, file.getName());
			if (file.isDirectory()) {
				if (recursive)
					copy(status_out, recursive, file, dst_file);
			} else if (file.isFile()) {
				FileUtils.copyFile(file, dst_file);
				status_out.writeObject(new UpdateStatus(UpdateStatus.LOG, UpdateStatus.COPIED, file.getName()));
			}
		}
	}
}
