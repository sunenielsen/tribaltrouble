package com.oddlabs.tt.delegate;

import java.net.InetAddress;
import java.util.ResourceBundle;

import org.lwjgl.input.Keyboard;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.camera.Camera;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.delegate.CameraDelegate;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.FreeQuitLabel;
import com.oddlabs.tt.gui.GUIImage;
import com.oddlabs.tt.gui.GUIObject;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.delegate.SelectionDelegate;
import com.oddlabs.tt.form.*;
import com.oddlabs.tt.gui.ImageBuyButton;
import com.oddlabs.tt.gui.KeyboardEvent;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.LabelBox;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.gui.MenuButton;
import com.oddlabs.tt.gui.Renderable;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.guievent.CloseListener;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.Network;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.landscape.WorldParameters;
import com.oddlabs.tt.net.Server;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.player.campaign.CampaignState;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.resource.IslandGenerator;
import com.oddlabs.tt.resource.WorldGenerator;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.net.WorldInitAction;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.trigger.GameOverTrigger;
import com.oddlabs.net.NetworkSelector;

public final strictfp class MainMenu extends Menu {
	public MainMenu(NetworkSelector network, GUIRoot gui_root, Camera camera) {
		super(network, gui_root, camera);
		reload();
	}

	private void addGameTypeButtons() {
		MenuButton tutorial = new MenuButton(Utils.getBundleString(bundle, "tutorial"), COLOR_NORMAL, COLOR_ACTIVE);
		addChild(tutorial);
		tutorial.addMouseClickListener(new TutorialListener());
		MenuButton campaign_menu = new MenuButton(Utils.getBundleString(bundle, "campaign"), COLOR_NORMAL, COLOR_ACTIVE);
		addChild(campaign_menu);
		campaign_menu.addMouseClickListener(new CampaignListener());
		MenuButton single_player = new MenuButton(Utils.getBundleString(bundle, "skirmish"), COLOR_NORMAL, COLOR_ACTIVE);
		addChild(single_player);
		single_player.addMouseClickListener(new SinglePlayerListener());
		if (!Settings.getSettings().hide_multiplayer) {
			MenuButton multi_player = new MenuButton(Utils.getBundleString(bundle, "multiplayer"), COLOR_NORMAL, COLOR_ACTIVE);
			addChild(multi_player);
			multi_player.addMouseClickListener(new MultiPlayerListener());
		}
	}

	private void addRegisterButton() {
		if (!Renderer.isRegistered() && !Settings.getSettings().hide_register) {
			MenuButton register_game = new MenuButton(Utils.getBundleString(bundle, "register"), COLOR_NORMAL, COLOR_ACTIVE);
			addChild(register_game);
			register_game.addMouseClickListener(new RegisterListener());
		}
	}

	private void addBuyButton() {
		if (!Renderer.isRegistered() && Settings.getSettings().online) {
			ImageBuyButton img_buy = new ImageBuyButton(getGUIRoot());
			addChild(img_buy);
		}
	}

	private void addUpdateButton() {
		if (LocalInput.getUpdateInfo() != null && !Settings.getSettings().hide_update) {
			MenuButton update_game = new MenuButton(Utils.getBundleString(bundle, "update"), COLOR_NORMAL, COLOR_ACTIVE);
			addChild(update_game);
			update_game.addMouseClickListener(new UpdateGameListener());
		}
	}

	protected void addButtons() {
		addGameTypeButtons();
		
		addRegisterButton();
		
		addDefaultOptionsButton();

		addUpdateButton();

		addExitButton();

		addBuyButton();

		if (Network.getMatchmakingClient().isConnected()) {
			new SelectGameMenu(getNetwork(), getGUIRoot(), this);
		}
	}

	private final strictfp class MultiPlayerListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			if (Network.getMatchmakingClient().isConnected()) {
				new SelectGameMenu(getNetwork(), getGUIRoot(), MainMenu.this);
			} else {
				Network.getMatchmakingClient().close();
				new LoginForm(getNetwork(), getGUIRoot(), MainMenu.this);
			}
		}
	}

	private final strictfp class RegisterListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			setMenuCentered(new RegistrationForm(getGUIRoot(), true, MainMenu.this));
		}
	}

	private final strictfp class UpdateGameListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			setMenuCentered(new UpdateGameForm(getGUIRoot(), MainMenu.this));
		}
	}

	private final strictfp class TutorialListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			setMenu(new TutorialForm(getNetwork(), getGUIRoot()));
		}
	}

	private final strictfp class CampaignListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			setMenu(new CampaignForm(getNetwork(), getGUIRoot(), MainMenu.this));
		}
	}

	private final strictfp class SinglePlayerListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			setMenu(new TerrainMenuForm(getNetwork(), getGUIRoot(), MainMenu.this));
		}
	}
}
