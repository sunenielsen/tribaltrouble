package com.oddlabs.matchserver;

import com.oddlabs.util.KeyManager;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.net.ARMIEvent;
import com.oddlabs.net.HostSequenceID;
import com.oddlabs.net.ConnectionInterface;
import com.oddlabs.net.SecureConnection;
import com.oddlabs.net.IllegalARMIEventException;
import com.oddlabs.net.ARMIInterfaceMethods;
import com.oddlabs.matchmaking.MatchmakingServerLoginInterface;
import com.oddlabs.matchmaking.MatchmakingClientInterface;
import com.oddlabs.matchmaking.TunnelAddress;
import com.oddlabs.matchmaking.Login;
import com.oddlabs.matchmaking.LoginDetails;
import com.oddlabs.registration.RegistrationInfo;
import com.oddlabs.registration.RegistrationKey;

import java.io.IOException;
import java.net.InetAddress;
import java.security.SignedObject;
import java.security.GeneralSecurityException;

public final strictfp class Authenticator implements MatchmakingServerLoginInterface, ConnectionInterface {
	private static int guest_postfix = 1;
	
	private final SecureConnection conn;
	private final MatchmakingClientInterface client_interface;
	private final MatchmakingServer server;
	private final InetAddress remote_address;
	private final ARMIInterfaceMethods interface_methods = new ARMIInterfaceMethods(MatchmakingServerLoginInterface.class);
	private final int host_id;
	private InetAddress local_remote_address;

	public Authenticator(MatchmakingServer server, SecureConnection conn, InetAddress remote_address, int host_id) {
		this.conn = conn;
		this.server = server;
		this.remote_address = remote_address;
		this.client_interface = (MatchmakingClientInterface)ARMIEvent.createProxy(conn, MatchmakingClientInterface.class);
		this.host_id = host_id;
		conn.setConnectionInterface(this);
	}

	public final void handle(Object sender, ARMIEvent event) {
		try {
			event.execute(interface_methods, this);
		} catch (IllegalARMIEventException e) {
			error(e);
		}
	}

	public final void writeBufferDrained(AbstractConnection conn) {
	}

	public final void error(AbstractConnection conn, IOException e) {
		error(e);
	}
	
	private final void error(Exception e) {
		close();
		MatchmakingServer.getLogger().warning("Exception e = " + e);
	}

	public final void connected(AbstractConnection conn) {
	}

	public final void setLocalRemoteAddress(InetAddress local_remote_address) {
		this.local_remote_address = local_remote_address;
	}

	public final static void checkUsername(String name) throws InvalidUsernameException {
		int min_username_length = DBInterface.getSettingsInt("min_username_length");
		if (name.length() < min_username_length)
			throw new InvalidUsernameException(MatchmakingClientInterface.USERNAME_ERROR_TOO_SHORT);

		int max_username_length = DBInterface.getSettingsInt("max_username_length");
		if (name.length() > max_username_length)
			throw new InvalidUsernameException(MatchmakingClientInterface.USERNAME_ERROR_TOO_LONG);

		String allowed_chars = DBInterface.getSetting("allowed_chars");
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (allowed_chars.indexOf(c) == -1)
				throw new InvalidUsernameException(MatchmakingClientInterface.USERNAME_ERROR_INVALID_CHARACTERS);
		}
	}

	public final void createUser(Login login, LoginDetails login_details, SignedObject reg_key, int revision) {
		String reg_key_encoded = checkKey(reg_key);
		if (login == null || !login.isValid() || reg_key_encoded == null) {
			close();
			return;
		}
		
		// check revision
		if (!revisionOK(revision)) {
			return;
		}
		
		try {
			checkUsername(login.getUsername());
		} catch (InvalidUsernameException e) {
			client_interface.loginError(e.getErrorCode());
			return;
		}
		
		if (login_details == null || !login_details.isValid()) {
			client_interface.loginError(MatchmakingClientInterface.USER_ERROR_INVALID_EMAIL);
			return;
		}
		
		try {
			if (DBInterface.getRegKeyUsername(reg_key_encoded) != null) {
				client_interface.loginError(MatchmakingClientInterface.USERNAME_ERROR_TOO_MANY);
				return;
			}
		} catch (IllegalArgumentException e) {
			close();
			return;
		}

		if (DBInterface.usernameExists(login.getUsername())) {
			client_interface.loginError(MatchmakingClientInterface.USERNAME_ERROR_ALREADY_EXISTS);
			return;
		}
		
		DBInterface.createUser(login, login_details, reg_key_encoded);
		MatchmakingServer.getLogger().info("Created user " + login.getUsername() + " with email address " + login_details.getEmail() + " with key " + reg_key_encoded);
		doLogin(login.getUsername(), reg_key_encoded, revision);
	}

	public final void login(Login login, SignedObject reg_key, int revision) {
		String reg_key_encoded = checkKey(reg_key);
		if (login == null || !login.isValid() || reg_key_encoded == null) {
			close();
			return;
		}
		
		if (!revisionOK(revision)) {
			return;
		}
		
		try {
			DBInterface.getRegKeyUsername(reg_key_encoded);
		} catch (IllegalArgumentException e) {
			close();
			return;
		}
		String username = login.getUsername().trim();
		if (!DBInterface.queryUser(username, login.getPasswordDigest())) {
			client_interface.loginError(MatchmakingClientInterface.USER_ERROR_NO_SUCH_USER);
			return;
		}
		
		doLogin(username, reg_key_encoded, revision);
	}

	public final void loginAsGuest(int revision) {
		if (revisionOK(revision)) {
			String username = "Guest" + guest_postfix++; 
			doLogin(username, null, revision);
		}
	}

	private final boolean revisionOK(int revision) {
		if (revision < DBInterface.getSettingsInt("revision")) {
			client_interface.loginError(MatchmakingClientInterface.USER_ERROR_VERSION_TOO_OLD);
			System.out.println("revision = " + revision + " | DBInterface.getSettingsInt(revision) = " + DBInterface.getSettingsInt("revision"));
			return false;
		} else
			return true;
	}

	private final String checkKey(SignedObject reg_key) {
		String reg_code = null;
		if (reg_key != null) {
			try {
				if (RegistrationKey.verify(server.getPublicRegKey(), reg_key)) {
					// This cast should not fail, because we signed it and the signature checked out ok
					RegistrationInfo reg_info = (RegistrationInfo)reg_key.getObject();
					reg_code = RegistrationKey.encode(reg_info.getKey());
				}
			} catch (Exception e) {
				MatchmakingServer.getLogger().warning("Could not verify signature because of: " + e.getMessage());
			}
		}
		return reg_code;
	}

	private final void doLogin(String username, String reg_key_encoded, int revision) {
		if (local_remote_address != null) {
			client_interface.loginOK(username, new TunnelAddress(getHostID(), remote_address, local_remote_address));
			server.loginClient(remote_address, local_remote_address, username, conn.getWrappedConnectionAndShutdown(), reg_key_encoded, revision, host_id);
		} else {
			error(new IllegalStateException("Client didnt set local_remote_address"));
		}
	}
	
	public final int getHostID() {
		return host_id;
	}

	private final void close() {
		conn.close();
	}
}
