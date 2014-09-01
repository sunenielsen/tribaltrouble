package com.oddlabs.tt.form;

import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.render.UIRenderer;

public strictfp interface LoadCallback {
	UIRenderer load(GUIRoot gui_root);
}
