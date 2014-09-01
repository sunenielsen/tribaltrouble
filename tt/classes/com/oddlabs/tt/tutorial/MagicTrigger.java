package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.form.TutorialForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.delegate.TutorialOverDelegate;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class MagicTrigger extends TutorialTrigger {
	private final boolean[] magic_used = new boolean[RacesResources.NUM_MAGIC];
	
	private final Unit chieftain;
	
	public MagicTrigger(Unit chieftain) {
		super(.1f, 20f, "magic");
		this.chieftain = chieftain;
	}

	protected final void run(Tutorial tutorial) {
		int last = chieftain.getLastMagicIndex();
		if (last != -1)
			magic_used[last] = true;
		for (int i = 0; i < magic_used.length; i++)
			if (!magic_used[i])
				return;
		tutorial.done(TutorialForm.TUTORIAL_CHIEFTAIN);
	}
}
