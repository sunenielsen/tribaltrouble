package com.oddlabs.tt.net;

public final strictfp class Network {
	private final static ChatHub chat_hub = new ChatHub();
	private final static MatchmakingClient matchmaking_client = new MatchmakingClient();
	private static MatchmakingListener matchmaking_listener;

	public final static MatchmakingListener getMatchmakingListener() {
		return matchmaking_listener;
	}

	public final static void setMatchmakingListener(MatchmakingListener listener) {
		matchmaking_listener = listener;
	}
	
	public final static void closeMatchmakingClient() {
		matchmaking_listener = null;
		matchmaking_client.close();
	}

	public static ChatHub getChatHub() {
		return chat_hub;
	}

	public static MatchmakingClient getMatchmakingClient() {
		return matchmaking_client;
	}
}
