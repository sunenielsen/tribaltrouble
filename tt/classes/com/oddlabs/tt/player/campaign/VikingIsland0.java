package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.IronSupply;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.RockSupply;
import com.oddlabs.tt.model.weapon.IronAxeWeapon;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.AI;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.player.UnitInfo;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.trigger.campaign.GameStartedTrigger;
import com.oddlabs.tt.trigger.campaign.PlayerEleminatedTrigger;
import com.oddlabs.tt.trigger.campaign.SupplyGatheredTrigger;
import com.oddlabs.tt.trigger.campaign.VictoryTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.net.GameNetwork;

public final strictfp class VikingIsland0 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(VikingIsland0.class.getName());
	
	public VikingIsland0(Campaign campaign) {
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
		GameNetwork game_network = startNewGame(network, gui_root, 256, Landscape.NATIVE, .5f, 1f, .1f, 45363, 0, VikingCampaign.MAX_UNITS, ai_names);
		game_network.getClient().getServerInterface().setPlayerSlot(0,
				PlayerSlot.HUMAN,
				RacesResources.RACE_VIKINGS,
				0,
				true,
				PlayerSlot.AI_NONE);
		game_network.getClient().setUnitInfo(0,
				new UnitInfo(false, false, 0, false,
					getCampaign().getState().getNumPeons(),
					getCampaign().getState().getNumRockWarriors(),
					getCampaign().getState().getNumIronWarriors(),
					getCampaign().getState().getNumRubberWarriors()));
		game_network.getClient().getServerInterface().setPlayerSlot(1,
				PlayerSlot.AI,
				RacesResources.RACE_VIKINGS,
				PlayerInfo.TEAM_NEUTRAL,
				true,
				PlayerSlot.AI_NEUTRAL_CAMPAIGN);
		game_network.getClient().setUnitInfo(1,
				new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().setPlayerSlot(2,
				PlayerSlot.AI,
				RacesResources.RACE_NATIVES,
				1,
				true,
				PlayerSlot.AI_PASSIVE_CAMPAIGN);
		game_network.getClient().setUnitInfo(2,
				new UnitInfo(true, true, 0, false, 0, 10, 5, 0));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		Runnable runnable;
		final Player local_player = getViewer().getLocalPlayer();
		final Player chieftain = getViewer().getWorld().getPlayers()[1];
		final Player enemy = getViewer().getWorld().getPlayers()[2];

		// Introduction
		runnable = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header0"),
						Utils.getBundleString(bundle, "dialog0"),
						getCampaign().getIcons().getFaces()[1],
						CampaignDialogForm.ALIGN_IMAGE_LEFT);
				addModalForm(dialog);
			}
		};
		new GameStartedTrigger(getViewer().getWorld(), runnable);

		// Disable Chieftain
		getViewer().getLocalPlayer().enableChieftains(false);

		// Winner prize
		final Runnable prize = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(0, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setIslandState(1, CampaignState.ISLAND_AVAILABLE);
				getCampaign().getState().setIslandState(3, CampaignState.ISLAND_AVAILABLE);
				getCampaign().victory(getViewer());
			}
		};
		// Winning condition
		runnable = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header1"),
						Utils.getBundleString(bundle, "dialog1"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						prize);
				addModalForm(dialog);
			}
		};
		new VictoryTrigger(getViewer(), runnable);

		// Place prisoners
		placePrisoners(chieftain, enemy, 0, 0, 0, 0, true);

		// Fill native armory with units and weapons
		final int num_units = 10;
		if (enemy.getArmory().getSupplyContainer(IronAxeWeapon.class).getNumSupplies() < num_units*3)
			enemy.getArmory().getSupplyContainer(IronAxeWeapon.class).increaseSupply(num_units*3);
		if (enemy.getArmory().getUnitContainer().getNumSupplies() < num_units*3)
			enemy.getArmory().getUnitContainer().increaseSupply(num_units*3);

		// Deploy and attack mid-game
		runnable = new Runnable() {
			public final void run() {
				Building armory = local_player.getArmory();
				if (armory != null && !armory.isDead()) {
					if (enemy.getArmory() != null && !enemy.getArmory().isDead()) {
						enemy.deployUnits(enemy.getArmory(), Building.KEY_DEPLOY_IRON_WARRIOR, num_units);
						AI.attackLandscape(enemy, armory, num_units);
					}
				}
			}
		};
		if (getCampaign().getState().getDifficulty() == CampaignState.DIFFICULTY_NORMAL) {
			new SupplyGatheredTrigger(getViewer().getLocalPlayer(), runnable, TreeSupply.class, 30);
			new SupplyGatheredTrigger(getViewer().getLocalPlayer(), runnable, RockSupply.class, 30);
			new SupplyGatheredTrigger(getViewer().getLocalPlayer(), runnable, IronSupply.class, 30);
		} else if (getCampaign().getState().getDifficulty() == CampaignState.DIFFICULTY_HARD) {
			new SupplyGatheredTrigger(getViewer().getLocalPlayer(), runnable, TreeSupply.class, 20);
			new SupplyGatheredTrigger(getViewer().getLocalPlayer(), runnable, RockSupply.class, 15);
			new SupplyGatheredTrigger(getViewer().getLocalPlayer(), runnable, IronSupply.class, 15);
		}

		// Defeat if netrauls eleminated
		runnable = new Runnable() {
			public final void run() {
				getCampaign().defeated(getViewer(), Utils.getBundleString(bundle, "game_over"));
			}
		};
		new PlayerEleminatedTrigger(runnable, chieftain);
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
