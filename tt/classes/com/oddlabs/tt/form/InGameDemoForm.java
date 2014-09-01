package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.tt.gui.BuyButton;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIImage;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.KeyboardEvent;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.LabelBox;
import com.oddlabs.tt.gui.OKListener;
import com.oddlabs.tt.gui.Renderable;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.util.Utils;

public final strictfp class InGameDemoForm extends DemoForm {
	private final WorldViewer viewer;

	public InGameDemoForm(WorldViewer viewer, String header, GUIImage img, String text) {
		super(viewer.getGUIRoot(), header, img, text);
		this.viewer = viewer;
	}

	protected final void doAdd() {
		super.doAdd();
		viewer.setPaused(true);
	}

	protected final void doRemove() {
		super.doRemove();
		viewer.setPaused(false);
	}
}
