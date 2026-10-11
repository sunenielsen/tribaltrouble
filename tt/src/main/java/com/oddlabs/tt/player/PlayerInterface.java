package com.oddlabs.tt.player;

import com.oddlabs.tt.model.Action;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.DeployType;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Supply;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.util.Target;
import org.jspecify.annotations.NonNull;

public interface PlayerInterface {
    void deployUnits(@NonNull Building building, @NonNull DeployType type, int num_units);

    /*	void deployPeons(Building building, int num_units);
        void deployRockWarriors(Building building, int num_units);
        void deployIronWarriors(Building building, int num_units);
        void deployRubberWarriors(Building building, int num_units);*/
    void createHarvesters(@NonNull Building building, int num_tree, int num_rock, int num_iron, int num_rubber);

    void buildRockWeapons(@NonNull Building building, int num_weapons, boolean infinite);

    void buildIronWeapons(@NonNull Building building, int num_weapons, boolean infinite);

    void buildRubberWeapons(@NonNull Building building, int num_weapons, boolean infinite);

    void doMagic(@NonNull Unit chieftain, int magic);

    void exitTower(@NonNull Building building);

    void trainChieftain(@NonNull Building building, boolean start);

    void deployChieftainFromShip(@NonNull Building building);

    void placeBuilding(Selectable<?> @NonNull [] selection, int template_id, int placing_grid_x, int placing_grid_y);

    void setRallyPoint(@NonNull Building building, @NonNull Target target);

    void setTarget(Selectable<?> @NonNull [] selection, @NonNull Target target, @NonNull Action action,
            boolean aggressive);

    void setRallyPoint(@NonNull Building building, int grid_x, int grid_y);

    void setSailingTarget(Selectable<?> @NonNull [] selection, @NonNull Target target);

    void setSailingTarget(Selectable<?> @NonNull [] selection, int grid_x, int grid_y);

    void setLandscapeTarget(Selectable<?> @NonNull [] selection, int grid_x, int grid_y, @NonNull Action action,
            boolean aggressive);

    void recallGatherers(@NonNull Building building, @NonNull Class<? extends Supply> supply_type, int amount);

    void setPreferredGamespeed(int speed);

    void changePreferredGamespeed(int delta);

    void viewCamera(float target_x, float target_y, float target_z, float horiz_angle, float vert_angle);

    void viewCursor(float x, float y, boolean on_map);

    void viewSelection(Selectable<?> @NonNull [] selection);

    void viewMapMode(boolean on);

    void viewTargeting(boolean on);

    void viewPanelMenu(int submenu);

    void viewPlacing(int building_index, int grid_x, int grid_y, boolean placing);

    void viewSelectionBox(float x1, float y1, float x2, float y2, boolean active);

    void viewBeacon(float x, float y);
}
