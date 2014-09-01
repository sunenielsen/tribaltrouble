package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.SceneryModel;
import com.oddlabs.tt.model.Unit;
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

public final strictfp class VikingIsland14 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(VikingIsland14.class.getName());
	
	public VikingIsland14(Campaign campaign) {
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
		GameNetwork game_network = startNewGame(network, gui_root, 1024, Landscape.NATIVE, .75f, .65f, .85f, 25, 14, VikingCampaign.MAX_UNITS, ai_names);
		game_network.getClient().getServerInterface().setPlayerSlot(0,
				PlayerSlot.HUMAN,
				RacesResources.RACE_VIKINGS,
				0,
				true,
				PlayerSlot.AI_NONE);
		game_network.getClient().setUnitInfo(0, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		int ai_difficulty;
		int ai_peons;
		switch (getCampaign().getState().getDifficulty()) {
			case CampaignState.DIFFICULTY_EASY:
				ai_difficulty = PlayerSlot.AI_NORMAL;
				ai_peons = 1;
				break;
			case CampaignState.DIFFICULTY_NORMAL:
				ai_difficulty = PlayerSlot.AI_HARD;
				ai_peons = 5;
				break;
			case CampaignState.DIFFICULTY_HARD:
				ai_difficulty = PlayerSlot.AI_HARD;
				ai_peons = 12;
				break;
			default:
				throw new RuntimeException();
		}
		game_network.getClient().getServerInterface().setPlayerSlot(2,
				PlayerSlot.AI,
				RacesResources.RACE_NATIVES,
				1,
				true,
				PlayerSlot.AI_HARD);
		game_network.getClient().setUnitInfo(2, new UnitInfo(true, true, 0, false, ai_peons, 0, 0, 0));
		game_network.getClient().getServerInterface().setPlayerSlot(3,
				PlayerSlot.AI,
				RacesResources.RACE_NATIVES,
				1,
				true,
				ai_difficulty);
		game_network.getClient().setUnitInfo(3, new UnitInfo(true, true, 0, false, ai_peons, 0, 0, 0));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		Runnable runnable;
		final Player local_player = getViewer().getLocalPlayer();
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

		// Insert viking men
		int start_x = 236*2;
		int start_y = 362*2;
		ResourceBundle player_bundle = ResourceBundle.getBundle(Player.class.getName());
		local_player.setActiveChieftain(new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_CHIEFTAIN), Utils.getBundleString(player_bundle, "chieftain_name"), false));
		local_player.getChieftain().increaseMagicEnergy(0, 1000);
		local_player.getChieftain().increaseMagicEnergy(1, 1000);

		for (int i = 0; i < getCampaign().getState().getNumPeons(); i++) {
			new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
		}
		for (int i = 0; i < getCampaign().getState().getNumRockWarriors(); i++) {
			new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		}
		for (int i = 0; i < getCampaign().getState().getNumIronWarriors(); i++) {
			new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		}
		for (int i = 0; i < getCampaign().getState().getNumRubberWarriors(); i++) {
			new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		}

		// Move start position (for the camera)
		getViewer().getCamera().reset(start_x, start_y);

		// Winner prize
		runnable = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(14, CampaignState.ISLAND_COMPLETED);
				getCampaign().victory(getViewer());
			}
		};

		// Winning condition
		new VictoryTrigger(getViewer(), runnable);

		// Insert treasures
		float dir = (float)StrictMath.sin(StrictMath.PI/4);
		float offset = HeightMap.METERS_PER_UNIT_GRID/2f;
		float shadow_diameter = 4.5f;
		new SceneryModel(getViewer().getWorld(), 163*2 + offset, 126*2 + offset, 0, 1, getViewer().getWorld().getRacesResources().getTreasures()[0], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));

		shadow_diameter = 2.6f;
		new SceneryModel(getViewer().getWorld(), 130*2 + offset, 124*2 + offset, -dir, -dir, getViewer().getWorld().getRacesResources().getTreasures()[3], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 152*2 + offset, 138*2 + offset, dir, dir, getViewer().getWorld().getRacesResources().getTreasures()[1], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 152*2 + offset, 144*2 + offset, 0, 1, getViewer().getWorld().getRacesResources().getTreasures()[3], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 140*2 + offset, 140*2 + offset, 0, 1, getViewer().getWorld().getRacesResources().getTreasures()[4], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 143*2 + offset, 116*2 + offset, 0, -1, getViewer().getWorld().getRacesResources().getTreasures()[1], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 142*2 + offset, 131*2 + offset, dir, -dir, getViewer().getWorld().getRacesResources().getTreasures()[5], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));

		new SceneryModel(getViewer().getWorld(), 423*2 + offset, 174*2 + offset, 0, 1, getViewer().getWorld().getRacesResources().getTreasures()[1], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 408*2 + offset, 161*2 + offset, -1, 0, getViewer().getWorld().getRacesResources().getTreasures()[3], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 426*2 + offset, 156*2 + offset, dir, -dir, getViewer().getWorld().getRacesResources().getTreasures()[5], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 418*2 + offset, 165*2 + offset, 0, 1, getViewer().getWorld().getRacesResources().getTreasures()[1], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 430*2 + offset, 165*2 + offset, 1, 0, getViewer().getWorld().getRacesResources().getTreasures()[3], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 419*2 + offset, 170*2 + offset, -dir, dir, getViewer().getWorld().getRacesResources().getTreasures()[4], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		new SceneryModel(getViewer().getWorld(), 416*2 + offset, 156*2 + offset, 0, -1, getViewer().getWorld().getRacesResources().getTreasures()[5], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
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
