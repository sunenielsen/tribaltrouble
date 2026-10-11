package com.oddlabs.tt.player;

import com.oddlabs.tt.landscape.LandscapeTarget;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Action;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.DeployType;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Ship;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.behaviour.Controller;
import com.oddlabs.tt.model.behaviour.PlaceBuildingController;
import com.oddlabs.tt.model.behaviour.RepairController;
import com.oddlabs.tt.model.weapon.IronAxeWeapon;
import com.oddlabs.tt.model.weapon.IronSpearWeapon;
import com.oddlabs.tt.model.weapon.RockAxeWeapon;
import com.oddlabs.tt.model.weapon.RockSpearWeapon;
import com.oddlabs.tt.model.weapon.RubberAxeWeapon;
import com.oddlabs.tt.model.weapon.RubberSpearWeapon;
import com.oddlabs.tt.pathfinder.FindOccupantFilter;
import com.oddlabs.tt.landscape.IslandInfo;
import com.oddlabs.tt.landscape.DensityMap;
import com.oddlabs.tt.util.Target;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class AdvancedAI extends AI {
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_NORMAL = 1;
    public static final int DIFFICULTY_HARD = 2;

    private static final int SCORE_PEON = 1;
    private static final int SCORE_WARRIOR_ROCK = 4;
    private static final int SCORE_WARRIOR_IRON = 5;
    private static final int SCORE_WARRIOR_RUBBER = 10;
    private static final int SCORE_CHIEFTAIN = 25;
    private static final float[] DEFENSE_FACTOR = new float[]{1f, 1.5f, 2f};

    private static final int[] MIN_UNITS_BUILDING_WEAPONS = new int[]{0, 3, 8};
    private static final int[] MIN_WEAPONS_IN_STOCK = new int[]{10, 5, 0}; // rushing penalty
    private static final int[] MIN_UNITS_REPRODUCING = new int[]{0, 5, 20};
    private static final int[] MAX_UNITS_GATHERING_TREE = new int[]{2, 5, 15};
    private static final int[] MAX_UNITS_GATHERING_ROCK = new int[]{1, 3, 9};
    private static final int[] MAX_UNITS_GATHERING_IRON = new int[]{1, 3, 10};
    private static final int[] MAX_UNITS_GATHERING_RUBBER = new int[]{0, 0, 3};

    private static final int[] UNITS_PER_TOWER1 = new int[]{1000, 1000, 90};
    private static final int[] UNITS_PER_TOWER2 = new int[]{1000, 1000, 120};

    private static final int SHIP_PEONS = 26;
    private static final int SHIP_WARRIORS = 50;
    private static final int SHIP_BUILDERS = 20;
    private static final float SHIP_MIN_HEALTH = 0.5f;
    private static final int SHIP_HOME_RANGE = 50;
    private static final int FLEET_SIZE = 3;

    private final int difficulty;

    private final int[] NUM_WARRIORS = new int[]{3, 7, 10};
    private final int[] NUM_WARRIORS_INCREASE = new int[]{1, 3, 5};
    private final int[] NUM_WARRIORS_MAX = new int[]{10, 19, 40};
    private final int[] NUM_WARRIORS_FOR_CHIEFTAIN = new int[]{1000, 1000, 20};

    private @Nullable LandscapeTarget defense_target = null;

    private @Nullable IslandInfo init_island = null;
    private int init_island_contact_x = -1;
    private int init_island_contact_y = -1;

    private boolean home_is_safe = false;

    private DensityMap density_map;

    public AdvancedAI(@NonNull Player owner, UnitInfo unit_info, int difficulty) {
        super(owner, unit_info);
        this.difficulty = difficulty;
        this.density_map = new DensityMap(owner.getWorld(), owner.getPlayerInfo().getTeam());
    }

    public int getDifficulty() {
        return difficulty;
    }

    @Override
    public void animate(float t) {
        if (!shouldDoAction(t))
            return;
        reclassify();
        nodeDefendBase();
        reclassify();
        if (getOwner().getUnitCountContainer().getNumSupplies() > UNITS_PER_TOWER2[difficulty])
            nodeGuardTowers(2);
        else if (getOwner().getUnitCountContainer().getNumSupplies() > UNITS_PER_TOWER1[difficulty])
            nodeGuardTowers(1);

        if (isArchipelago()) {
            density_map.update();
            if (hasFoundIsland()) {
                home_is_safe = !islandHasEnemies(init_island);
                reclassify();
                if (baseBuildingsDone())
                    nodeBuildShipAndLoad();
                nodeUseShip();
            } else {
                nodePickInitIsland();
            }
        }

        reclassify();
        nodeAttackWithWarriorsAndChieftain(NUM_WARRIORS[difficulty],
                NUM_WARRIORS[difficulty] >= NUM_WARRIORS_FOR_CHIEFTAIN[difficulty]);
        nodeAssignIdlePeons();
        if (getOwner().hasActiveChieftain()) {
            getOwner().getRace().getChieftainAI().decide(getOwner().getChieftain());
        }
    }

    private void nodeDefendBase() {
        int enemy_score = 0;
        if (getQuarters() != null) {
            enemy_score = scanForEnemies(getQuarters()[0]);
        }
        if (getArmory() != null && enemy_score == 0) {
            enemy_score = scanForEnemies(getArmory()[0]);
        }
        enemy_score = (int) (DEFENSE_FACTOR[difficulty] * enemy_score);
        if (getDefendingUnits() != null) {
            for (Selectable<?> defendingUnit : getDefendingUnits()) {
                enemy_score -= getUnitScore((Unit) defendingUnit);
            }
        }
        if (enemy_score > 0) {
            nodeDeployArmy();
            nodeDefend(enemy_score);
        }
    }

    private void nodeDeployArmy() {
        if (getArmory() != null) {
            Building armory = (Building) getArmory()[0];
            int num_units = armory.getUnitContainer().getNumSupplies() - MIN_UNITS_BUILDING_WEAPONS[difficulty];
            int num_weapons = numWeapons(armory) - MIN_WEAPONS_IN_STOCK[difficulty];
            if (num_units <= 0 || num_weapons <= 0)
                return;

            int num_warriors = Math.min(num_units, num_weapons);
            int num_rubber_units = Math.min(num_warriors, armory.getSupplyContainer(
                    RubberAxeWeapon.class).getNumSupplies());
            int num_iron_units = Math.min(num_warriors - num_rubber_units, armory.getSupplyContainer(
                    IronAxeWeapon.class).getNumSupplies());
            int num_rock_units = Math.min(num_warriors - num_rubber_units - num_iron_units, armory.getSupplyContainer(
                    RockAxeWeapon.class).getNumSupplies());
            if (num_rubber_units > 0) {
                getOwner().deployUnits(armory, DeployType.RUBBER_WARRIOR, num_rubber_units);
//				deployed += num_rubber_units*SCORE_WARRIOR_RUBBER;
            }
            if (num_iron_units > 0) {
                getOwner().deployUnits(armory, DeployType.IRON_WARRIOR, num_iron_units);
//				deployed += num_iron_units*SCORE_WARRIOR_IRON;
            }
            if (num_rock_units > 0) {
                getOwner().deployUnits(armory, DeployType.ROCK_WARRIOR, num_rock_units);
//				deployed += num_rock_units*SCORE_WARRIOR_ROCK;
            }
            num_units = armory.getUnitContainer().getNumSupplies();
            if (num_units > 0) {
                getOwner().deployUnits(armory, DeployType.PEON, num_units);
//				deployed += num_units*SCORE_PEON;
            }
//			result += deployed;
        }
    }

    private void nodeDefend(int score) {
        List<Unit> unit_list = new ArrayList<>();

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
            getOwner().setLandscapeTarget(units, defense_target.getGridX(), defense_target.getGridY(), Action.DEFEND,
                    true);
        }
    }

    private int addFromList(Selectable<?> @NonNull [] list, @NonNull List<Unit> new_list, int progress, int score) {
        int result = progress;
        for (Selectable<?> list1 : list) {
            Unit unit = (Unit) list1;
            new_list.add(unit);
            result += getUnitScore(unit);
            if (result > score)
                break;
        }
        return result;
    }

    private int scanForEnemies(@NonNull Selectable<?> src) {
        FindOccupantFilter<Unit> filter = new FindOccupantFilter<>(src.getPositionX(), src.getPositionY(), 30f, src,
                Unit.class);
        getUnitGrid().scan(filter, src.getGridX(), src.getGridY());
        int score = 0;
        defense_target = null;
        for (Unit unit : filter.getResult()) {
            if (!unit.isDead() && getOwner().isEnemy(unit.getOwner())) {
                score += getUnitScore(unit);
                if (defense_target == null)
                    defense_target = new LandscapeTarget(unit.getGridX(), unit.getGridY());
            }
        }
        return score;
    }

    private int getUnitScore(@NonNull Unit unit) {
        if (unit.getAbilities().hasAbilities(Abilities.HARVEST)) {
            return SCORE_PEON;
        } else if (unit.getAbilities().hasAbilities(Abilities.MAGIC)) {
            return SCORE_CHIEFTAIN;
        } else if (unit.getWeaponFactory().getType() == RockAxeWeapon.class
                || unit.getWeaponFactory().getType() == RockSpearWeapon.class) {
                    return SCORE_WARRIOR_ROCK;
                } else if (unit.getWeaponFactory().getType() == IronAxeWeapon.class
                        || unit.getWeaponFactory().getType() == IronSpearWeapon.class) {
                            return SCORE_WARRIOR_IRON;
                        } else if (unit.getWeaponFactory().getType() == RubberAxeWeapon.class
                                || unit.getWeaponFactory().getType() == RubberSpearWeapon.class) {
                                    return SCORE_WARRIOR_RUBBER;
                                }
        throw new RuntimeException();
    }

    private void nodeGuardTowers(int num_towers) {
        if ((getTowers() == null && num_towers > 0) || (getTowers() != null && num_towers > getTowers().length)) {
            nodeBuildTower(num_towers);
        } else if (num_towers > 0) {
            for (int i = 0; i < getTowers().length; i++) {
                if (!((Building) getTowers()[i]).getUnitContainer().isSupplyFull() && getIdleWarriors() != null
                        && getIdleWarriors().length > i) {
                    getOwner().setTarget(Selectable.newArray(getIdleWarriors()[i]), getTowers()[i], Action.DEFAULT,
                            false);
                    nodeDeployUnitsInArmory(1);
                }
            }
        }
    }

    private void nodeBuildTower(int number) {
        if (!towerUnderConstruction() && ((getTowers() == null && number == 1) || (getTowers() != null
                && getTowers().length < number)) && getQuarters() != null && getArmory() != null) {
            Selectable<?>[] builders = getPeons(10);
            if (builders.length == 0)
                return;

            Building origin = number % 2 == 1 ? (Building) getQuarters()[0] : (Building) getArmory()[0];
            int ox = origin.getGridX();
            int oy = origin.getGridY();
            int center = getOwner().getWorld().getHeightMap().getGridUnitsPerWorld() / 2;
            int dx = center - ox;
            int dy = center - oy;
            float inv_dist = 1f / (float) Math.sqrt(dx * dx + dy * dy);
            int tx = (int) (ox + 10f * dx * inv_dist);
            int ty = (int) (oy + 10f * dy * inv_dist);
            setTowerUnderConstruction(buildBuilding(Race.BUILDING_TOWER, builders, tx, ty));
        }
    }

    private void nodeAssignIdlePeons() {
        if (getIdlePeons() != null) {
            if (quartersUnderConstruction() && getConstructionSites() != null) {
                getOwner().setTarget(getIdlePeons(), getConstructionSites()[0], Action.DEFAULT, false);
            } else if (armoryUnderConstruction() && getConstructionSites() != null) {
                getOwner().setTarget(getIdlePeons(), getConstructionSites()[0], Action.DEFAULT, false);
            } else if (towerUnderConstruction() && getConstructionSites() != null) {
                getOwner().setTarget(getIdlePeons(), getConstructionSites()[0], Action.DEFAULT, false);
            } else if (shipUnderConstruction() && getConstructionSites() != null) {
                getOwner().setTarget(getIdlePeons(), getConstructionSites()[0], Action.DEFAULT, false);
            } else if (getQuarters() != null && !getQuarters()[0].isDead()) {
                getOwner().setTarget(getIdlePeons(), getQuarters()[0], Action.DEFAULT, false);
            }
        }
    }

    private void nodeAttackWithWarriorsAndChieftain(int num_warriors, boolean use_chieftain) {
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
            Selectable<?>[] warriors;
            if (idle_chieftain && use_chieftain) {
                warriors = Selectable.newArray(num_warriors + 1);
                warriors[num_warriors] = getIdleChieftains()[0];
            } else {
                warriors = Selectable.newArray(num_warriors);
            }

            System.arraycopy(getIdleWarriors(), 0, warriors, 0, num_warriors);
            Target target = findTarget(warriors[0].getGridX(), warriors[0].getGridY());
            if (target != null) {
                getOwner().setLandscapeTarget(warriors, target.getGridX(), target.getGridY(), Action.ATTACK, true);
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

    private void nodeTrainChieftain() {
        if (!getOwner().hasActiveChieftain() && !getOwner().isTrainingChieftain()) {
            if (getQuarters() != null) {
                getOwner().trainChieftain((Building) getQuarters()[0], true);
            }
        }
    }

    private void nodeDeployUnitsInArmory(int num_warriors) {
        Building armory = null;
        if (getArmory() != null && getArmory().length > 0) {
            armory = (Building) getArmory()[0];
        }
        if (armory != null) {
            if (!armory.isDead()) {
                int num_units = armory.getUnitContainer().getNumSupplies() - MIN_UNITS_BUILDING_WEAPONS[difficulty];
                int num_weapons = numWeapons(armory) - MIN_WEAPONS_IN_STOCK[difficulty];

                if (num_units >= num_warriors && num_weapons >= num_warriors) {
                    int num_rubber_units = Math.min(num_warriors, armory.getSupplyContainer(
                            RubberAxeWeapon.class).getNumSupplies());
                    int num_iron_units = Math.min(num_warriors - num_rubber_units, armory.getSupplyContainer(
                            IronAxeWeapon.class).getNumSupplies());
                    int num_rock_units = Math.min(num_warriors - num_rubber_units - num_iron_units,
                            armory.getSupplyContainer(RockAxeWeapon.class).getNumSupplies());
                    if (num_rubber_units > 0)
                        getOwner().deployUnits(armory, DeployType.RUBBER_WARRIOR, num_rubber_units);
                    if (num_iron_units > 0)
                        getOwner().deployUnits(armory, DeployType.IRON_WARRIOR, num_iron_units);
                    if (num_rock_units > 0)
                        getOwner().deployUnits(armory, DeployType.ROCK_WARRIOR, num_rock_units);
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

    private void nodeGather(@NonNull Building armory, int num_units) {
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
            if (num_units > 0 && tree < MAX_UNITS_GATHERING_TREE[difficulty] && tree <= rock && tree <= iron
                    && tree <= rubber) {
                getOwner().deployUnits(armory, DeployType.PEON_HARVEST_TREE, 1);
                deployed = true;
                tree++;
            } else if (num_units > 0 && rock < MAX_UNITS_GATHERING_ROCK[difficulty] && rock <= tree && rock <= iron
                    && rock <= rubber) {
                        getOwner().deployUnits(armory, DeployType.PEON_HARVEST_ROCK, 1);
                        deployed = true;
                        rock++;
                    } else if (num_units > 0 && iron < MAX_UNITS_GATHERING_IRON[difficulty] && iron <= tree
                            && iron <= rock && iron <= rubber) {
                                getOwner().deployUnits(armory, DeployType.PEON_HARVEST_IRON, 1);
                                deployed = true;
                                iron++;
                            } else if (num_units > 0 && rubber < MAX_UNITS_GATHERING_RUBBER[difficulty]
                                    && rubber <= tree && rubber <= rock && rubber <= iron) {
                                        getOwner().deployUnits(armory, DeployType.PEON_HARVEST_RUBBER, 1);
                                        deployed = true;
                                        rubber++;
                                    }
            num_units--;
        } while (deployed);
    }

    private void nodeTransferUnits(int num_units, @NonNull Building armory) {
        Building quarters = null;
        if (getQuarters() != null && getQuarters().length > 0) {
            quarters = (Building) getQuarters()[0];
        }
        if (quarters != null) {
            if (!quarters.isDead()) {
                quarters.setRallyPoint(armory);
                if (quarters.getUnitContainer().getNumSupplies() > MIN_UNITS_REPRODUCING[difficulty]) {
                    int units = Math.min(num_units,
                            quarters.getUnitContainer().getNumSupplies() - MIN_UNITS_REPRODUCING[difficulty]);
                    getOwner().deployUnits(quarters, DeployType.PEON, units);
                }
            }
        } else {
            nodeBuildQuarters();
        }
    }

    private void nodeBuildArmory() {
        if (!quartersUnderConstruction() && getQuarters() == null) {
            nodeBuildQuarters();
        }
        Building quarters = null;
        if (getQuarters() != null && getQuarters().length > 0) {
            quarters = (Building) getQuarters()[0];
        }
        if (!armoryUnderConstruction() && getArmory() == null
                && getQuarters() != null && getQuarters()[0].getAbilities().hasAbilities(Abilities.REPRODUCE)) {
            Selectable<?>[] builders = getPeons(20);
            if (builders.length < 20) {
                if (quarters != null && !quarters.isDead() && quarters.getUnitContainer().getNumSupplies() >= 20)
                    getOwner().deployUnits(quarters, DeployType.PEON, 20);
            }
            if (builders.length == 0)
                return;

            // TODO: Should use Quarters as origin, if it exists
            setArmoryUnderConstruction(buildBuilding(Race.BUILDING_ARMORY, builders, builders[0].getGridX(),
                    builders[0].getGridY()));
            reclassify();
        }
    }

    private void nodeBuildQuarters() {
        if (!quartersUnderConstruction() && getQuarters() == null) {
            Selectable<?>[] builders = getPeons(MIN_UNITS_REPRODUCING[difficulty]);
            if (builders.length == 0)
                return;

            // TODO: Should use Armory as origin, if it exists
            setQuartersUnderConstruction(buildBuilding(Race.BUILDING_QUARTERS, builders, builders[0].getGridX(),
                    builders[0].getGridY()));
            reclassify();
        }
    }

    private boolean baseBuildingsDone() {
        return getQuarters() != null && getArmory() != null
                && !quartersUnderConstruction() && !armoryUnderConstruction();
    }

    private void nodeBuildShipAndLoad() {
        List<Ship> ships = getOwnShips();

        nodeBuildShip(ships.size());

        for (Ship ship : ships.subList(0, Math.min(ships.size(), getMaxFleetSize()))) {
            if (ship.isComplete() && !ship.isMoving() && shipAtHome(ship) && !shipFullyCrewed(ship)) {
                nodeLoadShip(ship);
                return;
            }
        }
    }

    private @NonNull List<Ship> getOwnShips() {
        List<Ship> ships = new ArrayList<>();
        for (Selectable<?> s : getOwner().getUnits().getSet()) {
            if (s instanceof Ship ship && !ship.isDead())
                ships.add(ship);
        }
        return ships;
    }

    private void nodePickInitIsland() {
        Ship ship = getInitShip();
        if (ship == null) {
            return;
        }

        if (init_island == null) {
            var islands = getOwner().getWorld().getHeightMap().getIslandInfos();
            IslandInfo best = null;
            int best_d2 = 1000;
            int contact_x = 0;
            int contact_y = 0;
            for (int i = 0; i < islands.size(); i++) {
                var island = islands.get(i);
                if (island.trees() > 25 && island.rocks() > 10 && island.iron() > 10) {
                    for (var pt : island.contourPoints()) {
                        int dx = pt[0] - ship.getGridX();
                        int dy = pt[1] - ship.getGridY();
                        int d2 = dx * dx + dy * dy;
                        if (best == null || d2 < best_d2) {
                            best = island;
                            best_d2 = d2;
                            contact_x = pt[0];
                            contact_y = pt[1];
                        }
                    }
                }
            }
            if (best != null) {
                init_island_contact_x = contact_x;
                init_island_contact_y = contact_y;
                init_island = best;
            }
        }

        boolean landed = true;
        for (Ship start_ship : getOwnShips()) {
            if (shipIncomplete(start_ship)) {
                continue;
            }
            if (start_ship.getEntrance() == start_ship
                    || start_ship.getEntrance().getIslandId() != init_island.id()) {
                if (!start_ship.isMoving()) {
                    getOwner().setSailingTarget(Selectable.newArray(start_ship), init_island_contact_x,
                            init_island_contact_y);
                }
                deployWholeShip(start_ship);
                landed = false;
            } else if (start_ship.getShipHR().countUnits() > 0) {
                deployWholeShip(start_ship);
                landed = false;
            }
        }
        if (landed) {
            setFoundIsland();
        }
    }

    private void nodeUseShip() {
        List<Ship> ships = getOwnShips();
        for (Ship ship : ships) {
            useShip(ship);
        }
    }

    private boolean islandHasEnemies(IslandInfo island) {
        if (density_map.sum(island.minX(), island.minY(), island.maxX(), island.maxY()) == 0) {
            return false;
        }

        Selectable<?> enemy = getOwner().findNearestEnemy(island.centerX(), island.centerY(),
                s -> s.getIslandId() == island.id());
        return enemy != null;
    }

    private void pickSafeLandingPoint(Ship ship, int islandId) {
        var info = getOwner().getWorld().getHeightMap().getIslandInfo(islandId);
        var pts = info.contourPoints();
        int best_weight = 10000;
        var best_pt = pts.get(0);
        for (int i = 0; i < pts.size(); i++) {
            var pt = pts.get(i);
            int weight = density_map.getWeight(pt[0], pt[1], 1);
            if (i == 0 || weight < best_weight) {
                best_weight = weight;
                best_pt = pt;
            }
        }
        getOwner().setSailingTarget(Selectable.newArray(ship), best_pt[0], best_pt[1]);
    }

    private void useShip(@NonNull Ship ship) {

        if (ship.isDead() || !ship.isComplete() || ship.isMoving())
            return;

        boolean at_home = shipAtHome(ship);
        boolean should_escape = shipShouldEscape(ship);
        boolean battle_ready = shipBattleReady(ship);

        if (should_escape) {
            Building home = homeBuilding();
            if (home != null && ship.getEntrance().getIslandId() != home.getIslandId()) {
                getOwner().setSailingTarget(Selectable.newArray(ship), home);
            }
            return;
        }

        if (!home_is_safe) {
            Selectable<?> enemy = getOwner().findNearestEnemy(ship.getGridX(), ship.getGridY(), s -> s instanceof Ship
                    || objectOnBeach(s));
            if (battle_ready) {
                if (enemy != null) {
                    getOwner().setSailingTarget(Selectable.newArray(ship), enemy);
                    return;
                }
            }

            if (!at_home && enemy == null) {
                Building home = homeBuilding();
                if (home != null) {
                    getOwner().setSailingTarget(Selectable.newArray(ship), home);
                }
            }
        } else {
            if (battle_ready) {
                Selectable<?> enemy = getOwner().findNearestEnemy(ship.getGridX(), ship.getGridY(),
                        s -> !(s instanceof Ship) && (s.getIslandId() != 0));
                if (enemy != null) {
                    if (ship.getEntrance().getIslandId() != enemy.getIslandId()) {
                        pickSafeLandingPoint(ship, enemy.getIslandId());
                        return;
                    }

                    // Deploy warriors too early so they start getting off-board upon arrival
                    if (ship.getEntrance() == ship) {
                        deployShipWarriors(ship);
                        return;
                    }

                    if (ship.getEntrance().getIslandId() == enemy.getIslandId()) {
                        if (shipHasWarriors(ship)) {
                            deployShipWarriors(ship);
                            return;
                        }
                    }
                }
            }

            if (!shipHasWarriors(ship) && !at_home) {
                Building home = homeBuilding();
                if (home != null) {
                    getOwner().setSailingTarget(Selectable.newArray(ship), home);
                }
            }
        }
    }

    private boolean objectOnBeach(@NonNull Selectable s) {
        var dock = getOwner().getWorld().getHeightMap().getDockGrid();
        var map_size = getOwner().getWorld().getHeightMap().getGridUnitsPerWorld();
        int x = s.getGridX();
        int y = s.getGridY();
        int size = StrictMath.round(s.getSize());
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int cx = x + i - size / 2;
                int cy = y + i - size / 2;
                if (cx < 0 || cx >= map_size || cy < 0 || cy >= map_size) {
                    continue;
                }
                if (dock[cy][cx] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shipBattleReady(@NonNull Ship ship) {
        return shipFullyCrewed(ship) && !shipNeedsRepair(ship);
    }

    private boolean shipShouldEscape(@NonNull Ship ship) {
        return shipTooDamaged(ship);
    }

    private boolean shipTooDamaged(@NonNull Ship ship) {
        return ship.getHitPoints() < SHIP_MIN_HEALTH * ship.getBuildingTemplate().getMaxHitPoints();
    }

    private boolean shipNeedsRepair(@NonNull Ship ship) {
        return ship.getHitPoints() < ship.getBuildingTemplate().getMaxHitPoints();
    }

    private boolean shipFullyCrewed(@NonNull Ship ship) {
        int peons = ship.getShipHR().countPeons();
        int warriors = ship.getShipHR().countUnits() - peons;
        return peons >= SHIP_PEONS && warriors >= getMinWarriorsOnShip();
    }

    private boolean shipCrewedEnough(@NonNull Ship ship) {
        int peons = ship.getShipHR().countPeons();
        int warriors = ship.getShipHR().countUnits() - peons;
        return peons >= SHIP_PEONS / 2 && warriors >= getMinWarriorsOnShip() / 3;
    }

    private @Nullable Building homeBuilding() {
        if (getQuarters() != null)
            return (Building) getQuarters()[0];
        if (getArmory() != null)
            return (Building) getArmory()[0];
        return null;
    }

    private int homeIsland() {
        Building home = homeBuilding();
        return home != null ? home.getIslandId() : -1;
    }

    private boolean isShipNearIsland(Selectable s, IslandInfo info) {
        if (!(s instanceof Ship)) {
            return false;
        }

        int dx = info.centerX() - s.getGridX();
        int dy = info.centerY() - s.getGridY();
        int d2 = dx * dx + dy * dy;
        int r = Math.max(info.maxX() - info.minX(), info.maxY() - info.minY());
        if (d2 < r * r) {
            return true;
        }

        return false;
    }

    private boolean shipsAroundIsland(IslandInfo info) {
        Selectable<?> enemy = getOwner().findNearestEnemy(info.centerX(), info.centerY(), s -> isShipNearIsland(s,
                info));
        return enemy != null;
    }

    private boolean islandHasEnemies(int island) {
        var info = getOwner().getWorld().getHeightMap().getIslandInfo(island);
        if (info == null) {
            return false;
        }
        Selectable<?> enemy = getOwner().findNearestEnemy(info.centerX(), info.centerY(),
                s -> s.getIslandId() == island);
        return enemy != null;
    }

    private boolean homeHasEnemies() {
        int id = homeIsland();
        var info = getOwner().getWorld().getHeightMap().getIslandInfo(id);
        boolean on_land = islandHasEnemies(id);
        boolean around = shipsAroundIsland(info);
        return on_land || around;
    }

    private void deployWholeShip(Ship ship) {
        int peons = ship.getShipHR().countPeons();
        deployShipWarriors(ship);
        if (peons > 0) {
            getOwner().deployUnits(ship, DeployType.PEON, peons);
        }
    }

    private int getMaxFleetSize() {
        return home_is_safe ? FLEET_SIZE : 1;
    }

    private int getMinWarriorsOnShip() {
        return home_is_safe ? SHIP_WARRIORS : 15;
    }

    private boolean shipHasWarriors(Ship ship) {
        return ship.getShipHR().countPeons() < ship.getShipHR().countUnits();
    }

    private void deployShipWarriors(Ship ship) {
        int peons = ship.getShipHR().countPeons();
        int warriors = ship.getShipHR().countUnits() - peons;
        if (warriors > 0) {
            getOwner().deployUnits(ship, DeployType.RUBBER_WARRIOR, warriors);
            getOwner().deployUnits(ship, DeployType.IRON_WARRIOR, warriors);
            getOwner().deployUnits(ship, DeployType.ROCK_WARRIOR, warriors);
        }
    }

    private boolean shipAtHome(Ship ship) {
        Building entrance = ship.getEntrance();
        Building home = homeBuilding();
        if (home == null) {
            return false;
        }
        return entrance.getIslandId() == home.getIslandId();
    }

    private boolean closeToAny(@NonNull Target target, @NonNull Selectable<?> @Nullable [] buildings) {
        if (buildings == null)
            return false;

        for (Selectable<?> building : buildings) {
            if (building.isDead())
                continue;
            int dx = building.getGridX() - target.getGridX();
            int dy = building.getGridY() - target.getGridY();
            if (dx * dx + dy * dy <= SHIP_HOME_RANGE * SHIP_HOME_RANGE)
                return true;
        }
        return false;
    }

    private boolean shipDockedAt(@NonNull Ship ship, int island) {
        Building entrance = ship.getEntrance();
        return entrance != ship && !entrance.isDead() && entrance.getIslandId() == island;
    }

    private void nodeBuildShip(int fleet_size) {
        Ship ship = getIncompleteShip();
        if (ship == null && fleet_size >= getMaxFleetSize())
            return;

        Selectable<?>[] idle = getIdlePeons();
        int idle_count = idle != null ? idle.length : 0;

        int missing = SHIP_BUILDERS - (ship != null ? countBuilders(ship.getEntrance()) : 0);

        if (missing > idle_count && getQuarters() != null) {
            Building quarters = (Building) getQuarters()[0];
            deployPeonsFromQuarters(quarters, missing - idle_count, quarters);
            Building armory = (Building) getArmory()[0];
            deployPeonsFromQuarters(armory, missing - idle_count, armory);
        }

        if (idle_count == 0 || missing <= 0)
            return;

        if (ship != null) {
            Action action = ship.isComplete() ? Action.GATHER_REPAIR : Action.DEFAULT;
            getOwner().setTarget(lastN(idle, missing), ship, action, false);
        } else {
            Building origin = homeBuilding();
            buildShip(firstN(idle, SHIP_BUILDERS), origin.getGridX(), origin.getGridY());
        }
    }

    private boolean shipIncomplete(@NonNull Ship ship) {
        return !ship.isComplete() || ship.isDamaged();
    }

    private @Nullable Ship getIncompleteShip() {
        for (Selectable<?> s : getOwner().getUnits().getSet()) {
            if (s instanceof Ship ship && !ship.isDead() && shipIncomplete(ship))
                return ship;
        }
        Selectable<?>[] placing = getPlaceBuildingPeons();
        if (placing != null) {
            for (Selectable<?> s : placing) {
                if (!s.isDead() && s.getPrimaryController() instanceof PlaceBuildingController controller
                        && controller.getBuilding() instanceof Ship ship && !ship.isDead())
                    return ship;
            }
        }
        return null;
    }

    private @Nullable Ship getInitShip() {
        for (Selectable<?> s : getOwner().getUnits().getSet()) {
            if (s instanceof Ship ship && !ship.isDead() && !shipIncomplete(ship))
                return ship;
        }
        return null;
    }

    private int countBuilders(@NonNull Building b) {
        int builders = 0;
        for (Selectable<?> s : getOwner().getUnits().getSet()) {
            if (s.isDead()) {
                continue;
            }
            Controller controller = s.getPrimaryController();
            if (controller instanceof RepairController repair && repair.getBuilding() == b) {
                builders++;
            } else if (controller instanceof PlaceBuildingController placing && placing.getBuilding() == b) {
                builders++;
            }
        }
        return builders;
    }

    private void nodeLoadShip(@NonNull Ship ship) {
        int peons_aboard = ship.getShipHR().countPeons();
        int warriors_aboard = ship.getShipHR().countUnits() - peons_aboard;

        int peons_needed = SHIP_PEONS - peons_aboard;
        if (peons_needed > 0) {
            if (getIdlePeons() != null && getIdlePeons().length > 0) {
                getOwner().setTarget(firstN(getIdlePeons(), peons_needed), ship, Action.MOVE, false);
            } else {
                nodeDeployPeonsFromQuarters(ship, peons_needed);
            }
        }

        int warriors_needed = getMinWarriorsOnShip() - warriors_aboard;
        if (warriors_needed > 0) {
            if (getIdleWarriors() != null && getIdleWarriors().length > 0) {
                getOwner().setTarget(firstN(getIdleWarriors(), warriors_needed), ship, Action.MOVE, false);
            } else {
                nodeDeployUnitsInArmory(warriors_needed);
            }
        }
    }

    private void nodeDeployPeonsFromQuarters(@NonNull Ship ship, int num_peons) {
        if (getQuarters() == null)
            return;
        deployPeonsFromQuarters((Building) getQuarters()[0], num_peons, ship);
    }

    private int deployPeonsFromQuarters(@NonNull Building quarters, int num_peons, @NonNull Target rally) {
        if (quarters.isDead())
            return 0;

        int available = quarters.getUnitContainer().getNumSupplies() - MIN_UNITS_REPRODUCING[difficulty];
        if (available <= 0)
            return 0;

        int ordered = Math.min(num_peons, available);
        quarters.setRallyPoint(rally);
        getOwner().deployUnits(quarters, DeployType.PEON, ordered);
        return ordered;
    }

    private @NonNull Selectable<?> @NonNull [] firstN(@NonNull Selectable<?> @NonNull [] list, int n) {
        n = Math.min(n, list.length);
        Selectable<?>[] result = Selectable.newArray(n);
        System.arraycopy(list, 0, result, 0, n);
        return result;
    }

    private @NonNull Selectable<?> @NonNull [] lastN(@NonNull Selectable<?> @NonNull [] list, int n) {
        n = Math.min(n, list.length);
        Selectable<?>[] result = Selectable.newArray(n);
        System.arraycopy(list, list.length - n, result, 0, n);
        return result;
    }

    private @NonNull Selectable<?> @NonNull [] getPeons(int min_num_peons) {
        var idle = getIdlePeons();
        int idleCount = null != idle ? idle.length : 0;
        var active = Stream.of((Supplier<Selectable<?>[]>) this::getGatherIronPeons, this::getGatherRockPeons,
                this::getGatherTreePeons, this::getGatherRubberPeons).map(Supplier::get).filter(
                        Objects::nonNull).flatMap(Arrays::stream).limit(Math.max(min_num_peons - idleCount, 0));
        return (null != idle ? Stream.concat(Arrays.stream(idle), active) : active).toArray(Selectable[]::new);
    }

    /*	private final int getNumUnitsDeploying() {
            int result = 0;
            if (getArmory() != null) {
                Building armory = (Building)getArmory()[0];
                result += armory.getDeployContainer(DeployType.ROCK_WARRIOR).getNumSupplies();
                result += armory.getDeployContainer(DeployType.IRON_WARRIOR).getNumSupplies();
                result += armory.getDeployContainer(DeployType.RUBBER_WARRIOR).getNumSupplies();
                result += armory.getDeployContainer(DeployType.PEON).getNumSupplies();
            }
            return result;
        }
    */
    private int numWeapons(@NonNull Building armory) {
        return armory.getSupplyContainer(RockAxeWeapon.class).getNumSupplies() + armory.getSupplyContainer(
                IronAxeWeapon.class).getNumSupplies() + armory.getSupplyContainer(
                        RubberAxeWeapon.class).getNumSupplies();
    }

    private @Nullable Target findTarget(int start_x, int start_y) {
        Target best_building = getOwner().findNearestEnemyBuilding(start_x, start_y);
        Target best_target = getOwner().findNearestEnemy(start_x, start_y);
        if (best_building == null) {
            return best_target;
        }
        if (best_target == null) {
            return null;
        }

        int squared_dist_building = (best_building.getGridX() - start_x) * (best_building.getGridX() - start_x) + (best_building.getGridY() - start_y) * (best_building.getGridY() - start_y);
        int squared_dist_target = (best_target.getGridX() - start_x) * (best_target.getGridX() - start_x) + (best_target.getGridY() - start_y) * (best_target.getGridY() - start_y);

        return squared_dist_target < squared_dist_building / 2 ? best_target : best_building;
    }

    private boolean buildShip(Selectable<?> @NonNull [] selection, int grid_x, int grid_y) {
        if (selection.length == 0) {
            return false;
        }
        BuildingSiteScanFilter filter = new BuildingSiteScanFilter(getUnitGrid(),
                getOwner().getRace().getBuildingTemplate(Race.BUILDING_SHIP), 100, true, selection[0].getIslandId());
        getUnitGrid().scan(filter, grid_x, grid_y);
        List<? extends Target> target_list = filter.getResult();
        if (!target_list.isEmpty()) {
            Target target = target_list.getFirst();
            getOwner().placeBuilding(selection, Race.BUILDING_SHIP, target.getGridX(), target.getGridY());
            return true;
        } else {
            return false;
        }
    }

    private boolean buildBuilding(int building_type, Selectable<?> @NonNull [] selection, int grid_x, int grid_y) {
        if (selection.length == 0) {
            return false;
        }
        BuildingSiteScanFilter filter = new BuildingSiteScanFilter(getUnitGrid(),
                getOwner().getRace().getBuildingTemplate(building_type), 40, true, selection[0].getIslandId());
        getUnitGrid().scan(filter, grid_x, grid_y);
        List<? extends Target> target_list = filter.getResult();
        if (!target_list.isEmpty()) {
            Target target = target_list.getFirst();
            getOwner().placeBuilding(selection, building_type, target.getGridX(), target.getGridY());
            return true;
        } else {
            return false;
        }
    }
}
