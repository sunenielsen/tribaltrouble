package com.oddlabs.matchservlet;

public final strictfp class Validation {
	public final static int MAX_EMAIL_LENGTH = 60;

	public static boolean isValidEmail(String email) {
		return email != null && email.length() <= MAX_EMAIL_LENGTH && email.matches(".+@.+\\..+");
	}
}
