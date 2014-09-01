package com.oddlabs.tt.pathfinder;

public strictfp interface Movable {
	boolean isMoving();
	int getGridX();
	int getGridY();
	void setPosition(float x, float y);
	void setDirection(float dir_x, float dir_y);
	float getPositionX();
	float getPositionY();
	PathTracker getTracker();
	void free();
	void setGridPosition(int grid_x, int grid_y);
	void occupy();
	void markBlocking();
	boolean isDead();
}
