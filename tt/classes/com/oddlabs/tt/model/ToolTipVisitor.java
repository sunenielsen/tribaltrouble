package com.oddlabs.tt.model;

public strictfp interface ToolTipVisitor {
	void visitUnit(Unit unit);
	void visitBuilding(Building building);
	void visitSupply(Supply model);
	void visitSceneryModel(SceneryModel model);
}
