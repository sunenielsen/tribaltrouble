package com.oddlabs.tt.viewer;

import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.delegate.InGameMainMenu;
import com.oddlabs.tt.delegate.GameStatsDelegate;

public strictfp interface InGameInfo {
	void addGUI(WorldViewer viewer, InGameMainMenu menu, Group game_infos);
	void addGameOverGUI(WorldViewer viewer, GameStatsDelegate delegate, int header_y, Group buttons);
	void abort(WorldViewer viewer);
	void close(WorldViewer viewer);
	boolean isMultiplayer();
	boolean isRated();
	float getRandomStartPosition();
}
