package com.oddlabs.net;

import java.io.*;
import java.lang.reflect.Array;

public strictfp class DefaultARMIArgumentReader implements ARMIArgumentReader {
	public Object readArgument(Class type, ByteBufferInputStream in) throws IOException, ClassNotFoundException {
		if (type.equals(char.class)) {
			return new Character(in.buffer().getChar());
		} else if (type.equals(byte.class)) {
			return new Byte(in.buffer().get());
		} else if (type.equals(short.class)) {
			return new Short(in.buffer().getShort());
		} else if (type.equals(int.class)) {
			return new Integer(in.buffer().getInt());
		} else if (type.equals(long.class)) {
			return new Long(in.buffer().getLong());
		} else if (type.equals(float.class)) {
			return new Float(in.buffer().getFloat());
		} else if (type.equals(double.class)) {
			return new Double(in.buffer().getDouble());
		} else if (type.equals(boolean.class)) {
			return new Boolean(in.buffer().get() != 0);
		} else if (type.equals(HostSequenceID.class)) {
			int host_id = in.buffer().getInt();
			int seq_id = in.buffer().getInt();
			return new HostSequenceID(host_id, seq_id);
		} else if (type.equals(ARMIEvent.class)) {
			short event_size = in.buffer().getShort();
			return ARMIEvent.read(in.buffer(), event_size);
		} else {
			ObjectInputStream input_stream = new ObjectInputStream(in);
			try {
				return input_stream.readObject();
			} finally {
				input_stream.close();
			}
		}
	}
}
