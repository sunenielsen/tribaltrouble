package com.oddlabs.osutil;

import org.lwjgl.LWJGLUtil;

public abstract strictfp class OSUtil {
	public static OSUtil create() {
		int platform = LWJGLUtil.getPlatform();
		switch (platform) {
			case LWJGLUtil.PLATFORM_MACOSX:
				return new MacOSXUtil();
			case LWJGLUtil.PLATFORM_LINUX:
			case LWJGLUtil.PLATFORM_WINDOWS:
			default:
				throw new RuntimeException("Unsupported platform: " + platform);
		}
	}

	public abstract void registerAssociation(String game_name, Association association);
	public abstract void registerURLScheme(String game_name, URLAssociation association);
}
