package com.oddlabs.tt.pathfinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.landscape.HeightMap;

public final strictfp class Region extends Node {
	private final Map object_lists = new HashMap();
	private final List neighbours = new ArrayList();

	private int center_x;
	private int center_y;

	public final int getGridX() {
		return center_x;
	}

	public final int getGridY() {
		return center_y;
	}

	public final void setPosition(int center_x, int center_y) {
		this.center_x = center_x;
		this.center_y = center_y;
	}
	
	public String toString() {
		return "Region: " + center_x + " " + center_y;
	}
	public final PathNode newPath() {
		Node graph_node = this;
		assert graph_node != null;
		RegionNode current_node = null;
		while (graph_node != null) {
			current_node = new RegionNode(current_node, (Region)graph_node);
			graph_node = graph_node.getParent();
		}
		return current_node;
	}

	public final static void link(Region r1, Region r2) {
		if (r1 == null || r2 == null || r1 == r2 || r1.neighbours.contains(r2))
			return;
		r1.addNeighbour(r2);
		r2.addNeighbour(r1);
	}

	public final List getObjects(Class key) {
		List list = (List)object_lists.get(key);
		if (list == null) {
			list = new ArrayList();
			object_lists.put(key, list);
		}
		return list;
	}

	public final void registerObject(Class key, Object object) {
		getObjects(key).add(object);
	}

	public final void unregisterObject(Class key, Object object) {
		List list = (List)object_lists.get(key);
		list.remove(object);
	}

	private final void addNeighbour(Region n) {
		neighbours.add(n);
	}

	public final boolean addNeighbours(PathFinderAlgorithm finder, UnitGrid unit_grid) {
		for (int i = 0; i < neighbours.size(); i++) {
			Region neighbour = (Region)neighbours.get(i);
			if (!neighbour.isVisited())
				PathFinder.addToOpenList(finder, neighbour, this, estimateCost(neighbour.getGridX(), neighbour.getGridY()));
		}
		return false;
	}

	private final void debugVertex(HeightMap heightmap) {
		float xf = UnitGrid.coordinateFromGrid(getGridX());
		float yf = UnitGrid.coordinateFromGrid(getGridY());
		GL11.glVertex3f(xf, yf, heightmap.getNearestHeight(xf, yf) + 2f);
	}

	public final void debugRenderConnectionsReset() {
		if (!isVisited())
			return;
		setVisited(false);
		for (int i = 0; i < neighbours.size(); i++) {
			Region neighbour = (Region)neighbours.get(i);
			neighbour.debugRenderConnectionsReset();
		}
	}

	public final void debugRenderConnections(HeightMap heightmap) {
		if (isVisited())
			return;
		setVisited(true);
		for (int i = 0; i < neighbours.size(); i++) {
			Region neighbour = (Region)neighbours.get(i);
			debugVertex(heightmap);
			neighbour.debugVertex(heightmap);
			neighbour.debugRenderConnections(heightmap);
		}
	}
}
