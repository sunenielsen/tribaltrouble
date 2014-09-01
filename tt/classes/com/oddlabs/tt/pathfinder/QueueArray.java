package com.oddlabs.tt.pathfinder;

public final strictfp class QueueArray {
	private final RegionBuilderNode[] queue;
	private int start_index;
	private int end_index;

	public QueueArray(int size) {
		queue = new RegionBuilderNode[size];
		reset();
	}

	public final void reset() {
		start_index = 0;
		end_index = 0;
	}

	public final void addLast(RegionBuilderNode node) {
		queue[end_index] = node;
		end_index++;
	}

	public final RegionBuilderNode removeFirst() {
		assert !isEmpty();
		RegionBuilderNode node = queue[start_index];
		start_index++;
		return node;
	}

	public final boolean isEmpty() {
		return start_index == end_index;
	}
}
