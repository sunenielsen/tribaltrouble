package com.oddlabs.tt.gui;

import com.oddlabs.tt.guievent.*;
import com.oddlabs.tt.form.*;
import com.oddlabs.tt.net.*;
import com.oddlabs.matchmaking.ChatRoomUser;
import com.oddlabs.tt.util.Utils;

import java.util.*;

public strictfp class ChatPanel extends Panel implements ChatListener {
	private final static int PULLDOWN_INDEX_MESSAGE = 0;
	private final static int PULLDOWN_INDEX_INFO = 1;
	private final static int PULLDOWN_INDEX_IGNORE = 2;
	
	private final MultiColumnComboBox lobby_users_list_box;
	private final MultiColumnComboBox playing_users_list_box;
	private final TextBox chat_box;
	private final EditLine chat_line;
	private final HorizButton button_send;
	private final HorizButton button_leave;
	private final GUIRoot gui_root;

	private final int user_list_width;
	
	private PrivateMessageForm private_message_form;

	private static ResourceBundle getBundle() {
		return ResourceBundle.getBundle(ChatPanel.class.getName());
	}

	private final static String getI18N(String key) {
		return Utils.getBundleString(getBundle(), key);
	}

	public ChatPanel(GUIRoot gui_root, ChatRoomInfo info, int compare_width, int compare_height, int button_width, EnterListener chat_listener, MouseClickListener leave_listener) {
		super(getI18N("chat"));
		this.gui_root = gui_root;
		FormData fdata = Skin.getSkin().getFormData();
		Box pdata = Skin.getSkin().getPanelData().getBox();
		Box edata = Skin.getSkin().getEditBox();

		Label label_headline = new Label(info.getName(), Skin.getSkin().getHeadlineFont());
		addChild(label_headline);

		int edit_line_height = edata.getBottomOffset() + edata.getTopOffset() + Skin.getSkin().getEditFont().getHeight();
		int height = compare_height - pdata.getTopOffset() - pdata.getBottomOffset() - edit_line_height - label_headline.getHeight() - 2*fdata.getObjectSpacing();
		int user_list_height = (height - Skin.getSkin().getFormData().getObjectSpacing())/2;//- Skin.getSkin().getEditFont().getHeight();
		user_list_width = 2*button_width + 2*fdata.getObjectSpacing() - Skin.getSkin().getScrollBarData().getScrollBar().getWidth();

		ColumnInfo[] lobby_infos = new ColumnInfo[]{
			new ColumnInfo(getI18N("lobby"), user_list_width)};
		lobby_users_list_box = new MultiColumnComboBox(gui_root, lobby_infos, user_list_height, true);
		addChild(lobby_users_list_box);
		
		ColumnInfo[] playing_infos = new ColumnInfo[]{
			new ColumnInfo(getI18N("playing"), user_list_width)};
		playing_users_list_box = new MultiColumnComboBox(gui_root, playing_infos, user_list_height, true);
		addChild(playing_users_list_box);
		
		PulldownMenu lobby_pulldown_menu = new PulldownMenu();
		lobby_pulldown_menu.addItem(new PulldownItem(getI18N("message")));
		lobby_pulldown_menu.addItem(new PulldownItem(getI18N("info"))); 
		lobby_pulldown_menu.addItem(new PulldownItem("")); 
		lobby_pulldown_menu.addItemChosenListener(new PulldownListener(lobby_users_list_box));
		lobby_users_list_box.setPulldownMenu(lobby_pulldown_menu);
		
		ChatRoomUserDoubleClickedListener lobby_double_clicked = new ChatRoomUserDoubleClickedListener(lobby_pulldown_menu);
		lobby_users_list_box.addRowListener(lobby_double_clicked);

		PulldownMenu playing_pulldown_menu = new PulldownMenu();
		playing_pulldown_menu.addItem(new PulldownItem(getI18N("message")));
		playing_pulldown_menu.addItem(new PulldownItem(getI18N("info"))); 
		playing_pulldown_menu.addItem(new PulldownItem("")); 
		playing_pulldown_menu.addItemChosenListener(new PulldownListener(playing_users_list_box));
		playing_users_list_box.setPulldownMenu(playing_pulldown_menu);
		
		ChatRoomUserDoubleClickedListener playing_double_clicked = new ChatRoomUserDoubleClickedListener(playing_pulldown_menu);
		playing_users_list_box.addRowListener(playing_double_clicked);

		int width = compare_width - pdata.getLeftOffset() - pdata.getRightOffset() - lobby_users_list_box.getWidth();
		chat_box = new TextBox(width, height, Skin.getSkin().getEditFont(), -1);
		addChild(chat_box);

		chat_line = new EditLine(width, 256);
		addChild(chat_line);
		chat_line.addEnterListener(chat_listener);
		chat_line.addEnterListener(new ClearListener());

		button_send = new HorizButton(getI18N("send"), button_width);
		addChild(button_send);
		button_send.addMouseClickListener(new SendListener());

		button_leave = new HorizButton(getI18N("leave"), button_width);
		addChild(button_leave);
		button_leave.addMouseClickListener(leave_listener);

		// Place chat panel objects
		label_headline.place();
		chat_box.place(label_headline, BOTTOM_LEFT);
		lobby_users_list_box.place(chat_box, RIGHT_TOP, 0);
		playing_users_list_box.place(lobby_users_list_box, BOTTOM_LEFT);
		chat_line.place(chat_box, BOTTOM_LEFT);
		button_send.place(chat_line, RIGHT_MID);
		button_leave.place(button_send, RIGHT_MID);
		compileCanvas();
		update(info);
	}

	public final void update(ChatRoomInfo info) {
		ChatRoomUser[] users = info.getUsers();
		if (users != null) {
			lobby_users_list_box.clear();
			playing_users_list_box.clear();
			for (int i = 0; i < users.length; i++) {
				int label_width = user_list_width - (Skin.getSkin().getMultiColumnComboBoxData().getBox().getLeftOffset() + Skin.getSkin().getMultiColumnComboBoxData().getBox().getRightOffset());
				Label label = new Label(users[i].getNick(), Skin.getSkin().getMultiColumnComboBoxData().getFont(), label_width);
				Row row = new Row(new GUIObject[]{label}, users[i]);
				if (!users[i].isPlaying())
					lobby_users_list_box.addRow(row);
				else
					playing_users_list_box.addRow(row);
			}
		}
		refreshMessages();
	}

	public final void chat(ChatMessage message) {
		if (message.type != ChatMessage.CHAT_PRIVATE && message.type != ChatMessage.CHAT_CHATROOM)
			return;
		if (message.type != ChatMessage.CHAT_PRIVATE) {
			getTab().updateNotify();
		}
		refreshMessages();
	}

	private void refreshMessages() {
		List messages = Network.getMatchmakingClient().getChatRoomHistory();
		chat_box.clear();
		for (int i = 0; i < messages.size(); i++) {
			if (i != 0)
				chat_box.append("\n");
			chat_box.append((String)messages.get(i));
		}
		chat_box.setOffsetY(Integer.MAX_VALUE);
	}

	public final void setFocus() {
		chat_line.setFocus();
	}

	public final void connectionLost() {
		if (private_message_form != null)
			private_message_form.remove();
	}

	private strictfp final class SendListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			chat_line.enterPressedAll();
		}
	}

	private strictfp final class ClearListener implements EnterListener {
		public final void enterPressed(CharSequence text) {
			chat_line.clear();
		}
	}

	private final strictfp class PulldownListener implements ItemChosenListener {
		private final MultiColumnComboBox box;

		public PulldownListener(MultiColumnComboBox box) {
			this.box = box;
		}
		
		public final void itemChosen(PulldownMenu menu, int item_index) {
			ChatRoomUser user = (ChatRoomUser)box.getRightClickedRowData();
			String nick = user.getNick();
			switch (item_index) {
				case PULLDOWN_INDEX_MESSAGE:
					gui_root.addModalForm(new PrivateMessageForm(gui_root, nick));
					break;
				case PULLDOWN_INDEX_INFO:
					Network.getMatchmakingClient().requestInfo(gui_root, nick);
					break;
				case PULLDOWN_INDEX_IGNORE:
					if (ChatCommand.isIgnoring(nick))
						ChatCommand.unignore(gui_root.getInfoPrinter(), nick);
					else
						ChatCommand.ignore(gui_root.getInfoPrinter(), nick);
					break;
				default:
					throw new RuntimeException();
			}
			box.setFocus();
		}
	}

	private final strictfp class ChatRoomUserDoubleClickedListener implements RowListener {
		private final PulldownMenu menu;

		public ChatRoomUserDoubleClickedListener(PulldownMenu menu) {
			this.menu = menu;
		}
		
		public final void rowDoubleClicked(Object context) {
			ChatRoomUser user = (ChatRoomUser)context;
			private_message_form = new PrivateMessageForm(gui_root, user.getNick());
			gui_root.addModalForm(private_message_form);
		}

		public final void rowChosen(Object context) {
			ChatRoomUser user = (ChatRoomUser)context;
			String item_text;
			if (ChatCommand.isIgnoring(user.getNick()))
				item_text = getI18N("unignore");
			else
				item_text = getI18N("ignore");
			menu.getItem(PULLDOWN_INDEX_IGNORE).setLabelString(item_text);
		}
	}

}
