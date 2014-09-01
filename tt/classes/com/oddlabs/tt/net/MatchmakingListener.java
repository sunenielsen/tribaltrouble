package com.oddlabs.tt.net;

import com.oddlabs.matchmaking.Profile;
import com.oddlabs.tt.gui.ChatRoomInfo;

public strictfp interface MatchmakingListener extends ErrorListener {
	public void clearList(int type);
	public void receivedList(int type, Object[] names);
	public void loggedIn();
	public void loginError(int error_code);
	public void receivedProfiles(Profile[] profiles, String last_nick);
	public void joinedChat(ChatRoomInfo info);
	public void updateChatRoom(ChatRoomInfo info);
}
