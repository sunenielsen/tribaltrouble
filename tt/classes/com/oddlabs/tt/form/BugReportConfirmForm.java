package com.oddlabs.tt.form;

import com.oddlabs.tt.gui.*;
import com.oddlabs.tt.guievent.*;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.Utils;

import java.util.ResourceBundle;

public final strictfp class BugReportConfirmForm extends Form {
	private final GUIObject focus;
	
	public BugReportConfirmForm() {
		ResourceBundle bundle = ResourceBundle.getBundle(BugReportConfirmForm.class.getName());
		String confirm_str = Utils.getBundleString(bundle, "confirm_report");
		LabelBox info_label = new LabelBox(confirm_str, Skin.getSkin().getEditFont(), 400);
		addChild(info_label);
		HorizButton ok_button = new OKButton(80);
		HorizButton cancel_button = new CancelButton(80);
		focus = ok_button;
		addChild(ok_button);
		addChild(cancel_button);
		cancel_button.place(ORIGIN_BOTTOM_RIGHT);
		ok_button.place(cancel_button, LEFT_MID);
		ok_button.addMouseClickListener(new OKListener());
		cancel_button.addMouseClickListener(new CancelListener(this));
		// Place objects
		info_label.place();

		// headline
		compileCanvas();
		centerPos();
	}

	public final void setFocus() {
		focus.setFocus();
	}
	
	private final strictfp class OKListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			remove();
			Settings.getSettings().crashed = true;
			Renderer.shutdown();
		}
	}
}
