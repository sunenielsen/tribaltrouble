package com.oddlabs.tt.player;

import java.util.List;
import java.util.Random;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.gui.BuildSpinner;
import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.IronSupply;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.RockSupply;
import com.oddlabs.tt.model.RubberSupply;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.behaviour.DefendController;
import com.oddlabs.tt.model.behaviour.GatherController;
import com.oddlabs.tt.model.behaviour.IdleController;
import com.oddlabs.tt.model.behaviour.NullController;
import com.oddlabs.tt.model.behaviour.PlaceBuildingController;
import com.oddlabs.tt.model.behaviour.WalkController;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.util.Target;

public abstract strictfp class AI implements Animated {
	private final static float SLEEP_SECONDS = 2f;
	private final static float MIN_SLEEP_SECONDS = 5f;
	
	private final Player owner;
	private int INDEX_IDLE_PEONS;
	private int INDEX_IDLE_CHIEFTAINS;
	private int INDEX_IDLE_WARRIORS;
	private int INDEX_GATHER_TREE_PEONS;
	private int INDEX_GATHER_ROCK_PEONS;
	private int INDEX_GATHER_IRON_PEONS;
	private int INDEX_GATHER_RUBBER_PEONS;
	private int INDEX_ARMORY;
	private int INDEX_QUARTERS;
	private int INDEX_TOWERS;
	private int INDEX_CONSTRUCTION_SITES;
	private int INDEX_PLACE_BUILDING_PEONS;
	private int INDEX_DEFENDING_UNITS;

	private Selectable[][] lists;
	private boolean armory_under_construction = false;
	private boolean quarters_under_construction = false;
	private boolean tower_under_construction = false;
	private float sleep_time;

	public AI(Player owner, UnitInfo unit_info) {
		this.owner = owner;
		owner.getWorld().getAnimationManagerRealTime().registerAnimation(this);
		reset();

		if (unit_info != null) {
			int grid_start_x = UnitGrid.toGridCoordinate(owner.getStartX());
			int grid_start_y = UnitGrid.toGridCoordinate(owner.getStartY());
			if (unit_info.hasQuarters()) {
				owner.buildBuilding(Race.BUILDING_QUARTERS, grid_start_x, grid_start_y);
			}
			if (unit_info.hasArmory()) {
				owner.buildBuilding(Race.BUILDING_ARMORY, grid_start_x, grid_start_y);
			}
			for (int i = 0; i < unit_info.getNumTowers(); i++) {
				int center = owner.getWorld().getHeightMap().getGridUnitsPerWorld()/2;
				int dx = center - grid_start_x;
				int dy = center - grid_start_y;
				float inv_dist = 1f/(float)StrictMath.sqrt(dx*dx + dy*dy);
				int tx = (int)(grid_start_x + 10f*dx*inv_dist);
				int ty = (int)(grid_start_y + 10f*dy*inv_dist);
				owner.buildBuilding(Race.BUILDING_TOWER, tx, ty);
			}
			Random random = new Random(42);
			if (unit_info.hasChieftain()) {
				Target t = getTarget(random);
				Unit chieftain = new Unit(owner, t.getPositionX(), t.getPositionY(), null, owner.getRace().getUnitTemplate(Race.UNIT_CHIEFTAIN));
				owner.setActiveChieftain(chieftain);
			}
			for (int i = 0; i < unit_info.getNumPeons(); i++) {
				Target t = getTarget(random);
				new Unit(owner, t.getPositionX(), t.getPositionY(), null, owner.getRace().getUnitTemplate(Race.UNIT_PEON));
			}
			for (int i = 0; i < unit_info.getNumRockWarriors(); i++) {
				Target t = getTarget(random);
				new Unit(owner, t.getPositionX(), t.getPositionY(), null, owner.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
			}
			for (int i = 0; i < unit_info.getNumIronWarriors(); i++) {
				Target t = getTarget(random);
				new Unit(owner, t.getPositionX(), t.getPositionY(), null, owner.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
			}
			for (int i = 0; i < unit_info.getNumRubberWarriors(); i++) {
				Target t = getTarget(random);
				new Unit(owner, t.getPositionX(), t.getPositionY(), null, owner.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
			}
		}
	}

	protected final UnitGrid getUnitGrid() {
		return owner.getWorld().getUnitGrid();
	}

	protected final Player getOwner() {
		return owner;
	}

	protected final void reclassify() {
		lists = getOwner().classifyUnits();
		classifyIndex(lists);
	}

	protected final Selectable[] getIdlePeons() {
		if (INDEX_IDLE_PEONS == -1)
			return null;
		else
			return lists[INDEX_IDLE_PEONS];
	}

	protected final Selectable[] getIdleChieftains() {
		if (INDEX_IDLE_CHIEFTAINS == -1)
			return null;
		else
			return lists[INDEX_IDLE_CHIEFTAINS];
	}

	protected final Selectable[] getIdleWarriors() {
		if (INDEX_IDLE_WARRIORS == -1)
			return null;
		else
			return lists[INDEX_IDLE_WARRIORS];
	}

	protected final Selectable[] getGatherTreePeons() {
		if (INDEX_GATHER_TREE_PEONS == -1)
			return null;
		else
			return lists[INDEX_GATHER_TREE_PEONS];
	}

	protected final Selectable[] getGatherRockPeons() {
		if (INDEX_GATHER_ROCK_PEONS == -1)
			return null;
		else
			return lists[INDEX_GATHER_ROCK_PEONS];
	}

	protected final Selectable[] getGatherIronPeons() {
		if (INDEX_GATHER_IRON_PEONS == -1)
			return null;
		else
			return lists[INDEX_GATHER_IRON_PEONS];
	}

	protected final Selectable[] getGatherRubberPeons() {
		if (INDEX_GATHER_RUBBER_PEONS == -1)
			return null;
		else
			return lists[INDEX_GATHER_RUBBER_PEONS];
	}

	protected final Selectable[] getArmory() {
		if (INDEX_ARMORY == -1)
			return null;
		else
			return lists[INDEX_ARMORY];
	}

	protected final Selectable[] getQuarters() {
		if (INDEX_QUARTERS == -1)
			return null;
		else
			return lists[INDEX_QUARTERS];
	}

	protected final Selectable[] getTowers() {
		if (INDEX_TOWERS == -1)
			return null;
		else
			return lists[INDEX_TOWERS];
	}

	protected final Selectable[] getConstructionSites() {
		if (INDEX_CONSTRUCTION_SITES == -1)
			return null;
		else
			return lists[INDEX_CONSTRUCTION_SITES];
	}

	protected final Selectable[] getPlaceBuildingPeons() {
		if (INDEX_PLACE_BUILDING_PEONS == -1)
			return null;
		else
			return lists[INDEX_PLACE_BUILDING_PEONS];
	}

	protected final Selectable[] getDefendingUnits() {
		if (INDEX_DEFENDING_UNITS == -1)
			return null;
		else
			return lists[INDEX_DEFENDING_UNITS];
	}

	private final void classifyIndex(Selectable[][] lists) {
		INDEX_IDLE_PEONS = -1;
		INDEX_IDLE_CHIEFTAINS = -1;
		INDEX_IDLE_WARRIORS = -1;
		INDEX_GATHER_TREE_PEONS = -1;
		INDEX_GATHER_ROCK_PEONS = -1;
		INDEX_GATHER_IRON_PEONS = -1;
		INDEX_GATHER_RUBBER_PEONS = -1;
		INDEX_ARMORY = -1;
		INDEX_QUARTERS = -1;
		INDEX_TOWERS = -1;
		INDEX_CONSTRUCTION_SITES = -1;
		INDEX_PLACE_BUILDING_PEONS = -1;
		INDEX_DEFENDING_UNITS = -1;
		for (int i = 0; i < lists.length; i++) {
			Selectable s = lists[i][0];

			if (s.getPrimaryController() instanceof IdleController) {
				if (s.getAbilities().hasAbilities(Abilities.BUILD)) {
					INDEX_IDLE_PEONS = i;
				} else if (s.getAbilities().hasAbilities(Abilities.MAGIC)) {
					INDEX_IDLE_CHIEFTAINS = i;
				} else if (s.getAbilities().hasAbilities(Abilities.ATTACK)) {
					INDEX_IDLE_WARRIORS = i;
				}
			} else if (s.getPrimaryController() instanceof GatherController) {
				GatherController gc = (GatherController)s.getPrimaryController();
				if (gc.getSupplyType() == TreeSupply.class) {
					INDEX_GATHER_TREE_PEONS = i;
				} else if (gc.getSupplyType() == RockSupply.class) {
					INDEX_GATHER_ROCK_PEONS = i;
				} else if (gc.getSupplyType() == IronSupply.class) {
					INDEX_GATHER_IRON_PEONS = i;
				} else if (gc.getSupplyType() == RubberSupply.class) {
					INDEX_GATHER_RUBBER_PEONS = i;
				}
			} else if (s.getPrimaryController() instanceof NullController) {
				if (s.getAbilities().hasAbilities(Abilities.BUILD_ARMIES)) {
					INDEX_ARMORY = i;
					armory_under_construction = false;
					getOwner().buildRockWeapons((Building)s, BuildSpinner.INFINITE_LIMIT, true);
					getOwner().buildIronWeapons((Building)s, BuildSpinner.INFINITE_LIMIT, true);
					getOwner().buildRubberWeapons((Building)s, BuildSpinner.INFINITE_LIMIT, true);
				} else if (s.getAbilities().hasAbilities(Abilities.REPRODUCE)) {
					INDEX_QUARTERS = i;
					quarters_under_construction = false;
				} else if (s.getAbilities().hasAbilities(Abilities.ATTACK)) {
					INDEX_TOWERS = i;
				} else {
					INDEX_CONSTRUCTION_SITES = i;
				}
			} else if (s.getPrimaryController() instanceof PlaceBuildingController) {
				INDEX_PLACE_BUILDING_PEONS = i;
			} else if (s.getPrimaryController() instanceof DefendController) {
				INDEX_DEFENDING_UNITS = i;
			}
		}
		if (INDEX_CONSTRUCTION_SITES == -1 && INDEX_PLACE_BUILDING_PEONS == -1) {
			armory_under_construction = false;
			quarters_under_construction = false;
			tower_under_construction = false;
		}
	}

	protected final boolean armoryUnderConstruction() {
		return armory_under_construction;
	}

	protected final void setArmoryUnderConstruction(boolean armory_under_construction) {
		this.armory_under_construction = armory_under_construction;
	}

	protected final boolean quartersUnderConstruction() {
		return quarters_under_construction;
	}

	protected final void setQuartersUnderConstruction(boolean quarters_under_construction) {
		this.quarters_under_construction = quarters_under_construction;
	}

	protected final boolean towerUnderConstruction() {
		return tower_under_construction;
	}

	protected final void setTowerUnderConstruction(boolean tower_under_construction) {
		this.tower_under_construction = tower_under_construction;
	}

	private final void reset() {
		sleep_time = owner.getWorld().getRandom().nextFloat()*SLEEP_SECONDS + MIN_SLEEP_SECONDS;
	}

	protected final boolean shouldDoAction(float time) {
		sleep_time -= time;
		if (!Globals.run_ai || sleep_time >= 0)
			return false;
		reset();
		return true;
	}

	public final void manTowers(int num_towers) {
		reclassify();
		Selectable[] towers = getTowers();
		Selectable[] idle_warriors = getIdleWarriors();
		if (towers == null || idle_warriors == null)
			return;

		int length = (int)StrictMath.min(idle_warriors.length, towers.length);
		for (int i = 0; i < length; i++) {
			owner.setTarget(new Selectable[]{idle_warriors[i]}, towers[i], Target.ACTION_DEFAULT, false);
		}
	}

	public final static int attackLandscape(Player owner, Target target, int num_warriors) {
		int ordered = 0;
		Selectable[][] lists = owner.classifyUnits();
		for (int i = 0; i < lists.length; i++) {
			Selectable s = lists[i][0];
			if (s instanceof Unit && !(s.getPrimaryController() instanceof WalkController && ((WalkController)s.getPrimaryController()).isAgressive())) {
				for (int j = 0; j < lists[i].length; j++) {
					Unit unit = (Unit)s;
					if (unit.getAbilities().hasAbilities(Abilities.THROW)) {
						owner.setLandscapeTarget(new Selectable[]{lists[i][j]}, target.getGridX(), target.getGridY(), Target.ACTION_ATTACK, true);
						ordered++;
						if (ordered == num_warriors) {
							return ordered;
						}
					}
				}
			}
		}
		return ordered;
	}

	public final static Unit getWarrior(Player owner) {
		Selectable[][] lists = owner.classifyUnits();
		for (int i = 0; i < lists.length; i++) {
			Selectable s = lists[i][0];
			if (s instanceof Unit && ((Unit)s).getAbilities().hasAbilities(Abilities.THROW)) {
				return (Unit)s;
			}
		}
		return null;
	}

	protected final Target getTarget(Random random) {
		float RADIUS = 30;
		float target_x = owner.getStartX() + (random.nextFloat()*2-1)*RADIUS;
		float target_y = owner.getStartY() + (random.nextFloat()*2-1)*RADIUS;
		return getUnitGrid().findGridTargets(UnitGrid.toGridCoordinate(target_x), UnitGrid.toGridCoordinate(target_y), 1, false)[0];

	}

	public final void updateChecksum(StateChecksum checksum) {
	}
}
