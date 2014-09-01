package com.oddlabs.converter;

import java.io.File;

public strictfp class ObjectInfo {
	private final File file;

	public ObjectInfo(File file) {
		this.file = file;
	}

	public final File getFile() {
		return file;
	}
}
