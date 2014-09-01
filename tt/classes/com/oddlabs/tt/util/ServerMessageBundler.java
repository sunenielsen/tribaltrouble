package com.oddlabs.tt.util;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.registration.RegistrationKeyFormatException;

import java.util.ResourceBundle;

public final strictfp class ServerMessageBundler {
	private final static ResourceBundle bundle = ResourceBundle.getBundle(ServerMessageBundler.class.getName());
	
	public final static String getSizeString(int index) {
		switch (index) {
			case Game.SIZE_SMALL:
				return Utils.getBundleString(bundle, "size_small");
			case Game.SIZE_MEDIUM:
				return Utils.getBundleString(bundle, "size_medium");
			case Game.SIZE_LARGE:
				return Utils.getBundleString(bundle, "size_large");
			default:
				throw new RuntimeException();
		}
	}

	public final static String getTerrainTypeString(int index) {
		switch (index) {
			case Game.TERRAIN_TYPE_NATIVE:
				return Utils.getBundleString(bundle, "terrain_type_native");
			case Game.TERRAIN_TYPE_VIKING:
				return Utils.getBundleString(bundle, "terrain_type_viking");
			default:
				throw new RuntimeException();
		}
	}

	public final static String getRatedString(boolean rated) {
		if (rated)
			return Utils.getBundleString(bundle, "rated_yes");
		else
			return Utils.getBundleString(bundle, "rated_no");
	}

	public final static String getGamespeedString(int index) {
		switch (index) {
			case Game.GAMESPEED_PAUSE:
				return Utils.getBundleString(bundle, "gamespeed_pause");
			case Game.GAMESPEED_SLOW:
				return Utils.getBundleString(bundle, "gamespeed_slow");
			case Game.GAMESPEED_NORMAL:
				return Utils.getBundleString(bundle, "gamespeed_normal");
			case Game.GAMESPEED_FAST:
				return Utils.getBundleString(bundle, "gamespeed_fast");
			case Game.GAMESPEED_LUDICROUS:
				return Utils.getBundleString(bundle, "gamespeed_ludicrous");
			default:
				throw new RuntimeException();
		}
	}

	public final static String getHillsString(int index) {
		return (10*index) + "%";
	}

	public final static String getTreesString(int index) {
		return (10*index) + "%";
	}

	public final static String getSuppliesString(int index) {
		return (10*index) + "%";
	}

	public final static String getRegistrationKeyFormatExceptionMessage(RegistrationKeyFormatException e) {
		switch (e.getType()) {
			case RegistrationKeyFormatException.TYPE_INVALID_CHAR:
				return Utils.getBundleString(bundle, "invalid_char", new Object[]{new Character(e.getInvalidChar())});
			case RegistrationKeyFormatException.TYPE_INVALID_LENGTH:
				return Utils.getBundleString(bundle, "invalid_length", new Object[]{new Integer(e.getStrippedLength())});
			case RegistrationKeyFormatException.TYPE_INVALID_KEY:
				return Utils.getBundleString(bundle, "invalid_key");
			default:
				throw new RuntimeException();
		}
	}
}
