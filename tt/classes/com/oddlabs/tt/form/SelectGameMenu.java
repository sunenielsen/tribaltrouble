package com.oddlabs.tt.form;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.oddlabs.matchmaking.ChatRoomEntry;
import com.oddlabs.matchmaking.Game;
import com.oddlabs.matchmaking.GameHost;
import com.oddlabs.matchmaking.GameSession;
import com.oddlabs.matchmaking.MatchmakingServerInterface;
import com.oddlabs.matchmaking.Profile;
import com.oddlabs.matchmaking.RankingEntry;
import com.oddlabs.tt.font.Font;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.ChatPanel;
import com.oddlabs.tt.gui.ChatRoomInfo;
import com.oddlabs.tt.gui.ColumnInfo;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIImage;
import com.oddlabs.tt.gui.GUIObject;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.IntegerLabel;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.MultiColumnComboBox;
import com.oddlabs.tt.gui.Panel;
import com.oddlabs.tt.gui.PanelGroup;
import com.oddlabs.tt.gui.PulldownItem;
import com.oddlabs.tt.gui.PulldownMenu;
import com.oddlabs.tt.gui.Row;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.guievent.FocusListener;
import com.oddlabs.tt.guievent.EnterListener;
import com.oddlabs.tt.guievent.ItemChosenListener;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.guievent.RowListener;
import com.oddlabs.tt.net.ChatCommand;
import com.oddlabs.tt.net.MatchmakingListener;
import com.oddlabs.tt.net.Network;
import com.oddlabs.tt.net.GameNetwork;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.resource.WorldGenerator;
import com.oddlabs.tt.util.ServerMessageBundler;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.delegate.Menu;
import com.oddlabs.net.NetworkSelector;

