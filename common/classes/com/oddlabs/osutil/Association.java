package com.oddlabs.osutil;

public final strictfp class Association {
	final String extension;
	final String mime_type;
	final String icon_name;

	public Association(String extension, String mime_type, String icon_name) {
		this.extension = extension;
		this.mime_type = mime_type;
		this.icon_name = icon_name;
	}
}
