package com.oddlabs.net;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public final strictfp class ARMIInterfaceMethods {
	private final Class armi_interface;
	private final Method[] methods;

	public ARMIInterfaceMethods(Class armi_interface) {
		assert armi_interface.isInterface();
		this.armi_interface = armi_interface;
		this.methods = armi_interface.getMethods();
		Arrays.sort(methods, new MethodComparator());
		for (int i = 0; i < methods.length; i++)
			assert isLegal(methods[i]);
	}

	private final boolean isLegal(Method method) {
		return method.getReturnType().equals(void.class) && method.getExceptionTypes().length == 0;
	}
	
	final boolean isInstance(Object instance) {
		return armi_interface.isInstance(instance);
	}

	final Class getInterfaceClass() {
		return armi_interface;
	}
	
	final void invoke(Object instance, Method method, Object[] args) throws IllegalARMIEventException {
		try {
			method.invoke(instance, args);
		} catch (IllegalAccessException e) {
			throw new IllegalARMIEventException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new IllegalARMIEventException(e);
		}
	}

	final Method getMethod(byte index) {
		return methods[index];
	}

	final byte getMethodIndex(Method method) {
		for (byte i = 0; i < methods.length; i++)
			if (methods[i].equals(method))
				return i;
		throw new RuntimeException("Unknown method: " + method);
	}
}
