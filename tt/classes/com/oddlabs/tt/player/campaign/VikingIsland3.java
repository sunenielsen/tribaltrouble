package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.SceneryModel;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.player.UnitInfo;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.landscape.HeightMap;
import com.oddlabs.tt.trigger.campaign.GameStartedTrigger;
import com.oddlabs.tt.trigger.campaign.VictoryTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.net.GameNetwork;

public final strictfp class VikingIsland3 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(VikingIsland3.class.getName());

	public VikingIsland3(Campaign campaign) {
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
		GameNetwork game_network = startNewGame(network, gui_root, 512, Landscape.NATIVE, .75f, 1f, .5f, 96443, 3, VikingCampaign.MAX_UNITS, ai_names);
		game_network.getClient().getServerInterface().setPlayerSlot(0,
				PlayerSlot.HUMAN,
				RacesResources.RACE_VIKINGS,
				0,
				true,
				PlayerSlot.AI_NONE);
		game_network.getClient().setUnitInfo(0,
				new UnitInfo(false, false, 0, true,
					getCampaign().getState().getNumPeons(),
					getCampaign().getState().getNumRockWarriors(),
					getCampaign().getState().getNumIronWarriors(),
					getCampaign().getState().getNumRubberWarriors()));
		int ai_difficulty;
		int ai_peons;
		switch (getCampaign().getState().getDifficulty()) {
			case CampaignState.DIFFICULTY_EASY:
				ai_difficulty = PlayerSlot.AI_NORMAL;
				ai_peons = 2;
				break;
			case CampaignState.DIFFICULTY_NORMAL:
				ai_difficulty = PlayerSlot.AI_HARD;
				ai_peons = 5;
				break;
			case CampaignState.DIFFICULTY_HARD:
				ai_difficulty = PlayerSlot.AI_HARD;
				ai_peons = 15;
				break;
			default:
				throw new RuntimeException();
		}
		game_network.getClient().getServerInterface().setPlayerSlot(2,
				PlayerSlot.AI,
				RacesResources.RACE_NATIVES,
				1,
				true,
				ai_difficulty);
		game_network.getClient().setUnitInfo(2, new UnitInfo(true, true, 1, false, ai_peons, 0, 5, 0));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		Runnable runnable;
		final Player enemy = getViewer().getWorld().getPlayers()[1];

		// Introduction
		runnable = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header0"),
						Utils.getBundleString(bundle, "dialog0"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT);
				addModalForm(dialog);
			}
		};
		new GameStartedTrigger(getViewer().getWorld(), runnable);

		// Winner prize
		runnable = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(3, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setIslandState(2, CampaignState.ISLAND_AVAILABLE);
				getCampaign().getState().setIslandState(4, CampaignState.ISLAND_AVAILABLE);
				getCampaign().getState().setIslandState(8, CampaignState.ISLAND_AVAILABLE);
				getCampaign().victory(getViewer());
			}
		};
		// Winning condition
		new VictoryTrigger(getViewer(), runnable);

		// Put warrior in tower
		enemy.getAI().manTowers(1); // TODO: exchange with insertGuardTower()

		// Insert treasures
		float shadow_diameter = 2.6f;

		float offset = HeightMap.METERS_PER_UNIT_GRID/2f;
		float dir = (float)StrictMath.sin(StrictMath.PI/4);
		new SceneryModel(getViewer().getWorld(), 134*2 + offset, 29*2 + offset, dir, -dir, getViewer().getWorld().getRacesResources().getTreasures()[4], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 130*2 + offset, 28*2 + offset, 0, -1, getViewer().getWorld().getRacesResources().getTreasures()[1], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 130*2 + offset, 34*2 + offset, 0, 1, getViewer().getWorld().getRacesResources().getTreasures()[3], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 125*2 + offset, 37*2 + offset, 0, 1, getViewer().getWorld().getRacesResources().getTreasures()[1], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 121*2 + offset, 32*2 + offset, -1, 0, getViewer().getWorld().getRacesResources().getTreasures()[5], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 124*2 + offset, 28*2 + offset, -dir, -dir, getViewer().getWorld().getRacesResources().getTreasures()[3], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 136*2 + offset, 38*2 + offset, dir, dir, getViewer().getWorld().getRacesResources().getTreasures()[4], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 139*2 + offset, 33*2 + offset, 1, 0, getViewer().getWorld().getRacesResources().getTreasures()[1], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
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
