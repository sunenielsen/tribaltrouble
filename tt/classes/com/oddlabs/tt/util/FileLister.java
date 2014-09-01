package com.oddlabs.tt.util;

import com.oddlabs.tt.event.LocalEventQueue;
import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public final strictfp class FileLister implements FileListerInterface {
	private final FileListerListener listener;

	public FileLister(File dir, String pattern, FileListerListener listener) {
		this.listener = listener;
		newFiles((File[])LocalEventQueue.getQueue().getDeterministic().log(dir.listFiles(new PatternFilenameFilter(pattern))));
	}

	public final void newFiles(File[] new_files) {
		listener.newFiles(new_files);
	}

	private final strictfp class PatternFilenameFilter implements FilenameFilter {
		private final String pattern;

		public PatternFilenameFilter(String pattern) {
			this.pattern = pattern;
		}

		public final boolean accept(File dir, String name) {
			return Pattern.matches(pattern, name);
		}
	}
}
