package com.oddlabs.util;

public final strictfp class HashEntry extends ListElementImpl {
	private Object hash_entry;
	private int key;

	public HashEntry(int key, Object entry) {
		this.key = key;
		this.hash_entry = entry;
	}

	public final Object getEntry() {
		return hash_entry;
	}

	public final Object setEntry(Object entry) {
		Object old = hash_entry;
		hash_entry = entry;
		return old;
	}

	public final int getKey() {
		return key;
	}
}
