package com.oddlabs.tt.util;

import com.oddlabs.tt.net.Distributable;

public strictfp interface Target extends Distributable {
	public final static int ACTION_DEFAULT = 1;
	public final static int ACTION_MOVE = 2;
	public final static int ACTION_ATTACK = 3;
	public final static int ACTION_GATHER_REPAIR = 4;
	public final static int ACTION_DEFEND = 5;

	int getGridX();
	int getGridY();
	float getPositionX();
	float getPositionY();
	float getSize();
	boolean isDead();
}
