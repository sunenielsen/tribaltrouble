package com.oddlabs.tt.net;

public final strictfp class GameNetwork {
	private final Server server;
	private final Client client;

	public GameNetwork(Server server, Client client) {
		this.server = server;
		this.client = client;
		assert client != null;
	}

	public final void closeServer() {
		if (server != null)
			server.close();
	}

	public final Client getClient() {
		return client;
	}

	public final void close() {
		client.close();
		closeServer();
	}
}
