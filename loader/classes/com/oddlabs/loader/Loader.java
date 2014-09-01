package com.oddlabs.loader;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.List;
import java.util.ArrayList;

import java.lang.reflect.Method;

import com.oddlabs.updater.UpdateInfo;

public final strictfp class Loader {
	private final static File findGameDir(File root) {
		File[] datadirs = root.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().startsWith(UpdateInfo.DATA_DIR_PREFIX) &&
						file.isDirectory();
			}
		});
		if (datadirs == null || datadirs.length == 0)
			throw new RuntimeException("Cannot find game data in " + root);
		File newest_subdir = datadirs[0];
		for (int i = 1; i < datadirs.length; i++) {
			if (datadirs[i].lastModified() > newest_subdir.lastModified())
				newest_subdir = datadirs[i];
		}
		return newest_subdir;
	}

	public final static void main(String[] args) throws Exception {
		int arg_index = 0;
		String java_cmd = args[arg_index++];
		File classpath = new File(args[arg_index++]);
		String main_class_name = args[arg_index++];
		File game_dirs_root = new File("gamedata");
		File game_dir = findGameDir(game_dirs_root);
		System.err.println("LOADER: Starting from directory " + game_dir);
		File common_dir = new File(game_dir, UpdateInfo.COMMON_DIR_NAME);
		File native_dir = new File(game_dir, UpdateInfo.NATIVE_DIR_NAME);
		File svn_common_dir = new File(new File(common_dir, ".svn"), "text-base");
		File[] game_jars = svn_common_dir.listFiles(new JarFileFilter());
		List game_jar_urls = new ArrayList();
		for (int i = 0; i < game_jars.length; i++) {
			URL url = game_jars[i].toURL();
			game_jar_urls.add(url);
		}
		URL[] game_jar_urls_array = new URL[game_jar_urls.size()];
		game_jar_urls.toArray(game_jar_urls_array);
		ClassLoader class_loader = new SVNClassLoader(game_jar_urls_array, common_dir, native_dir);
		Class main_class = class_loader.loadClass(main_class_name);
		if (main_class == null)
			throw new RuntimeException("Could not locate class " + main_class_name);
		List bootstrap_args = new ArrayList();
		for (; arg_index < args.length; arg_index++)
			bootstrap_args.add(args[arg_index]);
		bootstrap_args.add("--bootstrap");
		bootstrap_args.add(java_cmd);
		bootstrap_args.add(classpath.getAbsolutePath());
		bootstrap_args.add(game_dir.getAbsolutePath());
		String[] bootstrap_args_array = new String[bootstrap_args.size()];
		bootstrap_args.toArray(bootstrap_args_array);
		Method main_method = main_class.getMethod("main", new Class[]{String[].class});
		main_method.invoke(null, new Object[]{bootstrap_args_array});
	}
}
