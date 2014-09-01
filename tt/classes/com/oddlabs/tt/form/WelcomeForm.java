package com.oddlabs.tt.form;

import com.oddlabs.tt.gui.*;
import com.oddlabs.tt.guievent.*;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.delegate.MainMenu;

import java.util.ResourceBundle;

public final strictfp class WelcomeForm extends Form {
	private final MainMenu main_menu;
	private final HorizButton update_button;
	private final GUIRoot gui_root;

	public WelcomeForm(GUIRoot gui_root, MainMenu main_menu) {
		this.main_menu = main_menu;
		this.gui_root = gui_root;
		ResourceBundle bundle = ResourceBundle.getBundle(WelcomeForm.class.getName());
		Label label_headline = new Label(Utils.getBundleString(bundle, "welcome_caption"), Skin.getSkin().getHeadlineFont());
		addChild(label_headline);
		
		LabelBox box = new LabelBox(Utils.getBundleString(bundle, "welcome_message"), Skin.getSkin().getEditFont(), 400);
		addChild(box);
		
		HorizButton ok_button = new OKButton(100);
		addChild(ok_button);
		ok_button.addMouseClickListener(new OKListener(this));
		
		update_button = new HorizButton(Utils.getBundleString(bundle, "update_now"), 100);
		addChild(update_button);
		update_button.addMouseClickListener(new UpdateListener());
		
		HorizButton register_button = new HorizButton(Utils.getBundleString(bundle, "register"), 100);
		addChild(register_button);
		register_button.addMouseClickListener(new RegisterListener());

		// Place objects
		label_headline.place();
		box.place(label_headline, BOTTOM_LEFT);
		ok_button.place(ORIGIN_BOTTOM_RIGHT);
		update_button.place(ok_button, LEFT_MID);
		register_button.place(update_button, LEFT_MID);

		// headline
		compileCanvas();
		centerPos();
	}

	public final void setFocus() {
		update_button.setFocus();
	}

	private final strictfp class RegisterListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			main_menu.setMenuCentered(new RegistrationForm(gui_root, true, main_menu));
		}
	}

	private final strictfp class UpdateListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			main_menu.setMenuCentered(new UpdateGameForm(gui_root, main_menu));
		}
	}
}
