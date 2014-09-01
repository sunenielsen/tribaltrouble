package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.tt.gui.ButtonObject;
import com.oddlabs.tt.gui.CancelButton;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.EditLine;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.delegate.Menu;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.guievent.EnterListener;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.util.Utils;

public final strictfp class NewProfileForm extends Form {
	private final static int BUTTON_WIDTH = 100;
	private final static int BUTTON_WIDTH_LONG = 150;
	private final static int EDITLINE_WIDTH = 240;

	private final Menu main_menu;
	private final ProfilesForm profiles_form;
	private final EditLine editline_nick;
	private final GUIRoot gui_root;
	
	public NewProfileForm(GUIRoot gui_root, Menu main_menu, ProfilesForm profiles_form) {
		this.gui_root = gui_root;
		this.main_menu = main_menu;
		this.profiles_form = profiles_form;

		ResourceBundle bundle = ResourceBundle.getBundle(NewProfileForm.class.getName());
		// headline
		Label label_headline = new Label(Utils.getBundleString(bundle, "create_profile_caption"), Skin.getSkin().getHeadlineFont());
		addChild(label_headline);

		// login
		Label label_nick = new Label(Utils.getBundleString(bundle, "nick"), Skin.getSkin().getEditFont());
		editline_nick = new EditLine(EDITLINE_WIDTH, 255);
		editline_nick.addEnterListener(new CreateProfileListener());
		addChild(label_nick);
		addChild(editline_nick);

		label_headline.place();
		label_nick.place(label_headline, BOTTOM_LEFT);
		editline_nick.place(label_nick, RIGHT_MID);

		Group group_buttons = new Group();
		ButtonObject button_create = new HorizButton(Utils.getBundleString(bundle, "create_profile"), BUTTON_WIDTH_LONG);
		button_create.addMouseClickListener(new CreateProfileListener());
		ButtonObject button_cancel = new CancelButton(BUTTON_WIDTH);
		button_cancel.addMouseClickListener(new CancelListener(this));
		
		group_buttons.addChild(button_create);
		group_buttons.addChild(button_cancel);

		button_cancel.place();
		button_create.place(button_cancel, LEFT_MID);
		
		group_buttons.compileCanvas();
		addChild(group_buttons);
		
		group_buttons.place(ORIGIN_BOTTOM_RIGHT);

		compileCanvas();
	}
	
	public final void setFocus() {
		editline_nick.setFocus();
	}

	public final void doCancel() {
		done();
	}

	private final void done() {
		main_menu.setMenuCentered(profiles_form);
	}
	
	private final void createProfile() {
		String nick = editline_nick.getContents();
		gui_root.addModalForm(new CreatingProfileForm(gui_root, profiles_form, main_menu, nick));
	}

	public final void connectionLost() {
		remove();
	}

	private final strictfp class CreateProfileListener implements MouseClickListener, EnterListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			createProfile();
		}

		public final void enterPressed(CharSequence text) {
			createProfile();
		}
	}
}
