package com.oddlabs.net;

import java.io.*;
import com.oddlabs.util.ByteBufferOutputStream;

public strictfp interface ARMIArgumentWriter {
	void writeArgument(Class type, Object arg, ByteBufferOutputStream out) throws IOException;
}
