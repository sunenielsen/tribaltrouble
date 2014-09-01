package com.oddlabs.tt.model;

public final strictfp class ChieftainContainer {
	private final static float PROGRESS_AMOUNT = 1f/40f;

	private final Building building;

	private boolean training = false;
	private float progress = 0f;

	public ChieftainContainer(Building building) {
		this.building = building;
	}

	public final float getBuildProgress() {
		return progress;
	}

	public final void startTraining() {
		training = true;
		((ReproduceUnitContainer)building.getUnitContainer()).resetProgress();
	}

	public final void stopTraining() {
		training = false;
		progress = 0f;
		((ReproduceUnitContainer)building.getUnitContainer()).resetProgress();
	}

	public final boolean isTraining() {
		return training;
	}

	public final void progress() {
		progress += PROGRESS_AMOUNT;
		if (progress >= 1f) {
			building.deployChieftain();
		}
	}
}
