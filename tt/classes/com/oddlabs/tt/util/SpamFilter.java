package com.oddlabs.tt.util;

public final strictfp class SpamFilter {
	public final static String scan(String string) {
		string = string.replaceAll("\\s+", " ");
		string = string.replaceAll("\\.{3,}", "...");
		string = string.replaceAll("\\?+", "?");
		string = string.replaceAll("\\!+", "!");
		return string;
	}
}
