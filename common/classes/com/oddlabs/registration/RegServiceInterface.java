package com.oddlabs.registration;

public strictfp interface RegServiceInterface {
	public final static String PRIVATE_KEY_FILE = "private_reg_key";
	public final static String PUBLIC_KEY_FILE = "public_reg_key";
	public final static String KEY_ALGORITHM = "RSA";
	public final static String SIGN_ALGORITHM = "SHA1WithRSA";
	public final static int REGSERVICE_PORT = 33215;

	public void register(RegistrationRequest reg_request);
}
