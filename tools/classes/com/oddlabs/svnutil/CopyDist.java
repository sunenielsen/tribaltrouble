package com.oddlabs.svnutil;

/*import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.ws.fs.FSEntryFactory;*/

import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.io.*;

import com.oddlabs.util.FileUtils;

import java.io.*;

public final strictfp class CopyDist {
/*	static {
		FSEntryFactory.setup();
		SVNRepositoryFactoryImpl.setup();
	}
*/
	private final static void pruneDist(File dist, File svndist, SVNWCClient workspace) throws SVNException, IOException {
		File[] svndist_files = svndist.listFiles(new FileFilter() {
			public final boolean accept(File file) {
				return !file.getName().equals(".svn");
			}
		});
		for (int i = 0; i < svndist_files.length; i++) {
			File svndist_file = svndist_files[i];
			File dist_file = new File(dist, svndist_file.getName());
			if (!dist_file.exists()) {
				workspace.doDelete(svndist_file, false, false);
				System.out.println("NOTICE: Removing " + svndist_file);
			} else if (svndist_file.isDirectory()) {
				pruneDist(dist_file, svndist_file, workspace);
			}
		}
	}

	private final static void copyDist(File dist, File svndist, SVNWCClient workspace) throws SVNException, IOException {
		File[] dist_files = dist.listFiles();
		for (int i = 0; i < dist_files.length; i++) {
			File dist_file = dist_files[i];
			File svndist_file = new File(svndist, dist_file.getName());
			if (dist_file.isDirectory()) {
				if (!svndist_file.exists()) {
					workspace.doAdd(svndist_file, false, true, false, false);
					System.out.println("NOTICE: Added directory " + svndist_file);
				}
				copyDist(dist_file, svndist_file, workspace);
			} else if (dist_file.isFile()) {
				boolean existed = svndist_file.exists();
				FileUtils.copyFile(dist_file, svndist_file);
				if (!existed) {
					workspace.doAdd(svndist_file, false, false, false, false);
					System.out.println("NOTICE: Added regular file " + svndist_file);
				}
			}
		}
	}

	public final static void main(String[] args) throws SVNException, IOException {
		int arg_index = 0;
		File dist_dir = new File(args[arg_index++]);
		File svndist_dir = new File(args[arg_index++]);
		SVNClientManager client_manager = SVNClientManager.newInstance();
		SVNWCClient workspace = client_manager.getWCClient();
//		SVNWCClient workspace = SVNWorkspaceManager.createWorkspace("file", svndist_dir.getAbsolutePath());
		copyDist(dist_dir, svndist_dir, workspace);
		pruneDist(dist_dir, svndist_dir, workspace);
	}
}
