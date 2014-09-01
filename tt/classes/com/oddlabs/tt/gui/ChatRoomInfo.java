package com.oddlabs.tt.gui;

import com.oddlabs.matchmaking.ChatRoomUser;
import com.oddlabs.tt.util.Utils;

import java.util.*;

public final strictfp class ChatRoomInfo {
	private final String name;

	private ChatRoomUser[] users;

	public ChatRoomInfo(String name) {
		this.name = name;
	}

	public final void setUsers(ChatRoomUser[] users) {
		this.users = users;
	}

	public final String getName() {
		return name;
	}

	public final ChatRoomUser[] getUsers() {
		return users;
	}
}
