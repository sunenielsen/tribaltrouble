package com.oddlabs.tt.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.util.StateChecksum;

public strictfp class SupplyManagers {
	private final Map supply_managers = new HashMap();
	
	public final void debugSpawn() {
		Iterator it = supply_managers.keySet().iterator();
		while (it.hasNext()) {
			SupplyManager manager = (SupplyManager)supply_managers.get(it.next());
			manager.debugSpawnSupply();
		}
	}

	public SupplyManagers(World world) {
		supply_managers.put(TreeSupply.class, new SupplyManager(world));
		supply_managers.put(RockSupply.class, new SupplyManager(world));
		supply_managers.put(IronSupply.class, new SupplyManager(world));
		supply_managers.put(RubberSupply.class, new RubberSupplyManager(world));
	}

	public final SupplyManager getSupplyManager(Class type) {
		return (SupplyManager)supply_managers.get(type);
	}
}
