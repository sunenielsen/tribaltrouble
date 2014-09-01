package com.oddlabs.loader;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

public final strictfp class SVNClassLoader extends URLClassLoader {
	private final File root;
	private final File lib_dir;

	public SVNClassLoader(URL[] urls, File root, File lib_dir) {
		super(urls);
		this.root = root;
		this.lib_dir = lib_dir;
	}

	private final String mapLibraryName(String name) {
		String os_name = System.getProperty("os.name");
		if (os_name.startsWith("Mac OS X"))
			return name + ".dylib";
		else if (os_name.startsWith("Windows"))
			return name + ".dll";
		else if (os_name.startsWith("Linux") || os_name.startsWith("FreeBSD"))
			return "lib" + name + ".so";
		else
			throw new RuntimeException("Unknown platform " + os_name);
	}
	
	protected final String findLibrary(String name) {
		File lib_root = new File(lib_dir, ".svn" + File.separator + "text-base");
		File[] libs = new File[]{new File(lib_root, System.mapLibraryName(name) + ".svn-base"), new File(lib_root, mapLibraryName(name) + ".svn-base")};
		for (int i = 0; i < libs.length; i++) {
			if (libs[i].exists() && libs[i].isFile())
				return libs[i].getAbsolutePath();
		}
		return super.findLibrary(name);
	}
	
	public final URL findResource(String name) {
		URL url = doFindResource(name);
		if (url != null)
			return url;
		else
			return super.findResource(name);
	}
	
	private final URL doFindResource(String name) {
		File path = root;
		StringTokenizer path_tokenizer = new StringTokenizer(name, "/");
		while (path_tokenizer.hasMoreTokens()) {
			String path_element = path_tokenizer.nextToken();
			if (!path_tokenizer.hasMoreTokens()) {
				path = new File(path, ".svn" + File.separator + "text-base" + File.separator +  path_element + ".svn-base");
				if (path.exists()) {
					try {
						return path.toURL();
					} catch (MalformedURLException e) {
						return null;
					}
				} else
					return null;
			} else
				path = new File(path, path_element);
		}
		return null;
	}

	protected final Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class result = findLoadedClass(name);
		if (result == null) {
			try {
				result = findClass(name);
			} catch (ClassNotFoundException e) {
				// Let any exception from the parent loadClass propagate
				result = getParent().loadClass(name);
			}
		}
		if (resolve)
			resolveClass(result);
		return result;
	}
	
	protected final Class findClass(String name) throws ClassNotFoundException {
		String url_class_name = "/" + name.replace('.', '/') + ".class";
		URL class_url = findResource(url_class_name);
		if (class_url == null)
			return super.findClass(name);
		try {
			URLConnection conn = class_url.openConnection();
			InputStream in = conn.getInputStream();
			int length = conn.getContentLength();
			byte[] buffer = new byte[length];
			while (length > 0) {
				int bytes_read = in.read(buffer, buffer.length - length, length);
				if (bytes_read == -1)
					throw new IOException("EOF reached");
				length -= bytes_read;
			}
			return defineClass(name, buffer, 0, buffer.length);
		} catch (IOException e) {
			throw new ClassNotFoundException("An Exception occurred while trying to load class " + name + ": ", e);
		}
	}
}
