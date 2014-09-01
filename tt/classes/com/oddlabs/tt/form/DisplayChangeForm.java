package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.DoNowListener;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.LabelBox;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.util.Utils;

public final strictfp class DisplayChangeForm extends Form {
	private final DoNowListener donow_listener;
	private final HorizButton later_button;

	public DisplayChangeForm(DoNowListener donow_listener) {
		this.donow_listener = donow_listener;
		ResourceBundle bundle = ResourceBundle.getBundle(DisplayChangeForm.class.getName());
		LabelBox info_label = new LabelBox(Utils.getBundleString(bundle, "warning_message"), Skin.getSkin().getEditFont(), 500);
		addChild(info_label);
		HorizButton now_button = new HorizButton(Utils.getBundleString(bundle, "now"), 120);
		addChild(now_button);
		now_button.addMouseClickListener(new NowListener());
		later_button = new HorizButton(Utils.getBundleString(bundle, "later"), 120);
		addChild(later_button);
		later_button.addMouseClickListener(new CancelListener(this));
		
		// Place objects
		info_label.place();
		now_button.place(ORIGIN_BOTTOM_RIGHT);
		later_button.place(now_button, LEFT_MID);

		compileCanvas();
		centerPos();
	}

	public final void setFocus() {
		later_button.setFocus();
	}

	private final strictfp class NowListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			remove();
			donow_listener.doChange(true);
		}
	}

	protected final void doCancel() {
		donow_listener.doChange(false);
	}
}
