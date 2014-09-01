package com.oddlabs.tt.net;

import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.resource.WorldGenerator;
import com.oddlabs.matchmaking.Game;

public strictfp interface GameClientInterface {
	public void setWorldGeneratorAndPlayerSlot(Game game, WorldGenerator generator, short player_slot);
	public void setPlayers(PlayerSlot[] players);
	public void startGame(int session_id);
	public void chat(int player_slot, String chat);
}
