package com.oddlabs.updater;

import java.io.Serializable;
import java.io.File;

public final strictfp class UpdateInfo implements Serializable {
	private final static long serialVersionUID = 1;

	public final static String DATA_DIR_PREFIX = "data-";
	public final static String COMMON_DIR_NAME = "common";
	public final static String NATIVE_DIR_NAME = "native";

	private final String java_cmd;
	private final String classpath;
	private final File data_dir;

	public UpdateInfo(String java_cmd, String classpath, File data_dir) {
		this.java_cmd = java_cmd;
		this.classpath = classpath;
		this.data_dir = data_dir;
	}

	public final String getJavaCommand() {
		return java_cmd;
	}

	public final String getClasspath() {
		return classpath;
	}

	public final File getDataDir() {
		return data_dir;
	}
}
