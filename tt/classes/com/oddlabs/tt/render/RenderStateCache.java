package com.oddlabs.tt.render;

import java.util.List;
import java.util.ArrayList;

final class RenderStateCache {
	private final RenderStateFactory factory;
	private final List cache = new ArrayList();
	private int current_index;

	RenderStateCache(RenderStateFactory factory) {
		this.factory = factory;
	}

	final void clear() {
		current_index = 0;
	}

	final Object get() {
		if (current_index == cache.size()) {
			cache.add(factory.create());
		}
		return cache.get(current_index++);
	}
}
