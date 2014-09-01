package com.oddlabs.osutil;

public final strictfp class URLAssociation {
	final String description;
	final String scheme;
	final String icon_name;

	public URLAssociation(String scheme, String description, String icon_name) {
		this.description = description;
		this.scheme = scheme;
		this.icon_name = icon_name;
	}
}
