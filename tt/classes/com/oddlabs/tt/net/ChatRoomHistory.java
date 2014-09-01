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

public final strictfp class ChatRoomHistory extends ChatHistory {

	private ChatRoomUser[] old_users;

	final void update(ChatRoomUser[] new_users) {
		if (old_users == null)
			return;
		Set new_users_set = new HashSet(Arrays.asList(new_users));
		Set old_users_set = new HashSet(Arrays.asList(old_users));
		Set joined_users = new HashSet(new_users_set);
		joined_users.removeAll(old_users_set);
		Iterator it = joined_users.iterator();
		ResourceBundle bundle = ResourceBundle.getBundle(ChatPanel.class.getName());
		while (it.hasNext()) {
			ChatRoomUser user = (ChatRoomUser)it.next();
			addMessage(Utils.getBundleString(bundle, "user_joined", new Object[]{user.getNick()}));
		}
		Set left_users = new HashSet(old_users_set);
		left_users.removeAll(new_users_set);
		it = left_users.iterator();
		while (it.hasNext()) {
			ChatRoomUser user = (ChatRoomUser)it.next();
			addMessage(Utils.getBundleString(bundle, "user_left", new Object[]{user.getNick()}));
		}
	}

	public final void chat(ChatMessage message) {
		if (message.type != ChatMessage.CHAT_PRIVATE && message.type != ChatMessage.CHAT_CHATROOM)
			return;
		addMessage(message.formatLong());
	}
}
