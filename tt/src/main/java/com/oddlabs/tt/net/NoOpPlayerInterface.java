package com.oddlabs.tt.net;

import com.oddlabs.tt.model.Action;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.DeployType;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Supply;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.player.PlayerInterface;
import com.oddlabs.tt.util.Target;
import org.jspecify.annotations.NonNull;

/**
 * A no-op player interface for spectators — all commands are ignored.
 */
public final class NoOpPlayerInterface implements PlayerInterface {
    @Override
    public void deployUnits(@NonNull Building building, @NonNull DeployType type, int num_units) {
    }

    @Override
    public void createHarvesters(@NonNull Building building, int num_tree, int num_rock, int num_iron, int num_rubber) {
    }

    @Override
    public void buildRockWeapons(@NonNull Building building, int num_weapons, boolean infinite) {
    }

    @Override
    public void buildIronWeapons(@NonNull Building building, int num_weapons, boolean infinite) {
    }

    @Override
    public void buildRubberWeapons(@NonNull Building building, int num_weapons, boolean infinite) {
    }

    @Override
    public void doMagic(@NonNull Unit chieftain, int magic) {
    }

    @Override
    public void exitTower(@NonNull Building building) {
    }

    @Override
    public void trainChieftain(@NonNull Building building, boolean start) {
    }

    @Override
    public void deployChieftainFromShip(@NonNull Building building) {
    }

    @Override
    public void placeBuilding(Selectable<?> @NonNull [] selection, int template_id, int placing_grid_x,
            int placing_grid_y) {
    }

    @Override
    public void setRallyPoint(@NonNull Building building, @NonNull Target target) {
    }

    @Override
    public void setTarget(Selectable<?> @NonNull [] selection, @NonNull Target target, @NonNull Action action,
            boolean aggressive) {
    }

    @Override
    public void setRallyPoint(@NonNull Building building, int grid_x, int grid_y) {
    }

    @Override
    public void setSailingTarget(Selectable<?> @NonNull [] selection, @NonNull Target target) {
    }

    @Override
    public void setSailingTarget(Selectable<?> @NonNull [] selection, int grid_x, int grid_y) {
    }

    @Override
    public void setLandscapeTarget(Selectable<?> @NonNull [] selection, int grid_x, int grid_y, @NonNull Action action,
            boolean aggressive) {
    }

    @Override
    public void recallGatherers(@NonNull Building building, @NonNull Class<? extends Supply> supply_type, int amount) {
    }

    @Override
    public void setPreferredGamespeed(int speed) {
    }

    @Override
    public void changePreferredGamespeed(int delta) {
    }

    @Override
    public void viewCamera(float target_x, float target_y, float target_z, float horiz_angle, float vert_angle) {
    }

    @Override
    public void viewCursor(float x, float y, boolean on_map) {
    }

    @Override
    public void viewSelection(Selectable<?> @NonNull [] selection) {
    }

    @Override
    public void viewMapMode(boolean on) {
    }

    @Override
    public void viewTargeting(boolean on) {
    }

    @Override
    public void viewPanelMenu(int submenu) {
    }

    @Override
    public void viewPlacing(int building_index, int grid_x, int grid_y, boolean placing) {
    }

    @Override
    public void viewSelectionBox(float x1, float y1, float x2, float y2, boolean active) {
    }

    @Override
    public void viewBeacon(float x, float y) {
    }
}
