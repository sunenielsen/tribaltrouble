package com.oddlabs.tt.resource;

import java.net.*;

import com.oddlabs.util.Utils;

public abstract strictfp class File implements ResourceDescriptor {
	private final URL url;

	protected File(URL url) {
		this.url = url;
		if (url == null)
			throw new NullPointerException();
	}

	protected File(String location) {
		this(Utils.makeURL(location));
	}

	public final URL getURL() {
		return url;
	}

	public String toString() {
		return url.toString();
	}

	public final int hashCode() {
		return url.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof File))
			return false;
		File other = (File)o;
		return url.equals(other.url);
	}
}
