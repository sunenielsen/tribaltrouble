package com.oddlabs.tt.net;

import java.io.*;
import com.oddlabs.net.DefaultARMIArgumentWriter;
import com.oddlabs.util.ByteBufferOutputStream;

final strictfp class GameArgumentWriter extends DefaultARMIArgumentWriter {
	private final DistributableTable distributable_table;

	GameArgumentWriter(DistributableTable table) {
		this.distributable_table = table;
	}

	public final void writeArgument(Class type, Object arg, ByteBufferOutputStream out) throws IOException {
		if (Distributable.class.isAssignableFrom(type)) {
			int name = distributable_table.getName((Distributable)arg);
			out.buffer().putInt(name);
		} else if (Distributable[].class.isAssignableFrom(type)) {
			Distributable[] distributables = (Distributable[])arg;
			out.buffer().putShort((short)distributables.length);
			for (int j = 0; j < distributables.length; j++) {
				int name = distributable_table.getName(distributables[j]);
				out.buffer().putInt(name);
			}
		} else {
			super.writeArgument(type, arg, out);
		}
	}
}
