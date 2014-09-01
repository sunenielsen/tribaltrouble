package com.oddlabs.tt.net;

import com.oddlabs.tt.resource.WorldGenerator;
import com.oddlabs.matchmaking.Game;

public strictfp interface ConfigurationListener extends ErrorListener {
	public void connected(Client client, Game game, WorldGenerator generator, int player_slot);
	public void setPlayers(PlayerSlot[] players);
	public void gameStarted();
}
