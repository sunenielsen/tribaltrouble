package com.oddlabs.util;

import java.io.*;
import java.nio.channels.FileChannel;

public final strictfp class FileUtils {
	public final static void copyFile(File src, File dst) throws IOException {
		FileChannel src_channel = new FileInputStream(src).getChannel();
		FileChannel dst_channel = new FileOutputStream(dst).getChannel();
		src_channel.transferTo(0, Long.MAX_VALUE, dst_channel);
		src_channel.close();
		dst_channel.close();
	}
}
