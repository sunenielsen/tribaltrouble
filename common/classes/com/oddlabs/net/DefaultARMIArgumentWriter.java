package com.oddlabs.net;

import java.io.*;
import com.oddlabs.util.ByteBufferOutputStream;

public strictfp class DefaultARMIArgumentWriter implements ARMIArgumentWriter {
	public void writeArgument(Class type, Object arg, ByteBufferOutputStream out) throws IOException {
		if (type.equals(char.class)) {
			char c = ((Character)arg).charValue();
			out.buffer().putChar(c);
		} else if (type.equals(byte.class)) {
			byte b = ((Byte)arg).byteValue();
			out.buffer().put(b);
		} else if (type.equals(short.class)) {
			short s = ((Short)arg).shortValue();
			out.buffer().putShort(s);
		} else if (type.equals(int.class)) {
			int integer = ((Integer)arg).intValue();
			out.buffer().putInt(integer);
		} else if (type.equals(long.class)) {
			long l = ((Long)arg).longValue();
			out.buffer().putLong(l);
		} else if (type.equals(float.class)) {
			float f = ((Float)arg).intValue();
			out.buffer().putFloat(f);
		} else if (type.equals(double.class)) {
			double d = ((Double)arg).doubleValue();
			out.buffer().putDouble(d);
		} else if (type.equals(boolean.class)) {
			boolean bool = ((Boolean)arg).booleanValue();
			byte val = bool ? (byte)1 : (byte)0;
			out.buffer().put(val);
		} else if (type.equals(HostSequenceID.class)) {
			HostSequenceID host_seq = (HostSequenceID)arg;
			out.buffer().putInt(host_seq.getHostID());
			out.buffer().putInt(host_seq.getSequenceID());
		} else if (type.equals(ARMIEvent.class)) {
			ARMIEvent event = (ARMIEvent)arg;
			out.buffer().putShort(event.getEventSize());
			event.write(out.buffer());
		} else {
			ObjectOutputStream obj_output_stream = new ObjectOutputStream(out);
			try {
				obj_output_stream.writeObject(arg);
			} finally {
				obj_output_stream.close();
			}
		}
	}
}
