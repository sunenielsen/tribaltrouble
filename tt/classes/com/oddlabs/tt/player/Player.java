package com.oddlabs.tt.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.ToolTipBox;
import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.landscape.LandscapeTarget;
import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.landscape.WorldParameters;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Army;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.IronSupply;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.RockSupply;
import com.oddlabs.tt.model.RubberSupply;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.SupplyContainer;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.behaviour.NullController;
import com.oddlabs.tt.model.weapon.IronAxeWeapon;
import com.oddlabs.tt.model.weapon.RockAxeWeapon;
import com.oddlabs.tt.model.weapon.RubberAxeWeapon;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.util.Utils;

public final strictfp class Player implements PlayerInterface {
	public final static int INITIAL_UNIT_COUNT = 20;
	public final static int MAX_BUILDING_COUNT = 20;
	public final static int DEFAULT_MAX_UNIT_COUNT = 250;

	public static final float[][] COLORS = {{1f, .75f, 0f, 1f},
											{0f, .5f, 1f, 1f},
											{1f, 0f, .25f, 1f},
											{0f, 1f, .75f, 1f},
											{.75f, 0f, 1f, 1f},
											{.75f, 1f, 0f, 1f}};
	
	private final World world;
	private final PlayerInfo player_info;
	private final Army units = new Army();
	private final SupplyContainer unit_count;
	private final SupplyContainer building_count = new SupplyContainer(MAX_BUILDING_COUNT);

	private final float[] color;

//	private final String team_tip;

	private AI ai = null;

	private Unit chieftain = null;
	private boolean training_chieftain = false;
	private float start_x;
	private float start_y;

	// stats
	private int units_lost;
	private int buildings_lost;
	private int units_killed;
	private int buildings_destroyed;
	private int units_moved;
	private int weapons_thrown;
	private int magics;

	private int tree_harvested;
	private int rock_harvested;
	private int iron_harvested;
	private int rubber_harvested;

	private boolean can_build_chieftains = true;
	private boolean can_repair = true;
	private boolean can_attack = true;
	private boolean[] can_build = new boolean[Race.NUM_BUILDINGS];
	private boolean can_move = true;
	private boolean can_build_towers = true;
	private boolean can_build_quarters = true;
	private boolean can_exit_towers = true;
	private boolean can_use_rubber = true;
	private boolean can_set_rally = true;
	private boolean can_harvest = true;
	private boolean can_build_armies = true;
	private boolean can_build_weapons = true;
	private boolean can_transport = true;
	private boolean[] can_do_magic = new boolean[RacesResources.NUM_MAGIC];

	private float hit_bonus;

	private int preferred_speed = World.GAMESPEED_DONTCARE;

	public Player(World world, PlayerInfo player_info, float[] color) {
		this.world = world;
		this.color = color;
		for (int i = 0; i < can_do_magic.length; i++)
			can_do_magic[i] = true;
		for (int i = 0; i < can_build.length; i++)
			can_build[i] = true;
		this.player_info = player_info;
		this.unit_count = new SupplyContainer(world.getMaxUnitCount());
//		this.team_tip = Utils.getBundleString(bundle, "team", new Object[]{Integer.toString(player_info.getTeam() + 1)});
	}

	public final void changePreferredGamespeed(int delta) {
		int old_speed = getGamespeed();
		int new_speed = Math.max(Game.GAMESPEED_PAUSE, Math.min(old_speed + delta, Game.GAMESPEED_LUDICROUS));
		setPreferredGamespeed(new_speed);
	}

	public final void setPreferredGamespeed(int speed) {
		if (!World.isValidPreferredGamespeed(speed))
			return;
		if (preferred_speed != speed) {
			int old_speed = preferred_speed;
			this.preferred_speed = speed;
			if (World.isValidGamespeed(preferred_speed) && World.isValidGamespeed(old_speed))
				world.getNotificationListener().playerGamespeedChanged();
			world.gamespeedChanged();
		}
	}

	public final int getGamespeed() {
		if (World.isValidGamespeed(preferred_speed))
			return preferred_speed;
		else
			return world.getGamespeed();
	}

	public final int getPreferredGamespeed() {
		return preferred_speed;
	}

	public final float getHitBonus() {
		return hit_bonus;
	}

	public final void setHitBonus(float bonus) {
		this.hit_bonus = bonus;
	}

	public final World getWorld() {
		return world;
	}

	public final void enableArmies(boolean enabled) {
		can_build_armies = enabled;
	}

	public final void enableWeapons(boolean enabled) {
		can_build_weapons = enabled;
	}

	public final void enableTransporting(boolean enabled) {
		can_transport = enabled;
	}

	public final void enableHarvesting(boolean enabled) {
		can_harvest = enabled;
	}

	public final void enableRubber(boolean enabled) {
		can_use_rubber = enabled;
	}

	public final void enableChieftains(boolean enabled) {
		can_build_chieftains = enabled;
	}

	public final void enableTowerExits(boolean enabled) {
		can_exit_towers = enabled;
	}

	public final void enableRepairing(boolean enabled) {
		can_repair = enabled;
	}

	public final void enableBuilding(int building, boolean enabled) {
		can_build[building] = enabled;
	}

	public final void enableAttacking(boolean enabled) {
		can_attack = enabled;
	}

	public final void enableRallyPoints(boolean enabled) {
		can_set_rally = enabled;
	}

	public final void enableMoving(boolean enabled) {
		can_move = enabled;
	}

	public final boolean canTransport() {
		return can_transport;
	}

	public final boolean canBuildWeapons() {
		return can_build_weapons;
	}

	public final boolean canHarvest() {
		return can_harvest;
	}

	public final boolean canBuildArmies() {
		return can_build_armies;
	}

	public final boolean canSetRallyPoints() {
		return can_set_rally;
	}

	public final boolean canUseRubber() {
		return can_use_rubber;
	}

	public final void enableMagic(int magic_index, boolean enabled) {
		can_do_magic[magic_index] = enabled;
	}

	public final boolean canDoMagic(int magic_index) {
		return can_do_magic[magic_index];
	}

	public final boolean canExitTowers() {
		return can_exit_towers;
	}

	public final boolean canAttack() {
		return can_attack;
	}

	public final boolean canMove() {
		return can_move;
	}

	public final boolean canBuild(int building) {
		return can_build[building] && getBuildingCountContainer().getNumSupplies() < Player.MAX_BUILDING_COUNT;
	}

	public final boolean canRepair() {
		return can_repair;
	}

	public final boolean canBuildChieftains() {
		return can_build_chieftains;
	}

	public final String toString() {
		return player_info.toString();
	}

	public final PlayerInfo getPlayerInfo() {
		return player_info;
	}

	public final void setAI(AI ai) {
		this.ai = ai;
	}

	public final AI getAI() {
		return ai;
	}

	public final Building buildBuilding(int building_type, int grid_x, int grid_y) {
		BuildingSiteScanFilter filter = new BuildingSiteScanFilter(world.getUnitGrid(), getRace().getBuildingTemplate(building_type), 40, true);
		world.getUnitGrid().scan(filter, grid_x, grid_y);
		List target_list = filter.getResult();
		Building b = null;
		if (target_list.size() > 0) {
			Target t = (Target)target_list.get(0);
			b = new Building(this, getRace().getBuildingTemplate(building_type), t.getGridX(), t.getGridY());
			b.place();
			b.repair(1000);
		}
		return b;
	}

	public final void init(float[] starting_location) {
		this.start_x = starting_location[0];
		this.start_y = starting_location[1];
	}

	public final Selectable findNearestEnemy(int start_x, int start_y) {
		return findNearestEnemy(start_x, start_y, null);
	}

	public final Selectable findNearestEnemy(int start_x, int start_y, Selectable target) {
		return findNearestEnemy(start_x, start_y, target, Selectable.class);
	}

	public final int getStatus() {
		Set units = getUnits().getSet();
		Iterator it = units.iterator();
		int status = 0;
		while (it.hasNext()) {
			Selectable s = (Selectable)it.next();
			status += s.getStatusValue();
		}
		return status;
	}
	
	public final Selectable findNearestEnemy(int start_x, int start_y, Selectable target, Class type) {
		Player[] players = world.getPlayers();
		int best_dist_squared = Integer.MAX_VALUE;
		Selectable best_target = null;
		for (int i = 0; i < players.length; i++) {
			if (isEnemy(players[i])) {
				Set units = players[i].getUnits().getSet();
				Iterator it = units.iterator();
				while (it.hasNext()) {
					Selectable s = (Selectable)it.next();
					if (!(type.isInstance(s)) || s == target) {
						continue;
					}
					int dx = s.getGridX() - start_x;
					int dy = s.getGridY() - start_y;
					int dist_squared = dx*dx + dy*dy;
					if (best_dist_squared > dist_squared) {
						best_dist_squared = dist_squared;
						best_target = s;
					}
				}
			}
		}
		return best_target;
	}

	public final Selectable findNearestEnemyBuilding(int start_x, int start_y) {
		return findNearestEnemy(start_x, start_y, null, Building.class);
	}

	public final Race getRace() {
		return getWorld().getRacesResources().getRace(player_info.getRace());
	}

	public final SupplyContainer getUnitCountContainer() {
		return unit_count;
	}
	
	public final SupplyContainer getBuildingCountContainer() {
		return building_count;
	}

	public final void setActiveChieftain(Unit chieftain) {
		this.chieftain = chieftain;
	}

	public final Building getArmory() {
		Selectable[][] lists = classifyUnits();
		for (int i = 0; i < lists.length; i++) {
			Selectable s = lists[i][0];
			if (s.getPrimaryController() instanceof NullController && s.getAbilities().hasAbilities(Abilities.BUILD_ARMIES)) {
				return (Building)s;
			}
		}
		return null;
	}

	public final Building getQuarters() {
		Selectable[][] lists = classifyUnits();
		for (int i = 0; i < lists.length; i++) {
			Selectable s = lists[i][0];
			if (s.getPrimaryController() instanceof NullController && s.getAbilities().hasAbilities(Abilities.REPRODUCE)) {
				return (Building)s;
			}
		}
		return null;
	}

	public final boolean isAlive() {
		int units = getUnitCountContainer().getNumSupplies();
		return units > 0 || hasActiveChieftain() || getQuarters() != null;
	}

	
	public final boolean hasActiveChieftain() {
		return chieftain != null;
	}

	public final Unit getChieftain() {
		return chieftain;
	}
	
	public final void setTrainingChieftain(boolean training_chieftain) {
		assert this.training_chieftain != training_chieftain;
		this.training_chieftain = training_chieftain;
	}

	public final boolean isTrainingChieftain() {
		return training_chieftain;
	}
	
	public final float[] getColor() {
		return color;
	}

	public final void deployUnits(Building building, int type, int num_units) {
		if (isValid(building))
			building.deployUnits(type, num_units);
	}

	public final void createHarvesters(Building building, int num_tree, int num_rock, int num_iron, int num_rubber) {
		if (isValid(building))
			building.createHarvesters(num_tree, num_rock, num_iron, num_rubber);
	}

	public final void buildRockWeapons(Building building, int num_weapons, boolean infinite) {
		if (isValid(building))
			building.buildWeapons(RockAxeWeapon.class, num_weapons, infinite);
	}

	public final void buildIronWeapons(Building building, int num_weapons, boolean infinite) {
		if (isValid(building))
			building.buildWeapons(IronAxeWeapon.class, num_weapons, infinite);
	}

	public final void buildRubberWeapons(Building building, int num_weapons, boolean infinite) {
		if (isValid(building))
			building.buildWeapons(RubberAxeWeapon.class, num_weapons, infinite);
	}

	public final void doMagic(Unit chieftain, int magic) {
		if (isValid(chieftain))
			chieftain.doMagic(magic, true);
	}

	public final void exitTower(Building building) {
		if (isValid(building))
			building.exitTower();
	}

	public final void trainChieftain(Building building, boolean start) {
		if (isValid(building))
			building.trainChieftain(start);
	}

	public final void placeBuilding(Selectable[] selection, int template_id, int placing_grid_x, int placing_grid_y) {
		Building building = new Building(this, getRace().getBuildingTemplate(template_id), placing_grid_x, placing_grid_y);
		for (int i = 0; i < selection.length; i++) {
			if (isValid(selection[i]))
				selection[i].initTarget(building, Target.ACTION_DEFAULT, false);
		}
	}

	public final void setRallyPoint(Building building, Target target) {
		if (isValid(building) && target != null)
			building.setRallyPoint(target);
	}

	public final void setRallyPoint(Building building, int grid_x, int grid_y) {
		setRallyPoint(building, new LandscapeTarget(grid_x, grid_y));
	}

	public final void setTarget(Selectable[] selection, Target target, int action, boolean aggressive) {
		for (int i = 0; i < selection.length; i++) {
			if (isValid(selection[i]))
				selection[i].initTarget(target, action, aggressive);
		}
	}

	public final void killSelection(Selectable[] selection) {
		for (int i = 0; i < selection.length; i++) {
			if (selection[i] != null)
				selection[i].hit(10000, 0f, 1f, this);
		}
	}

	public final void setLandscapeTarget(Selectable[] selection, int grid_x, int grid_y, int action, boolean aggressive) {
		if (selection.length == 0)
			return;
		int grid_size = world.getUnitGrid().getGridSize();
		if (grid_x < 0 || grid_x >= grid_size || grid_y < 0 || grid_y >= grid_size)
			return;
		Target[] targets = world.getUnitGrid().findGridTargets(grid_x, grid_y, selection.length, selection.length != 1);
		for (int i = 0; i < selection.length; i++) {
			if (isValid(selection[i]))
				selection[i].initTarget(targets[i], action, aggressive);
		}
	}

	private final boolean isValid(Selectable s) {
		return s != null && !s.isDead() && s.getOwner() == this;
	}

	public final float getStartX() {
		return start_x;
	}

	public final void setStartX(float x) {
		start_x = x;
	}

	public final float getStartY() {
		return start_y;
	}

	public final void setStartY(float y) {
		start_y = y;
	}

	public final boolean isEnemy(Player other_player) {
		if (other_player.player_info.getTeam() == PlayerInfo.TEAM_NEUTRAL
				|| this.player_info.getTeam() == PlayerInfo.TEAM_NEUTRAL) {
			return false;
		}
		return other_player.player_info.getTeam() != this.player_info.getTeam();
	}

	public final boolean teamHasBuilding() {
		Player[] players = world.getPlayers();
		for (int i = 0; i < players.length; i++)
			if (players[i].getPlayerInfo().getTeam() == player_info.getTeam() && players[i].getBuildingCountContainer().getNumSupplies() > 0)
				return true;
		return false;
	}

	public final Army getUnits() {
		return units;
	}

	public final Selectable[][] classifyUnits() {
		Map map = new HashMap();
		List lists = new ArrayList();
		Iterator it = units.getSet().iterator();
		while (it.hasNext()) {
			Selectable unit = (Selectable)it.next();
			String key = unit.getPrimaryController().getKey();
			List list = (List)map.get(key);
			if (list == null) {
				list = new ArrayList();
				map.put(key, list);
				lists.add(list);
			}
			list.add(unit);
		}
		Selectable[][] result = new Selectable[lists.size()][];
		for (int i = 0; i < result.length; i++) {
			List list = (List)lists.get(i);
			Selectable[] array = new Selectable[list.size()];
			list.toArray(array);
			result[i] = array;
		}
		return result;
	}

	public final void magicCast() {
		magics++;
	}
	
	public final int getMagics() {
		return magics;
	}
	
	public final void weaponThrown() {
		weapons_thrown++;
	}
	
	public final int getWeaponsThrown() {
		return weapons_thrown;
	}
	
	public final void unitMoved() {
		units_moved++;
	}
	
	public final int getUnitsMoved() {
		return units_moved;
	}
	
	public final void unitLost() {
		units_lost++;
	}
	
	public final int getUnitsLost() {
		return units_lost;
	}
	
	public final void buildingLost() {
		buildings_lost++;
	}
	
	public final int getBuildingsLost() {
		return buildings_lost;
	}
	
	public final void unitKilled() {
		units_killed++;
	}
	
	public final int getUnitsKilled() {
		return units_killed;
	}
	
	public final void buildingDestroyed() {
		buildings_destroyed++;
	}
	
	public final int getBuildingsDestroyed() {
		return buildings_destroyed;
	}
	
	public final void harvested(Class type) {
		if (type == TreeSupply.class) {
			tree_harvested++;
		} else if (type == RockSupply.class) {
			rock_harvested++;
		} else if (type == IronSupply.class) {
			iron_harvested++;
		} else if (type == RubberSupply.class) {
			rubber_harvested++;
		} else
			throw new RuntimeException();
	}
	
	public final int getTreeHarvested() {
		return tree_harvested;
	}
	
	public final int getRockHarvested() {
		return rock_harvested;
	}
	
	public final int getIronHarvested() {
		return iron_harvested;
	}
	
	public final int getRubberHarvested() {
		return rubber_harvested;
	}
}
