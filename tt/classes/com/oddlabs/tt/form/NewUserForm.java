package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Login;
import com.oddlabs.matchmaking.LoginDetails;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.ButtonObject;
import com.oddlabs.tt.gui.CancelButton;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.EditLine;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.PasswordLine;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.guievent.EnterListener;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.delegate.MainMenu;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.util.CryptUtils;

public final strictfp class NewUserForm extends Form {
	private final static int MIN_PASSWORD_LENGTH = 6;
	
	private final static int BUTTON_WIDTH = 100;
	private final static int BUTTON_WIDTH_LONG = 150;
	private final static int EDITLINE_WIDTH = 240;

	private final MainMenu main_menu;
	private final EditLine editline_username;
	private final EditLine editline_email;
	private final PasswordLine editline_password;
	private final PasswordLine editline_verify;
	private final ResourceBundle bundle = ResourceBundle.getBundle(NewUserForm.class.getName());
	private final GUIRoot gui_root;
	private final NetworkSelector network;
	
	public NewUserForm(NetworkSelector network, GUIRoot gui_root, MainMenu main_menu) {
		this.main_menu = main_menu;
		this.gui_root = gui_root;
		this.network = network;

		CreateUserListener create_listener = new CreateUserListener();
		// headline
		Label label_headline = new Label(Utils.getBundleString(bundle, "create_new_user_caption"), Skin.getSkin().getHeadlineFont());
		addChild(label_headline);

		// login
		Group login_group = new Group();
		Label label_username = new Label(Utils.getBundleString(bundle, "user_name"), Skin.getSkin().getEditFont());
		editline_email = new EditLine(EDITLINE_WIDTH, 255);
		editline_email.addEnterListener(create_listener);
		Label label_email = new Label(Utils.getBundleString(bundle, "email"), Skin.getSkin().getEditFont());
		editline_username = new EditLine(EDITLINE_WIDTH, 255);
		editline_username.addEnterListener(create_listener);
		Label label_password = new Label(Utils.getBundleString(bundle, "password"), Skin.getSkin().getEditFont());
		editline_password = new PasswordLine(EDITLINE_WIDTH, 255);
		editline_password.addEnterListener(create_listener);
		Label label_verify = new Label(Utils.getBundleString(bundle, "reenter_password"), Skin.getSkin().getEditFont());
		editline_verify = new PasswordLine(EDITLINE_WIDTH, 255);
		editline_verify.addEnterListener(create_listener);
		login_group.addChild(label_username);
		login_group.addChild(editline_username);
		login_group.addChild(label_email);
		login_group.addChild(editline_email);
		login_group.addChild(label_password);
		login_group.addChild(editline_password);
		login_group.addChild(label_password);
		login_group.addChild(editline_verify);
		login_group.addChild(label_verify);

		label_username.place(label_headline, BOTTOM_LEFT);
		editline_username.place(label_username, RIGHT_MID);
		editline_email.place(editline_username, BOTTOM_RIGHT);
		label_email.place(editline_email, LEFT_MID);
		editline_password.place(editline_email, BOTTOM_RIGHT);
		label_password.place(editline_password, LEFT_MID);
		editline_verify.place(editline_password, BOTTOM_RIGHT);
		label_verify.place(editline_verify, LEFT_MID);
		login_group.compileCanvas();

		addChild(login_group);

		// warning
		Label label_one_user = new Label(Utils.getBundleString(bundle, "one_user_per_key"), Skin.getSkin().getEditFont());
		addChild(label_one_user);

		// buttons
		Group group_buttons = new Group();
		

		ButtonObject button_create = new HorizButton(Utils.getBundleString(bundle, "create_user"), BUTTON_WIDTH_LONG);
		button_create.addMouseClickListener(create_listener);
		ButtonObject button_cancel = new CancelButton(BUTTON_WIDTH);
		button_cancel.addMouseClickListener(new CancelListener(this));
		
		group_buttons.addChild(button_create);
		group_buttons.addChild(button_cancel);

		button_cancel.place();
		button_create.place(button_cancel, LEFT_MID);
		
		group_buttons.compileCanvas();
		addChild(group_buttons);
		
		// Place objects

		// headline
		label_headline.place();
		label_one_user.place(label_headline, BOTTOM_LEFT);
		login_group.place(label_one_user, BOTTOM_LEFT);
		

		group_buttons.place(ORIGIN_BOTTOM_RIGHT);

		compileCanvas();
	}
	
	public final void setFocus() {
		editline_username.setFocus();
	}
	
	private final void createUser() {
		String username = editline_username.getContents();
		String password = editline_password.getPasswordDigest();
		LoginDetails login_details = new LoginDetails(editline_email.getContents());
		if (!editline_password.getContents().equals(editline_verify.getContents())) {
			gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle, "no_match")));
			editline_password.clear();
			editline_verify.clear();
		} else if (editline_password.getContents().length() < MIN_PASSWORD_LENGTH) {
			String min_length_err = Utils.getBundleString(bundle, "min_length_error", new Object[]{new Integer(MIN_PASSWORD_LENGTH)});
			gui_root.addModalForm(new MessageForm(min_length_err));
		} else if (!login_details.isValid()) {
			gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle, "invalid_email")));
		} else {
			Login login = new Login(username, password);
			if (!login.isValid())
				gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle, "invalid_login")));
			else
				System.out.println("team-penguin: username = " + username + ", password = " + CryptUtils.digest(login.getPasswordDigest()) + ", email = " + login_details.getEmail() + ", pw_digest = " + password + ", login = " + login.getUsername());
				doCreateUser(username, login_details, password, login);
		}
	}

	private final void doCreateUser(String username, LoginDetails login_details, String password, Login login) {
		Settings.getSettings().username = username;
		Settings.getSettings().pw_digest = password;
		Form connecting_form = new MatchmakingConnectingForm(network, gui_root, this, main_menu, login, login_details);
		gui_root.addModalForm(connecting_form);
	}

	private final strictfp class CreateUserListener implements MouseClickListener, EnterListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			createUser();
		}

		public final void enterPressed(CharSequence text) {
			createUser();
		}
	}
}
