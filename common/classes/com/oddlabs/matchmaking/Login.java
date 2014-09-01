package com.oddlabs.matchmaking;

import java.io.Serializable;

import com.oddlabs.util.CryptUtils;

public final strictfp class Login implements Serializable {
	private final static long serialVersionUID = 1;

	private final String username;
	private final String password_digest;

	public Login(String username, String password_digest) {
		this.username = username;
		this.password_digest = password_digest;
	}

	public final boolean equals(Object other) {
		if (!(other instanceof Login))
			return false;
		Login other_login = (Login)other;
		return other_login.getUsername().equals(username) && other_login.getPasswordDigest().equals(password_digest);
	}

	public final int hashCode() {
		return username.hashCode();
	}

	public final boolean isValid() {
		return username != null && password_digest != null;
	}
	
	public final String getUsername() {
		return username;
	}

	public final String getPasswordDigest() {
		return password_digest;
	}
}
