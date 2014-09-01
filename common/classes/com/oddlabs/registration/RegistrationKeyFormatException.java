package com.oddlabs.registration;

public final strictfp class RegistrationKeyFormatException extends NumberFormatException {
	private static final long serialVersionUID = 3673901824484153336L;

	public final static int TYPE_INVALID_CHAR = 0;
	public final static int TYPE_INVALID_LENGTH = 1;
	public final static int TYPE_INVALID_KEY = 2;

	private final int type;

	private int stripped_length;
	private char invalid_char;

	public RegistrationKeyFormatException(int type) {
		this.type = type;
	}

	public RegistrationKeyFormatException(int type, char invalid_char) {
		this(type);
		this.invalid_char = invalid_char;
	}

	public RegistrationKeyFormatException(int type, int stripped_length) {
		this(type);
		this.stripped_length = stripped_length;
	}

	public final int getType() {
		return type;
	}

	public final char getInvalidChar() {
		return invalid_char;
	}

	public final int getStrippedLength() {
		return stripped_length;
	}
}
