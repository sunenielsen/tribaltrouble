package com.oddlabs.updater;

import java.io.File;
import java.io.IOException;

import org.tmatesoft.svn.core.internal.ws.fs.*;

import org.tmatesoft.svn.core.ISVNEntryFactory;
import org.tmatesoft.svn.core.ISVNRootEntry;
import org.tmatesoft.svn.core.SVNWorkspaceManager;
import org.tmatesoft.svn.core.io.SVNException;

public class UpdateEntryFactory extends SVNWorkspaceManager implements ISVNEntryFactory {
	public static void setup() {
		SVNWorkspaceManager.registerRootFactory("update", new UpdateEntryFactory());
	}

	public ISVNRootEntry createEntry(String location) throws SVNException {
		if (location == null) {
			throw new SVNException("invalid location: " + location);
		}
		File dir = new File(location);
		if (dir.exists() && !dir.isDirectory()) {
			throw new SVNException(location + " is not a directory");
		}
		try {
			FSAdminArea area = new FSAdminArea(dir);
			return new UpdateRootEntry(area, dir.getCanonicalPath(), null);
		} catch (IOException e) {
			throw new SVNException(e);
		}
	}
}
