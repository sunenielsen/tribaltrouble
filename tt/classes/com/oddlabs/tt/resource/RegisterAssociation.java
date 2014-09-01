package com.oddlabs.tt.resource;

import com.oddlabs.osutil.OSUtil;
import com.oddlabs.osutil.MacOSXUtil;
import com.oddlabs.osutil.Association;
import com.oddlabs.osutil.URLAssociation;

import org.lwjgl.LWJGLUtil;

public final strictfp class RegisterAssociation {
	public static void main(String[] args) {
		register(new MacOSXUtil());
	}

	public static void register() {
		OSUtil util = OSUtil.create();
		register(util);
	}

	private static void register(OSUtil util) {
		String game_name = "Tribal Trouble";
		util.registerAssociation(game_name, new Association(
					"ttkey",
					"application/x-oddlabs-tt-key", "key"));
		util.registerAssociation(game_name, new Association(
					"ttgame",
					"aplication/x-oddlabs-tt-game", "island"));
		util.registerURLScheme(game_name, new URLAssociation("ttgame", game_name + " Game", "island"));
	}
}
