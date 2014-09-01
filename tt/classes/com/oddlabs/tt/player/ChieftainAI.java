package com.oddlabs.tt.player;

import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.landscape.World;

import java.util.Set;

public strictfp abstract class ChieftainAI {
	public abstract void decide(Unit chieftain);

	protected final int numEnemyUnits(Player owner) {
		Player[] players = owner.getWorld().getPlayers();
		int count = 0;
		for (int i = 0; i < players.length; i++) {
			if (owner.isEnemy(players[i])) {
				Set units = players[i].getUnits().getSet();
				count += units.size();
			}
		}
		return count;
	}
}
