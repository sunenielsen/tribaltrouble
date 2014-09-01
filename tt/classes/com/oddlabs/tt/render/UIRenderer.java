package com.oddlabs.tt.render;

import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.util.ToolTip;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.viewer.AmbientAudio;

public strictfp interface UIRenderer {
	void render(AmbientAudio ambient, CameraState camera_state, GUIRoot gui_root);
	void pickHover(boolean can_hover_behind, CameraState camera, int x, int y);
	ToolTip getToolTip();
	void renderGUI(GUIRoot gui_root);
	boolean clearColorBuffer();
}
