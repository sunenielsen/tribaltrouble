package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.CounterLabel;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.landscape.LandscapeTarget;
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
import com.oddlabs.tt.trigger.campaign.PlayerEleminatedTrigger;
import com.oddlabs.tt.trigger.campaign.TimeTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.net.GameNetwork;

public final strictfp class NativeIsland4 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(NativeIsland4.class.getName());
	private final int minutes = 15;
	private final CounterLabel counter = new CounterLabel(minutes*60f, Skin.getSkin().getHeadlineFont(), true);

	private boolean alive;
	
	public NativeIsland4(Campaign campaign) {
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
		GameNetwork game_network = startNewGame(network, gui_root, 512, Landscape.VIKING, .8f, .8f, .8f, 19*19, 4, NativeCampaign.MAX_UNITS, ai_names);
		game_network.getClient().getServerInterface().setPlayerSlot(0,
				PlayerSlot.HUMAN,
				RacesResources.RACE_NATIVES,
				0,
				true,
				PlayerSlot.AI_NONE);
		game_network.getClient().setUnitInfo(0,
				new UnitInfo(false, false, 0, false,
					0,//getCampaign().getState().getNumPeons(),
					0,//getCampaign().getState().getNumRockWarriors(),
					0,//getCampaign().getState().getNumIronWarriors(),
					0));//getCampaign().getState().getNumRubberWarriors()));
		game_network.getClient().getServerInterface().setPlayerSlot(1,
				PlayerSlot.AI,
				RacesResources.RACE_NATIVES,
				0,
				true,
				PlayerSlot.AI_NEUTRAL_CAMPAIGN);
		game_network.getClient().setUnitInfo(1, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().setPlayerSlot(2,
				PlayerSlot.AI,
				RacesResources.RACE_VIKINGS,
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
		final Player captives = getViewer().getWorld().getPlayers()[1];
		final Player enemy = getViewer().getWorld().getPlayers()[2];

		// Introduction
		final Runnable dialog1 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header1"),
						Utils.getBundleString(bundle, "dialog1"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT);
				addModalForm(dialog);
			}
		};
		final Runnable dialog0 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header0"),
						Utils.getBundleString(bundle, "dialog0"),
						getCampaign().getIcons().getFaces()[2],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						dialog1);
				addModalForm(dialog);
			}
		};
		new GameStartedTrigger(getViewer().getWorld(), dialog0);

		// Move start position and insert men
		final int start_x = 45*2;
		final int start_y = 44*2;
		getViewer().getCamera().reset(start_x, start_y);
		ResourceBundle player_bundle = ResourceBundle.getBundle(Player.class.getName());
		local_player.setActiveChieftain(new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_CHIEFTAIN), Utils.getBundleString(player_bundle, "native_chieftain_name"), false));
		local_player.getChieftain().increaseMagicEnergy(0, 1000);
		local_player.getChieftain().increaseMagicEnergy(1, 1000);
		for (int i = 0; i < getCampaign().getState().getNumPeons(); i++)
			new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
		for (int i = 0; i < getCampaign().getState().getNumRockWarriors(); i++)
			new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		for (int i = 0; i < getCampaign().getState().getNumIronWarriors(); i++)
			new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		for (int i = 0; i < getCampaign().getState().getNumRubberWarriors(); i++)
			new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));


		// Insert captives
		final int captive_x = 39;
		final int captive_y = 38;
		for (int i = 0; i < 10; i++)
			new Unit(captives, captive_x*2, captive_y*2, null, captives.getRace().getUnitTemplate(Race.UNIT_PEON));

		// Winner prize
		runnable = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(4, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setNumPeons(getCampaign().getState().getNumPeons() + captives.getUnitCountContainer().getNumSupplies());
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
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 125, 161);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 125, 150);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 136, 153);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 180, 184);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 158, 154);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 163, 177);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 173, 175);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 165, 167);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 104, 192);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 108, 185);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 103, 210);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 115, 205);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 155, 185);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 145, 171);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 108, 150);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 130, 189);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER,  82, 170);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER,  65, 167);

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
				attack(enemy, new LandscapeTarget(captive_x, captive_y), attack1);
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

		// Defeat if netrauls eleminated
		runnable = new Runnable() {
			public final void run() {
				getCampaign().defeated(getViewer(), Utils.getBundleString(bundle, "game_over"));
			}
		};
		new PlayerEleminatedTrigger(runnable, captives);
	}

	private final boolean isAlive() {
		return alive;
	}

	public final void removeCounter() {
		alive = false;
		counter.remove();
		getViewer().getWorld().getAnimationManagerGameTime().removeAnimation(counter);
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
