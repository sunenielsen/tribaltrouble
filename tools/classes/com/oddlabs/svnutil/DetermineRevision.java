package com.oddlabs.svnutil;

import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.io.*;

import java.io.*;

public final strictfp class DetermineRevision implements ISVNStatusHandler {
	private long latest_revision = -1;
	
	public void handleStatus(SVNStatus status) {
		if (status.getContentsStatus() == SVNStatusType.STATUS_EXTERNAL)
			return;
		if (status.getContentsStatus() == SVNStatusType.STATUS_MODIFIED || status.getPropertiesStatus() == SVNStatusType.STATUS_MODIFIED)
			System.err.println("WARNING: '" + status.getFile() + "' is locally modified!");
		long file_revision = status.getRevision().getNumber();
		latest_revision = Math.max(latest_revision, file_revision);
	}
	
	private DetermineRevision(File workspace_location, File revision_file) throws SVNException, IOException {
		SVNClientManager client_manager = SVNClientManager.newInstance();
		SVNStatusClient workspace = client_manager.getStatusClient();
		workspace.doStatus(workspace_location, true, false, true, false, this);
		if (latest_revision == -1)
			throw new RuntimeException("Unable to determine revision");
System.out.println("revision: " + latest_revision);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(revision_file));
		String version = Long.toString(latest_revision);
		out.writeObject(version);
		out.close();
	}
	
	public static void main(String[] args) throws SVNException, IOException {
		File workspace_location = new File(args[0]);
		File revision_file = new File(args[1]);
		new DetermineRevision(workspace_location, revision_file);
	}
}
