package com.oddlabs.tt.form;

import com.oddlabs.tt.gui.*;
import com.oddlabs.tt.delegate.Menu;
import com.oddlabs.net.NetworkSelector;

public final strictfp class TerrainMenuForm extends Form implements TerrainMenuListener {
	private final TerrainMenu terrain;

	public TerrainMenuForm(NetworkSelector network, GUIRoot gui_root, Menu main_menu) {
		terrain = new TerrainMenu(network, gui_root, main_menu, false, this);
		addChild(terrain);
		terrain.place();
		compileCanvas();
	}

	public final void setFocus() {
		terrain.getButtonOK().setFocus();
	}

	public void terrainMenuCancel() {
		cancel();
	}
	
	public void terrainMenuOK() {
		
	}
}
