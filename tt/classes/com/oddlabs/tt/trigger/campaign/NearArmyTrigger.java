package com.oddlabs.tt.trigger.campaign;

import java.util.List;

import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.pathfinder.FindOccupantFilter;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.trigger.IntervalTrigger;

public final strictfp class NearArmyTrigger extends IntervalTrigger {
	private final Unit[] src;
	private final float r;
	private final Player player;
	private final Runnable runnable;

	public NearArmyTrigger(Unit[] src, float r, Player player, Runnable runnable) {
		super(player.getWorld(), .25f, 0f);
		this.src = src;
		this.r = r;
		this.player = player;
		this.runnable = runnable;
	}

	protected final void check() {
		for (int i = 0; i < src.length; i++) {
			if (src[i].isDead())
				continue;
			FindOccupantFilter filter = new FindOccupantFilter(src[i].getPositionX(), src[i].getPositionY(), r, src[i], Selectable.class);
			player.getWorld().getUnitGrid().scan(filter, src[i].getGridX(), src[i].getGridY());
			List target_list = filter.getResult();
			for (int j = 0; j < target_list.size(); j++) {
				Unit unit = (Unit)target_list.get(j);
				if (!unit.isDead() && unit.getOwner() == player) {
					triggered();
					return;
				}
			}
		}
	}

	protected final void done() {
		runnable.run();
	}
}
