package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.tt.gui.ButtonObject;
import com.oddlabs.tt.gui.CancelButton;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.EditLine;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.OKButton;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.guievent.EnterListener;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.net.Network;
import com.oddlabs.tt.util.Utils;

public final strictfp class PrivateMessageForm extends Form {
	private final static int BUTTON_WIDTH = 100;
	private final static int EDITLINE_WIDTH = 240;

	private final EditLine editline_name;
	private final String nick;
	private final GUIRoot gui_root;
	
	public PrivateMessageForm(GUIRoot gui_root, String nick) {
		this.gui_root = gui_root;
		this.nick = nick;
		ResourceBundle bundle = ResourceBundle.getBundle(PrivateMessageForm.class.getName());
		// headline
		Label label_headline = new Label(Utils.getBundleString(bundle, "private_message_caption"), Skin.getSkin().getHeadlineFont());
		addChild(label_headline);

		Label label_name = new Label(Utils.getBundleString(bundle, "to", new Object[]{nick}), Skin.getSkin().getEditFont());
		editline_name = new EditLine(EDITLINE_WIDTH, 256);
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
		centerPos();
	}
	
	public final void setFocus() {
		editline_name.setFocus();
	}

	private final void send() {
		String message = editline_name.getContents();
		Network.getMatchmakingClient().sendPrivateMessage(gui_root, nick, message);
		remove();
	}

	private final strictfp class OKListener implements MouseClickListener, EnterListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			send();
		}

		public final void enterPressed(CharSequence text) {
			send();
		}
	}
}
