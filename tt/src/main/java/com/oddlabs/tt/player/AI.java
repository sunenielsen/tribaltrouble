package com.oddlabs.tt.player;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.gui.BuildSpinner;
import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Action;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.IronSupply;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.RockSupply;
import com.oddlabs.tt.model.RubberSupply;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.Ship;
import com.oddlabs.tt.model.behaviour.DefendController;
import com.oddlabs.tt.model.behaviour.GatherController;
import com.oddlabs.tt.model.behaviour.IdleController;
import com.oddlabs.tt.model.behaviour.NullController;
import com.oddlabs.tt.model.behaviour.PlaceBuildingController;
import com.oddlabs.tt.model.behaviour.WalkController;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.util.Target;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Random;

public abstract class AI implements Animated {
    private static final float SLEEP_SECONDS = 2f;
    private static final float MIN_SLEEP_SECONDS = 5f;

    private final @NonNull Player owner;
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
    private int INDEX_SHIPS;
    private int INDEX_CONSTRUCTION_SITES;
    private int INDEX_PLACE_BUILDING_PEONS;
    private int INDEX_DEFENDING_UNITS;

    private Selectable<?>[][] lists;
    private boolean armory_under_construction = false;
    private boolean quarters_under_construction = false;
    private boolean tower_under_construction = false;
    private boolean ship_under_construction = false;
    private boolean found_island = false;
    private float sleep_time;

    public AI(@NonNull Player owner, @Nullable UnitInfo unit_info) {
        this.owner = owner;
        owner.getWorld().getAnimationManagerRealTime().registerAnimation(this);
        reset();

        if (unit_info != null) {
            int grid_start_x = UnitGrid.toGridCoordinate(owner.getStartX());
            int grid_start_y = UnitGrid.toGridCoordinate(owner.getStartY());
            if (isArchipelago()) {
                List<Ship> ships = Ship.newStartingShips(owner, grid_start_x, grid_start_y,
                        unit_info.numPeonsAndWarriors());
                Ship ship = ships.get(0);
                for (int i = 0; i < unit_info.numPeons(); i++) {
                    Unit unit = new Unit(owner, ship.getPositionX(), ship.getPositionY(), null,
                            owner.getRace().getUnitTemplate(
                                    Race.UNIT_PEON));
                    Ship.board(ships, unit);
                }
                for (int i = 0; i < unit_info.numRockWarriors(); i++) {
                    Unit unit = new Unit(owner, ship.getPositionX(), ship.getPositionY(), null,
                            owner.getRace().getUnitTemplate(
                                    Race.UNIT_WARRIOR_ROCK));
                    Ship.board(ships, unit);
                }
                for (int i = 0; i < unit_info.numIronWarriors(); i++) {
                    Unit unit = new Unit(owner, ship.getPositionX(), ship.getPositionY(), null,
                            owner.getRace().getUnitTemplate(
                                    Race.UNIT_WARRIOR_IRON));
                    Ship.board(ships, unit);
                }
                for (int i = 0; i < unit_info.numRubberWarriors(); i++) {
                    Unit unit = new Unit(owner, ship.getPositionX(), ship.getPositionY(), null,
                            owner.getRace().getUnitTemplate(
                                    Race.UNIT_WARRIOR_RUBBER));
                    Ship.board(ships, unit);
                }
                found_island = false;
            } else {
                if (unit_info.hasQuarters()) {
                    owner.buildBuilding(Race.BUILDING_QUARTERS, grid_start_x, grid_start_y);
                }
                if (unit_info.hasArmory()) {
                    owner.buildBuilding(Race.BUILDING_ARMORY, grid_start_x, grid_start_y);
                }
                for (int i = 0; i < unit_info.numTowers(); i++) {
                    int center = owner.getWorld().getHeightMap().getGridUnitsPerWorld() / 2;
                    int dx = center - grid_start_x;
                    int dy = center - grid_start_y;
                    float inv_dist = 1f / (float) Math.sqrt(dx * dx + dy * dy);
                    int tx = (int) (grid_start_x + 10f * dx * inv_dist);
                    int ty = (int) (grid_start_y + 10f * dy * inv_dist);
                    owner.buildBuilding(Race.BUILDING_TOWER, tx, ty);
                }
                Random random = new Random(42);
                if (unit_info.hasChieftain()) {
                    Target t = getTarget(random);
                    Unit chieftain = new Unit(owner, t.getPositionX(), t.getPositionY(), null,
                            owner.getRace().getUnitTemplate(Race.UNIT_CHIEFTAIN));
                    owner.setActiveChieftain(chieftain);
                }
                for (int i = 0; i < unit_info.numPeons(); i++) {
                    Target t = getTarget(random);
                    new Unit(owner, t.getPositionX(), t.getPositionY(), null, owner.getRace().getUnitTemplate(
                            Race.UNIT_PEON));
                }
                for (int i = 0; i < unit_info.numRockWarriors(); i++) {
                    Target t = getTarget(random);
                    new Unit(owner, t.getPositionX(), t.getPositionY(), null, owner.getRace().getUnitTemplate(
                            Race.UNIT_WARRIOR_ROCK));
                }
                for (int i = 0; i < unit_info.numIronWarriors(); i++) {
                    Target t = getTarget(random);
                    new Unit(owner, t.getPositionX(), t.getPositionY(), null, owner.getRace().getUnitTemplate(
                            Race.UNIT_WARRIOR_IRON));
                }
                for (int i = 0; i < unit_info.numRubberWarriors(); i++) {
                    Target t = getTarget(random);
                    new Unit(owner, t.getPositionX(), t.getPositionY(), null, owner.getRace().getUnitTemplate(
                            Race.UNIT_WARRIOR_RUBBER));
                }
                found_island = true;
            }

        }
    }