public final strictfp class SelectGameMenu extends Form implements MatchmakingListener, TerrainMenuListener {
	private static final int BUTTON_WIDTH_SHORT = 60;
	private final static int BUTTON_WIDTH = 110;
	private final static int BUTTON_WIDTH_LONG = 150;
	private final static int BUTTON_WIDTH_EXTRA_LONG = 170;

	private final static int PANEL_INDEX_GAME = 0;
	private final static int PANEL_INDEX_CHAT = 1;
	private final static int PANEL_INDEX_HIGHSCORE = 2;
			
	private final Menu main_menu;
	private final ProfilesForm profiles_form;
	private final Panel[] panels = new Panel[3];

	// List of games
	private final Panel game_list_panel;
	private final MultiColumnComboBox game_list_box;
	private final List game_hosts = new ArrayList();

	// List of chat rooms
	private final Panel chat_room_list_panel;
	private final MultiColumnComboBox chat_room_list_box;
	private final List chat_rooms = new ArrayList();
	private final GUIRoot gui_root;
	private final NetworkSelector network;

	private final MultiColumnComboBox ranking_list_box;

	private final int game_name_size;
	private final int user_name_size;
	private final int room_name_size;

	private final ResourceBundle bundle = ResourceBundle.getBundle(SelectGameMenu.class.getName());

	private GameMenu game_panel;
	private ChatPanel chat_panel;
	private PanelGroup panel_group;

	public SelectGameMenu(NetworkSelector network, GUIRoot gui_root, Menu main_menu) {
		this(network, gui_root, main_menu, 0);
	}

	public SelectGameMenu(NetworkSelector network, GUIRoot gui_root, Menu main_menu, int panel_index) {
		this.main_menu = main_menu;
		this.gui_root = gui_root;
		this.network = network;

		// Game panel
		game_list_panel = new Panel(Utils.getBundleString(bundle, "games_caption"));
		Label label_headline = new Label(Utils.getBundleString(bundle, "multiplayer_caption"), Skin.getSkin().getHeadlineFont());
		game_list_panel.addChild(label_headline);
		game_list_panel.addFocusListener(new GameListPanelListener());
		game_name_size = 340;
		ColumnInfo[] infos = new ColumnInfo[]{
			new ColumnInfo(Utils.getBundleString(bundle, "game_name"), game_name_size),
			new ColumnInfo(Utils.getBundleString(bundle, "rated"), 120),
			new ColumnInfo(Utils.getBundleString(bundle, "speed"), 120),
			new ColumnInfo(Utils.getBundleString(bundle, "map_size"), 120)};
		game_list_box = new MultiColumnComboBox(gui_root, infos, 350);
		game_list_box.addRowListener(new GameDoubleClickedListener());
		game_list_panel.addChild(game_list_box);

		PulldownMenu game_list_pulldown_menu = new PulldownMenu();
		game_list_pulldown_menu.addItem(new PulldownItem(Utils.getBundleString(bundle, "join")));
		game_list_pulldown_menu.addItem(new PulldownItem(Utils.getBundleString(bundle, "game_info")));
		game_list_pulldown_menu.addItemChosenListener(new PulldownListener(game_list_box));
		game_list_box.setPulldownMenu(game_list_pulldown_menu);

		HorizButton update_list_button = new HorizButton(Utils.getBundleString(bundle, "update_list"), BUTTON_WIDTH_EXTRA_LONG);
		game_list_panel.addChild(update_list_button);
		update_list_button.addMouseClickListener(new UpdateGameListListener());
		
		HorizButton create_button = new HorizButton(Utils.getBundleString(bundle, "create_game"), BUTTON_WIDTH_LONG);
		game_list_panel.addChild(create_button);
		create_button.addMouseClickListener(new CreateGameListener());
		
		HorizButton join_button = new HorizButton(Utils.getBundleString(bundle, "join_game"), BUTTON_WIDTH);
		game_list_panel.addChild(join_button);
		join_button.addMouseClickListener(new JoinGameListener());

		// Place game panel objects
		label_headline.place();
		game_list_box.place(label_headline, BOTTOM_LEFT);
		
		update_list_button.place(game_list_box, BOTTOM_LEFT);
		create_button.place(update_list_button, RIGHT_MID);
		join_button.place(create_button, RIGHT_MID);
		
		game_list_panel.compileCanvas();
		panels[PANEL_INDEX_GAME] = game_list_panel;

		// League panel
		Panel highscore_list_panel = new Panel(Utils.getBundleString(bundle, "league_caption"));
		label_headline = new Label(Utils.getBundleString(bundle, "league_description"), Skin.getSkin().getHeadlineFont());
		highscore_list_panel.addChild(label_headline);
		user_name_size = 250;
		ColumnInfo[] score_infos = new ColumnInfo[]{
			new ColumnInfo(Utils.getBundleString(bundle, "rank"), 50),
			new ColumnInfo(Utils.getBundleString(bundle, "name"), user_name_size),
			new ColumnInfo(Utils.getBundleString(bundle, "rating"), 100),
			new ColumnInfo(Utils.getBundleString(bundle, "wins"), 100),
			new ColumnInfo(Utils.getBundleString(bundle, "losses"), 100),
			new ColumnInfo(Utils.getBundleString(bundle, "invalid"), 100)};
		ranking_list_box = new MultiColumnComboBox(gui_root, score_infos, 350);
		highscore_list_panel.addChild(ranking_list_box);

		HorizButton update_scores_button = new HorizButton(Utils.getBundleString(bundle, "update_scores"), BUTTON_WIDTH_EXTRA_LONG);
		highscore_list_panel.addChild(update_scores_button);
		update_scores_button.addMouseClickListener(new UpdateScoresListener());
		
		// Place score panel objects
		label_headline.place();
		ranking_list_box.place(label_headline, BOTTOM_LEFT);
		
		update_scores_button.place(ranking_list_box, BOTTOM_LEFT);
		
		highscore_list_panel.compileCanvas();
		panels[PANEL_INDEX_HIGHSCORE] = highscore_list_panel;

		// Chat room list panel
		chat_room_list_panel = new Panel(Utils.getBundleString(bundle, "chat_caption"));
		label_headline = new Label(Utils.getBundleString(bundle, "chat_rooms_caption"), Skin.getSkin().getHeadlineFont());
		chat_room_list_panel.addChild(label_headline);

		room_name_size = 600;
		infos = new ColumnInfo[]{
			new ColumnInfo(Utils.getBundleString(bundle, "room"), room_name_size),
			new ColumnInfo(Utils.getBundleString(bundle, "users"), 100)};
		chat_room_list_box = new MultiColumnComboBox(gui_root, infos, 350);
		chat_room_list_box.addRowListener(new RoomDoubleClickedListener());
		chat_room_list_panel.addChild(chat_room_list_box);

		update_list_button = new HorizButton(Utils.getBundleString(bundle, "update_rooms"), BUTTON_WIDTH_EXTRA_LONG);
		chat_room_list_panel.addChild(update_list_button);
		update_list_button.addMouseClickListener(new UpdateRoomListListener());
		
		create_button = new HorizButton(Utils.getBundleString(bundle, "create_room"), BUTTON_WIDTH_LONG);
		chat_room_list_panel.addChild(create_button);
		create_button.addMouseClickListener(new CreateRoomListener());
		
		join_button = new HorizButton(Utils.getBundleString(bundle, "join_room"), BUTTON_WIDTH);
		chat_room_list_panel.addChild(join_button);
		join_button.addMouseClickListener(new JoinRoomListener());

		// Place chat room list panel
		label_headline.place();
		chat_room_list_box.place(label_headline, BOTTOM_LEFT);
		update_list_button.place(chat_room_list_box, BOTTOM_LEFT);
		create_button.place(update_list_button, RIGHT_MID);
		join_button.place(create_button, RIGHT_MID);
		chat_room_list_panel.compileCanvas();

		// Common
		ChatRoomInfo info = Network.getMatchmakingClient().getChatRoomInfo();
		if (info != null) {
			chat_panel = createChatRoomPanel(info);
			panels[PANEL_INDEX_CHAT] = chat_panel;
		} else {
			panels[PANEL_INDEX_CHAT] = chat_room_list_panel;
		}
		panel_group = new PanelGroup(panels, panel_index);
		addChild(panel_group);

		HorizButton logout_button = new HorizButton(Utils.getBundleString(bundle, "logout"), BUTTON_WIDTH);
		addChild(logout_button);
		logout_button.addMouseClickListener(new CancelListener(this));
	
		panel_group.place();
		logout_button.place(ORIGIN_BOTTOM_RIGHT);
		compileCanvas();

		Network.setMatchmakingListener(this);
		updateList(MatchmakingServerInterface.TYPE_GAME);
		updateList(MatchmakingServerInterface.TYPE_CHAT_ROOM_LIST);
		updateList(MatchmakingServerInterface.TYPE_RANKING_LIST);

		profiles_form = new ProfilesForm(gui_root, main_menu, this);
		if (Network.getMatchmakingClient().getProfile() == null && Renderer.isRegistered()) {
			main_menu.setMenuCentered(profiles_form);
			Network.getMatchmakingClient().requestProfiles();
		} else {
			main_menu.setMenuCentered(this);
			if (Network.getMatchmakingClient().getProfile() == null && !Renderer.isRegistered()) {
				ResourceBundle db = ResourceBundle.getBundle(DemoForm.class.getName());
				Form demo_form = new DemoForm(gui_root, Utils.getBundleString(db, "profile_unavailable_header"), new GUIImage(512, 256, 0f, 0f, 1f, 1f, "/textures/gui/demo_multiplayer"), Utils.getBundleString(db, "profile_unavailable"));
				gui_root.addModalForm(demo_form);
				Network.getMatchmakingClient().setProfile(null);
			}
		}
	}

	private final void setPanel(int index, Panel panel) {
		panels[index] = panel;
		PanelGroup temp_group = new PanelGroup(panels, index);
		temp_group.setPos(panel_group.getX(), panel_group.getY());
		panel_group.remove();
		panel_group = temp_group;
		addChild(panel_group);
		panel.setFocus();
	}

	private final ChatPanel createChatRoomPanel(ChatRoomInfo info) {
		ChatPanel panel = new ChatPanel(gui_root, info, chat_room_list_panel.getWidth(), chat_room_list_panel.getHeight(), BUTTON_WIDTH_SHORT, new SendChatListener(), new LeaveListener());
		Network.getChatHub().addListener(panel);
		return panel;
	}

	public final void createGameMenu(GameNetwork game_network, Game game, WorldGenerator generator, int player_slot) {
		game_panel = new GameMenu(game_network, gui_root, this, game, generator, player_slot, game_list_panel.getWidth(), game_list_panel.getHeight(), BUTTON_WIDTH);
		setGameMenu(game_panel);
		game_network.getClient().setConfigurationListener(game_panel);
	}

	public final void setFocus() {
		game_list_panel.setFocus();
	}
	
	private final static void updateList(int type) {
		Network.getMatchmakingClient().requestList(type);
	}

	public final void connectionLost() {
		leaveChatRoom();
		remove();
		if (profiles_form != null)
			profiles_form.connectionLost();
		gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle, "connection_lost")));
	}

	public final void loggedIn() {
		assert false;
	}

	public final void loginError(int error_code) {
		assert false;
	}
	
	public void terrainMenuCancel() {
		setPanel(PANEL_INDEX_GAME, game_list_panel);
	}

	public void terrainMenuOK() {

	}

	private void setGameMenu(Panel panel) {
		updateList(MatchmakingServerInterface.TYPE_GAME);
		setPanel(PANEL_INDEX_GAME, panel);
	}
	
	public void removeGameMenu() {
		setPanel(PANEL_INDEX_GAME, game_list_panel);
	}
	
	public final void joinedChat(ChatRoomInfo info) {
		if (chat_panel != null) {
			chat_panel.connectionLost();
			Network.getChatHub().removeListener(chat_panel);
		}
		chat_panel = createChatRoomPanel(info);
		setPanel(PANEL_INDEX_CHAT, chat_panel);
	}

	protected final void doRemove() {
		super.doRemove();
		Network.getChatHub().removeListener(chat_panel);
	}

	public final void receivedProfiles(Profile[] profiles, String last_nick) {
		profiles_form.receivedProfiles(profiles, last_nick);
	}

	public final void updateChatRoom(ChatRoomInfo info) {
		chat_panel.update(info);
	}

	public final void receivedList(int type, Object[] names) {
		switch (type) {
			case MatchmakingServerInterface.TYPE_GAME:
				for (int i = 0; i < names.length; i++)
					game_hosts.add(names[i]);
				updateGameListGUI();
				break;
			case MatchmakingServerInterface.TYPE_CHAT_ROOM_LIST:
				for (int i = 0; i < names.length; i++)
					chat_rooms.add(names[i]);
				updateChatRoomListGUI();
				break;
			case MatchmakingServerInterface.TYPE_RANKING_LIST:
				for (int i = 0; i < names.length; i++)
					updateRankingList((RankingEntry)names[i]);
				break;
			default:
				throw new RuntimeException();
		}
	}

	public final void clearList(int type) {
		switch (type) {
			case MatchmakingServerInterface.TYPE_GAME:
				game_hosts.clear();
				game_list_box.clear();
				break;
			case MatchmakingServerInterface.TYPE_CHAT_ROOM_LIST:
				chat_rooms.clear();
				chat_room_list_box.clear();
				break;
			case MatchmakingServerInterface.TYPE_RANKING_LIST:
				ranking_list_box.clear();
				break;
			default:
				throw new RuntimeException();
		}
	}

	private final void updateRankingList(RankingEntry ranking) {
		Row row = new Row(new GUIObject[]{
			new IntegerLabel(ranking.getRanking(), Skin.getSkin().getMultiColumnComboBoxData().getFont()),
			new Label(ranking.getName(), Skin.getSkin().getMultiColumnComboBoxData().getFont(), user_name_size),
			new IntegerLabel(ranking.getRating(), Skin.getSkin().getMultiColumnComboBoxData().getFont()),
			new IntegerLabel(ranking.getWins(), Skin.getSkin().getMultiColumnComboBoxData().getFont()),
			new IntegerLabel(ranking.getLosses(), Skin.getSkin().getMultiColumnComboBoxData().getFont()),
			new IntegerLabel(ranking.getInvalid(), Skin.getSkin().getMultiColumnComboBoxData().getFont())}, ranking);
		ranking_list_box.addRow(row);
	}

	private final void updateGameListGUI() {
		Font combofont = Skin.getSkin().getMultiColumnComboBoxData().getFont();
		for (int i = 0; i < game_hosts.size(); i++) {
			GameHost game_host = (GameHost)game_hosts.get(i);
			String rated = ServerMessageBundler.getRatedString(game_host.getGame().isRated());
			String size = ServerMessageBundler.getSizeString(game_host.getGame().getSize());;
			Row row = new Row(new GUIObject[]{
				new Label(game_host.getGame().getName(), combofont, game_name_size),
				new Label(rated, combofont),
				new Label(ServerMessageBundler.getGamespeedString(game_host.getGame().getGamespeed()), combofont),
				new Label(size, combofont)},
								game_host);
			game_list_box.addRow(row);
		}
	}

	private final void updateChatRoomListGUI() {
		Font combofont = Skin.getSkin().getMultiColumnComboBoxData().getFont();
		for (int i = 0; i < chat_rooms.size(); i++) {
			ChatRoomEntry chat_room_info = (ChatRoomEntry)chat_rooms.get(i);
			String users_and_max = Utils.getBundleString(bundle, "users_and_max", new Object[]{new Integer(chat_room_info.getNumJoined()),
				new Integer(MatchmakingServerInterface.MAX_ROOM_USERS)});
			Row row = new Row(new GUIObject[]{
				new Label(chat_room_info.getName(), combofont, room_name_size),
				new Label(users_and_max, combofont)},
								chat_room_info);
			chat_room_list_box.addRow(row);
		}
	}

	protected final void doCancel() {
		leaveChatRoom();
		if (game_panel != null)
			game_panel.cancel();
		Network.getMatchmakingClient().close();
	}

	private final void leaveChatRoom() {
		Network.getMatchmakingClient().leaveChatRoom();
		if (chat_panel != null)
			chat_panel.connectionLost();
		chat_panel = null;
		setPanel(PANEL_INDEX_CHAT, chat_room_list_panel);
	}

	private final void joinGame(GameHost selected_game) {
		if (Network.getMatchmakingClient().getProfile() != null) {
			if (selected_game != null) {
				boolean rated = selected_game.getGame().isRated();
				if (rated && Network.getMatchmakingClient().getProfile().getWins() < GameSession.MIN_WINS_FOR_RANKING) {
					String min_wins = Utils.getBundleString(bundle, "min_wins", new Object[]{new Integer(GameSession.MIN_WINS_FOR_RANKING)});
					gui_root.addModalForm(new MessageForm(min_wins));
				} else {
					Game game = selected_game.getGame();
					main_menu.joinGame(network, gui_root.getGUI(), selected_game.getHostID(), game.isRated(), game.getGamespeed(), game.getMapcode(), this, game.getRandomStartPos(), game.getMaxUnitCount());
				}
			}
		}
	}

	private final void joinRoom(ChatRoomEntry chat_room_info) {
		if (Network.getMatchmakingClient().getProfile() != null) {
			if (chat_room_info != null)
				Network.getMatchmakingClient().joinRoom(gui_root, chat_room_info.getName());
		}
	}

	private final strictfp class RoomDoubleClickedListener implements RowListener {
		public final void rowDoubleClicked(Object context) {
			ChatRoomEntry chat_room_info = (ChatRoomEntry)context;
			joinRoom(chat_room_info);
		}
		
		public final void rowChosen(Object context) {
		}
	}

	private final strictfp class GameListPanelListener implements FocusListener {
		public final void activated(boolean activated) {
			if (activated)
				updateList(MatchmakingServerInterface.TYPE_GAME);
		}
	}

	private final strictfp class GameDoubleClickedListener implements RowListener {
		public final void rowDoubleClicked(Object context) {
			GameHost selected_game = (GameHost)context;
			joinGame(selected_game);
		}
		
		public final void rowChosen(Object context) {
		}
	}

	private final strictfp class JoinGameListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			GameHost selected_game = (GameHost)game_list_box.getSelected();
			joinGame(selected_game);
		}
	}

	private final strictfp class UpdateScoresListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			updateList(MatchmakingServerInterface.TYPE_RANKING_LIST);
		}
	}
	
	private final strictfp class UpdateGameListListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			updateList(MatchmakingServerInterface.TYPE_GAME);
		}
	}
	
	private final strictfp class CreateGameListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			if (Network.getMatchmakingClient().getProfile() != null) {
				Panel panel = new Panel(Utils.getBundleString(bundle, "game"));
				Group g = new TerrainMenu(network, gui_root, main_menu, true, SelectGameMenu.this);
				panel.addChild(g);
				g.place();
				panel.compileCanvas();
				setPanel(PANEL_INDEX_GAME, panel);
			}
		}
	}

	private final strictfp class JoinRoomListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			ChatRoomEntry chat_room_info = (ChatRoomEntry)chat_room_list_box.getSelected();
			joinRoom(chat_room_info);
		}
	}

	private final strictfp class UpdateRoomListListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			updateList(MatchmakingServerInterface.TYPE_CHAT_ROOM_LIST);
		}
	}
	
	private final strictfp class CreateRoomListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			if (Network.getMatchmakingClient().getProfile() != null) {
				main_menu.setMenuCentered(new CreateChatRoomForm(gui_root, main_menu, SelectGameMenu.this));
			}
		}
	}

	private strictfp final class SendChatListener implements EnterListener {
		public final void enterPressed(CharSequence text) {
			String chat = text.toString();
			if (!chat.equals("")) {
				if (!ChatCommand.filterCommand(gui_root.getInfoPrinter(), chat)) {
					Network.getMatchmakingClient().getInterface().sendMessageToRoom(chat);
				}
			}
		}
	}

	private strictfp final class LeaveListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			leaveChatRoom();
		}
	}

	private final strictfp class PulldownListener implements ItemChosenListener {
		private final MultiColumnComboBox box;

		public PulldownListener(MultiColumnComboBox box) {
			this.box = box;
		}

		public final void itemChosen(PulldownMenu menu, int item_index) {
			GameHost host = (GameHost)box.getRightClickedRowData();
			switch (item_index) {
				case 0: //Join
					joinGame(host);
					break;
				case 1: //Info
					gui_root.addModalForm(new GameInfoForm(host.getGame()));
					break;
				default:
					throw new RuntimeException();
			}
			box.setFocus();
		}
	}

}
