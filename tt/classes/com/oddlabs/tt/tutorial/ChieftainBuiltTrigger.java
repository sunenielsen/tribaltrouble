package com.oddlabs.tt.tutorial;

import java.util.Iterator;
import java.util.Set;

import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class ChieftainBuiltTrigger extends TutorialTrigger {
	private Unit chieftain = null;
	
	public ChieftainBuiltTrigger() {
		super(.1f, 0f, "chieftain_built");
	}

	protected final void run(Tutorial tutorial) {
		Set set = tutorial.getViewer().getLocalPlayer().getUnits().getSet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Selectable s = (Selectable)it.next();
			if (s instanceof Unit) {
				Unit u = (Unit)s;
				if (u.getAbilities().hasAbilities(Abilities.MAGIC)) {
					chieftain = u;
					tutorial.next(new MagicTrigger(chieftain));
				}
			}
		}
	}
}
