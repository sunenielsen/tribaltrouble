package com.oddlabs.tt.form;

import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Set;
import java.util.Random;
import java.util.HashSet;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.matchmaking.GameSession;
import com.oddlabs.matchmaking.MatchmakingServerInterface;
import com.oddlabs.tt.font.Font;
import com.oddlabs.tt.gui.Box;
import com.oddlabs.tt.gui.Diode;
import com.oddlabs.tt.gui.EditLine;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.FormData;
import com.oddlabs.tt.gui.GUIImage;
import com.oddlabs.tt.gui.GUIObject;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.Panel;
import com.oddlabs.tt.gui.PulldownButton;
import com.oddlabs.tt.gui.PulldownItem;
import com.oddlabs.tt.gui.PulldownMenu;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.TextBox;
import com.oddlabs.tt.guievent.EnterListener;
import com.oddlabs.tt.guievent.ItemChosenListener;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.net.ChatMessage;
import com.oddlabs.tt.net.ChatCommand;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.ChatListener;
import com.oddlabs.tt.net.ConfigurationListener;
import com.oddlabs.tt.net.Network;
import com.oddlabs.tt.net.GameNetwork;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.resource.WorldGenerator;
import com.oddlabs.tt.util.Utils;

public final strictfp class GameMenu extends Panel implements ConfigurationListener, ChatListener {
	private final static int OPEN_INDEX = 0;
	private final static int CLOSED_INDEX = 1;
	private final static int COMPUTER_EASY_INDEX = 2;
	private final static int COMPUTER_NORMAL_INDEX = 3;
	private final static int COMPUTER_HARD_INDEX = 4;

	private final static int SEND_BUTTON_WIDTH = 60;

	private final static int RATING_WIDTH = 80;

	private final PulldownButton[] slot_buttons;
	private final PulldownButton[] race_buttons;
	private final PulldownButton[] team_buttons;
	private final Label[] ratings;
	private final Label chat_info;
	private final TextBox chat_box;
	private final EditLine chat_line;
	private final Diode[] ready_marks;
	private final HorizButton send_button;
	private final HorizButton ready_button;
	private final HorizButton start_button;
	private final SelectGameMenu owner;
	private final GUIRoot gui_root;
	private final int local_player_slot;
	private final boolean rated;
	private final Game game;
	private final ResourceBundle bundle = getBundle();
	private final GameNetwork game_network;
	private SortedSet human_names = new TreeSet();

	private boolean updating;
	private boolean ready;

	public final static ResourceBundle getBundle() {
		return ResourceBundle.getBundle(GameMenu.class.getName());
	}

	public GameMenu(GameNetwork game_network, GUIRoot gui_root, SelectGameMenu owner, Game game, WorldGenerator generator, int player_slot, int compare_width, int compare_height, int button_width) {
		super(Utils.getBundleString(getBundle(), "game_caption"));
		this.game_network = game_network;
		this.owner = owner;
		this.gui_root = gui_root;
		this.local_player_slot = player_slot;
		this.rated = game.isRated();
		this.game = game;

		String tag = "";
		if (rated)
			tag = Utils.getBundleString(bundle, "rated") + " ";
		Label game_name_label = new Label(Utils.getBundleString(bundle, "game") + " " + tag + game.getName(), Skin.getSkin().getHeadlineFont());
		slot_buttons = new PulldownButton[MatchmakingServerInterface.MAX_PLAYERS];
		race_buttons = new PulldownButton[MatchmakingServerInterface.MAX_PLAYERS];
		team_buttons = new PulldownButton[MatchmakingServerInterface.MAX_PLAYERS];
		ready_marks = new Diode[MatchmakingServerInterface.MAX_PLAYERS];
		ratings = new Label[MatchmakingServerInterface.MAX_PLAYERS];
		Group player_group = new Group();
		GUIObject previous = null;
		for (int i = 0; i < MatchmakingServerInterface.MAX_PLAYERS; i++)
			previous = createPlayerPulldown(gui_root, player_group, previous, slot_buttons, race_buttons, team_buttons, ready_marks, ratings, i, MatchmakingServerInterface.MAX_PLAYERS);
		player_group.compileCanvas();
		addChild(player_group);

		Box pdata = Skin.getSkin().getPanelData().getBox();
		FormData fdata = Skin.getSkin().getFormData();
		
		int width = compare_width - pdata.getLeftOffset() - pdata.getRightOffset();
		chat_info = new Label(Utils.getBundleString(bundle, "chat"), Skin.getSkin().getEditFont(), width);
		Group chat_line_group = new Group();
		chat_line = new EditLine(width - SEND_BUTTON_WIDTH - fdata.getObjectSpacing(), 100);
		send_button = new HorizButton(Utils.getBundleString(bundle, "send"), SEND_BUTTON_WIDTH);
		send_button.addMouseClickListener(new SendListener());
		chat_line_group.addChild(chat_line);
		chat_line.place();
		chat_line_group.addChild(send_button);
		send_button.place(chat_line, RIGHT_MID);
		chat_line_group.compileCanvas();
		addChild(chat_line_group);
		
		chat_line.addEnterListener(new ChatListener());
		addChild(game_name_label);
		addChild(chat_info);
		
		start_button = new HorizButton(Utils.getBundleString(bundle, "start"), button_width);
		if (local_player_slot == 0) {
			addChild(start_button);
			start_button.addMouseClickListener(new StartListener());
		}
		int height = compare_height - pdata.getTopOffset() - pdata.getBottomOffset() - chat_info.getHeight()
			- chat_line.getHeight() - game_name_label.getHeight() - player_group.getHeight() - start_button.getHeight() - 5*fdata.getObjectSpacing();
		chat_box = new TextBox(width, height, Skin.getSkin().getEditFont(), -1);
		addChild(chat_box);
		ready_button = new HorizButton(Utils.getBundleString(bundle, "ready"), button_width);
		addChild(ready_button);
		ready_button.addMouseClickListener(new ReadyListener());
		HorizButton cancel_button = new HorizButton(Utils.getBundleString(bundle, "cancel"), button_width);
		addChild(cancel_button);
		cancel_button.addMouseClickListener(new CancelButtonListener());
		HorizButton info_button = new HorizButton(Utils.getBundleString(bundle, "info"), button_width);
		addChild(info_button);
		info_button.addMouseClickListener(new InfoButtonListener());
		
		game_name_label.place();
		player_group.place(game_name_label, BOTTOM_LEFT);
		chat_info.place(player_group, BOTTOM_LEFT);
		chat_box.place(chat_info, BOTTOM_LEFT);
		chat_line_group.place(chat_box, BOTTOM_LEFT);
		cancel_button.place(chat_line_group, BOTTOM_RIGHT);
		info_button.place(chat_line_group, BOTTOM_LEFT);
		ready_button.place(cancel_button, LEFT_MID);
		if (local_player_slot == 0)
			start_button.place(ready_button, LEFT_MID);
		Font font = Skin.getSkin().getEditFont();
		if (rated) {
			Label rating = new Label(Utils.getBundleString(bundle, "rating"), font, RATING_WIDTH, Label.ALIGN_RIGHT);
			addChild(rating);
			rating.place(player_group, TOP_RIGHT);
		}
		compileCanvas();
	}

	private final void adjustPlayerSlot(int player_slot) {
		if (updating || game_network.getClient() == null)
			return;
		PlayerSlot player = game_network.getClient().getPlayers()[player_slot];
		int index = slot_buttons[player_slot].getMenu().getChosenItemIndex();
		if (index == COMPUTER_HARD_INDEX && !Renderer.isRegistered()) {
			slot_buttons[player_slot].getMenu().chooseItem(COMPUTER_NORMAL_INDEX);
			index = COMPUTER_NORMAL_INDEX;
			ResourceBundle db = ResourceBundle.getBundle(DemoForm.class.getName());
			Form demo_form = new DemoForm(gui_root, Utils.getBundleString(db, "hard_ai_unavailable_header"), new GUIImage(512, 256, 0f, 0f, 1f, 1f, "/textures/gui/demo_hardai"), Utils.getBundleString(db, "hard_ai_unavailable"));
			gui_root.addModalForm(demo_form);
		}
		int race_index = race_buttons[player_slot].getMenu().getChosenItemIndex();
		int team_index = team_buttons[player_slot].getMenu().getChosenItemIndex();
		int difficulty_index = slot_buttons[player_slot].getMenu().getChosenItemIndex() - 1;
		boolean race_changed = player.getInfo() == null || race_index != player.getInfo().getRace();
		boolean team_changed = player.getInfo() == null || team_index != player.getInfo().getTeam();
		boolean ready_changed = ready != player.isReady();
		boolean difficulty_changed = player.getInfo() == null || player.getAIDifficulty() != difficulty_index;
		PulldownButton slot_button = slot_buttons[player_slot];
		switch (index) {
			case OPEN_INDEX:
				if ((player.getType() != PlayerSlot.OPEN && player.getType() != PlayerSlot.HUMAN) || race_changed || team_changed || ready_changed) {
					if (player_slot == local_player_slot) {
						int new_type = PlayerSlot.HUMAN;
						game_network.getClient().getServerInterface().setPlayerSlot(player_slot, new_type, race_index, team_index, ready, PlayerSlot.AI_NONE);
					} else {
						game_network.getClient().getServerInterface().resetSlotState(player_slot, true);
					}
				}
				break;
			case CLOSED_INDEX:
				if (player.getType() != PlayerSlot.CLOSED || race_changed || team_changed) {
					slot_button.getMenu().getItem(OPEN_INDEX).setLabelString(Utils.getBundleString(bundle, "open"));
					game_network.getClient().getServerInterface().resetSlotState(player_slot, false);
				}
				break;
			case COMPUTER_EASY_INDEX:
			case COMPUTER_NORMAL_INDEX:
			case COMPUTER_HARD_INDEX:
				assert !rated;
				boolean new_ai = player.getType() != PlayerSlot.AI;
				if (new_ai || race_changed || team_changed || difficulty_changed) {
					slot_button.getMenu().getItem(OPEN_INDEX).setLabelString(Utils.getBundleString(bundle, "open"));
					if (new_ai) {
						team_index = player_slot;
						race_index = new Random(LocalEventQueue.getQueue().getHighPrecisionManager().getTick()).nextInt(RacesResources.getNumRaces());
					}
					game_network.getClient().getServerInterface().setPlayerSlot(player_slot, PlayerSlot.AI, race_index, team_index, true, difficulty_index);
				}
				break;
			default:
				throw new RuntimeException("Invalid item index");
		}
	}
	
	public final void connected(Client client, Game game, WorldGenerator generator, int player_slot) {
		assert false;
	}

	public final void setFocus() {
		chat_line.setFocus();
	}

	private final int countHumans(PlayerSlot[] players) {
		int result = 0;
		for (int i = 0; i < players.length; i++)
			if (players[i].getType() == PlayerSlot.HUMAN)
				result++;
		return result;
	}

	public final void setPlayers(PlayerSlot[] players) {
		int num_humans = countHumans(players);
		int[] player_slots = new int[num_humans];
		int[] player_ratings = new int[num_humans];
		int[] player_teams = new int[num_humans];
		int human_index = 0;
		updating = true;
		SortedSet new_human_names = new TreeSet();
		for (int i = 0; i < players.length; i++) {
			PlayerSlot player = players[i];
			PulldownButton slot_button = slot_buttons[i];
			PulldownButton race_button = race_buttons[i];
			PulldownButton team_button = team_buttons[i];
			Diode ready_mark = ready_marks[i];
			ready_mark.setLit(player.isReady());
			race_button.getMenu().chooseItem(player.getInfo() != null ? player.getInfo().getRace() : 0);
			team_button.getMenu().chooseItem(player.getInfo() != null ? player.getInfo().getTeam() : 0);
			if (player.getType() != PlayerSlot.CLOSED) {
				slot_button.getMenu().getItem(OPEN_INDEX).setLabelString(Utils.getBundleString(bundle, "open"));
				slot_button.getMenu().chooseItem(OPEN_INDEX);
			} else {
				slot_button.getMenu().chooseItem(CLOSED_INDEX);
			}
			race_button.setDisabled(true);
			team_button.setDisabled(true);
			if (player.getInfo() != null) {
				PlayerInfo player_info = player.getInfo();
				switch (player.getType()) {
					case PlayerSlot.AI:
						assert !rated;
						slot_button.getMenu().chooseItem(player.getAIDifficulty() + 1);
						race_button.setDisabled(!canControlSlot(i));
						team_button.setDisabled(!canControlSlot(i));
						break;
					case PlayerSlot.HUMAN:
						String player_name = player_info.getName();
						new_human_names.add(player_name);
						slot_button.getMenu().getItem(OPEN_INDEX).setLabelString(player_name);
						slot_button.getMenu().chooseItem(OPEN_INDEX);
						race_button.setDisabled(i != local_player_slot);
						team_button.setDisabled(i != local_player_slot);
						player_slots[human_index] = i;
						player_ratings[human_index] = player.getRating();
						player_teams[human_index] = player_info.getTeam();
						human_index++;
						break;
					default:
						throw new RuntimeException("Unknown Player type: " + player.getType());
				}
			}
		}
		if (rated)
			updateRatedLabels(player_slots, player_ratings, GameSession.calculateMatchPoints(player_ratings, player_teams));
		Iterator it = new_human_names.iterator();
		while (it.hasNext()) {
			String name = (String)it.next();
			if (human_names.contains(name))
				human_names.remove(name);
			else
				playerJoined(name);
		}
		it = human_names.iterator();
		while (it.hasNext()) {
			String name = (String)it.next();
			playerLeft(name);
		}
		human_names = new_human_names;
		setReady(players[local_player_slot].isReady());
		setStartEnable(players);
		updating = false;
	}

	private final void updateRatedLabels(int[] player_slots, int[] player_ratings, int[][] points) {
		for (int i = 0; i < ratings.length; i++) {
			ratings[i].clear();
		}
		for (int i = 0; i < player_slots.length; i++) {
			int slot = player_slots[i];
			if (slot == local_player_slot) {
				int win = points[i][GameSession.WIN];
				int lose = points[i][GameSession.LOSE];
				String rating_change_message = Utils.getBundleString(bundle, "rating_change_message", new Object[]{
					Integer.toString(win), Integer.toString(-lose)});
				chat_info.set(rating_change_message);

			}
			ratings[slot].set("" + player_ratings[i]);
		}
	}

	private final boolean canControlSlot(int slot) {
		return local_player_slot == 0 || slot == local_player_slot;
	}

	private final GUIObject createPlayerPulldown(GUIRoot gui_root, Group group,
			GUIObject previous,
			PulldownButton[] slot_buttons,
			PulldownButton[] race_buttons,
			PulldownButton[] team_buttons,
			Diode[] ready_marks,
			Label[] ratings,
			int index,
			int num_players) {
		PulldownMenu pulldown_menu = new PulldownMenu();
		PulldownItem open_item = new PulldownItem(Utils.getBundleString(bundle, "open"));
		PulldownItem closed_item = new PulldownItem(Utils.getBundleString(bundle, "closed"));
		PulldownItem computer_easy_item = new PulldownItem(Utils.getBundleString(bundle, "easy_ai"));
		PulldownItem computer_normal_item = new PulldownItem(Utils.getBundleString(bundle, "normal_ai"));
		PulldownItem computer_hard_item = new PulldownItem(Utils.getBundleString(bundle, "hard_ai"));
		pulldown_menu.addItem(open_item);
		pulldown_menu.addItem(closed_item);
		if (!rated) {
			pulldown_menu.addItem(computer_easy_item);
			pulldown_menu.addItem(computer_normal_item);
			pulldown_menu.addItem(computer_hard_item);
		}
		PulldownButton pulldown_button = new PulldownButton(gui_root, pulldown_menu, CLOSED_INDEX, 150);
		slot_buttons[index] = pulldown_button;
		group.addChild(pulldown_button);
		if (previous != null)
			pulldown_button.place(previous, BOTTOM_MID);
		else
			pulldown_button.place();
		pulldown_menu.addItemChosenListener(new PlayerSlotListener(index));
		pulldown_button.setDisabled(local_player_slot != 0 || index == local_player_slot);

		PulldownMenu race_pulldown_menu = new PulldownMenu();
		for (int i = 0; i < RacesResources.getNumRaces(); i++) {
			PulldownItem race_item = new PulldownItem(RacesResources.getRaceName(i));
			race_pulldown_menu.addItem(race_item);
		}
		PulldownMenu team_pulldown_menu = new PulldownMenu();
		int num_teams = num_players;
		if (rated)
			num_teams = 2;
		for (int i = 0; i < num_teams; i++) {
			String team_str = Utils.getBundleString(bundle, "team", new Object[]{Integer.toString(i + 1)});
			PulldownItem race_item = new PulldownItem(team_str);
			team_pulldown_menu.addItem(race_item);
		}
		PulldownButton race_pulldown_button = new PulldownButton(gui_root, race_pulldown_menu, 0, 115);
		PulldownButton team_pulldown_button = new PulldownButton(gui_root, team_pulldown_menu, index%num_teams, 115);
		race_buttons[index] = race_pulldown_button;
		team_buttons[index] = team_pulldown_button;
		group.addChild(race_pulldown_button);
		group.addChild(team_pulldown_button);
		race_pulldown_button.place(pulldown_button, RIGHT_MID);
		team_pulldown_button.place(race_pulldown_button, RIGHT_MID);
		race_pulldown_menu.addItemChosenListener(new PlayerSlotListener(index));
		team_pulldown_menu.addItemChosenListener(new PlayerSlotListener(index));
		race_pulldown_button.setDisabled(!canControlSlot(index));
		team_pulldown_button.setDisabled(!canControlSlot(index));

		Diode ready_mark = new Diode();
		ready_marks[index] = ready_mark;
		group.addChild(ready_mark);
		ready_mark.place(team_pulldown_button, RIGHT_MID);
		Font font = Skin.getSkin().getEditFont();
		ratings[index] = new Label("", font, RATING_WIDTH, Label.ALIGN_RIGHT);
		if (rated) {
			group.addChild(ratings[index]);
			ratings[index].place(ready_mark, RIGHT_MID);
		}
		String player_str = Utils.getBundleString(bundle, "player", new Object[]{Integer.toString(index + 1)});
		Label label = new Label(player_str, Skin.getSkin().getEditFont());
		label.setColor(Player.COLORS[index]);
		group.addChild(label);
		label.place(pulldown_button, LEFT_MID);

		return pulldown_button;
	}

	protected final void doAdd() {
		super.doAdd();
		Network.getChatHub().addListener(this);
	}

	protected final void doRemove() {
		super.doRemove();
		Network.getChatHub().removeListener(this);
	}

	public final void connectionLost() {
		remove();
		owner.removeGameMenu();
		gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle, "connection_lost")));
	}

	public final void gameStarted() {
//		owner.removeGameMenu();
		setDisabled(true);
	}

	private final void finishChatAppend() {
		chat_box.setOffsetY(Integer.MAX_VALUE);
		getTab().updateNotify();
	}

	public final void chat(ChatMessage message) {
		if (message.type != ChatMessage.CHAT_GAME_MENU)
			return;
		if (chat_box.length() > 0)
			chat_box.append("\n");

		ResourceBundle ingame_bundle = ResourceBundle.getBundle(InGameChatForm.class.getName());

		chat_box.append(message.formatLong());
		finishChatAppend();
	}

	private final void playerLeft(String name) {
		if (chat_box.length() > 0)
			chat_box.append("\n");
		chat_box.append(Utils.getBundleString(bundle, "left_game", new Object[]{name}));
		finishChatAppend();
	}

	private final void playerJoined(String name) {
		if (chat_box.length() > 0)
			chat_box.append("\n");
		chat_box.append(Utils.getBundleString(bundle, "joined_game", new Object[]{name}));
		finishChatAppend();
	}
	
	private final void setReady(boolean r) {
		if (r != ready) {
			ready = r;
			ready_button.setDisabled(ready);
			adjustPlayerSlot(local_player_slot);
		}
	}

	private final void setStartEnable(PlayerSlot[] players) {
		start_button.setDisabled(true);
		if (local_player_slot != 0)
			return;
		for (int i = 0; i < players.length; i++)
			if (!players[i].isReady())
				return;
		start_button.setDisabled(false);
	}

	final void cancel() {
		game_network.close();
		owner.removeGameMenu();
	}

	private final strictfp class InfoButtonListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			gui_root.addModalForm(new GameInfoForm(game));
		}
	}

	private final strictfp class CancelButtonListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			cancel();
		}
	}

	private final strictfp class ReadyListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			setReady(true);
		}
	}

	private static int getNumTeams(PlayerSlot[] players) {
		Set teams = new HashSet();
		for (int i = 0; i < players.length; i++) {
			PlayerSlot current = players[i];
			if (current.getInfo() != null)
				teams.add(new Integer(current.getInfo().getTeam()));
		}
		return teams.size();
	}

	private final strictfp class StartListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			final int MIN_TEAMS = 2;
			int num_teams = getNumTeams(game_network.getClient().getPlayers());
			if (num_teams < MIN_TEAMS) {
				String err_msg = Utils.getBundleString(bundle, "min_teams", new Object[]{Integer.toString(MIN_TEAMS)});
				gui_root.addModalForm(new MessageForm(err_msg));
			} else {
				game_network.getClient().getServerInterface().startServer();
			}
		}
	}

	private final strictfp class PlayerSlotListener implements ItemChosenListener {
		private final int player_slot;

		public PlayerSlotListener(int player_slot) {
			this.player_slot = player_slot;
		}

		public final void itemChosen(PulldownMenu menu, int item_index) {
			setReady(false);
			adjustPlayerSlot(player_slot);
		}
	}

	private final strictfp class ChatListener implements EnterListener {
		public final void enterPressed(CharSequence text) {
			String chat = text.toString();
			if (!chat.equals("")) {
				chat_line.clear();
				if (!ChatCommand.filterCommand(gui_root.getInfoPrinter(), chat))
					game_network.getClient().getServerInterface().chat(chat);
			}
		}
	}

	private strictfp final class SendListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			chat_line.enterPressedAll();
		}
	}

}
