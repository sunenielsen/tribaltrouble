package com.oddlabs.tt.util;

import java.io.File;
import java.io.IOException;

public strictfp interface FileLoaderListener {
	public void error(IOException e);
	public void data(byte[] data, int num_bytes, boolean eof);
	public void newFile(File filename, long length);
}
