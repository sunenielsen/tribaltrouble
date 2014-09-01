package com.oddlabs.tt.resource;

import java.util.List;
import java.util.ArrayList;

public abstract strictfp class NativeResource {
	private final static Object list_lock = new Object();
	private final static List finalized_resources = new ArrayList();
	private static int count;

	public NativeResource() {
		count++;
	}

	protected final void finalize() {
		synchronized (list_lock) {
			finalized_resources.add(this);
		}
	}

	public final static void deleteFinalized() {
		synchronized (list_lock) {
			for (int i = 0; i < finalized_resources.size(); i++) {
				NativeResource r = (NativeResource)finalized_resources.get(i);
				count--;
				r.doDelete();
			}
			finalized_resources.clear();
		}
	}
	
	public final static void gc() {
		System.gc();
		Runtime.getRuntime().runFinalization();
		deleteFinalized();
	}

	public final static int getCount() {
		return count;
	}

	protected abstract void doDelete();
}
