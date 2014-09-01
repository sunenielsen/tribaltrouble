package com.oddlabs.tt.model;

import java.util.ArrayList;
import java.util.List;

import com.oddlabs.tt.landscape.LandscapeResources;
import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.pathfinder.Occupant;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.util.Target;

public final strictfp class RubberGroup {
	private final int MIN_CHICKENS_PER_GROUP = 3;
	private final int MAX_CHICKENS_PER_GROUP = 7;
	
	private final static float SPAWN_TIME = 2f;

	private final World world;
	private final List supplies = new ArrayList();
	
	public RubberGroup(World world) {
		this.world = world;
		int[] group_position = getGroupPosition();
		if (group_position != null) {
			int num_supplies = MIN_CHICKENS_PER_GROUP + world.getRandom().nextInt(MAX_CHICKENS_PER_GROUP - MIN_CHICKENS_PER_GROUP + 1);
			Target[] supply_positions = world.getUnitGrid().findGridTargets(group_position[0], group_position[1], num_supplies, true);
			float spawn_x = UnitGrid.coordinateFromGrid(group_position[0]);
			float spawn_y = UnitGrid.coordinateFromGrid(group_position[1]);
			for (int i = 0; i < num_supplies; i++) {
				int grid_x = supply_positions[i].getGridX();
				int grid_y = supply_positions[i].getGridY();
				float x = UnitGrid.coordinateFromGrid(grid_x);
				float y = UnitGrid.coordinateFromGrid(grid_y);
				RubberSupply supply = new RubberSupply(world, world.getLandscapeResources().getChicken(), 2f, grid_x, grid_y, x, y, 0f, this, spawn_x, spawn_y);
				supplies.add(supply);
				new SupplySpawnAnimation(supply, SPAWN_TIME);
			}
			((RubberSupplyManager)world.getSupplyManager(RubberSupply.class)).newGroup();
		}
	}

	private final int[] getGroupPosition() {
		List tree_positions = world.getHeightMap().getTrees();
		int start_index = world.getRandom().nextInt(tree_positions.size());
		int index = (start_index + 1)%tree_positions.size();
		while (index != start_index) {
			int[] coords = (int[])tree_positions.get(index);
			Occupant occ = world.getUnitGrid().getOccupant(coords[0], coords[1]);
			if (occ instanceof TreeSupply)
				return coords;
			index = (index + 1)%tree_positions.size();
		}
		return null;
	}

	public final void remove(RubberSupply supply) {
		boolean in_list = supplies.remove(supply);
		assert in_list;
		if (supplies.size() == 0)
			((RubberSupplyManager)world.getSupplyManager(RubberSupply.class)).emptyGroup();
	}
}
