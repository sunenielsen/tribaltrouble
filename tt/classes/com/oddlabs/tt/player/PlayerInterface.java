package com.oddlabs.tt.player;

import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.util.Target;

public strictfp interface PlayerInterface {
	void deployUnits(Building building, int type, int num_units);
/*	void deployPeons(Building building, int num_units);
	void deployRockWarriors(Building building, int num_units);
	void deployIronWarriors(Building building, int num_units);
	void deployRubberWarriors(Building building, int num_units);*/
	void createHarvesters(Building building, int num_tree, int num_rock, int num_iron, int num_rubber);
	void buildRockWeapons(Building building, int num_weapons, boolean infinite);
	void buildIronWeapons(Building building, int num_weapons, boolean infinite);
	void buildRubberWeapons(Building building, int num_weapons, boolean infinite);
	void doMagic(Unit chieftain, int magic);
	void exitTower(Building building);
	void trainChieftain(Building building, boolean start);
	void placeBuilding(Selectable[] selection, int template_id, int placing_grid_x, int placing_grid_y);
	void setRallyPoint(Building building, Target target);
	void setTarget(Selectable[] selection, Target target, int action, boolean aggressive);
	void setRallyPoint(Building building, int grid_x, int grid_y);
	void setLandscapeTarget(Selectable[] selection, int grid_x, int grid_y, int action, boolean aggressive);
	void setPreferredGamespeed(int speed);
	void changePreferredGamespeed(int delta);
}
