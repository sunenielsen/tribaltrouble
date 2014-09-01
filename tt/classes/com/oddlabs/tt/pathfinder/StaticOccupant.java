package com.oddlabs.tt.pathfinder;

public final strictfp class StaticOccupant implements Occupant {
	public final int getPenalty() {
		return Occupant.STATIC;
	}

	public final int getGridX() {
		throw new RuntimeException();
	}

	public final int getGridY() {
		throw new RuntimeException();
	}

	public final float getPositionX() {
		throw new RuntimeException();
	}

	public final float getPositionY() {
		throw new RuntimeException();
	}

	public final float getSize() {
		throw new RuntimeException();
	}

	public final boolean isDead() {
		throw new RuntimeException();
	}

	public final void startRespond() {
		throw new RuntimeException();
	}
	
	public final void stopRespond() {
		throw new RuntimeException();
	}
}
