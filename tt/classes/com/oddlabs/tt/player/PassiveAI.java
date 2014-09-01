package com.oddlabs.tt.player;

import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.behaviour.IdleController;
import com.oddlabs.tt.util.Target;

public final strictfp class PassiveAI extends AI {
	private final boolean walk_around;

	public PassiveAI(Player owner, UnitInfo unit_info, boolean walk_around) {
		super(owner, unit_info);
		this.walk_around = walk_around;
	}

	public final void animate(float time) {
		if (walk_around) {
			if (!shouldDoAction(time))
				return;
			Selectable[][] lists = getOwner().classifyUnits();

			for (int i = 0; i < lists.length; i++) {
				Selectable s = lists[i][0];
				if (s.getPrimaryController() instanceof IdleController) {
					for (int j = 0; j < lists[i].length; j++) {
						float r = getOwner().getWorld().getRandom().nextFloat();
						if (r < .2) {
							Target walkable_target = getTarget(getOwner().getWorld().getRandom());
							getOwner().setTarget(new Selectable[]{lists[i][j]}, walkable_target, Target.ACTION_ATTACK, true);
						}
					}
				}
			}
			if (getOwner().hasActiveChieftain()) {
				getOwner().getRace().getChieftainAI().decide(getOwner().getChieftain());
			}
		}
	}
}
