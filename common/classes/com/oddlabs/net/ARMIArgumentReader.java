package com.oddlabs.net;

import java.io.*;

public strictfp interface ARMIArgumentReader {
	Object readArgument(Class type, ByteBufferInputStream in) throws IOException, ClassNotFoundException;
}
