package com.oddlabs.tt.net;

import com.oddlabs.tt.util.SpamFilter;

public final strictfp class ChatMessage {
	public final static int CHAT_NORMAL = 1;
	public final static int CHAT_TEAM = 2;
	public final static int CHAT_PRIVATE = 3;
	public final static int CHAT_CHATROOM = 4;
	public final static int CHAT_GAME_MENU = 5;
	
	public final String nick;
	public final String message;
	public final int type;

	public ChatMessage(String nick, String msg, int type) {
		this.nick = nick;
		this.message = SpamFilter.scan(msg);
		this.type = type;
	}

	public final String formatShort() {
		return "<" + nick + "> " + message;
	}

	public final String formatLong() {
		switch (type) {
			case CHAT_TEAM:
				return "(Team) " + formatShort();
			case CHAT_PRIVATE:
				return "(Private) " + formatShort();
			case CHAT_NORMAL: /* Fall through */
			case CHAT_CHATROOM:
			case CHAT_GAME_MENU:
				return formatShort();
			default:
				throw new RuntimeException();
		}
	}
}
