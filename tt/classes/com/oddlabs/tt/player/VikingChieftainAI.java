package com.oddlabs.tt.player;

import java.util.List;

import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.pathfinder.FindOccupantFilter;
import com.oddlabs.tt.pathfinder.UnitGrid;

public final strictfp class VikingChieftainAI extends ChieftainAI {
	private final static int NUM_UNITS_FOR_STUN = 5;
	private final static int NUM_UNITS_FOR_BLAST = 7;

	public final void decide(Unit chieftain) {
		nodeBlast(chieftain);
		nodeStun(chieftain);
	}

	private final void nodeStun(Unit chieftain) {
		if (chieftain.getMagicProgress(RacesResources.INDEX_MAGIC_STUN) < 1)
			return;

		float hit_radius = 30f;
		int num_enemy_units = numEnemyUnits(chieftain.getOwner());
		int num_enemy_units_close = getNumEnemyUnitsClose(chieftain, hit_radius, Unit.class);
		if (num_enemy_units_close >= NUM_UNITS_FOR_STUN
				|| (num_enemy_units < NUM_UNITS_FOR_STUN && num_enemy_units_close > 1)
				|| (chieftain.getHitPoints() <= 2 && num_enemy_units_close > 1)) {
			chieftain.doMagic(RacesResources.INDEX_MAGIC_STUN, false);
		}
	}

	private final void nodeBlast(Unit chieftain) {
		if (chieftain.getMagicProgress(RacesResources.INDEX_MAGIC_BLAST) < 1)
			return;

		float hit_radius = chieftain.getOwner().getRace().getMagicFactory(1).getHitRadius();
		int num_enemy_units = numEnemyUnits(chieftain.getOwner());
		int num_enemy_units_close = getNumEnemyUnitsClose(chieftain, hit_radius, Selectable.class);
		int num_friendly_units_close = getNumFriendlyUnitsClose(chieftain, hit_radius);
		if (2*num_friendly_units_close < num_enemy_units_close
				&& (num_enemy_units_close >= NUM_UNITS_FOR_BLAST
				|| (num_enemy_units < NUM_UNITS_FOR_BLAST && num_enemy_units_close > 1)
				|| (chieftain.getHitPoints() <= 2 && num_enemy_units_close > 1))) {
			chieftain.doMagic(RacesResources.INDEX_MAGIC_BLAST, false);
		}
	}

	private final int getNumEnemyUnitsClose(Unit chieftain, float hit_radius, Class type) {
		FindOccupantFilter filter = new FindOccupantFilter(chieftain.getPositionX(), chieftain.getPositionY(), hit_radius, chieftain, type);
		chieftain.getUnitGrid().scan(filter, chieftain.getGridX(), chieftain.getGridY());
		List target_list = filter.getResult();
		int num_enemy_units_close = 0;
		for (int i = 0; i < target_list.size(); i++) {
			Selectable s = (Selectable)target_list.get(i);
			if (s.isDead())
				continue;

			float dx = s.getPositionX() - chieftain.getPositionX();
			float dy = s.getPositionY() - chieftain.getPositionY();
			float squared_dist = dx*dx + dy*dy;
			if (chieftain.getOwner().isEnemy(s.getOwner()) && squared_dist < hit_radius*hit_radius) {
				num_enemy_units_close++;
			}
		}
		return num_enemy_units_close;
	}

	private final int getNumFriendlyUnitsClose(Unit chieftain, float hit_radius) {
		FindOccupantFilter filter = new FindOccupantFilter(chieftain.getPositionX(), chieftain.getPositionY(), hit_radius, chieftain, Selectable.class);
		chieftain.getUnitGrid().scan(filter, chieftain.getGridX(), chieftain.getGridY());
		List target_list = filter.getResult();
		int num_friendly_units_close = 0;
		for (int i = 0; i < target_list.size(); i++) {
			Selectable s = (Selectable)target_list.get(i);
			if (s.isDead())
				continue;

			float dx = s.getPositionX() - chieftain.getPositionX();
			float dy = s.getPositionY() - chieftain.getPositionY();
			float squared_dist = dx*dx + dy*dy;
			if (!chieftain.getOwner().isEnemy(s.getOwner()) && squared_dist < hit_radius*hit_radius) {
				num_friendly_units_close++;
			}
		}
		return num_friendly_units_close;
	}
}
