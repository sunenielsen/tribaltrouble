package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Profile;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.ColumnInfo;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIObject;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.IntegerLabel;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.MultiColumnComboBox;
import com.oddlabs.tt.gui.Row;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.delegate.Menu;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.guievent.RowListener;
import com.oddlabs.tt.net.Network;
import com.oddlabs.tt.util.Utils;

public final strictfp class ProfilesForm extends Form {
	private final static int NICK_SIZE = 200;

	private final Menu main_menu;
	private final SelectGameMenu game_menu;
	private final MultiColumnComboBox profile_list_box;
	private final HorizButton join_button;
	private final GUIRoot gui_root;
	private final ResourceBundle bundle = ResourceBundle.getBundle(ProfilesForm.class.getName());

	// to be removed if connection lost
	private NewProfileForm new_profile_form;
	private QuestionForm confirm_delete_form;
	
	public ProfilesForm(GUIRoot gui_root, Menu main_menu, SelectGameMenu game_menu) {
		this.gui_root = gui_root;
		this.main_menu = main_menu;
		this.game_menu = game_menu;
		Label label_headline = new Label(Utils.getBundleString(bundle, "profiles_caption"), Skin.getSkin().getHeadlineFont());
		addChild(label_headline);

		ColumnInfo[] infos = new ColumnInfo[]{
			new ColumnInfo(Utils.getBundleString(bundle, "nick"), NICK_SIZE),
			new ColumnInfo(Utils.getBundleString(bundle, "rating"), 120),
			new ColumnInfo(Utils.getBundleString(bundle, "wins"), 100),
			new ColumnInfo(Utils.getBundleString(bundle, "losses"), 100),
			new ColumnInfo(Utils.getBundleString(bundle, "invalid"), 100)};
		profile_list_box = new MultiColumnComboBox(gui_root, infos, 200);
		profile_list_box.addRowListener(new JoinListener());
		addChild(profile_list_box);

		HorizButton create_profile_button = new HorizButton(Utils.getBundleString(bundle, "create_new_profile"), 150);
		addChild(create_profile_button);
		create_profile_button.addMouseClickListener(new CreateProfileListener());

		HorizButton delete_profile_button = new HorizButton(Utils.getBundleString(bundle, "delete_profile"), 150);
		addChild(delete_profile_button);
		delete_profile_button.addMouseClickListener(new DeleteProfileListener());

		join_button = new HorizButton(Utils.getBundleString(bundle, "join"), 100);
		addChild(join_button);
		join_button.addMouseClickListener(new JoinListener());

		HorizButton logout_button = new HorizButton(Utils.getBundleString(bundle, "logout"), 100);
		addChild(logout_button);
		logout_button.addMouseClickListener(new CancelListener(this));

		label_headline.place();
		profile_list_box.place(label_headline, BOTTOM_LEFT);
		logout_button.place(ORIGIN_BOTTOM_RIGHT);
		join_button.place(logout_button, LEFT_MID);
		delete_profile_button.place(join_button, LEFT_MID);
		create_profile_button.place(delete_profile_button, LEFT_MID);

		compileCanvas();
	}
	
	public final void setFocus() {
		join_button.setFocus();
	}

	protected void doCancel() {
		Network.getMatchmakingClient().close(); 
	}

	public final void receivedProfiles(Profile[] profiles, String last_nick) {
		profile_list_box.clear();
		Row selected_row = null;
		for (int i = 0; i < profiles.length; i++) {
			Profile p = profiles[i];
			Row row = new Row(new GUIObject[]{
				new Label(p.getNick(), Skin.getSkin().getMultiColumnComboBoxData().getFont(), NICK_SIZE),
				new IntegerLabel(p.getRating(), Skin.getSkin().getMultiColumnComboBoxData().getFont()),
				new IntegerLabel(p.getWins(), Skin.getSkin().getMultiColumnComboBoxData().getFont()),
				new IntegerLabel(p.getLosses(), Skin.getSkin().getMultiColumnComboBoxData().getFont()),
				new IntegerLabel(p.getInvalid(), Skin.getSkin().getMultiColumnComboBoxData().getFont())}, p.getNick());
			profile_list_box.addRow(row);
			if (p.getNick().equalsIgnoreCase(last_nick))
				selected_row = row;
		}
		if (selected_row != null)
			profile_list_box.selectRow(selected_row);
	}

	private final void join(String nick) {
		Network.getMatchmakingClient().setProfile(nick);
		main_menu.setMenuCentered(game_menu);
	}

	private final strictfp class CreateProfileListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			new_profile_form = new NewProfileForm(gui_root, main_menu, ProfilesForm.this);
			main_menu.setMenu(new_profile_form);
		}
	}

	private final strictfp class DeleteProfileListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			String nick = (String)profile_list_box.getSelected();
			if (nick == null) {
				gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle, "no_profiles")));
			} else {
				String confirm_str = Utils.getBundleString(bundle, "confirm_delete", new Object[]{nick});
				confirm_delete_form = new QuestionForm(confirm_str, new ActionDeleteListener(nick));
				gui_root.addModalForm(confirm_delete_form);
			}
		}
	}

	public void connectionLost() {
		if (new_profile_form != null)
			new_profile_form.connectionLost();
		if (confirm_delete_form != null)
			confirm_delete_form.connectionLost();
		remove();
	}

	private final strictfp class ActionDeleteListener implements MouseClickListener {
		private final String nick;

		public ActionDeleteListener(String nick) {
			this.nick = nick;
		}

		public final void mouseClicked(int button, int x, int y, int clicks) {
			Network.getMatchmakingClient().deleteProfile(nick);
			Network.getMatchmakingClient().requestProfiles();
		}
	}

	private final strictfp class JoinListener implements MouseClickListener, RowListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			String nick = (String)profile_list_box.getSelected();
			if (nick == null) {
				gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle, "no_profiles")));
			} else {
				join(nick);
			}
		}

		public final void rowDoubleClicked(Object row_context) {
			String nick = (String)row_context;
			if (nick != null)
				join(nick);
		}
		
		public final void rowChosen(Object row_context) {
		}
	}
}
