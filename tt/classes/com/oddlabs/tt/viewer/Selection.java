package com.oddlabs.tt.viewer;

import com.oddlabs.tt.model.Army;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.player.Player;

import java.util.Iterator;

public final strictfp class Selection {
	private final Army[] shortcut_armies = new Army[10];
	private final Player local_player;
	private SelectionArmy current_selection;

	public Selection(Player local_player) {
		this.local_player = local_player;
		clearSelection();
	}

	public final SelectionArmy getCurrentSelection() {
		return current_selection;
	}

	public final void clearSelection() {
		current_selection = new SelectionArmy(local_player);
	}

	public final void clearShortcutArmies() {
		for (int i = 0; i < shortcut_armies.length; i++) {
			shortcut_armies[i] = null;
		}
	}

	final void removeFromArmies(Selectable selectable) {
		current_selection.remove(selectable);
		for (int i = 0; i < shortcut_armies.length; i++) {
			if (shortcut_armies[i] != null)
				shortcut_armies[i].remove(selectable);
		}
	}

	public final void setShortcutArmy(int index) {
		if (shortcut_armies[index] != null)
			shortcut_armies[index].clear();
		else
			shortcut_armies[index] = new Army();

		Iterator it = current_selection.getSet().iterator();
		while (it.hasNext()) {
			Selectable s = (Selectable)it.next();
			shortcut_armies[index].add(s);
		}
	}

	public final boolean enableShortcutArmy(int index) {
		boolean empty = true;
		if (shortcut_armies[index] != null) {
			current_selection.clear();
			Iterator it = shortcut_armies[index].getSet().iterator();
			while (it.hasNext()) {
				Selectable s = (Selectable)it.next();
				current_selection.add(s);
				empty = false;
			}
		}
		return !empty;
	}
}
