package com.oddlabs.tt.net;

import java.io.*;
import java.lang.reflect.Array;
import com.oddlabs.net.DefaultARMIArgumentReader;
import com.oddlabs.net.ByteBufferInputStream;

final strictfp class GameArgumentReader extends DefaultARMIArgumentReader {
	private final DistributableTable distributable_table;

	GameArgumentReader(DistributableTable table) {
		this.distributable_table = table;
	}

	public final Object readArgument(Class type, ByteBufferInputStream in) throws IOException, ClassNotFoundException {
		if (Distributable.class.isAssignableFrom(type)) {
			int name = in.buffer().getInt();
			return distributable_table.getDistributable(name);
		} else if (Distributable[].class.isAssignableFrom(type)) {
			short length = in.buffer().getShort();
			Distributable[] distributables = (Distributable[])Array.newInstance(type.getComponentType(), length);
			for (int j = 0; j < distributables.length; j++) {
				int name = in.buffer().getInt();
				distributables[j] = distributable_table.getDistributable(name);
			}
			return distributables;
		} else {
			return super.readArgument(type, in);
		}
	}
}
