package com.oddlabs.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.oddlabs.util.ByteBufferOutputStream;

public final strictfp class ARMIEvent implements Serializable {
	private final static long serialVersionUID = 1;

	private final static ByteBufferOutputStream static_byte_stream = new ByteBufferOutputStream(false);
	private final static short HEADER_SIZE = 1;
	private final static ARMIArgumentWriter default_writer = new DefaultARMIArgumentWriter();
	private final static ARMIArgumentReader default_reader = new DefaultARMIArgumentReader();

	private final byte method_id;
	private final byte[] command_stream;

	public static final Object createProxy(ARMIEventWriter broker, Class armi_interface) {
		return createProxy(broker, default_writer, armi_interface);
	}

	public static final Object createProxy(ARMIEventWriter broker, ARMIArgumentWriter writer, Class armi_interface) {
		ARMIInterfaceMethods armi_interface_methods = new ARMIInterfaceMethods(armi_interface);
		ARMIInvocationHandler handler = new ARMIInvocationHandler(broker, writer, armi_interface_methods);
		return Proxy.newProxyInstance(ARMIEvent.class.getClassLoader(), new Class[]{armi_interface}, handler);
	}

	public final short getEventSize() {
		int command_stream_length = command_stream != null ? command_stream.length : 0;
		return (short)(HEADER_SIZE + command_stream_length);
	}

	public final void write(ByteBuffer buffer) {
		buffer.put(method_id);
		if (command_stream != null)
			buffer.put(command_stream);
	}

	public final static ARMIEvent read(ByteBuffer buffer, short size) {
		byte method_id = buffer.get();
		int stream_length = size - HEADER_SIZE;
		byte[] command_stream;
		if (stream_length > 0) {
			command_stream = new byte[stream_length];
			buffer.get(command_stream);
		} else
			command_stream = null;
		return new ARMIEvent(method_id, command_stream);
	}

	private final static byte[] createByteArrayFromCommand(ARMIArgumentWriter writer, Class[] method_parameter_types, Object[] args) {
		if (args != null) { 
			try {
				static_byte_stream.reset();
				for (int i = 0; i < args.length; i++) {
					Object arg = args[i];
					Class type = method_parameter_types[i];
					writer.writeArgument(type, arg, static_byte_stream);
				}
				return static_byte_stream.toByteArray();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else
			return null;
	}
	
	public ARMIEvent(ARMIArgumentWriter writer, Class[] method_parameter_types, byte method_id, Object[] args) {
		this(method_id, createByteArrayFromCommand(writer, method_parameter_types, args));
	}

	private ARMIEvent(byte method_id, byte[] command_stream) {
		this.method_id = method_id;
		this.command_stream = command_stream;
	}

	private Object[] parseArgs(ARMIArgumentReader reader, Method method) throws IOException, ClassNotFoundException {
		Class[] parameter_types = method.getParameterTypes();
		int num_params = parameter_types.length;
		if (num_params == 0)
			return null;
		Object[] args;
		args = new Object[num_params];
		ByteBufferInputStream byte_stream = new ByteBufferInputStream(command_stream);
		for (int i = 0; i < args.length; i++) {
			Class type = parameter_types[i];
			args[i] = reader.readArgument(type, byte_stream);
		}
		return args;
	}

	public final void execute(ARMIInterfaceMethods interface_methods, Object instance) throws IllegalARMIEventException {
		execute(interface_methods, default_reader, instance);
	}

	public final void execute(ARMIInterfaceMethods interface_methods, ARMIArgumentReader reader, Object instance) throws IllegalARMIEventException {
		Method method;
		try {
			method = interface_methods.getMethod(method_id);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalARMIEventException(e);
		}
		assert instance != null;
		Object[] args;
		try {
			args = parseArgs(reader, method);
		} catch (Throwable e) {
			throw new IllegalARMIEventException(e);
		}
		interface_methods.invoke(instance, method, args);
	}
}
