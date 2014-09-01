package com.oddlabs.updater;

import java.io.File;

import org.tmatesoft.svn.core.ISVNEntry;
import org.tmatesoft.svn.core.ISVNRootEntry;
import org.tmatesoft.svn.core.internal.ws.fs.FSAdminArea;
import org.tmatesoft.svn.core.internal.ws.fs.FSRootEntry;
import org.tmatesoft.svn.util.PathUtil;

public class UpdateRootEntry extends FSRootEntry implements ISVNRootEntry {
	public UpdateRootEntry(FSAdminArea area, String id, String location) {
		super(area, id, location);
	}

	private final String convertEntryPath(ISVNEntry entry) {
		File converted_path = new File(getAdminArea(entry), "text-base" + File.separator + entry.getName() + ".svn-base");
		return converted_path.getPath();
	}

	private final File getAdminArea(ISVNEntry entry) {
		File adminDir;
		if (entry.isDirectory()) {
			adminDir = new File(entry.getPath() + "/.svn");
		} else {
			adminDir = new File(PathUtil.removeTail(entry.getPath()) + "/.svn");
		}
		return adminDir;
	}

	public File getWorkingCopyFile(ISVNEntry entry) {
		if (entry.isDirectory())
			return new File(getID(), entry.getPath());
		else
			return new File(getID(), convertEntryPath(entry));
	}
}
