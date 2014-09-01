package com.oddlabs.tt.util;

import java.io.File;

public strictfp interface FileListerListener {
	public void newFiles(File[] files);
}
