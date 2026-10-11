package com.oddlabs.tt.model;

import com.oddlabs.tt.model.weapon.IronAxeWeapon;
import com.oddlabs.tt.model.weapon.IronSpearWeapon;
import com.oddlabs.tt.model.weapon.RockAxeWeapon;
import com.oddlabs.tt.model.weapon.RockSpearWeapon;
import com.oddlabs.tt.model.weapon.RubberAxeWeapon;
import com.oddlabs.tt.model.weapon.RubberSpearWeapon;


import org.joml.Vector2f;
import org.joml.Vector3f;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ShipHR {
    private static final float WARRIOR_INCREASE = 10f;

    private final boolean vikings;

    protected float unitSize(Unit unit) {
        if (unit.isWarrior()) {
            return 1.20f;
        } else {
            return 0.70f;
        }
    }

    interface Row {
        public abstract boolean canFit(Unit unit);

        public abstract void seat(Unit unit);

        public abstract void exit(Unit unit);

        public abstract ShipAllocation getAllocation(Unit unit);

        public abstract void killAll();

        public abstract List<Unit> allUnits();

        public abstract Unit findUnit(UnitTemplate template);

        public abstract boolean needRowers();

        public abstract boolean canSparePeons();

        public abstract int countRowers();

        public abstract boolean empty();
    }

    class Rudder implements Row {
        private Unit unit = null;
        private ShipAllocation alloc;

        public Rudder(Ship ship, float x, float y, float z) {
            alloc = new ShipAllocation(new Vector3f(x, y, z), new Vector2f(0.0f, 1.0f), ShipAllocation.STEERING);
            var owner = ship.getOwner();
            unit = new Unit(owner, x, y, null, owner.getRace().getUnitTemplate(Race.UNIT_PEON), null, false, false,
                    true);
            unit.mount(ship, alloc);
        }

        public boolean canFit(Unit unit) {
            return false;
        }

        public void seat(Unit unit) {
            assert (false);
        }

        public void exit(Unit unit) {
            assert (false);
        }

        public ShipAllocation getAllocation(Unit unit) {
            if (this.unit == unit) {
                return alloc;
            } else {
                return null;
            }
        }

        public void killAll() {
            unit.drown();
            unit = null;
        }

        public List<Unit> allUnits() {
            return new ArrayList<Unit>();
        }

        public Unit findUnit(UnitTemplate template) {
            return null;
        }

        public boolean needRowers() {
            return false;
        }

        public boolean canSparePeons() {
            return false;
        }

        public int countRowers() {
            return 0;
        }

        public boolean empty() {
            return unit == null;
        }
    }

    class LowerDeckRow implements Row {
        private final float x;
        private final float y0;
        private final float y1;
        private final float z;
        private final float total;
        private final boolean left_rower;
        private final boolean right_rower;
        private float used;
        private final HashMap<Unit, ShipAllocation> allocs = new HashMap<>();
        private final List<Unit> all_units = new ArrayList<>();
        private final List<Unit> peons = new ArrayList<>();
        private final List<Unit> warriors = new ArrayList<>();

        public LowerDeckRow(float x, float y0, float y1, float z, boolean left_rower, boolean right_rower) {
            this.x = x;
            this.y0 = y0;
            this.y1 = y1;
            this.z = z;
            this.total = Math.abs(y1 - y0);
            this.used = 0.0f;
            this.left_rower = left_rower;
            this.right_rower = right_rower;
        }

        public boolean canFit(Unit unit) {
            if (unit.isChieftain()) {
                return false;
            }
            float size = unitSize(unit);
            return total - used > size;
        }

        public void seat(Unit unit) {
            if (!allocs.containsKey(unit)) {
                float size = unitSize(unit);
                allocs.put(unit, new ShipAllocation());
                all_units.add(unit);
                if (unit.isWarrior()) {
                    warriors.add(unit);
                } else if (!unit.isChieftain()) {
                    peons.add(unit);
                }
                reassign();
            }
        }

        public void exit(Unit unit) {
            if (allocs.containsKey(unit)) {
                allocs.remove(unit);
                all_units.remove(unit);
                peons.remove(unit);
                warriors.remove(unit);
                reassign();
                assert (used >= 0.0f);
            }
        }

        public void killAll() {
            for (int i = 0; i < all_units.size(); i++) {
                Unit unit = all_units.get(i);
                unit.drown();
            }
            all_units.clear();
            peons.clear();
            warriors.clear();
            allocs.clear();
        }

        public ShipAllocation getAllocation(Unit unit) {
            if (allocs.containsKey(unit)) {
                return allocs.get(unit);
            } else {
                return null;
            }
        }

        public List<Unit> allUnits() {
            return all_units;
        }

        public Unit findUnit(UnitTemplate template) {
            for (int i = 0; i < all_units.size(); i++) {
                Unit unit = all_units.get(i);
                if (unit.getTemplate() == template) {
                    return unit;
                }
            }
            return null;
        }

        public boolean needRowers() {
            int required = (left_rower ? 1 : 0) + (right_rower ? 1 : 0);
            return peons.size() < required;
        }

        public boolean canSparePeons() {
            int required = (left_rower ? 1 : 0) + (right_rower ? 1 : 0);
            return peons.size() > required;
        }

        public int countRowers() {
            int required = (left_rower ? 1 : 0) + (right_rower ? 1 : 0);
            return Math.min(peons.size(), required);
        }

        private void reassign() {
            used = 0.0f;
            for (int i = 0; i < all_units.size(); i++) {
                used += unitSize(all_units.get(i));
            }
            if (all_units.size() == 1) {
                Unit unit = all_units.get(0);
                float size = unitSize(unit);
                ShipAllocation alloc = allocs.get(unit);
                alloc.setRotation(1.0f, 0.0f);
                if (unit.isWarrior()) {
                    alloc.setOffset(x, (y0 + y1) * 0.5f, z);
                    alloc.setRole(ShipAllocation.SITTING);
                } else {
                    if (right_rower) {
                        alloc.setOffset(x, y0 + size * 0.5f, z);
                        alloc.setRole(ShipAllocation.ROWING_RIGHT);
                    } else if (left_rower) {
                        alloc.setOffset(x, y1 - size * 0.5f, z);
                        alloc.setRole(ShipAllocation.ROWING_LEFT);
                    } else {
                        alloc.setOffset(x, (y0 + y1) * 0.5f, z);
                        alloc.setRole(ShipAllocation.SITTING);
                    }
                }
            } else if (all_units.size() > 1) {
                float gap = (total - used) / (all_units.size() - 1);
                float yOffset = Math.min(y0, y1);
                int tmpCounter = 0;
                Unit leftRower = null;
                Unit rightRower = null;
                if (left_rower && peons.size() > tmpCounter) {
                    leftRower = peons.get(tmpCounter);
                    tmpCounter++;
                }
                if (right_rower && peons.size() > tmpCounter) {
                    rightRower = peons.get(tmpCounter);
                    tmpCounter++;
                }
                if (rightRower != null) {
                    ShipAllocation alloc = allocs.get(rightRower);
                    float s = unitSize(rightRower);
                    alloc.setOffset(x, yOffset + s * 0.5f, z);
                    alloc.setRole(ShipAllocation.ROWING_RIGHT);
                    yOffset += gap + s;
                }
                for (int i = 0; i < all_units.size(); i++) {
                    Unit unit = all_units.get(i);
                    if (unit == leftRower || unit == rightRower) {
                        continue;
                    }
                    ShipAllocation alloc = allocs.get(unit);
                    float s = unitSize(unit);
                    alloc.setOffset(x, yOffset + s * 0.5f, z);
                    yOffset += gap + s;
                }
                if (leftRower != null) {
                    ShipAllocation alloc = allocs.get(leftRower);
                    float s = unitSize(leftRower);
                    alloc.setOffset(x, yOffset + s * 0.5f, z);
                    alloc.setRole(ShipAllocation.ROWING_LEFT);
                    yOffset += gap + s;
                }
            }
        }

        public boolean empty() {
            return all_units.size() == 0;
        }
    }

    class UpperDeckRow implements Row {
        private Unit left = null;
        private Unit right = null;
        private ShipAllocation leftAlloc;
        private ShipAllocation rightAlloc;
        private Row alt;

        public void setAlt(Row row) {
            alt = row;
        }

        public UpperDeckRow(float x, float y, float z) {
            leftAlloc = new ShipAllocation(new Vector3f(x, y, z), new Vector2f(0.0f, 1.0f), ShipAllocation.FIGHTING);
            rightAlloc = new ShipAllocation(new Vector3f(x, -y, z), new Vector2f(0.0f, -1.0f), ShipAllocation.FIGHTING);
        }

        public boolean canFit(Unit unit) {
            return unit.isWarrior() && !unit.isChieftain() && (left == null || right == null) && (alt == null
                    || alt.empty());
        }

        public void seat(Unit unit) {
            if (canFit(unit)) {
                if (left == null) {
                    left = unit;
                } else if (right == null) {
                    right = unit;
                }
            }
        }

        public void exit(Unit unit) {
            if (left == unit) {
                left = null;
            } else if (right == unit) {
                right = null;
            }
        }

        public void killAll() {
            if (left != null) {
                left.drown();
                left = null;
            }
            if (right != null) {
                right.drown();
                right = null;
            }
        }

        public ShipAllocation getAllocation(Unit unit) {
            if (left == unit) {
                return leftAlloc;
            } else if (right == unit) {
                return rightAlloc;
            }
            return null;
        }

        public List<Unit> allUnits() {
            List<Unit> ret = new ArrayList<>();
            if (left != null) {
                ret.add(left);
            }
            if (right != null) {
                ret.add(right);
            }
            return ret;
        }

        public Unit findUnit(UnitTemplate template) {
            if (left != null && left.getTemplate() == template) {
                return left;
            }
            if (right != null && right.getTemplate() == template) {
                return right;
            }
            return null;
        }

        public boolean needRowers() {
            return false;
        }

        public boolean canSparePeons() {
            return false;
        }

        public int countRowers() {
            return 0;
        }

        public boolean empty() {
            return left == null && right == null;
        }
    }

    class ChieftainRow implements Row {
        private Unit unit = null;
        private ShipAllocation alloc;
        private Row alt1 = null;
        private Row alt2 = null;

        public ChieftainRow(float x, float y, float z) {
            alloc = new ShipAllocation(new Vector3f(x, y, z), new Vector2f(1.0f, 0.0f), ShipAllocation.FIGHTING);
        }

        public void setAlts(Row r1, Row r2) {
            alt1 = r1;
            alt2 = r2;
        }

        public boolean canFit(Unit unit) {
            return unit.isChieftain() && alt1.empty() && alt2.empty();
        }

        public void seat(Unit unit) {
            if (unit.isChieftain()) {
                this.unit = unit;
            }
        }

        public void exit(Unit unit) {
            if (this.unit == unit) {
                this.unit = null;
            }
        }

        public void killAll() {
            if (unit != null) {
                unit.drown();
            }
            unit = null;
        }

        public ShipAllocation getAllocation(Unit unit) {
            if (this.unit == unit) {
                return alloc;
            }
            return null;
        }

        public List<Unit> allUnits() {
            List<Unit> ret = new ArrayList<>();
            if (unit != null) {
                ret.add(unit);
            }
            return ret;
        }

        public Unit findUnit(UnitTemplate template) {
            return null;
        }

        public boolean needRowers() {
            return false;
        }

        public boolean canSparePeons() {
            return false;
        }

        public int countRowers() {
            return 0;
        }

        public boolean empty() {
            return unit == null;
        }
    }

    private LinkedHashMap<Unit, Row> unit2row = new LinkedHashMap<>();
    private ChieftainRow chieftainRow;

    private ArrayList<Row> rows = new ArrayList<>();

    public ShipHR(Ship ship, boolean vikings) {
        this.vikings = vikings;
        if (vikings) {
            rows.add(new Rudder(ship, -11.14f, -0.43f, +0.54f));
            rows.add(new LowerDeckRow(-9.62f, -1.98f, +1.98f, 0.42f, true, true));
            rows.add(new LowerDeckRow(-8.18f, -2.31f, +2.31f, 0.42f, true, true));
            rows.add(new LowerDeckRow(-6.71f, -2.54f, +2.54f, 0.43f, true, true));
            rows.add(new LowerDeckRow(-5.32f, +0.25f, +2.75f, 0.44f, true, false));
            rows.add(new LowerDeckRow(-5.32f, -2.75f, -0.25f, 0.44f, false, true));
            rows.add(new LowerDeckRow(-3.42f, +0.39f, +2.90f, 0.45f, true, false));
            rows.add(new LowerDeckRow(-3.42f, -2.90f, -0.39f, 0.45f, false, true));
            rows.add(new LowerDeckRow(-1.75f, -2.99f, +2.99f, 0.51f, true, true));
            rows.add(new LowerDeckRow(-0.24f, -3.07f, +3.07f, 0.55f, true, true));
            rows.add(new LowerDeckRow(+1.30f, -3.00f, +3.00f, 0.58f, true, true));
            rows.add(new LowerDeckRow(+2.80f, -2.90f, +2.90f, 0.59f, true, true));
            rows.add(new LowerDeckRow(+4.32f, -2.80f, +2.80f, 0.62f, true, true));
            rows.add(new LowerDeckRow(+5.81f, -2.64f, +2.64f, 0.65f, true, true));
            rows.add(new LowerDeckRow(+7.31f, +0.18f, +2.45f, 0.66f, true, false));
            rows.add(new LowerDeckRow(+7.31f, -2.45f, -0.18f, 0.66f, false, true));
            rows.add(new LowerDeckRow(+8.80f, -2.19f, +2.19f, 0.68f, true, true));
            var r1 = new UpperDeckRow(+9.64f, +1.17f, +3.29f);
            var r2 = new UpperDeckRow(+7.89f, +1.36f, +3.29f);
            rows.add(r1);
            rows.add(r2);
            chieftainRow = new ChieftainRow(+8.76f, +0.00f, +3.29f);
            rows.add(chieftainRow);
            chieftainRow.setAlts(r1, r2);
            r1.setAlt(chieftainRow);
            r2.setAlt(chieftainRow);
            rows.add(new UpperDeckRow(+6.53f, +1.60f, +3.29f));
            rows.add(new UpperDeckRow(+5.12f, +1.74f, +3.29f));
            rows.add(new UpperDeckRow(+3.78f, +1.80f, +3.29f));
            rows.add(new UpperDeckRow(+2.10f, +2.07f, +3.11f));
            rows.add(new UpperDeckRow(+0.56f, +2.07f, +3.11f));
            rows.add(new UpperDeckRow(-0.82f, +2.07f, +3.11f));
            rows.add(new UpperDeckRow(-2.60f, +2.07f, +3.11f));
            rows.add(new UpperDeckRow(-5.24f, +1.82f, +3.13f));
            rows.add(new UpperDeckRow(-7.17f, +1.86f, +2.99f));
            rows.add(new UpperDeckRow(-8.99f, +1.31f, +2.99f));
            rows.add(new UpperDeckRow(-10.49f, +1.00f, +2.99f));
        } else {
            rows.add(new Rudder(ship, -11.4f, +0.87f, +2.455f));
            rows.add(new LowerDeckRow(-9.39f, -2.88f, +2.88f, +0.37f, true, true));
            rows.add(new LowerDeckRow(-7.92f, -2.88f, +2.88f, +0.37f, true, true));
            rows.add(new LowerDeckRow(-6.47f, -2.88f, +2.88f, +0.37f, true, true));
            rows.add(new LowerDeckRow(-4.84f, -2.88f, +2.88f, +0.37f, true, true));
            rows.add(new LowerDeckRow(-3.44f, -2.88f, +2.88f, +0.37f, true, true));
            rows.add(new LowerDeckRow(-1.96f, -2.88f, +2.88f, +0.37f, true, true));
            rows.add(new LowerDeckRow(-0.42f, -2.88f, +2.88f, +0.37f, true, true));
            rows.add(new LowerDeckRow(+1.10f, -2.88f, +2.88f, +0.37f, true, true));
            rows.add(new LowerDeckRow(+2.46f, -2.88f, +2.88f, +0.37f, true, true));
            rows.add(new LowerDeckRow(+4.17f, -2.84f, +2.84f, +0.37f, true, true));
            rows.add(new LowerDeckRow(+5.57f, -2.77f, +2.77f, +0.37f, true, true));
            rows.add(new LowerDeckRow(+7.01f, -2.42f, +2.42f, +0.37f, true, true));
            rows.add(new LowerDeckRow(+8.62f, -2.42f, +2.42f, +0.37f, true, true));
            var r1 = new UpperDeckRow(+8.00f, +1.09f, +3.24f);
            var r2 = new UpperDeckRow(+9.50f, +0.83f, +3.24f);
            rows.add(r1);
            rows.add(r2);
            chieftainRow = new ChieftainRow(+8.75f, +0.00f, +3.24f);
            rows.add(chieftainRow);
            r1.setAlt(chieftainRow);
            r2.setAlt(chieftainRow);
            chieftainRow.setAlts(r1, r2);
            rows.add(new UpperDeckRow(-9.39f, +1.01f, +3.41f));
            rows.add(new UpperDeckRow(-7.75f, +1.22f, +3.41f));
            rows.add(new UpperDeckRow(-6.02f, +1.35f, +3.41f));
            rows.add(new UpperDeckRow(-3.93f, +1.22f, +3.24f));
            rows.add(new UpperDeckRow(-2.50f, +1.22f, +3.24f));
            rows.add(new UpperDeckRow(-1.00f, +1.22f, +3.24f));
            rows.add(new UpperDeckRow(+0.50f, +1.22f, +3.24f));
            rows.add(new UpperDeckRow(+2.00f, +1.22f, +3.24f));
            rows.add(new UpperDeckRow(+3.50f, +1.22f, +3.24f));
            rows.add(new UpperDeckRow(+5.00f, +1.22f, +3.24f));
            rows.add(new UpperDeckRow(+6.50f, +1.22f, +3.24f));
        }
    }

    public boolean canAllocate(Unit unit) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).canFit(unit)) {
                return true;
            }
        }
        return false;
    }

    public ShipAllocation tryAllocate(Unit unit) {
        if (!unit.isWarrior() && !unit.isChieftain()) {
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                if (row.needRowers()) {
                    row.seat(unit);
                    unit2row.put(unit, row);
                    return row.getAllocation(unit);
                }
            }
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                if (row.canFit(unit)) {
                    row.seat(unit);
                    unit2row.put(unit, row);
                    return row.getAllocation(unit);
                }
            }
        } else {
            for (int i = rows.size() - 1; i >= 0; i--) {
                Row row = rows.get(i);
                if (row.canFit(unit)) {
                    row.seat(unit);
                    unit2row.put(unit, row);
                    ShipAllocation alloc = row.getAllocation(unit);
                    if (alloc.getRole() == ShipAllocation.FIGHTING) {
                        unit.increaseRange(WARRIOR_INCREASE);
                    }
                    return alloc;
                }
            }
        }
        return null;
    }

    public void killCrew() {
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).killAll();
            unit2row.clear();
        }
    }

    public void removeUnit(Unit unit) {
        if (unit2row.containsKey(unit)) {
            Row row = unit2row.get(unit);
            row.exit(unit);
            unit.setReference(null);
            unit2row.remove(unit);
        }
    }

    public Unit exitUnit(UnitTemplate template) {
        boolean warrior = (template.getWeaponFactory().getType() != null);
        if (warrior) {
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                Unit unit = row.findUnit(template);
                if (unit != null) {
                    ShipAllocation alloc = row.getAllocation(unit);
                    row.exit(unit);
                    unit2row.remove(unit);
                    unit.setReference(null);
                    unit.unmount();
                    if (alloc.getRole() == ShipAllocation.FIGHTING) {
                        unit.increaseRange(-WARRIOR_INCREASE);
                    }
                    return unit;
                }
            }
        } else {
            for (int i = rows.size() - 1; i >= 0; i--) {
                Row row = rows.get(i);
                Unit unit = row.findUnit(template);
                if (unit != null && row.canSparePeons()) {
                    row.exit(unit);
                    unit2row.remove(unit);
                    unit.setReference(null);
                    unit.unmount();
                    return unit;
                }
            }
            for (int i = rows.size() - 1; i >= 0; i--) {
                Row row = rows.get(i);
                Unit unit = row.findUnit(template);
                if (unit != null) {
                    row.exit(unit);
                    unit2row.remove(unit);
                    unit.setReference(null);
                    unit.unmount();
                    return unit;
                }
            }
        }
        return null;
    }

    private boolean isRock(Class type) {
        return type == RockAxeWeapon.class || type == RockSpearWeapon.class;
    }

    private boolean isIron(Class type) {
        return type == IronAxeWeapon.class || type == IronSpearWeapon.class;
    }

    private boolean isRubber(Class type) {
        return type == RubberAxeWeapon.class || type == RubberSpearWeapon.class;
    }

    private boolean isSameType(Class type, Unit unit) {
        if (!unit.isWarrior()) {
            return type == Unit.class;
        } else {
            Class unitType = unit.getWeaponFactory().getType();
            if (isRock(unitType) && isRock(type)) return true;
            if (isIron(unitType) && isIron(type)) return true;
            if (isRubber(unitType) && isRubber(type)) return true;
            return false;
        }
    }

    public int countUnitsOfType(Class type) {
        int result = 0;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            List<Unit> units = row.allUnits();
            for (int j = 0; j < units.size(); j++) {
                Unit unit = units.get(j);
                if (isSameType(type, unit)) {
                    result++;
                }
            }
        }
        return result;
    }

    public int countUnits() {
        return unit2row.size();
    }

    public int countPeons() {
        int result = 0;
        Unit[] units = unit2row.keySet().toArray(new Unit[0]);
        for (int j = 0; j < units.length; j++) {
            Unit unit = units[j];
            if (!unit.isWarrior()) {
                result++;
            }
        }
        return result;
    }

    public Unit pickVictim(float random) {
        int index = StrictMath.round(random * 200);
        if (index >= unit2row.size()) {
            return null;
        }
        Iterator<Map.Entry<Unit, Row>> it = unit2row.entrySet().iterator();
        Map.Entry<Unit, Row> victim = it.next();
        for (int i = 0; i < index; i++) {
            victim = it.next();
        }
        Unit unit = victim.getKey();
        Row row = victim.getValue();
        return unit;
    }

    public Unit exitChieftain() {
        if (hasChieftain()) {
            Unit unit = chieftainRow.allUnits().get(0);
            ShipAllocation alloc = chieftainRow.getAllocation(unit);
            chieftainRow.exit(unit);
            unit2row.remove(unit);
            unit.setReference(null);
            unit.unmount();
            if (alloc.getRole() == ShipAllocation.FIGHTING) {
                unit.increaseRange(-WARRIOR_INCREASE);
            }
            return unit;
        }
        return null;
    }

    public boolean hasChieftain() {
        return !chieftainRow.empty();
    }

    public int countRowers() {
        int result = 0;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            result += row.countRowers();
        }
        return result;
    }
}
