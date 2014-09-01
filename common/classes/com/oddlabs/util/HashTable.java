package com.oddlabs.util;

public final strictfp class HashTable {
	private final static int DEFAULT_INITIAL_ENTRIES = 10;
	private final static int DEFAULT_MUL_FACTOR = 2;
	private final static float DEFAULT_LOAD_FACTOR = 0.75f;

	private LinkedList[] entries;
	private float load_factor;
	private int num_entries;
	private int mul_factor;

	public HashTable() {
		load_factor = DEFAULT_LOAD_FACTOR;
		mul_factor = DEFAULT_MUL_FACTOR;
		entries = new LinkedList[DEFAULT_INITIAL_ENTRIES];
		num_entries = 0;
	}

	public final int size() {
		return num_entries;
	}

	private final int hash(int key) {
		int hash = key % entries.length;
		if (hash >= 0)
			return hash;
		else
			return hash + entries.length;
	}

	public final Object put(int key, Object val) {
		int hash = hash(key);
		if (entries[hash] == null) {
			entries[hash] = new LinkedList();
		} else {
			HashEntry current_entry = (HashEntry)entries[hash].getFirst();
			while (current_entry != null) {
				int current_key = current_entry.getKey();
				if (current_key == key) {
					Object result = current_entry.setEntry(val);
					return result;
				}
				current_entry = (HashEntry)current_entry.getNext();
			}
		}
		HashEntry hash_entry = new HashEntry(key, val);
		entries[hash].addLast(hash_entry);
		num_entries++;
		if (num_entries > load_factor*entries.length)
			rehash();
		return null;
	}

	public final Object get(int key) {
		int hash = hash(key);
		if (entries[hash] == null)
			return null;
		HashEntry current_entry = (HashEntry)entries[hash].getFirst();
		while (current_entry != null) {
			int current_key = current_entry.getKey();
			if (current_key == key)
				return current_entry.getEntry();
			current_entry = (HashEntry)current_entry.getNext();
		}
		return null;
	}

	public final Object remove(int key) {
		int hash = hash(key);

		if (entries[hash] == null)
			return null;
		HashEntry current_entry = (HashEntry)entries[hash].getFirst();
		while (current_entry != null) {
			int current_key = current_entry.getKey();
			if (current_key == key) {
				Object result = current_entry.getEntry();
				entries[hash].remove(current_entry);
				return result;
			}
			current_entry = (HashEntry)current_entry.getNext();
		}
		return null;
	}

	private final void rehash() {
		LinkedList[] old_entries = entries;
		entries = new LinkedList[entries.length*mul_factor];
		for (int i = 0; i < old_entries.length; i++) {
			if (old_entries[i] != null) {
				HashEntry current_entry = (HashEntry)old_entries[i].getFirst();
				while (current_entry != null) {
					int hash = hash(current_entry.getKey());
					HashEntry next_entry = (HashEntry)current_entry.getNext();
					if (entries[hash] == null)
						entries[hash] = new LinkedList();
					entries[hash].addLast(current_entry);
					current_entry = next_entry;
				}
			}
		}
	}
}
