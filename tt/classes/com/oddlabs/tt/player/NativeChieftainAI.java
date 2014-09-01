package com.oddlabs.tt.player;

import java.util.List;

import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.pathfinder.FindOccupantFilter;
import com.oddlabs.tt.pathfinder.UnitGrid;

public final strictfp class NativeChieftainAI extends ChieftainAI {
	private final static int NUM_UNITS_FOR_LIGHTNING = 2;
	private final static int NUM_UNITS_FOR_POISON = 5;

	public final void decide(Unit chieftain) {
		nodeLightningCloud(chieftain);
		nodePoisonFog(chieftain);
	}

	private final void nodeLightningCloud(Unit chieftain) {
		if (chieftain.getMagicProgress(RacesResources.INDEX_MAGIC_LIGHTNING) < 1)
			return;

		float hit_radius = 30f;
		int num_enemy_units = numEnemyUnits(chieftain.getOwner());
		int num_enemy_units_close = getNumEnemyUnitsClose(chieftain, hit_radius);
		if (num_enemy_units_close >= NUM_UNITS_FOR_LIGHTNING
				|| (num_enemy_units < NUM_UNITS_FOR_LIGHTNING && num_enemy_units_close > 1)
				|| (chieftain.getHitPoints() <= 2 && num_enemy_units_close > 1)) {
			chieftain.doMagic(RacesResources.INDEX_MAGIC_LIGHTNING, false);
		}
	}

	private final void nodePoisonFog(Unit chieftain) {
		if (chieftain.getMagicProgress(RacesResources.INDEX_MAGIC_POISON) < 1)
			return;

		float hit_radius = chieftain.getOwner().getRace().getMagicFactory(RacesResources.INDEX_MAGIC_POISON).getHitRadius();
		int num_enemy_units = numEnemyUnits(chieftain.getOwner());
		int num_enemy_units_close = getNumEnemyUnitsClose(chieftain, hit_radius);
		int num_friendly_units_close = getNumFriendlyUnitsClose(chieftain, hit_radius);
		if (2*num_friendly_units_close < num_enemy_units_close
				&& (num_enemy_units_close >= NUM_UNITS_FOR_POISON
				|| (num_enemy_units < NUM_UNITS_FOR_POISON && num_enemy_units_close > 1)
				|| (chieftain.getHitPoints() <= 2 && num_enemy_units_close > 1))) {
			chieftain.doMagic(RacesResources.INDEX_MAGIC_POISON, false);
		}
	}

	private final int getNumEnemyUnitsClose(Unit chieftain, float hit_radius) {
		FindOccupantFilter filter = new FindOccupantFilter(chieftain.getPositionX(), chieftain.getPositionY(), hit_radius, chieftain, Unit.class);
		chieftain.getUnitGrid().scan(filter, chieftain.getGridX(), chieftain.getGridY());
		List target_list = filter.getResult();
		int num_enemy_units_close = 0;
		for (int i = 0; i < target_list.size(); i++) {
			Unit unit = (Unit)target_list.get(i);
			if (unit.isDead())
				continue;

			float dx = unit.getPositionX() - chieftain.getPositionX();
			float dy = unit.getPositionY() - chieftain.getPositionY();
			float squared_dist = dx*dx + dy*dy;
			if (chieftain.getOwner().isEnemy(unit.getOwner()) && squared_dist < hit_radius*hit_radius) {
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
