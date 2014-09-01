package com.oddlabs.tt.tutorial;

import com.oddlabs.tt.viewer.InGameInfo;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.gui.GUIObject;
import com.oddlabs.tt.form.TutorialForm;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.delegate.Menu;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.delegate.InGameMainMenu;
import com.oddlabs.tt.delegate.GameStatsDelegate;
import com.oddlabs.tt.render.Renderer;

public final strictfp class TutorialInGameInfo implements InGameInfo {
	private int next_tutorial = -1;

	public final boolean setNextTutorial(GUIRoot gui_root, int next_tutorial) {
		if (TutorialForm.checkTutorial(gui_root, next_tutorial)) {
			this.next_tutorial = next_tutorial;
			return true;
		} else
			return false;
	}

	public final boolean isRated() {
		return false;
	}

	public final boolean isMultiplayer() {
		return false;
	}

	public final float getRandomStartPosition() {
		return 0f;
	}

	public final void addGUI(WorldViewer viewer, InGameMainMenu menu, Group game_infos) {
		menu.addAbortButton(Utils.getBundleString(Menu.bundle, "end_tutorial"));
	}

	public final void addGameOverGUI(WorldViewer viewer, GameStatsDelegate delegate, int header_y, Group group) {
		throw new RuntimeException("Not implemented");
	}

	public final void abort(WorldViewer viewer) {
		next_tutorial = -1;
		viewer.close();
	}

	public final void close(WorldViewer viewer) {
		if (next_tutorial != -1)
			TutorialForm.startTutorial(viewer.getNetwork(), viewer.getGUIRoot(), next_tutorial);
		else
			Renderer.startMenu(viewer.getNetwork(), viewer.getGUIRoot().getGUI());
	}
}
