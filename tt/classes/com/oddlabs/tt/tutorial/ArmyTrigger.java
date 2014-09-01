package com.oddlabs.tt.tutorial;

import java.util.Iterator;
import java.util.Set;

import com.oddlabs.tt.form.TutorialForm;
import com.oddlabs.tt.gui.ActionButtonPanel;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.delegate.SelectionDelegate;
import com.oddlabs.tt.delegate.TutorialOverDelegate;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class ArmyTrigger extends TutorialTrigger {
	private final static int ARMY_SIZE = 10;
	
	public ArmyTrigger(Player local_player) {
		super(1f, 0f, "army", new Object[]{new Integer(ARMY_SIZE)});
		local_player.enableMoving(true);
	}

	protected final void run(Tutorial tutorial) {
		Set set = tutorial.getViewer().getLocalPlayer().getUnits().getSet();
		Iterator it = set.iterator();
		int count = 0;
		while (it.hasNext()) {
			Selectable s = (Selectable)it.next();
			if (s instanceof Unit && s.getAbilities().hasAbilities(Abilities.THROW)) {
				count++;
			}
		}
		if (count >= ARMY_SIZE)
			tutorial.done(TutorialForm.TUTORIAL_ARMORY);
	}
}
