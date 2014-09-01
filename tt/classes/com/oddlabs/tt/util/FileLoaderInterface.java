package com.oddlabs.tt.util;

import java.io.IOException;
import java.io.File;

public strictfp interface FileLoaderInterface {
	public void error(IOException e);
	public void data(byte[] data, int num_bytes_read, boolean eof);
	public void newFile(File file, long length);
}
