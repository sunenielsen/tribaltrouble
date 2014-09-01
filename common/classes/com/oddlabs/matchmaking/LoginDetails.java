package com.oddlabs.matchmaking;

import java.io.Serializable;

import com.oddlabs.util.Utils;

public final strictfp class LoginDetails implements Serializable {
	private final static long serialVersionUID = 1;

	public final static int MAX_EMAIL_LENGTH = 60;

	private final String email;

	public LoginDetails(String email) {
		this.email = email;
	}

	public final boolean equals(Object other) {
		if (!(other instanceof LoginDetails))
			return false;
		LoginDetails other_login = (LoginDetails)other;
		return other_login.getEmail().equals(email);
	}

	public final int hashCode() {
		return email.hashCode();
	}

	public final boolean isValid() {
		return email != null && email.length() <= MAX_EMAIL_LENGTH && email.matches(Utils.EMAIL_PATTERN);
	}
	
	public final String getEmail() {
		return email;
	}
}
