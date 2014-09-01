package com.oddlabs.tt.net;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.Iterator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import com.oddlabs.matchmaking.ChatRoomUser;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.gui.ChatPanel;

public final strictfp class InGameChatHistory extends ChatHistory {
	public final void chat(ChatMessage message) {
		if (message.type == ChatMessage.CHAT_PRIVATE || message.type == ChatMessage.CHAT_NORMAL ||  message.type == ChatMessage.CHAT_TEAM) {
			addMessage(message.formatLong());
		}
	}
}
