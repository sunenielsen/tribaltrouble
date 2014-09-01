package com.oddlabs.tt.player;

import java.util.ArrayList;
import java.util.List;

import com.oddlabs.tt.landscape.LandscapeTarget;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.weapon.IronAxeWeapon;
import com.oddlabs.tt.model.weapon.IronSpearWeapon;
import com.oddlabs.tt.model.weapon.RockAxeWeapon;
import com.oddlabs.tt.model.weapon.RockSpearWeapon;
import com.oddlabs.tt.model.weapon.RubberAxeWeapon;
import com.oddlabs.tt.model.weapon.RubberSpearWeapon;
import com.oddlabs.tt.pathfinder.FindOccupantFilter;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.util.Target;

public final strictfp class AdvancedAI extends AI {
	public final static int DIFFICULTY_EASY = 0;
	public final static int DIFFICULTY_NORMAL = 1;
	public final static int DIFFICULTY_HARD = 2;

	private final static int SCORE_PEON = 1;
	private final static int SCORE_WARRIOR_ROCK = 4;
	private final static int SCORE_WARRIOR_IRON = 5;
	private final static int SCORE_WARRIOR_RUBBER = 10;
	private final static int SCORE_CHIEFTAIN = 25;
	private final static float[] DEFENSE_FACTOR = new float[]{1f, 1.5f, 2f};

	private final static int[] MIN_UNITS_BUILDING_WEAPONS = new int[]{0, 3, 8};
	private final static int[] MIN_WEAPONS_IN_STOCK = new int[]{10, 5, 0}; // rushing penalty
	private final static int[] MIN_UNITS_REPRODUCING = new int[]{0, 5, 20};
	private final static int[] MAX_UNITS_GATHERING_TREE = new int[]{2, 5, 15};
	private final static int[] MAX_UNITS_GATHERING_ROCK = new int[]{1, 3, 9};
	private final static int[] MAX_UNITS_GATHERING_IRON = new int[]{1, 3, 10};
	private final static int[] MAX_UNITS_GATHERING_RUBBER = new int[]{0, 0, 3};

	private final static int[] UNITS_PER_TOWER1 = new int[]{1000, 1000, 90};
	private final static int[] UNITS_PER_TOWER2 = new int[]{1000, 1000, 120};

	private final int difficulty;

	private int[] NUM_WARRIORS = new int[]{3, 7, 10};
	private int[] NUM_WARRIORS_INCREASE = new int[]{1, 3, 5};
	private int[] NUM_WARRIORS_MAX = new int[]{10, 19, 40};
	private int[] NUM_WARRIORS_FOR_CHIEFTAIN = new int[]{1000, 1000, 20};

	private LandscapeTarget defense_target = null;

	public AdvancedAI(Player owner, UnitInfo unit_info, int difficulty) {
		super(owner, unit_info);
		this.difficulty = difficulty;
	}

	public final void animate(float t) {
		if (!shouldDoAction(t))
			return;
		reclassify();
		nodeDefendBase();
		reclassify();
		if (getOwner().getUnitCountContainer().getNumSupplies() > UNITS_PER_TOWER2[difficulty])
			nodeGuardTowers(2);
		else if (getOwner().getUnitCountContainer().getNumSupplies() > UNITS_PER_TOWER1[difficulty])
			nodeGuardTowers(1);

		reclassify();
		nodeAttackWithWarriorsAndChieftain(NUM_WARRIORS[difficulty], NUM_WARRIORS[difficulty] >= NUM_WARRIORS_FOR_CHIEFTAIN[difficulty]);
		nodeAssignIdlePeons();
		if (getOwner().hasActiveChieftain()) {
			getOwner().getRace().getChieftainAI().decide(getOwner().getChieftain());
		}
	}

	private final void nodeDefendBase() {
		int enemy_score = 0;
		if (getQuarters() != null) {
			enemy_score = scanForEnemies(getQuarters()[0]);
		}
		if (getArmory() != null && enemy_score == 0) {
			enemy_score = scanForEnemies(getArmory()[0]);
		}
		enemy_score = (int)(DEFENSE_FACTOR[difficulty]*enemy_score);
		if (getDefendingUnits() != null) {
			for (int i = 0; i < getDefendingUnits().length; i++) {
				enemy_score -= getUnitScore((Unit)getDefendingUnits()[i]);
			}
		}
		if (enemy_score > 0) {
			nodeDeployArmy();
			nodeDefend(enemy_score);
		}
	}

	private final void nodeDeployArmy() {
		if (getArmory() != null) {
			Building armory = (Building)getArmory()[0];
			int num_units = armory.getUnitContainer().getNumSupplies() - MIN_UNITS_BUILDING_WEAPONS[difficulty];
			int num_weapons = numWeapons(armory) - MIN_WEAPONS_IN_STOCK[difficulty];
			if (num_units <= 0 || num_weapons <= 0)
				return;

			int num_warriors = StrictMath.min(num_units, num_weapons);
			int num_rubber_units = StrictMath.min(num_warriors, armory.getSupplyContainer(RubberAxeWeapon.class).getNumSupplies());
			int num_iron_units = StrictMath.min(num_warriors - num_rubber_units, armory.getSupplyContainer(IronAxeWeapon.class).getNumSupplies());
			int num_rock_units = StrictMath.min(num_warriors - num_rubber_units - num_iron_units, armory.getSupplyContainer(RockAxeWeapon.class).getNumSupplies());
			if (num_rubber_units > 0) {
				getOwner().deployUnits(armory, Building.KEY_DEPLOY_RUBBER_WARRIOR, num_rubber_units);
//				deployed += num_rubber_units*SCORE_WARRIOR_RUBBER;
			}
			if (num_iron_units > 0) {
				getOwner().deployUnits(armory, Building.KEY_DEPLOY_IRON_WARRIOR, num_iron_units);
//				deployed += num_iron_units*SCORE_WARRIOR_IRON;
			}
			if (num_rock_units > 0) {
				getOwner().deployUnits(armory, Building.KEY_DEPLOY_ROCK_WARRIOR, num_rock_units);
//				deployed += num_rock_units*SCORE_WARRIOR_ROCK;
			}
			num_units = armory.getUnitContainer().getNumSupplies();
			if (num_units > 0) {
				getOwner().deployUnits(armory, Building.KEY_DEPLOY_PEON, num_units);
//				deployed += num_units*SCORE_PEON;
			}
//			result += deployed;
		}
	}

	private final void nodeDefend(int score) {
		ArrayList unit_list = new ArrayList();

		int result = 0;
		if (getIdleWarriors() != null && result < score) {
			result = addFromList(getIdleWarriors(), unit_list, result, score);
		}
		if (getIdlePeons() != null && result < score) {
			result = addFromList(getIdlePeons(), unit_list, result, score);
		}
		if (getGatherTreePeons() != null && result < score) {
			result = addFromList(getGatherTreePeons(), unit_list, result, score);
		}
		if (getGatherRockPeons() != null && result < score) {
			result = addFromList(getGatherRockPeons(), unit_list, result, score);
		}
		if (getGatherIronPeons() != null && result < score) {
			result = addFromList(getGatherIronPeons(), unit_list, result, score);
		}
		if (getGatherRubberPeons() != null && result < score) {
			result = addFromList(getGatherRubberPeons(), unit_list, result, score);
		}

		if (result > 0) {
			Unit[] units = new Unit[unit_list.size()];
			unit_list.toArray(units);
			getOwner().setLandscapeTarget(units, defense_target.getGridX(), defense_target.getGridY(), Target.ACTION_DEFEND, true);
		}
	}

	private final int addFromList(Selectable[] list, ArrayList new_list, int progress, int score) {
		int result = progress;
		for (int i = 0; i < list.length; i++) {
			Unit unit = (Unit)list[i];
			new_list.add(unit);
			result += getUnitScore(unit);
			if (result > score)
				break;
		}
		return result;
	}

	private final int scanForEnemies(Selectable src) {
		FindOccupantFilter filter = new FindOccupantFilter(src.getPositionX(), src.getPositionY(), 30f, src, Unit.class);
		getUnitGrid().scan(filter, src.getGridX(), src.getGridY());
		List target_list = filter.getResult();
		int score = 0;
		defense_target = null;
		for (int i = 0; i < target_list.size(); i++) {
			Unit unit = (Unit)target_list.get(i);
			if (!unit.isDead() && getOwner().isEnemy(unit.getOwner())) {
				score += getUnitScore(unit);
				if (defense_target == null)
					defense_target = new LandscapeTarget(unit.getGridX(), unit.getGridY());
			}
		}
		return score;
	}

	private final int getUnitScore(Unit unit) {
		if (unit.getAbilities().hasAbilities(Abilities.HARVEST)) {
			return SCORE_PEON;
		} else if (unit.getAbilities().hasAbilities(Abilities.MAGIC)) {
			return SCORE_CHIEFTAIN;
		} else if (unit.getWeaponFactory().getType() == RockAxeWeapon.class || unit.getWeaponFactory().getType() == RockSpearWeapon.class) {
			return SCORE_WARRIOR_ROCK;
		} else if (unit.getWeaponFactory().getType() == IronAxeWeapon.class || unit.getWeaponFactory().getType() == IronSpearWeapon.class) {
			return SCORE_WARRIOR_IRON;
		} else if (unit.getWeaponFactory().getType() == RubberAxeWeapon.class || unit.getWeaponFactory().getType() == RubberSpearWeapon.class) {
			return SCORE_WARRIOR_RUBBER;
		}
		throw new RuntimeException();
	}

	private final void nodeGuardTowers(int num_towers) {
		if ((getTowers() == null && num_towers > 0) || (getTowers() != null && num_towers > getTowers().length)) {
			nodeBuildTower(num_towers);
		} else if (num_towers > 0) {
			for (int i = 0; i < getTowers().length; i++) {
				if (!((Building)getTowers()[i]).getUnitContainer().isSupplyFull() && getIdleWarriors() != null && getIdleWarriors().length > i) {
					Selectable[] warrior = new Selectable[1];
					warrior[0] = getIdleWarriors()[i];
					getOwner().setTarget(warrior, getTowers()[i], Target.ACTION_DEFAULT, false);
					nodeDeployUnitsInArmory(1);
				}
			}
		}
	}

	private final void nodeBuildTower(int number) {
		if (!towerUnderConstruction() && ((getTowers() == null && number == 1) || (getTowers() != null && getTowers().length < number)) && getQuarters() != null && getArmory() != null) {
			Selectable[] builders = getPeons(10);
			if (builders.length == 0)
				return;

			Building origin;
			if (number%2 == 1)
				origin = (Building)getQuarters()[0];
			else
				origin = (Building)getArmory()[0];
			int ox = origin.getGridX();
			int oy = origin.getGridY();
			int center = getOwner().getWorld().getHeightMap().getGridUnitsPerWorld()/2;
			int dx = center - ox;
			int dy = center - oy;
			float inv_dist = 1f/(float)StrictMath.sqrt(dx*dx + dy*dy);
			int tx = (int)(ox + 10f*dx*inv_dist);
			int ty = (int)(oy + 10f*dy*inv_dist);
			setTowerUnderConstruction(buildBuilding(Race.BUILDING_TOWER, builders, tx, ty));
		}
	}

	private final void nodeAssignIdlePeons() {
		if (getIdlePeons() != null) {
			if (quartersUnderConstruction() && getConstructionSites() != null) {
				getOwner().setTarget(getIdlePeons(), getConstructionSites()[0], Target.ACTION_DEFAULT, false);
			} else if (armoryUnderConstruction() && getConstructionSites() != null) {
				getOwner().setTarget(getIdlePeons(), getConstructionSites()[0], Target.ACTION_DEFAULT, false);
			} else if (towerUnderConstruction() && getConstructionSites() != null) {
				getOwner().setTarget(getIdlePeons(), getConstructionSites()[0], Target.ACTION_DEFAULT, false);
			} else if (getQuarters() != null && !((Building)getQuarters()[0]).isDead()) {
				getOwner().setTarget(getIdlePeons(), getQuarters()[0], Target.ACTION_DEFAULT, false);
			}
		}
	}

	private final void nodeAttackWithWarriorsAndChieftain(int num_warriors, boolean use_chieftain) {
/*
System.out.print("nodeAttackWithWarriorsAndChieftain");
if (getIdleWarriors() == null)
	System.out.println(" | no idling warriors");
else
	System.out.println(" | " + getIdleWarriors().length + " idling warriors");
*/
		if (getIdleWarriors() != null && getIdleWarriors().length >= num_warriors 
				&& (!use_chieftain || getOwner().hasActiveChieftain())) {
			boolean idle_chieftain = getIdleChieftains() != null && getIdleChieftains().length >= 1;
			Selectable[] warriors;
			if (idle_chieftain && use_chieftain) {
				warriors = new Selectable[num_warriors + 1];
				warriors[num_warriors] = getIdleChieftains()[0];
			} else {
				warriors = new Selectable[num_warriors];
			}

			for (int i = 0; i < num_warriors; i++) {
				warriors[i] = getIdleWarriors()[i];
			}
			Target target = findTarget(warriors[0].getGridX(), warriors[0].getGridY());
			if (target != null) {
				getOwner().setLandscapeTarget(warriors, target.getGridX(), target.getGridY(), Target.ACTION_ATTACK, true);
				if (NUM_WARRIORS[difficulty] < NUM_WARRIORS_MAX[difficulty])
					NUM_WARRIORS[difficulty] += NUM_WARRIORS_INCREASE[difficulty];
			}
		} else {
			if (getIdleWarriors() != null) {
				nodeDeployUnitsInArmory(num_warriors - getIdleWarriors().length);
			} else {
				nodeDeployUnitsInArmory(num_warriors);
			}
			if (use_chieftain)
				nodeTrainChieftain();
		}
	}

	private final void nodeTrainChieftain() {
		if (!getOwner().hasActiveChieftain() && !getOwner().isTrainingChieftain()) {
			if (getQuarters() != null) {
				getOwner().trainChieftain((Building)getQuarters()[0], true);
			}
		}
	}

	private final void nodeDeployUnitsInArmory(int num_warriors) {
		Building armory = null;
		if (getArmory() != null && getArmory().length > 0) {
			armory = (Building)getArmory()[0];
		}
		if (armory != null) {
			if (!armory.isDead()) {
				int num_units = armory.getUnitContainer().getNumSupplies() - MIN_UNITS_BUILDING_WEAPONS[difficulty];
				int num_weapons = numWeapons(armory) - MIN_WEAPONS_IN_STOCK[difficulty];

				if (num_units >= num_warriors && num_weapons >= num_warriors) {
					int num_rubber_units = StrictMath.min(num_warriors, armory.getSupplyContainer(RubberAxeWeapon.class).getNumSupplies());
					int num_iron_units = StrictMath.min(num_warriors - num_rubber_units, armory.getSupplyContainer(IronAxeWeapon.class).getNumSupplies());
					int num_rock_units = StrictMath.min(num_warriors - num_rubber_units - num_iron_units, armory.getSupplyContainer(RockAxeWeapon.class).getNumSupplies());
					if (num_rubber_units > 0)
						getOwner().deployUnits(armory, Building.KEY_DEPLOY_RUBBER_WARRIOR, num_rubber_units);
					if (num_iron_units > 0)
						getOwner().deployUnits(armory, Building.KEY_DEPLOY_IRON_WARRIOR, num_iron_units);
					if (num_rock_units > 0)
						getOwner().deployUnits(armory, Building.KEY_DEPLOY_ROCK_WARRIOR, num_rock_units);
				} else {
					if (num_units < num_warriors) {
						nodeTransferUnits(num_warriors - num_units, armory);
					}
					if (num_weapons < num_warriors) {
						nodeGather(armory, num_units);
					}
				}
			}
		} else {
			nodeBuildArmory();
		}
	}

	private final void nodeGather(Building armory, int num_units) {
		int tree = 0;
		int rock = 0;
		int iron = 0;
		int rubber = 0;

		if (getGatherTreePeons() != null)
			tree = getGatherTreePeons().length;
		if (getGatherRockPeons() != null)
			rock = getGatherRockPeons().length;
		if (getGatherIronPeons() != null)
			iron = getGatherIronPeons().length;
		if (getGatherRubberPeons() != null)
			rubber = getGatherRubberPeons().length;

		if (tree >= MAX_UNITS_GATHERING_TREE[difficulty])
			tree = Integer.MAX_VALUE;
		if (rock >= MAX_UNITS_GATHERING_ROCK[difficulty])
			rock = Integer.MAX_VALUE;
		if (iron >= MAX_UNITS_GATHERING_IRON[difficulty])
			iron = Integer.MAX_VALUE;
		if (rubber >= MAX_UNITS_GATHERING_RUBBER[difficulty])
			rubber = Integer.MAX_VALUE;

		boolean deployed;
		do {
			deployed = false;
			if (num_units > 0 && tree < MAX_UNITS_GATHERING_TREE[difficulty] && tree <= rock && tree <= iron && tree <= rubber) {
				getOwner().deployUnits(armory, Building.KEY_DEPLOY_PEON_HARVEST_TREE, 1);
				deployed = true;
				tree++;
			} else if (num_units > 0 && rock < MAX_UNITS_GATHERING_ROCK[difficulty] && rock <= tree && rock <= iron && rock <= rubber) {
				getOwner().deployUnits(armory, Building.KEY_DEPLOY_PEON_HARVEST_ROCK, 1);
				deployed = true;
				rock++;
			} else if (num_units > 0 && iron < MAX_UNITS_GATHERING_IRON[difficulty] && iron <= tree && iron <= rock && iron <= rubber) {
				getOwner().deployUnits(armory, Building.KEY_DEPLOY_PEON_HARVEST_IRON, 1);
				deployed = true;
				iron++;
			} else if (num_units > 0 && rubber < MAX_UNITS_GATHERING_RUBBER[difficulty] && rubber <= tree && rubber <= rock && rubber <= iron) {
				getOwner().deployUnits(armory, Building.KEY_DEPLOY_PEON_HARVEST_RUBBER, 1);
				deployed = true;
				rubber++;
			}
			num_units--;
		} while (deployed);
	}

	private final void nodeTransferUnits(int num_units, Building armory) {
		Building quarters = null;
		if (getQuarters() != null && getQuarters().length > 0) {
			quarters = (Building)getQuarters()[0];
		}
		if (quarters != null) {
			if (!quarters.isDead()) {
				quarters.setRallyPoint(armory);
				if (quarters.getUnitContainer().getNumSupplies() > MIN_UNITS_REPRODUCING[difficulty]) {
					int units = (int)StrictMath.min(num_units, quarters.getUnitContainer().getNumSupplies() - MIN_UNITS_REPRODUCING[difficulty]);
					getOwner().deployUnits(quarters, Building.KEY_DEPLOY_PEON, units);
				}
			}
		} else {
			nodeBuildQuarters();
		}
	}

	private final void nodeBuildArmory() {
		if (!quartersUnderConstruction() && getQuarters() == null) {
			nodeBuildQuarters();
		}
		Building quarters = null;
		if (getQuarters() != null && getQuarters().length > 0) {
			quarters = (Building)getQuarters()[0];
		}
		if (!armoryUnderConstruction() && getArmory() == null
				&& getQuarters() != null && ((Building)getQuarters()[0]).getAbilities().hasAbilities(Abilities.REPRODUCE)) {
			Selectable[] builders = getPeons(20);
			if (builders.length < 20) {
				if (quarters != null && !quarters.isDead() && quarters.getUnitContainer().getNumSupplies() >= 20)
					getOwner().deployUnits(quarters, Building.KEY_DEPLOY_PEON, 20);
			}
			if (builders.length == 0)
				return;

			// TODO: Should use Quarters as origin, if it exists
			setArmoryUnderConstruction(buildBuilding(Race.BUILDING_ARMORY, builders, builders[0].getGridX(), builders[0].getGridY()));
			reclassify();
		}
	}

	private final void nodeBuildQuarters() {
		if (!quartersUnderConstruction() && getQuarters() == null) {
			Selectable[] builders = getPeons(MIN_UNITS_REPRODUCING[difficulty]);
			if (builders.length == 0)
				return;

			// TODO: Should use Armory as origin, if it exists
			setQuartersUnderConstruction(buildBuilding(Race.BUILDING_QUARTERS, builders, builders[0].getGridX(), builders[0].getGridY()));
			reclassify();
		}
	}

	private final Selectable[] getPeons(int min_num_peons) {
		ArrayList builders = new ArrayList();
		if (getIdlePeons() != null) {
			for (int i = 0; i < getIdlePeons().length; i++) {
				builders.add(getIdlePeons()[i]);
			}
		}

		Selectable[][] peon_types = new Selectable[][]{getGatherIronPeons(), getGatherRockPeons(), getGatherTreePeons(), getGatherRubberPeons()};
		for (int i = 0; i < peon_types.length; i++) {
			if (builders.size() < min_num_peons && peon_types[i] != null) {
				for (int j = 0; j < peon_types[i].length; j++) {
					builders.add(peon_types[i][j]);
					if (builders.size() == min_num_peons) {
						break;
					}
				}
			}
		}
		Selectable[] result = new Selectable[builders.size()];
		builders.toArray(result);
		return result;
	}

/*	private final int getNumUnitsDeploying() {
		int result = 0;
		if (getArmory() != null) {
			Building armory = (Building)getArmory()[0];
			result += armory.getDeployContainer(Building.KEY_DEPLOY_ROCK_WARRIOR).getNumSupplies();
			result += armory.getDeployContainer(Building.KEY_DEPLOY_IRON_WARRIOR).getNumSupplies();
			result += armory.getDeployContainer(Building.KEY_DEPLOY_RUBBER_WARRIOR).getNumSupplies();
			result += armory.getDeployContainer(Building.KEY_DEPLOY_PEON).getNumSupplies();
		}
		return result;
	}
*/
	private final int numWeapons(Building armory) {
		return armory.getSupplyContainer(RockAxeWeapon.class).getNumSupplies()
			+ armory.getSupplyContainer(IronAxeWeapon.class).getNumSupplies()
			+ armory.getSupplyContainer(RubberAxeWeapon.class).getNumSupplies();
	}

	private final Target findTarget(int start_x, int start_y) {
		Target best_building = getOwner().findNearestEnemyBuilding(start_x, start_y);
		Target best_target = getOwner().findNearestEnemy(start_x, start_y);
		if (best_building == null) {
			return best_target;
		}
		if (best_target == null) {
			return null;
		}

		int squared_dist_building = (best_building.getGridX() - start_x)*(best_building.getGridX() - start_x)
			+ (best_building.getGridY() - start_y)*(best_building.getGridY() - start_y);
		int squared_dist_target = (best_target.getGridX() - start_x)*(best_target.getGridX() - start_x)
			+ (best_target.getGridY() - start_y)*(best_target.getGridY() - start_y);

		if (squared_dist_target < squared_dist_building/2)
			return best_target;
		else
			return best_building;
	}

	private final boolean buildBuilding(int building_type, Selectable[] selection, int grid_x, int grid_y) {
		BuildingSiteScanFilter filter = new BuildingSiteScanFilter(getUnitGrid(), getOwner().getRace().getBuildingTemplate(building_type), 40, true);
		getUnitGrid().scan(filter, grid_x, grid_y);
		List target_list = filter.getResult();
		if (target_list.size() > 0) {
			Target target = (Target)target_list.get(0);
			getOwner().placeBuilding(selection, building_type, target.getGridX(), target.getGridY());
			return true;
		} else {
			return false;
		}
	}
}
