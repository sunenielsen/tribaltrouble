package com.oddlabs.matchmaking;

import java.net.InetAddress;
import java.security.SignedObject;

public strictfp interface MatchmakingServerLoginInterface {
	public void setLocalRemoteAddress(InetAddress local_remote_address);
	public void login(Login login, SignedObject reg_key, int revision);
	public void loginAsGuest(int revision);
	public void createUser(Login login, LoginDetails login_details, SignedObject reg_key, int revision);
}