    protected boolean isArchipelago() {
        return getOwner().getWorld().getMapSize() == Game.SIZE_ARCHIPELAGO;
    }

    protected boolean hasFoundIsland() {
        return found_island;
    }

    protected void setFoundIsland() {
        found_island = true;
    }

    protected final @NonNull UnitGrid getUnitGrid() {
        return owner.getWorld().getUnitGrid();
    }

    protected final @NonNull Player getOwner() {
        return owner;
    }

    protected final void reclassify() {
        lists = getOwner().classifyUnits();
        classifyIndex(lists);
    }

    protected final @NonNull Selectable<?> @Nullable [] getIdlePeons() {
        return INDEX_IDLE_PEONS == -1 ? null : lists[INDEX_IDLE_PEONS];
    }

    protected final @NonNull Selectable<?> @Nullable [] getIdleChieftains() {
        return INDEX_IDLE_CHIEFTAINS == -1 ? null : lists[INDEX_IDLE_CHIEFTAINS];
    }

    protected final @NonNull Selectable<?> @Nullable [] getIdleWarriors() {
        return INDEX_IDLE_WARRIORS == -1 ? null : lists[INDEX_IDLE_WARRIORS];
    }

    protected final @NonNull Selectable<?> @Nullable [] getGatherTreePeons() {
        return INDEX_GATHER_TREE_PEONS == -1 ? null : lists[INDEX_GATHER_TREE_PEONS];
    }

    protected final @NonNull Selectable<?> @Nullable [] getGatherRockPeons() {
        return INDEX_GATHER_ROCK_PEONS == -1 ? null : lists[INDEX_GATHER_ROCK_PEONS];
    }

    protected final @NonNull Selectable<?> @Nullable [] getGatherIronPeons() {
        return INDEX_GATHER_IRON_PEONS == -1 ? null : lists[INDEX_GATHER_IRON_PEONS];
    }

    protected final @NonNull Selectable<?> @Nullable [] getGatherRubberPeons() {
        return INDEX_GATHER_RUBBER_PEONS == -1 ? null : lists[INDEX_GATHER_RUBBER_PEONS];
    }

    protected final @NonNull Selectable<?> @Nullable [] getArmory() {
        return INDEX_ARMORY == -1 ? null : lists[INDEX_ARMORY];
    }

    protected final @NonNull Selectable<?> @Nullable [] getQuarters() {
        return INDEX_QUARTERS == -1 ? null : lists[INDEX_QUARTERS];
    }

    protected final @NonNull Selectable<?> @Nullable [] getTowers() {
        return INDEX_TOWERS == -1 ? null : lists[INDEX_TOWERS];
    }

    protected final @NonNull Selectable<?> @Nullable [] getShips() {
        return INDEX_SHIPS == -1 ? null : lists[INDEX_SHIPS];
    }

    protected final @NonNull Selectable<?> @Nullable [] getConstructionSites() {
        return INDEX_CONSTRUCTION_SITES == -1 ? null : lists[INDEX_CONSTRUCTION_SITES];
    }

    protected final @NonNull Selectable<?> @Nullable [] getPlaceBuildingPeons() {
        return INDEX_PLACE_BUILDING_PEONS == -1 ? null : lists[INDEX_PLACE_BUILDING_PEONS];
    }

    protected final @NonNull Selectable<?> @Nullable [] getDefendingUnits() {
        return INDEX_DEFENDING_UNITS == -1 ? null : lists[INDEX_DEFENDING_UNITS];
    }

