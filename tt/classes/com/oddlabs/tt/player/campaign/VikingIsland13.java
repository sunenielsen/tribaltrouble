package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.CounterLabel;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.player.UnitInfo;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.trigger.campaign.GameStartedTrigger;
import com.oddlabs.tt.trigger.campaign.TimeTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.net.GameNetwork;

public final strictfp class VikingIsland13 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(VikingIsland13.class.getName());
	private final int minutes = 15;
	private final CounterLabel counter = new CounterLabel(minutes*60f, Skin.getSkin().getHeadlineFont(), true);

	private boolean alive;
	
	public VikingIsland13(Campaign campaign) {
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
		GameNetwork game_network = startNewGame(network, gui_root, 512, Landscape.NATIVE, 1f, 1f, .8f, 16, 13, VikingCampaign.MAX_UNITS, ai_names);
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
		game_network.getClient().setUnitInfo(2, new UnitInfo(true, true, 0, false, 0, 10, 30, 0));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		alive = true;
		counter.start(getViewer().getWorld().getAnimationManagerGameTime());
		counter.setPos(0, 0);
		getViewer().getGUIRoot().addChild(counter);

		Runnable runnable;
		final Player local_player = getViewer().getLocalPlayer();
		final Player enemy = getViewer().getWorld().getPlayers()[1];

		// Introduction
		runnable = new Runnable() {
			public final void run() {
				String stay_alive_dialog = Utils.getBundleString(bundle, "dialog0", new Object[]{new Integer(minutes)});
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header0"),
						stay_alive_dialog,
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT);
				addModalForm(dialog);
			}
		};

		new GameStartedTrigger(getViewer().getWorld(), runnable);
		// Winner prize
		runnable = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(13, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setIslandState(12, CampaignState.ISLAND_AVAILABLE);
				getCampaign().getState().setIslandState(14, CampaignState.ISLAND_AVAILABLE);
				if (isAlive()) {
					removeCounter();
				}
				getCampaign().victory(getViewer());
			}
		};
		// Winning condition
		new TimeTrigger(getViewer().getWorld(), minutes*60f, runnable);
/*
// done by DefeatTrigger in super
		// Remove counter if defeated
		runnable = new Runnable() {
			public final void run() {
				counter.remove();
			}
		};
		new DefeatTrigger(getCampaign(), local_player.getChieftain(), runnable);
*/
		// Insert native towers
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 167, 60);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 171, 55);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 160, 60);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 142, 70);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 135, 72);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 130, 74);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 125, 76);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 120, 71);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 115, 67);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 95, 68);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 93, 63);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 92, 57);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 90, 52);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 96, 38);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 99, 34);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 105, 24);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 164, 51);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 103, 57);

		final int attack1;
		final int attack2;
		final int attack3;
		final int attack4;
		final int attack5;
		final int attack6;
		switch (getCampaign().getState().getDifficulty()) {
			case CampaignState.DIFFICULTY_EASY:
				attack1 = 5;
				attack2 = 15;
				attack3 = 20;
				attack4 = 35;
				attack5 = 35;
				attack6 = 35;
				break;
			case CampaignState.DIFFICULTY_NORMAL:
				attack1 = 10;
				attack2 = 30;
				attack3 = 40;
				attack4 = 70;
				attack5 = 70;
				attack6 = 70;
				break;
			case CampaignState.DIFFICULTY_HARD:
				attack1 = 20;
				attack2 = 60;
				attack3 = 80;
				attack4 = 90;
				attack5 = 90;
				attack6 = 90;
				break;
			default:
				throw new RuntimeException();
		}

		// Fill native armory with units and weapons
		refillArmory(enemy);

		// Attack1
		runnable = new Runnable() {
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
		new TimeTrigger(getViewer().getWorld(), 3.5f*60f, runnable);

		// Attack2
		runnable = new Runnable() {
			public final void run() {
				Building armory = local_player.getArmory();
				Unit chieftain = local_player.getChieftain();
				if (armory != null && !armory.isDead()) {
					attack(enemy, armory, attack2);
				} else if (chieftain != null && !chieftain.isDead()) {
					attack(enemy, chieftain, attack2);
				}
				refillArmory(enemy);
				deploy(enemy, attack3);
			}
		};
		new TimeTrigger(getViewer().getWorld(), 4.5f*60f, runnable);

		// Attack3
		runnable = new Runnable() {
			public final void run() {
				Building armory = local_player.getArmory();
				Unit chieftain = local_player.getChieftain();
				if (armory != null && !armory.isDead()) {
					attack(enemy, armory, attack3);
				} else if (chieftain != null && !chieftain.isDead()) {
					attack(enemy, chieftain, attack3);
				}
				refillArmory(enemy);
				deploy(enemy, attack4);
			}
		};
		new TimeTrigger(getViewer().getWorld(), 6*60f, runnable);

		// Attack4
		runnable = new Runnable() {
			public final void run() {
				Building armory = local_player.getArmory();
				Unit chieftain = local_player.getChieftain();
				if (armory != null && !armory.isDead()) {
					attack(enemy, armory, attack4);
				} else if (chieftain != null && !chieftain.isDead()) {
					attack(enemy, chieftain, attack4);
				}
				refillArmory(enemy);
				deploy(enemy, attack5);
			}
		};
		new TimeTrigger(getViewer().getWorld(), 9*60f, runnable);

		// Attack5
		runnable = new Runnable() {
			public final void run() {
				Building armory = local_player.getArmory();
				Unit chieftain = local_player.getChieftain();
				if (armory != null && !armory.isDead()) {
					attack(enemy, armory, attack5);
				} else if (chieftain != null && !chieftain.isDead()) {
					attack(enemy, chieftain, attack5);
				}
				refillArmory(enemy);
				deploy(enemy, attack6);
			}
		};
		new TimeTrigger(getViewer().getWorld(), 11*60f, runnable);

		// Attack6
		runnable = new Runnable() {
			public final void run() {
				Building armory = local_player.getArmory();
				Unit chieftain = local_player.getChieftain();
				if (armory != null && !armory.isDead()) {
					attack(enemy, armory, attack6);
				} else if (chieftain != null && !chieftain.isDead()) {
					attack(enemy, chieftain, attack6);
				}
				refillArmory(enemy);
			}
		};
		new TimeTrigger(getViewer().getWorld(), 12.5f*60f, runnable);
	}

	private final boolean isAlive() {
		return alive;
	}

	public final void removeCounter() {
		alive = false;
		counter.stop();
		counter.remove();
	}

	public final CharSequence getHeader() {
		return Utils.getBundleString(bundle, "header");
	}

	public final CharSequence getDescription() {
		return Utils.getBundleString(bundle, "description");
	}

	public final CharSequence getCurrentObjective() {
		return Utils.getBundleString(bundle, "objective", new Object[]{new Integer(minutes)});
	}
}
