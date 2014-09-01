package com.oddlabs.util;

import java.lang.reflect.*;

public final strictfp class WindowsRegistryInterface {
	public final static String queryRegistrationKey(String root, String subkey, String value) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException, InvocationTargetException {
		Class org_lwjgl_opengl_WindowsRegistry = Class.forName("org.lwjgl.opengl.WindowsRegistry");
		Field root_key_enum_field = org_lwjgl_opengl_WindowsRegistry.getDeclaredField(root);
		root_key_enum_field.setAccessible(true);
		int root_key_enum = root_key_enum_field.getInt(null);
		Method queryRegistrationKey_method = org_lwjgl_opengl_WindowsRegistry.getDeclaredMethod("queryRegistrationKey", new Class[]{int.class, String.class, String.class});
		queryRegistrationKey_method.setAccessible(true);
//		queryRegistrationKey_method.setAccessible(true);
		Object result = queryRegistrationKey_method.invoke(null, new Object[]{new Integer(root_key_enum), subkey, value});
		return (String)result;
	}
}
