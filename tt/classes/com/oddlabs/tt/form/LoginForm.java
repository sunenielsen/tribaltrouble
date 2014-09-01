package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Login;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.ButtonObject;
import com.oddlabs.tt.gui.CancelButton;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.CheckBox;
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
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.delegate.MainMenu;
import com.oddlabs.net.NetworkSelector;

public final strictfp class LoginForm extends Form implements RegisterProgressFormListener {
	private final static int BUTTON_WIDTH = 100;
	private final static int EDITLINE_WIDTH = 240;

	private final MainMenu main_menu;
	private final GUIRoot gui_root;
	private final NetworkSelector network;
	private final EditLine editline_username;
	private final PasswordLine editline_password;
	private final CheckBox remember_checkbox;
	private final ResourceBundle bundle = ResourceBundle.getBundle(LoginForm.class.getName());
	
	public LoginForm(NetworkSelector network, GUIRoot gui_root, MainMenu main_menu) {
		this.main_menu = main_menu;
		this.gui_root = gui_root;
		this.network = network;
		boolean remember = Settings.getSettings().remember_login;
		if (!remember) {
			Settings.getSettings().username = "";
			Settings.getSettings().pw_digest = "";
		}

		// headline
		Label label_headline = new Label(Utils.getBundleString(bundle, "login_caption"), Skin.getSkin().getHeadlineFont());
		addChild(label_headline);

		// login
		LoginListener login_listener = new LoginListener();
		Group login_group = new Group();
		Label label_username = new Label(Utils.getBundleString(bundle, "username"), Skin.getSkin().getEditFont());
		editline_username = new EditLine(EDITLINE_WIDTH, 255);
		editline_username.addEnterListener(login_listener);
		editline_username.append(Settings.getSettings().username);
		Label label_password = new Label(Utils.getBundleString(bundle, "password"), Skin.getSkin().getEditFont());
		editline_password = new PasswordLine(EDITLINE_WIDTH, 255);
		editline_password.addEnterListener(login_listener);
		if (remember) {
			editline_password.append("*************");
			editline_password.setPasswordDigest(Settings.getSettings().pw_digest);
		}
		remember_checkbox = new CheckBox(remember, Utils.getBundleString(bundle, "remember_login"));

		login_group.addChild(label_username);
		login_group.addChild(editline_username);
		login_group.addChild(label_password);
		login_group.addChild(editline_password);
		login_group.addChild(remember_checkbox);

		label_username.place();
		editline_username.place(label_username, RIGHT_MID);
		editline_password.place(editline_username, BOTTOM_RIGHT);
		label_password.place(editline_password, LEFT_MID);
		remember_checkbox.place(editline_password, BOTTOM_LEFT);
		login_group.compileCanvas();

		addChild(login_group);
		// buttons
		Group group_buttons = new Group();
		

		ButtonObject button_newuser = new HorizButton(Utils.getBundleString(bundle, "new_account"), BUTTON_WIDTH);
		button_newuser.addMouseClickListener(new NewUserListener());
		ButtonObject button_ok = new HorizButton(Utils.getBundleString(bundle, "login"), BUTTON_WIDTH);
		button_ok.addMouseClickListener(login_listener);
		ButtonObject button_cancel = new CancelButton(BUTTON_WIDTH);
		button_cancel.addMouseClickListener(new CancelListener(this));
		
		group_buttons.addChild(button_newuser);
		group_buttons.addChild(button_ok);
		group_buttons.addChild(button_cancel);

		button_cancel.place();
		button_ok.place(button_cancel, LEFT_MID);
		button_newuser.place(button_ok, LEFT_MID);
		
		group_buttons.compileCanvas();
		addChild(group_buttons);
		
		// Place objects

		// headline
		label_headline.place();
		login_group.place(label_headline, BOTTOM_LEFT);

		group_buttons.place(ORIGIN_BOTTOM_RIGHT);

		compileCanvas();

		if (Renderer.isRegistered()) {
			if (Renderer.getRegistrationClient().getRegistrationInfo() != null) {
				main_menu.setMenu(this);
			} else {
				RegisterProgressForm form = new RegisterProgressForm(gui_root, this);
				Renderer.getRegistrationClient().setListener(form);
				Renderer.getRegistrationClient().register(true);
			}
		} else {
			Form form = new MatchmakingConnectingForm(network, gui_root, null, main_menu, null, null);
			main_menu.setMenu(form);
			form.centerPos();
		}
	}

	public final void registrationFailed() {
	}
	
	public final void registrationCompleted() {
		main_menu.setMenu(this);
	}
	
	public final void setFocus() {
		editline_username.setFocus();
	}
	
	private final void login() {
		String username = editline_username.getContents();
		String password = editline_password.getPasswordDigest();
		Login login = new Login(username, password);
		if (!login.isValid())
			gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle, "invalid_login")));
		else
			doLogin(username, password, login, remember_checkbox.isMarked());
	}

	private final void doLogin(String username, String password, Login login, boolean remember_login) {
		if (remember_login) {
			Settings.getSettings().username = username;
			Settings.getSettings().pw_digest = password;
		}
		Settings.getSettings().remember_login = remember_login;
		Form connecting_form = new MatchmakingConnectingForm(network, gui_root, this, main_menu, login, null);
		gui_root.addModalForm(connecting_form);
	}

	private final strictfp class NewUserListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			remove();
			main_menu.setMenu(new NewUserForm(network, gui_root, main_menu));
		}
	}

	private final strictfp class LoginListener implements MouseClickListener, EnterListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			login();
		}

		public final void enterPressed(CharSequence text) {
			login();
		}
	}
}
