package com.oddlabs.matchserver;

import com.oddlabs.matchmaking.MatchmakingServerInterface;
import com.oddlabs.matchmaking.MatchmakingClientInterface;
import com.oddlabs.matchmaking.ChatRoomUser;

import java.io.Serializable;
import java.util.*;

public final strictfp class ChatRoom {
	private final static Map chat_rooms = new HashMap();
	
	private final Set users = new HashSet();
	private final String name;

	public ChatRoom(String name) {
		this.name = name;
	}

	public final static Map getChatRooms() {
		return chat_rooms;
	}

	public final static void joinStandardChatRoom(Client client) {
		int room_postfix = 0;
		while (true) {
			room_postfix++;
			ChatRoom room = getChatRoom("Chatroom" + room_postfix);
			if (room.getUsers().size() > MatchmakingServerInterface.MAX_ROOM_USERS/2) {
				// skip room
				continue;
			}
			client.joinRoom(room.getName());
			return;
		}
	}

	public final static ChatRoom getChatRoom(String room_name) {
		ChatRoom room = (ChatRoom)chat_rooms.get(room_name);
		if (room == null) {
			room = new ChatRoom(room_name);
			chat_rooms.put(room_name, room);
		}
		return room;
	}

	public final static boolean isNameValid(String name) {
		return name != null && name.length() <= MatchmakingServerInterface.MAX_ROOM_NAME_LENGTH
			&& name.length() >= MatchmakingServerInterface.MIN_ROOM_NAME_LENGTH && areCharactersValid(name);
	}
	
	private final static boolean areCharactersValid(String name) {
		for (int i = 0; i < name.length(); i++)
			if (MatchmakingServerInterface.ALLOWED_ROOM_CHARS.indexOf(name.charAt(i)) == -1)
				return false;
		return true;
	}

	public final void join(Client client) {
		// TODO check for size!!!!
		users.add(client);
		sendUsers();
	}

	public final void sendUsers() {
		Iterator it = users.iterator();
		ChatRoomUser[] chat_room_users = new ChatRoomUser[users.size()];
		int i = 0;
		while (it.hasNext()) {
			Client client = (Client)it.next();
			chat_room_users[i] = new ChatRoomUser(client.getProfile().getNick(), client.isPlaying());
			i++;
		}
		it = users.iterator();
		while (it.hasNext()) {
			Client client = (Client)it.next();
			client.getClientInterface().receiveChatRoomUsers(chat_room_users);
		}
	}

	public final void sendMessage(String msg, String owner) {
		Iterator it = users.iterator();
		while (it.hasNext()) {
			Client client = (Client)it.next();
			client.getClientInterface().receiveChatRoomMessage(msg, owner);
		}
	}

	public final Set getUsers() {
		return users;
	}

	public final void leave(Client client) {
		if (users.contains(client)) {
			users.remove(client);
			if (users.size() == 0) {
				chat_rooms.remove(getName());
			} else {
				sendUsers();
			}
		}
	}

	public final String getName() {
		return name;
	}
}
