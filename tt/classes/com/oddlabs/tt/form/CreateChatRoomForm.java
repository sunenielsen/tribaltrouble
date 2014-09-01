package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.MatchmakingServerInterface;
import com.oddlabs.tt.gui.ButtonObject;
import com.oddlabs.tt.gui.CancelButton;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.EditLine;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.delegate.Menu;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.OKButton;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.guievent.EnterListener;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.net.Network;
import com.oddlabs.tt.util.Utils;

public final strictfp class CreateChatRoomForm extends Form {
	private final static int BUTTON_WIDTH = 100;
	private final static int EDITLINE_WIDTH = 240;

	private final EditLine editline_name;
	private final Menu main_menu;
	private final SelectGameMenu menu;
	private final ResourceBundle bundle = ResourceBundle.getBundle(CreateChatRoomForm.class.getName());
	private final GUIRoot gui_root;
	
	public CreateChatRoomForm(GUIRoot gui_root, Menu main_menu, SelectGameMenu menu) {
		this.gui_root = gui_root;
		this.main_menu = main_menu;
		this.menu = menu;
		// headline
		Label label_headline = new Label(Utils.getBundleString(bundle, "create_room"), Skin.getSkin().getHeadlineFont());
		addChild(label_headline);

		Label label_name = new Label(Utils.getBundleString(bundle, "name"), Skin.getSkin().getEditFont());
		editline_name = new EditLine(EDITLINE_WIDTH,
				MatchmakingServerInterface.MAX_ROOM_NAME_LENGTH,
				MatchmakingServerInterface.ALLOWED_ROOM_CHARS,
				EditLine.LEFT_ALIGNED);
		editline_name.addEnterListener(new OKListener());
		
		addChild(label_name);
		addChild(editline_name);


		ButtonObject button_ok = new OKButton(BUTTON_WIDTH);
		button_ok.addMouseClickListener(new OKListener());
		ButtonObject button_cancel = new CancelButton(BUTTON_WIDTH);
		button_cancel.addMouseClickListener(new CancelListener(this));
		
		addChild(button_ok);
		addChild(button_cancel);

		// Place objects
		label_headline.place();
		label_name.place(label_headline, BOTTOM_LEFT);
		editline_name.place(label_name, RIGHT_MID);
		button_cancel.place(ORIGIN_BOTTOM_RIGHT);
		button_ok.place(button_cancel, LEFT_MID);
		compileCanvas();
	}
	
	public final void setFocus() {
		editline_name.setFocus();
	}

	protected final void doCancel() {
		main_menu.setMenuCentered(menu);
	}

	private final void create() {
		String name = editline_name.getContents();
		if (name.length() < MatchmakingServerInterface.MIN_ROOM_NAME_LENGTH) {
			String min_name_error = Utils.getBundleString(bundle, "min_name_error", new Object[]{new Integer(MatchmakingServerInterface.MIN_ROOM_NAME_LENGTH)});
			gui_root.addModalForm(new MessageForm(min_name_error));
		} else {
			Network.getMatchmakingClient().joinRoom(gui_root, name);
		}
		main_menu.setMenuCentered(menu);
	}

	private final strictfp class OKListener implements MouseClickListener, EnterListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			create();
		}

		public final void enterPressed(CharSequence text) {
			create();
		}
	}
}
