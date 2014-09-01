package com.oddlabs.tt.net;

import java.util.List;
import java.util.ArrayList;

public final strictfp class ChatHub implements ChatListener {
	private final List listeners = new ArrayList();

	public final void addListener(ChatListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public final void removeListener(ChatListener listener) {
		listeners.remove(listener);
	}

	public final void chat(ChatMessage message) {
		if (!ChatCommand.isIgnoring(message.nick)) {
			for (int i = 0; i < listeners.size(); i++) {
				ChatListener listener = (ChatListener)listeners.get(i);
				listener.chat(message);
			}
		}
	}
}
