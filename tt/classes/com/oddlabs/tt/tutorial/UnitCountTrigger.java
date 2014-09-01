package com.oddlabs.tt.tutorial;

import java.util.Iterator;
import java.util.Set;

import com.oddlabs.tt.form.TutorialForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.delegate.TutorialOverDelegate;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class UnitCountTrigger extends TutorialTrigger {
	private final int target_count;
	
	public UnitCountTrigger(int target_count) {
		super(1f, 0f, "unit_count", new Object[]{new Integer(target_count)});
		this.target_count = target_count;
	}

	protected final void run(Tutorial tutorial) {
		Set set = tutorial.getViewer().getLocalPlayer().getUnits().getSet();
		Iterator it = set.iterator();
		int count = 0;
		while (it.hasNext()) {
			Selectable s = (Selectable)it.next();
			if (s instanceof Unit)
				count++;
		}
		if (count >= target_count)
			tutorial.done(TutorialForm.TUTORIAL_QUARTERS);
	}
}
