package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.UnitInfo;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.trigger.campaign.GameStartedTrigger;
import com.oddlabs.tt.trigger.campaign.VictoryTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.net.GameNetwork;

public final strictfp class NativeIsland5 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(NativeIsland5.class.getName());

	public NativeIsland5(Campaign campaign) {
		super(campaign);
	}

	public final void init(NetworkSelector network, GUIRoot gui_root) {
		String[] ai_names = new String[]{Utils.getBundleString(bundle, "name0"),
			Utils.getBundleString(bundle, "name1"),
			Utils.getBundleString(bundle, "name2"),
			Utils.getBundleString(bundle, "name3"),
			Utils.getBundleString(bundle, "name4"),
			Utils.getBundleString(bundle, "name5")};
		// gametype, owner, game, meters_per_world, hills, vegetation_amount, supplies_amount, seed, speed, map_code
		GameNetwork game_network = startNewGame(network, gui_root, 512, Landscape.VIKING, 1f, 1f, 1f, 4, 5, NativeCampaign.MAX_UNITS, ai_names);
		game_network.getClient().getServerInterface().setPlayerSlot(0,
				PlayerSlot.HUMAN,
				RacesResources.RACE_NATIVES,
				0,
				true,
				PlayerSlot.AI_NONE);
		game_network.getClient().setUnitInfo(0,
				new UnitInfo(false, false, 0, true,
					getCampaign().getState().getNumPeons(),
					getCampaign().getState().getNumRockWarriors(),
					getCampaign().getState().getNumIronWarriors(),
					getCampaign().getState().getNumRubberWarriors()));

		int ai_peons;
		switch (getCampaign().getState().getDifficulty()) {
			case CampaignState.DIFFICULTY_EASY:
				ai_peons = 5;
				break;
			case CampaignState.DIFFICULTY_NORMAL:
				ai_peons = 10;
				break;
			case CampaignState.DIFFICULTY_HARD:
				ai_peons = 25;
				break;
			default:
				throw new RuntimeException();
		}
		game_network.getClient().getServerInterface().setPlayerSlot(2,
				PlayerSlot.AI,
				RacesResources.RACE_VIKINGS,
				1,
				true,
				PlayerSlot.AI_HARD);
		game_network.getClient().setUnitInfo(2, new UnitInfo(true, true, 0, false, ai_peons, 0, 0, 0));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		Runnable runnable;
		// Introduction
		final Runnable dialog0 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header0"),
						Utils.getBundleString(bundle, "dialog0"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT);
				addModalForm(dialog);
			}
		};
		new GameStartedTrigger(getViewer().getWorld(), dialog0);

		// Winner prize
		final Runnable prize = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(5, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setIslandState(3, CampaignState.ISLAND_AVAILABLE);
				getCampaign().getState().setIslandState(6, CampaignState.ISLAND_AVAILABLE);
				getCampaign().victory(getViewer());
			}
		};

		// Winning condition
		runnable = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header1"),
						Utils.getBundleString(bundle, "dialog1"),
						getCampaign().getIcons().getFaces()[8],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						prize);
				addModalForm(dialog);
			}
		};
		new VictoryTrigger(getViewer(), runnable);
	}

	public final CharSequence getHeader() {
		return Utils.getBundleString(bundle, "header");
	}

	public final CharSequence getDescription() {
		return Utils.getBundleString(bundle, "description");
	}

	public final CharSequence getCurrentObjective() {
		return Utils.getBundleString(bundle, "objective");
	}
}
