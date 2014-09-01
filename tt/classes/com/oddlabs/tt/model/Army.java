package com.oddlabs.tt.model;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Iterator;

public strictfp class Army {
	private final Set selection = new LinkedHashSet();

	public final Selectable[] filter(int ability_filter) {
		int count = 0;
		Iterator it = selection.iterator();
		while (it.hasNext()) {
			Selectable s = (Selectable)it.next();
			if (s.getAbilities().hasAbilities(ability_filter))
				count++;
		}
		Selectable[] filtered = new Selectable[count];
		it = selection.iterator();
		int index = 0;
		while (it.hasNext()) {
			Selectable s = (Selectable)it.next();
			if (s.getAbilities().hasAbilities(ability_filter))
				filtered[index++] = s;
		}
		return filtered;
	}

	public final boolean containsAbility(int ability_filter) {
		Iterator it = selection.iterator();
		while (it.hasNext()) {
			Selectable s = (Selectable)it.next();
			if (s.getAbilities().hasAbilities(ability_filter))
				return true;
		}
		return false;
	}

	public void clear() {
		selection.clear();
	}

	public void remove(Selectable selectable) {
		selection.remove(selectable);
	}

	public final boolean contains(Selectable selectable) {
		return selection.contains(selectable);
	}

	public final Set getSet() {
		return selection;
	}

	public void add(Selectable selectable) {
		selection.add(selectable);
	}

	public final int size() {
		return selection.size();
	}
}
