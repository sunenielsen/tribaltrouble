package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.weapon.IronAxeWeapon;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.player.UnitInfo;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.trigger.campaign.GameStartedTrigger;
import com.oddlabs.tt.trigger.campaign.PlayerEleminatedTrigger;
import com.oddlabs.tt.trigger.campaign.ReinforcementsTrigger;
import com.oddlabs.tt.trigger.campaign.VictoryTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.net.GameNetwork;

public final strictfp class VikingIsland9 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(VikingIsland9.class.getName());
	
	public VikingIsland9(Campaign campaign) {
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
		GameNetwork game_network = startNewGame(network, gui_root, 256, Landscape.NATIVE, 1f, .85f, .85f, 777777777, 9, VikingCampaign.MAX_UNITS, ai_names);
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
		game_network.getClient().getServerInterface().setPlayerSlot(2,
				PlayerSlot.AI,
				RacesResources.RACE_NATIVES,
				1,
				true,
				PlayerSlot.AI_PASSIVE_CAMPAIGN);
		game_network.getClient().setUnitInfo(2, new UnitInfo(true, true, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().setPlayerSlot(3,
				PlayerSlot.AI,
				RacesResources.RACE_NATIVES,
				PlayerInfo.TEAM_NEUTRAL,
				true,
				PlayerSlot.AI_NEUTRAL_CAMPAIGN);
		game_network.getClient().setUnitInfo(3, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		Runnable runnable;
		final Player enemy = getViewer().getWorld().getPlayers()[1];
		final Player chief_tribe = getViewer().getWorld().getPlayers()[2];

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

		// Insert native chieftain
		chief_tribe.setActiveChieftain(new Unit(chief_tribe, 56*2, 110*2, null, chief_tribe.getRace().getUnitTemplate(Race.UNIT_CHIEFTAIN)));

		// Defeat if netrauls eleminated
		runnable = new Runnable() {
			public final void run() {
				getCampaign().defeated(getViewer(), Utils.getBundleString(bundle, "game_over"));
			}
		};
		new PlayerEleminatedTrigger(runnable, chief_tribe);

		// Towers
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 50, 85);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 52, 81);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 54, 96);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 61, 104);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 57, 104);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 78, 90);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 72, 88);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 71, 83);

		// Fill native armory with units and weapons
		int num_extra_units = 130;
		if (enemy.getArmory().getSupplyContainer(IronAxeWeapon.class).getNumSupplies() < num_extra_units)
			enemy.getArmory().getSupplyContainer(IronAxeWeapon.class).increaseSupply(num_extra_units);
		if (enemy.getArmory().getUnitContainer().getNumSupplies() < num_extra_units)
			enemy.getArmory().getUnitContainer().increaseSupply(num_extra_units);

		// Deploy units now, and reinforcements when needed
		enemy.deployUnits(enemy.getArmory(), Building.KEY_DEPLOY_IRON_WARRIOR, 20);
		new ReinforcementsTrigger(enemy, Building.KEY_DEPLOY_IRON_WARRIOR);

		// Winner prize
		final Runnable prize = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(9, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setIslandState(10, CampaignState.ISLAND_AVAILABLE);
				getCampaign().victory(getViewer());
			}
		};
		runnable = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header1"),
						Utils.getBundleString(bundle, "dialog1"),
						getCampaign().getIcons().getFaces()[7],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						prize);
				addModalForm(dialog);
			}
		};

		// Winning condition
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
