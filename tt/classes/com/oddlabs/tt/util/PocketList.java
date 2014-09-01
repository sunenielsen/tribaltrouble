package com.oddlabs.tt.util;

import java.util.*;

public final strictfp class PocketList {
	private final List[] pockets;
	private int min_list_index;
	private int max_list_index;
	private int size;

	public PocketList(int num_pockets) {
		pockets = new List[num_pockets];
		for (int i = 0; i < pockets.length; i++)
			pockets[i] = new ArrayList();
		reset();
	}

	public final void add(int cost, Object obj) {
		if (cost >= pockets.length)
			cost = pockets.length - 1;
		pockets[cost].add(obj);
		if (cost < min_list_index)
			min_list_index = cost;
		if (cost > max_list_index)
			max_list_index = cost;
		size++;
	}

	public final Object removeBest() {
		List current_pocket = pockets[min_list_index];
		while (current_pocket.isEmpty()) {
			min_list_index++;
			current_pocket = pockets[min_list_index];
		}
		Object node = current_pocket.remove(current_pocket.size() - 1);
		size--;
		return node;
	}

	public final void clear() {
		for (int i = min_list_index; i <= max_list_index; i++)
			pockets[i].clear();
//		check();
		reset();
	}

/*	private final void check() {
		for (int i = 0; i < pockets.length; i++)
			assert pockets[i].isEmpty(): min_list_index + " " + max_list_index + " " + i;
	}
*/
	public final int size() {
		return size;
	}

	private final void reset() {
		min_list_index = pockets.length;
		max_list_index = 0;
		size = 0;
	}
}
