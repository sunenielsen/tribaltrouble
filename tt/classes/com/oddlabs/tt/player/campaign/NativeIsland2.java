package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.GUIRoot;
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
import com.oddlabs.tt.trigger.campaign.VictoryTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.net.GameNetwork;

public final strictfp class NativeIsland2 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(NativeIsland2.class.getName());
	
	public NativeIsland2(Campaign campaign) {
		super(campaign);
	}

	public final void init(NetworkSelector network, GUIRoot gui_root) {
		String[] ai_names = new String[]{Utils.getBundleString(bundle, "name0"),
			Utils.getBundleString(bundle, "name1"),
			Utils.getBundleString(bundle, "name2"),
			Utils.getBundleString(bundle, "name3"),
			Utils.getBundleString(bundle, "name4"),
			Utils.getBundleString(bundle, "name5")};
		GameNetwork game_network = startNewGame(network, gui_root, 256, Landscape.VIKING, .75f, 1f, 1f, 10, 2, NativeCampaign.MAX_UNITS, ai_names);
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
				PlayerInfo.TEAM_NEUTRAL,
				true,
				PlayerSlot.AI_NEUTRAL_CAMPAIGN);
		game_network.getClient().setUnitInfo(1, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().setPlayerSlot(2,
				PlayerSlot.AI,
				RacesResources.RACE_VIKINGS,
				1,
				true,
				PlayerSlot.AI_PASSIVE_CAMPAIGN);
		game_network.getClient().setUnitInfo(2, new UnitInfo(true, true, 0, false, 0, 10, 5, 0));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		Runnable runnable;
		final Player local_player = getViewer().getLocalPlayer();
		final Player captives = getViewer().getWorld().getPlayers()[1];
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

		// Winner prize
		final Runnable prize = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(2, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setIslandState(3, CampaignState.ISLAND_AVAILABLE);
				getCampaign().getState().setNumPeons(getCampaign().getState().getNumPeons() + captives.getUnitCountContainer().getNumSupplies());
				getCampaign().victory(getViewer());
			}
		};
		runnable = new Runnable() {
			public final void run() {
				String message = Utils.getBundleString(bundle, "dialog1", new Object[]{new Integer(captives.getUnitCountContainer().getNumSupplies())});
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header1"),
						message,
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						prize);
				addModalForm(dialog);
			}
		};

		// Winning condition
		new VictoryTrigger(getViewer(), runnable);

		// Place natives
		int start_x = 100*2;
		int start_y = 73*2;
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

		// Move start position (for the camera)
		getViewer().getCamera().reset(start_x, start_y);

		// Place prisoners
		placePrisoners(captives, enemy, 10, 0, 0, 0, false);

		final int attack1 = 5;
		final int attack2 = 10;
		final int defense = 10;

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
				new TimeTrigger(getViewer().getWorld(), 16f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 21f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 26f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 31f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 36f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 41f*60f, attack2_runnable);
				break;
			case CampaignState.DIFFICULTY_NORMAL:
				new TimeTrigger(getViewer().getWorld(), 5f*60f, attack1_runnable);
				new TimeTrigger(getViewer().getWorld(), 8.5f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 13f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 17f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 21f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 25f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 29f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 33f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 37f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 41f*60f, attack2_runnable);
				break;
			case CampaignState.DIFFICULTY_HARD:
				new TimeTrigger(getViewer().getWorld(), 4f*60f, attack1_runnable);
				new TimeTrigger(getViewer().getWorld(), 7f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 11f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 15f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 19f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 23f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 27f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 31f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 35f*60f, attack2_runnable);
				new TimeTrigger(getViewer().getWorld(), 39f*60f, attack2_runnable);
				break;
			default:
				throw new RuntimeException();
		}

		// Defeat if netrauls eleminated
		runnable = new Runnable() {
			public final void run() {
				getCampaign().defeated(getViewer(), Utils.getBundleString(bundle, "game_over"));
			}
		};
		new PlayerEleminatedTrigger(runnable, captives);

		// Insert towers
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 42, 83);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 63, 89);
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
