package com.oddlabs.matchmaking;

import com.oddlabs.net.ARMIEvent;
import com.oddlabs.net.HostSequenceID;

public strictfp interface MatchmakingServerInterface {
	public final static int TYPE_NONE = 0;
	public final static int TYPE_GAME = 1;
	public final static int TYPE_CHAT_ROOM_LIST = 2;
	public final static int TYPE_RANKING_LIST = 3;
	
	public final static int MATCHMAKING_SERVER_PORT = 33214;

	public final static int MAX_PLAYERS = 6;
	public final static int MIN_PLAYERS = 1;
	public final static int MIN_ROOM_NAME_LENGTH = 1;
	public final static int MAX_ROOM_NAME_LENGTH = 20;
	public final static int MAX_ROOM_USERS = 50;
	public final static String ALLOWED_ROOM_CHARS = "abcdefghijklmnopqrstuvwxyz\u00E6\u00F8\u00E5ABCDEFGHIJKLMNOPQRSTUVWXYZ\u00C6\u00D8\u00C50123456789\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F9\u00FA\u00FB\u00FC\u00FD\u00FF-_,.:;?+={}[]()/&%#!<\\>'*";

	public void setProfile(String nick);
	public void createProfile(String nick);
	public void deleteProfile(String nick);
	public void requestProfiles();
public void logPriority(String nick, int priority);
	
	public void registerGame(Game game);
	public void unregisterGame();
	
	public void sendMessageToRoom(String msg);
	public void sendPrivateMessage(String nick, String msg);
	public void joinRoom(String name);
	public void leaveRoom();
	public void requestInfo(String nick);
	
	public void requestList(int type, int update_key);
	
	public void acceptTunnel(HostSequenceID host_seq);
	public void openTunnel(int address_to, int seq);
	public void closeTunnel(HostSequenceID address_to);
	public void routeEvent(HostSequenceID from, ARMIEvent event);
	public void multicastEvent(ARMIEvent event);
	public void setMulticast(HostSequenceID[] addresses);

	public void gameStartedNotify(GameSession game_session);
	public void gameQuitNotify(String nick);
	public void freeQuitStopNotify();
	public void gameLostNotify();
	public void gameWonNotify();
	public void updateGameStatus(int tick, int[] status);
}
