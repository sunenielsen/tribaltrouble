package com.oddlabs.tt.pathfinder;

public final strictfp class DirectionNode {
	private final float inv_length;
	private final int direction_x;
	private final int direction_y;

	public DirectionNode(float inv_length, int direction_x, int direction_y) {
		this.inv_length = inv_length;
		this.direction_x = direction_x;
		this.direction_y = direction_y;
	}

	public final float getInvLength() {
		return inv_length;
	}

	public final int getDirectionX() {
		return direction_x;
	}

	public final int getDirectionY() {
		return direction_y;
	}
}
