package com.oddlabs.tt.model;

import com.oddlabs.tt.model.weapon.*;
import com.oddlabs.tt.particle.Emitter;
import com.oddlabs.tt.particle.Lightning;
import com.oddlabs.tt.landscape.LandscapeTargetRespond;

public strictfp interface ElementVisitor {
	void visitUnit(Unit selectable);
	void visitBuilding(Building selectable);
	void visitEmitter(Emitter emitter);
	void visitLightning(Lightning lightning);
	void visitRespond(LandscapeTargetRespond respond);
	void visitSupplyModel(SupplyModel model);
	void visitSceneryModel(SceneryModel model);
	void visitRubberSupply(RubberSupply model);
	void visitDirectedThrowingWeapon(DirectedThrowingWeapon model);
	void visitRotatingThrowingWeapon(RotatingThrowingWeapon model);
	void visitPlants(Plants plants);
}
