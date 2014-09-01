package com.oddlabs.loader;

import java.io.FileFilter;
import java.io.File;

public final strictfp class JarFileFilter implements FileFilter {
	public final boolean accept(File file) {
		return file.isFile() && file.getName().endsWith(".jar.svn-base");
	}
}
