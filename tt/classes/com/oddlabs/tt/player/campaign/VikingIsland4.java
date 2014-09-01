package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.model.Building;
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
import com.oddlabs.tt.trigger.campaign.TimeTrigger;
import com.oddlabs.tt.trigger.campaign.VictoryTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.net.GameNetwork;

public final strictfp class VikingIsland4 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(VikingIsland4.class.getName());

	public VikingIsland4(Campaign campaign) {
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
		GameNetwork game_network = startNewGame(network, gui_root, 256, Landscape.NATIVE, .65f, 1f, .5f, 786433, 4, VikingCampaign.MAX_UNITS, ai_names);
		game_network.getClient().getServerInterface().setPlayerSlot(0,
				PlayerSlot.HUMAN,
				RacesResources.RACE_VIKINGS,
				0,
				true,
				PlayerSlot.AI_NONE);
		game_network.getClient().setUnitInfo(0, new UnitInfo(false, false, 0, true,
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
		game_network.getClient().setUnitInfo(1, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().setPlayerSlot(2,
				PlayerSlot.AI,
				RacesResources.RACE_NATIVES,
				1,
				true,
				PlayerSlot.AI_PASSIVE_CAMPAIGN);
		game_network.getClient().setUnitInfo(2, new UnitInfo(true, true, 2, true, 10, 10, 10, 10));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		Runnable runnable;
		final Player local_player = getViewer().getLocalPlayer();
		final Player captive = getViewer().getWorld().getPlayers()[1];
		final Player enemy = getViewer().getWorld().getPlayers()[2];

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

		final int attack1;
		final int attack2;
		final int defense;
		switch (getCampaign().getState().getDifficulty()) {
			case CampaignState.DIFFICULTY_EASY:
				attack1 = 4;
				attack2 = 8;
				defense = 10;
				break;
			case CampaignState.DIFFICULTY_NORMAL:
				attack1 = 7;
				attack2 = 12;
				defense = 12;
				break;
			case CampaignState.DIFFICULTY_HARD:
				attack1 = 11;
				attack2 = 16;
				defense = 20;
				break;
			default:
				throw new RuntimeException();
		}

		// Attack1
		Runnable attack1_runnable = new Runnable() {
			public final void run() {
				Building armory = local_player.getArmory();
				Unit chieftain = local_player.getChieftain();
				if (armory != null && !armory.isDead()) {
					attack(enemy, armory, attack1);
				} else if (chieftain != null && !chieftain.isDead()) {
					attack(enemy, chieftain, attack1);
				}
				refillArmory(enemy);
				deploy(enemy, attack2);
			}
		};

		// Attack2
		Runnable attack2_runnable = new Runnable() {
			public final void run() {
				Building armory = local_player.getArmory();
				Unit chieftain = local_player.getChieftain();
				if (armory != null && !armory.isDead()) {
					attack(enemy, armory, attack2);
				} else if (chieftain != null && !chieftain.isDead()) {
					attack(enemy, chieftain, attack1);
				}
				refillArmory(enemy);
				deploy(enemy, defense);
			}
		};
		switch (getCampaign().getState().getDifficulty()) {
			case CampaignState.DIFFICULTY_EASY:
				new TimeTrigger(getViewer().getWorld(), 7f*60f, attack1_runnable);
				new TimeTrigger(getViewer().getWorld(), 11f*60f, attack2_runnable);
				break;
			case CampaignState.DIFFICULTY_NORMAL:
				new TimeTrigger(getViewer().getWorld(), 4.5f*60f, attack1_runnable);
				new TimeTrigger(getViewer().getWorld(), 8f*60f, attack2_runnable);
				break;
			case CampaignState.DIFFICULTY_HARD:
				new TimeTrigger(getViewer().getWorld(), 4f*60f, attack1_runnable);
				new TimeTrigger(getViewer().getWorld(), 6.5f*60f, attack2_runnable);
				break;
			default:
				throw new RuntimeException();
		}

		// Winner prize
		final Runnable prize = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(4, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setIslandState(5, CampaignState.ISLAND_AVAILABLE);
				getCampaign().getState().setHasMagic0(true);
				getCampaign().victory(getViewer());
			}
		};

		// Winning condition
		runnable = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header1"),
						Utils.getBundleString(bundle, "dialog1"),
						getCampaign().getIcons().getFaces()[5],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						prize);
				addModalForm(dialog);
			}
		};
		new VictoryTrigger(getViewer(), runnable);

		// Place prisoner
		placePrisoners(captive, enemy, 0, 0, 0, 0, true);

		// Put warrior in tower
		enemy.getAI().manTowers(1); // TODO: replace with insertGuardTower()

		// Fill native armory with units and weapons
		int num_reinforcements = 100;
		if (enemy.getArmory().getSupplyContainer(IronAxeWeapon.class).getNumSupplies() < num_reinforcements)
			enemy.getArmory().getSupplyContainer(IronAxeWeapon.class).increaseSupply(num_reinforcements);
		if (enemy.getArmory().getUnitContainer().getNumSupplies() < num_reinforcements)
			enemy.getArmory().getUnitContainer().increaseSupply(num_reinforcements);

		// Deploy reinforcements when needed
		new ReinforcementsTrigger(enemy, Building.KEY_DEPLOY_IRON_WARRIOR);

		// Defeat if netrauls eleminated
		runnable = new Runnable() {
			public final void run() {
				getCampaign().defeated(getViewer(), Utils.getBundleString(bundle, "game_over"));
			}
		};
		new PlayerEleminatedTrigger(runnable, captive);
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