    private void classifyIndex(@NonNull Selectable<?> @NonNull [] @NonNull [] lists) {
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
        INDEX_SHIPS = -1;
        INDEX_CONSTRUCTION_SITES = -1;
        INDEX_PLACE_BUILDING_PEONS = -1;
        INDEX_DEFENDING_UNITS = -1;
        for (int i = 0; i < lists.length; i++) {
            Selectable<?> s = lists[i][0];

            if (s.getPrimaryController() instanceof IdleController) {
                if (s.getAbilities().hasAbilities(Abilities.BUILD)) {
                    INDEX_IDLE_PEONS = i;
                } else if (s.getAbilities().hasAbilities(Abilities.MAGIC)) {
                    INDEX_IDLE_CHIEFTAINS = i;
                } else if (s.getAbilities().hasAbilities(Abilities.ATTACK)) {
                    INDEX_IDLE_WARRIORS = i;
                }
            } else if (s.getPrimaryController() instanceof GatherController<?> gc) {
                Class<?> supply_type = gc.getSupplyType();
                if (supply_type == TreeSupply.class) {
                    INDEX_GATHER_TREE_PEONS = i;
                } else if (supply_type == RockSupply.class) {
                    INDEX_GATHER_ROCK_PEONS = i;
                } else if (supply_type == IronSupply.class) {
                    INDEX_GATHER_IRON_PEONS = i;
                } else if (supply_type == RubberSupply.class) {
                    INDEX_GATHER_RUBBER_PEONS = i;
                }
            } else if (s.getPrimaryController() instanceof NullController) {
                if (s.getAbilities().hasAbilities(Abilities.BUILD_ARMIES)) {
                    INDEX_ARMORY = i;
                    armory_under_construction = false;
                    getOwner().buildRockWeapons((Building) s, BuildSpinner.INFINITE_LIMIT, true);
                    getOwner().buildIronWeapons((Building) s, BuildSpinner.INFINITE_LIMIT, true);
                    getOwner().buildRubberWeapons((Building) s, BuildSpinner.INFINITE_LIMIT, true);
                } else if (s.getAbilities().hasAbilities(Abilities.REPRODUCE)) {
                    INDEX_QUARTERS = i;
                    quarters_under_construction = false;
                } else if (s.getAbilities().hasAbilities(Abilities.ATTACK)) {
                    INDEX_TOWERS = i;
                } else if (s.getAbilities().hasAbilities(Abilities.SAIL)) {
                    INDEX_SHIPS = i;
                    ship_under_construction = false;
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
            ship_under_construction = false;
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

    protected final boolean shipUnderConstruction() {
        return ship_under_construction;
    }

    protected final void setShipUnderConstruction(boolean ship_under_construction) {
        this.ship_under_construction = ship_under_construction;
    }

    private void reset() {
        sleep_time = owner.getWorld().getRandom().nextFloat() * SLEEP_SECONDS + MIN_SLEEP_SECONDS;
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
        Selectable<?>[] towers = getTowers();
        Selectable<?>[] idle_warriors = getIdleWarriors();
        if (towers == null || idle_warriors == null)
            return;

        int length = Math.min(idle_warriors.length, towers.length);
        for (int i = 0; i < length; i++) {
            owner.setTarget(Selectable.newArray(idle_warriors[i]), towers[i], Action.DEFAULT, false);
        }
    }

    public static int attackLandscape(@NonNull Player owner, @NonNull Target target, int num_warriors) {
        int ordered = 0;
        Selectable<?>[][] lists = owner.classifyUnits();
        for (Selectable<?>[] list : lists) {
            Selectable<?> s = list[0];
            if (s instanceof Unit unit && !(s.getPrimaryController() instanceof WalkController
                    && ((WalkController) s.getPrimaryController()).isAgressive())) {
                for (Selectable<?> thrower : list) {
                    if (unit.getAbilities().hasAbilities(Abilities.THROW)) {
                        owner.setLandscapeTarget(Selectable.newArray(thrower), target.getGridX(), target.getGridY(),
                                Action.ATTACK, true);
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

    public static @Nullable Unit getWarrior(@NonNull Player owner) {
        Selectable<?>[][] lists = owner.classifyUnits();
        for (Selectable<?>[] list : lists) {
            Selectable<?> s = list[0];
            if (s instanceof Unit unit && s.getAbilities().hasAbilities(Abilities.THROW)) {
                return unit;
            }
        }
        return null;
    }

    protected final Target getTarget(@NonNull Random random) {
        float RADIUS = 30;
        float target_x = owner.getStartX() + (random.nextFloat() * 2 - 1) * RADIUS;
        float target_y = owner.getStartY() + (random.nextFloat() * 2 - 1) * RADIUS;
        return getUnitGrid().findGridTargets(UnitGrid.toGridCoordinate(target_x), UnitGrid.toGridCoordinate(target_y),
                1, false)[0];

    }
}
