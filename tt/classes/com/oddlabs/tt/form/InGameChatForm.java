package com.oddlabs.tt.form;

import java.util.ResourceBundle;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.net.ChatMessage;
import com.oddlabs.tt.net.ChatMethod;
import com.oddlabs.tt.net.ChatListener;
import com.oddlabs.tt.net.Network;
import com.oddlabs.tt.gui.EditLine;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.InfoPrinter;
import com.oddlabs.tt.gui.RadioButton;
import com.oddlabs.tt.gui.RadioButtonGroup;
import com.oddlabs.tt.delegate.SelectionDelegate;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.InfoPrinter;
import com.oddlabs.tt.gui.TextBox;
import com.oddlabs.tt.guievent.EnterListener;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.net.ChatCommand;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.util.Utils;

public final strictfp class InGameChatForm extends Form implements ChatListener {
	private static final int CHAT_WIDTH = 400;
	private static final int BUTTON_WIDTH = 50;
	private static final int CHAT_HEIGHT = 150;

	private final EditLine chat_line;
	private final InfoPrinter info_printer;
	private final TextBox chat_box;
	private final RadioButtonGroup radio_button_group;
	private final RadioButton radio_all;
	private final RadioButton radio_team;
	private final ResourceBundle bundle = getBundle();
	private final WorldViewer viewer;

	private final static ResourceBundle getBundle() {
		return ResourceBundle.getBundle(InGameChatForm.class.getName());
	}
	
	public InGameChatForm(InfoPrinter info_printer, WorldViewer viewer) {
		super(Utils.getBundleString(getBundle(), "chat"));
		this.viewer = viewer;

		this.info_printer = info_printer;
		chat_line = new EditLine(CHAT_WIDTH, 256);
		addChild(chat_line);
		chat_line.addEnterListener(new ChatListener());

		HorizButton button_send = new HorizButton(Utils.getBundleString(bundle, "send"), BUTTON_WIDTH);
		addChild(button_send);
		button_send.addMouseClickListener(new SendListener());

		chat_box = new TextBox(CHAT_WIDTH + BUTTON_WIDTH, CHAT_HEIGHT, Skin.getSkin().getEditFont(), -1);
		addChild(chat_box);

		radio_button_group = new RadioButtonGroup();

		radio_all = new RadioButton(true, radio_button_group, Utils.getBundleString(bundle, "send_to_all"));
		addChild(radio_all);

		radio_team = new RadioButton(false, radio_button_group, Utils.getBundleString(bundle, "send_to_team"));
		addChild(radio_team);

		chat_line.place();
		button_send.place(chat_line, RIGHT_MID);
		chat_box.place(chat_line, TOP_LEFT);
		radio_all.place(chat_line, BOTTOM_LEFT);
		radio_team.place(chat_line, BOTTOM_RIGHT);
		compileCanvas();
		Network.getMatchmakingClient().clearInGameChatHistory();
	}

	protected final void doAdd() {
		super.doAdd();
		Network.getChatHub().addListener(this);
		refreshMessages();
	}

	protected final void doRemove() {
		super.doRemove();
		Network.getChatHub().removeListener(this);
	}

	public final void setReceivers(boolean all) {
		if (all)
			radio_button_group.mark(radio_all);
		else
			radio_button_group.mark(radio_team);
	}

	public final void setFocus() {
		chat_line.setFocus();
	}

	public final void chat(ChatMessage message) {
		refreshMessages();
	}

	private void refreshMessages() {
		List messages = Network.getMatchmakingClient().getInGameChatHistory();
		chat_box.clear();
		for (int i = 0; i < messages.size(); i++) {
			if (i != 0)
				chat_box.append("\n");
			chat_box.append((String)messages.get(i));
		}
		chat_box.setOffsetY(Integer.MAX_VALUE);
	}

	public void mouseMoved(int x, int y) {
		((SelectionDelegate)getParent()).mouseMoved(x, y);
	}

	private strictfp final class ChatListener implements EnterListener {
		public final void enterPressed(CharSequence text) {
			String chat = text.toString();
			if (!chat.equals("")) {
				chat_line.clear();
				Map commands = new HashMap();
				ChatMethod cheat = new ChatMethod() {
					public final void execute(InfoPrinter info_printer, String text) {
						viewer.getCheat().enable();
					}
				};
				commands.put("iamacheater", cheat);
				if (!ChatCommand.filterCommand(info_printer, commands, chat)) {
					viewer.getPeerHub().sendChat(chat, radio_button_group.getMarked() == radio_team);
				}
			} else {
				cancel();
			}
		}
	}

	private strictfp final class SendListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			chat_line.enterPressedAll();
		}
	}
}
