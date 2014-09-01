package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.MatchmakingClientInterface;
import com.oddlabs.tt.gui.CancelButton;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.net.Network;
import com.oddlabs.tt.net.ProfileListener;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.delegate.Menu;

public final strictfp class CreatingProfileForm extends Form implements ProfileListener {
	private final Form profiles_form;
	private final Menu main_menu;
	private final ResourceBundle bundle = ResourceBundle.getBundle(CreatingProfileForm.class.getName());
	private final GUIRoot gui_root;
	
	public CreatingProfileForm(GUIRoot gui_root, Form profiles_form, Menu main_menu, String nick) {
		this.gui_root = gui_root;
		this.profiles_form = profiles_form;
		this.main_menu = main_menu;
		Label info_label = new Label(Utils.getBundleString(bundle, "creating"), Skin.getSkin().getHeadlineFont());
		addChild(info_label);
		HorizButton cancel_button = new CancelButton(120);
		addChild(cancel_button);
		cancel_button.addMouseClickListener(new CancelListener(this));
		
		// Place objects
		info_label.place();
		cancel_button.place(info_label, BOTTOM_MID);

		// headline
		compileCanvas();
		centerPos();

		Network.getMatchmakingClient().setCreatingProfileListener(this);
		Network.getMatchmakingClient().createProfile(nick);
	}

	public final void success() {
		remove();
		main_menu.setMenuCentered(profiles_form);
		Network.getMatchmakingClient().requestProfiles();
	}
	
	public final void error(int error_code) {
		remove();
		String error_message;
		switch (error_code) {
			case MatchmakingClientInterface.USERNAME_ERROR_TOO_MANY:
				error_message = Utils.getBundleString(bundle, "username_error_too_many");
				break;
			case MatchmakingClientInterface.PROFILE_ERROR_GUEST:
				error_message = Utils.getBundleString(bundle, "profile_error_guest", new Object[]{"Guest"});
				break;
			case MatchmakingClientInterface.USERNAME_ERROR_ALREADY_EXISTS:
				error_message = Utils.getBundleString(bundle, "username_error_already_exists");
				break;
			case MatchmakingClientInterface.USERNAME_ERROR_INVALID_CHARACTERS:
				error_message = Utils.getBundleString(bundle, "username_error_invalid_characters");
				break;
			case MatchmakingClientInterface.USERNAME_ERROR_TOO_LONG:
				error_message = Utils.getBundleString(bundle, "username_error_too_long");
				break;
			case MatchmakingClientInterface.USERNAME_ERROR_TOO_SHORT:
				error_message = Utils.getBundleString(bundle, "username_error_too_short");
				break;
			default:
				throw new RuntimeException("Unknown error code: " + error_code);
		}
		gui_root.addModalForm(new MessageForm(error_message));
	}
	
	protected final void doCancel() {
	}
}
