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

public abstract strictfp class ChatHistory implements ChatListener {
	private final static int MAX_HISTORY = 50;

	private final List messages = new LinkedList();

	public final void clear() {
		messages.clear();
	}

	public abstract void chat(ChatMessage message);

	protected final void addMessage(String msg) {
		messages.add(msg);
		if (messages.size() > MAX_HISTORY)
			messages.remove(0);
	}

	final List getMessages() {
		return messages;
	}
}
