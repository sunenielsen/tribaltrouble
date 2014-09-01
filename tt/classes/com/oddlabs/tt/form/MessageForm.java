package com.oddlabs.tt.form;

import com.oddlabs.tt.gui.*;
import com.oddlabs.tt.guievent.*;

public strictfp class MessageForm extends Form {
	private final static int MAX_WIDTH = 500;

	public MessageForm(String head, String message) {
		this(head, message, null, null);
	}

	public MessageForm(String head, String message, String button, MouseClickListener listener) {
		int head_width = StrictMath.min(MAX_WIDTH, Skin.getSkin().getHeadlineFont().getWidth(head));
		int message_width = StrictMath.min(MAX_WIDTH, Skin.getSkin().getEditFont().getWidth(message));
		int width = StrictMath.max(head_width, message_width);
		
		LabelBox head_label = new LabelBox(head, Skin.getSkin().getHeadlineFont(), width);
		addChild(head_label);
		LabelBox info_label = new LabelBox(message, Skin.getSkin().getEditFont(), width);
		addChild(info_label);
		HorizButton ok_button;
		if (button == null) {
			ok_button = new OKButton(70);
			ok_button.addMouseClickListener(new OKListener(this));
		} else {
			ok_button = new HorizButton(button, 70);
			ok_button.addMouseClickListener(listener);
		}
		addChild(ok_button);
		// Place objects
		head_label.place();
		info_label.place(head_label, BOTTOM_LEFT);
		ok_button.place(info_label, BOTTOM_MID);

		// headline
		compileCanvas();
		centerPos();
	}
	
	public MessageForm(String message) {
		int width = Math.min(500, Skin.getSkin().getEditFont().getWidth(message));
		LabelBox info_label = new LabelBox(message, Skin.getSkin().getEditFont(), width);
		addChild(info_label);
		HorizButton ok_button = new OKButton(70);
		addChild(ok_button);
		ok_button.place(info_label, BOTTOM_MID);
		ok_button.addMouseClickListener(new OKListener(this));
		// Place objects
		info_label.place();

		// headline
		compileCanvas();
		centerPos();
	}
}
