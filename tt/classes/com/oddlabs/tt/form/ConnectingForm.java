package com.oddlabs.tt.form;

import java.util.Random;
import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.gui.CancelButton;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.ConfigurationListener;
import com.oddlabs.tt.net.Network;
import com.oddlabs.tt.net.GameNetwork;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.resource.WorldGenerator;
import com.oddlabs.tt.util.Utils;

public final strictfp class ConnectingForm extends Form implements ConfigurationListener {
	private final SelectGameMenu owner;
	private final boolean multiplayer;
	private final ResourceBundle bundle = ResourceBundle.getBundle(ConnectingForm.class.getName());
	private final GUIRoot gui_root;
	private final GameNetwork game_network;

	public ConnectingForm(GameNetwork game_network, GUIRoot gui_root, SelectGameMenu owner, boolean multiplayer) {
		this.game_network = game_network;
		this.gui_root = gui_root;
		this.owner = owner;
		this.multiplayer = multiplayer;
		ResourceBundle bundle = ResourceBundle.getBundle(ConnectingForm.class.getName());

		Label info_label;
		if (multiplayer)
			info_label = new Label(Utils.getBundleString(bundle, "connecting"), Skin.getSkin().getHeadlineFont());
		else
			info_label = new Label(Utils.getBundleString(bundle, "starting"), Skin.getSkin().getHeadlineFont());
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
	}

	public final void connected(Client client, Game game, WorldGenerator generator, int player_slot) {
		if (multiplayer) {
			Random random = new Random(LocalEventQueue.getQueue().getHighPrecisionManager().getTick());
			random.nextFloat(); // first one allways in same area
			int race = (int)(random.nextFloat()*(RacesResources.getNumRaces() - 1) + .5f);
			int team = player_slot;
			if (game.isRated())
				team = player_slot%2;
			client.getServerInterface().setPlayerSlot(player_slot, PlayerSlot.HUMAN, race, team, false, PlayerSlot.AI_NONE);
			remove();
			owner.createGameMenu(game_network, game, generator, player_slot);
//			GameMenu panel = new GameMenu(owner, game, generator, player_slot);
//			owner.setGameMenu(panel);
//			Network.setConfigurationListener(panel);
		} else {
			assert player_slot == 0: "player_slot must be 0";
		}
	}

	public final void chat(int player_slot, String chat) {
	}
	
	public final void setPlayers(PlayerSlot[] players) {
		assert !multiplayer;
	}

	public final void connectionLost() {
		remove();
		gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle, "connection_lost")));
	}

	public final void gameStarted() {
		remove();
//		main_menu.remove();
		assert !multiplayer;
	}

	protected final void doCancel() {
		game_network.close();
	}
}
