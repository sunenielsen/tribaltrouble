package com.oddlabs.tt.form;

import com.oddlabs.tt.gui.*;
import com.oddlabs.tt.guievent.*;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.util.Utils;

import java.util.ResourceBundle;

public final strictfp class WarningForm extends Form {
	private final static int MAX_WIDTH = 500;

	private final CheckBox show_next_time;
	
	public WarningForm(String head, String message) {
		ResourceBundle bundle = ResourceBundle.getBundle(WarningForm.class.getName());
		int head_width = StrictMath.min(MAX_WIDTH, Skin.getSkin().getHeadlineFont().getWidth(head));
		int message_width = StrictMath.min(MAX_WIDTH, Skin.getSkin().getEditFont().getWidth(message));
		int width = StrictMath.max(head_width, message_width);
		
		Group group = new Group();
		addChild(group);
		LabelBox head_label = new LabelBox(head, Skin.getSkin().getHeadlineFont(), width);
		group.addChild(head_label);
		LabelBox info_label = new LabelBox(message, Skin.getSkin().getEditFont(), width);
		group.addChild(info_label);
		show_next_time = new CheckBox(false, Utils.getBundleString(bundle, "dont_show"));
		group.addChild(show_next_time);
		
		head_label.place();
		info_label.place(head_label, BOTTOM_LEFT);
		show_next_time.place(info_label, BOTTOM_LEFT);
		group.compileCanvas();
		
		HorizButton ok_button = new OKButton(70);
		addChild(ok_button);
		ok_button.addMouseClickListener(new OKListener());
		// Place objects
		group.place();
		ok_button.place(group, BOTTOM_MID);

		// headline
		compileCanvas();
		centerPos();
	}

	private final strictfp class OKListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			Settings.getSettings().warning_no_sound = !show_next_time.isMarked();
			remove();
		}
	}
}
