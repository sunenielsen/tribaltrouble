package com.oddlabs.matchmaking;

import java.net.InetAddress;
import com.oddlabs.net.ARMIEvent;
import com.oddlabs.net.HostSequenceID;

public strictfp interface MatchmakingClientInterface {
	// error codes
	public final static int PROFILE_ERROR_GUEST = 2;
	
	public final static int USER_ERROR_INVALID_EMAIL = 7;
	public final static int USER_ERROR_NO_SUCH_USER = 8;
	public final static int USER_ERROR_VERSION_TOO_OLD = 9;
	
	public final static int USERNAME_ERROR_TOO_MANY = 1;
	public final static int USERNAME_ERROR_ALREADY_EXISTS = 3;
	public final static int USERNAME_ERROR_INVALID_CHARACTERS = 4;
	public final static int USERNAME_ERROR_TOO_LONG = 5;
	public final static int USERNAME_ERROR_TOO_SHORT = 6;

	public final static int CHAT_ERROR_TOO_MANY_USERS = 10;
	public final static int CHAT_ERROR_INVALID_NAME = 11;
	public final static int CHAT_ERROR_NO_SUCH_NICK = 12;

	public void updateProfileList(Profile[] profiles, String last_profile_nick);
	public void updateProfile(Profile profiles);
	public void createProfileError(int error_code);
	public void createProfileSuccess();
	
	public void joiningChatRoom(String room_name);
	public void error(int error_code);
	public void receiveChatRoomUsers(ChatRoomUser[] users);
	public void receiveChatRoomMessage(String nick, String msg);
	public void receivePrivateMessage(String nick, String msg);
	public void receiveInfo(Profile profile);
	
	public void updateStart(int type);
	public void updateList(int type, Object[] names);
	public void updateComplete(int next_update_key);
	
	public void gameWonAck();
	
	public void tunnelOpened(HostSequenceID from, InetAddress inet_address, InetAddress local_inet_address, Profile name);
	public void tunnelClosed(HostSequenceID from);
	public void tunnelAccepted(HostSequenceID from);
	public void receiveRoutedEvent(HostSequenceID from, ARMIEvent event);
	
	public void loginOK(String username, TunnelAddress address);
	public void loginError(int error_code);
}
