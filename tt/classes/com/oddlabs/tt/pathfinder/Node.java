package com.oddlabs.tt.pathfinder;

abstract strictfp class Node {
/*	private int x;
	private int y;*/
	private int cost;
	private int total_cost;
	private Node parent;
	private boolean visited;

	protected Node() {
		reset();
	}

/*	public final String toString() {
		return x + " " + y;
	}

	protected final void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public final int getGridX() {
		return x;
	}

	public final int getGridY() {
		return y;
	}
*/
	public final Node getParent() {
		return parent;
	}

	public final void reset() {
		visited = false;
	}

	public final void setPathInitial(int initial_cost) {
		cost = 0;
		parent = null;
		setTotalCost(initial_cost);
	}

	public final void setPath(Node parent, int cost, int estimated_cost) {
		this.cost = parent.cost + cost;
		this.parent = parent;
		setTotalCost(this.cost + estimated_cost);
	}

	protected final void setVisited(boolean visited) {
		this.visited = visited;
	}

	private final void setTotalCost(int total_cost) {
		assert !this.visited;
		setVisited(true);
		this.total_cost = total_cost;
	}

	public final int getTotalCost() {
		return total_cost;
	}

	public final boolean isVisited() {
		return visited;
	}

	public final int estimateCost(int dest_x, int dest_y) {
		int vec_x = dest_x - getGridX();
		int vec_y = dest_y - getGridY();
		int abs_dx = StrictMath.abs(vec_x);
		int abs_dy = StrictMath.abs(vec_y);
		int max;
		int min;
		if (abs_dx > abs_dy) {
			max = abs_dx;
			min = abs_dy;
		} else {
			max = abs_dy;
			min = abs_dx;
		}
		return RegionBuilder.STRAIGHT*(max - min) + RegionBuilder.DIAGONAL*min;
//		return StrictMath.max(StrictMath.abs(vec_x), StrictMath.abs(vec_y));
//		return StrictMath.abs(vec_x) + StrictMath.abs(vec_y);
	}

	public abstract boolean addNeighbours(PathFinderAlgorithm finder, UnitGrid unit_grid);
	public abstract PathNode newPath();
	public abstract int getGridX();
	public abstract int getGridY();
}
