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

public strictfp class DemoForm extends Form {
	public DemoForm(GUIRoot gui_root, String header, GUIImage img, String text) {
		ResourceBundle bundle = ResourceBundle.getBundle(DemoForm.class.getName());
		Label label_headline = new Label(header, Skin.getSkin().getHeadlineFont());
		addChild(label_headline);
		addChild(img);
		LabelBox text_box = new LabelBox(text, Skin.getSkin().getEditFont(), 512);
		addChild(text_box);

		HorizButton buy_button = new BuyButton(gui_root, Utils.getBundleString(bundle, "buy_now_caption"), 120);
		addChild(buy_button);
		HorizButton continue_button = new HorizButton(Utils.getBundleString(bundle, "continue"), 100);
		addChild(continue_button);
		continue_button.addMouseClickListener(new OKListener(this));

		// Place objects
		label_headline.place();
		img.place(label_headline, BOTTOM_LEFT);
		text_box.place(img, BOTTOM_LEFT);
		continue_button.place(text_box, BOTTOM_RIGHT);
		buy_button.place(continue_button, LEFT_MID);
		compileCanvas();
		
		centerPos();
	}

	protected final void keyRepeat(KeyboardEvent event) {
		super.keyPressed(event);
	}
}
