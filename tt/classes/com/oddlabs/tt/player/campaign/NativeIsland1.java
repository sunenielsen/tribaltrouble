package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.delegate.JumpDelegate;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.SceneryModel;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.GameNetwork;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.player.UnitInfo;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.landscape.HeightMap;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.trigger.campaign.DeathTrigger;
import com.oddlabs.tt.trigger.campaign.GameStartedTrigger;
import com.oddlabs.tt.trigger.campaign.TimeTrigger;
import com.oddlabs.tt.trigger.campaign.VictoryTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;

public final strictfp class NativeIsland1 extends Island {
	private final static int NUM_CAPTIVES = 10;
	private final ResourceBundle bundle = ResourceBundle.getBundle(NativeIsland1.class.getName());

	private int objective = 0;
	
	public NativeIsland1(Campaign campaign) {
		super(campaign);
	}

	public final void init(NetworkSelector network, GUIRoot gui_root) {
		String[] ai_names = new String[]{Utils.getBundleString(bundle, "name0"),
			Utils.getBundleString(bundle, "name1"),
			Utils.getBundleString(bundle, "name2"),
			Utils.getBundleString(bundle, "name3"),
			Utils.getBundleString(bundle, "name4"),
			Utils.getBundleString(bundle, "name5")};
		GameNetwork game_network = startNewGame(network, gui_root, 256, Landscape.VIKING, .75f, 1f, .5f, 1, 1, NativeCampaign.MAX_UNITS, ai_names);
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
		game_network.getClient().getServerInterface().setPlayerSlot(2,
				PlayerSlot.AI,
				RacesResources.RACE_VIKINGS,
				1,
				true,
				PlayerSlot.AI_PASSIVE_CAMPAIGN);
		game_network.getClient().setUnitInfo(2, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().setPlayerSlot(4,
				PlayerSlot.AI,
				RacesResources.RACE_VIKINGS,
				1,
				true,
				PlayerSlot.AI_NEUTRAL_CAMPAIGN);
		game_network.getClient().setUnitInfo(4, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		final Player local_player = getViewer().getLocalPlayer();
		final Player enemy = getViewer().getWorld().getPlayers()[1];
		final Player guards = getViewer().getWorld().getPlayers()[2];

		// Introduction
		final int start_x = 24*2;
		final int start_y = 86*2;
		final Runnable camera_jump0 = new Runnable() {
			public final void run() {
				getViewer().getGUIRoot().pushDelegate(new JumpDelegate(getViewer(), getViewer().getCamera(), start_x, start_y, 200f, 3f));
			}
		};
		final Runnable dialog1 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header1"),
						Utils.getBundleString(bundle, "dialog1"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						camera_jump0);
				addModalForm(dialog);
			}
		};
		final Runnable dialog0 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header0"),
						Utils.getBundleString(bundle, "dialog0"),
						getCampaign().getIcons().getFaces()[4],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						dialog1);
				addModalForm(dialog);
			}
		};
		new GameStartedTrigger(getViewer().getWorld(), dialog0);

		// insert local_player
		ResourceBundle player_bundle = ResourceBundle.getBundle(Player.class.getName());
		local_player.setActiveChieftain(new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_CHIEFTAIN), Utils.getBundleString(player_bundle, "native_chieftain_name"), false));
		local_player.getChieftain().increaseMagicEnergy(0, 1000);
		local_player.getChieftain().increaseMagicEnergy(1, 1000);
		for (int i = 0; i < getCampaign().getState().getNumPeons(); i++)
			new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));

		// insert slaves
		final int captive_start_x = 48*2;
		final int captive_start_y = 96*2;
		float shadow_diameter = local_player.getRace().getUnitTemplate(Race.UNIT_PEON).getShadowDiameter();
		SpriteKey sprite_renderer = local_player.getRace().getUnitTemplate(Race.UNIT_PEON).getSpriteRenderer();

		final float offset = HeightMap.METERS_PER_UNIT_GRID/2f;
		float dir = (float)StrictMath.sin(StrictMath.PI/4);

		final SceneryModel[] scenery_models = new SceneryModel[10];
		scenery_models[0] = new SceneryModel(getViewer().getWorld(), 48*2 + offset, 96*2 + offset, 1, 0, sprite_renderer, shadow_diameter, true, Utils.getBundleString(bundle, "captive"), Unit.ANIMATION_THROWING, 1, .1f);
		scenery_models[1] = new SceneryModel(getViewer().getWorld(), 48*2 + offset, 95*2 + offset, dir, dir, sprite_renderer, shadow_diameter, true, Utils.getBundleString(bundle, "captive"), Unit.ANIMATION_THROWING, 1, .15f);
		scenery_models[2] = new SceneryModel(getViewer().getWorld(), 48*2 + offset, 98*2 + offset, dir, -dir, sprite_renderer, shadow_diameter, true, Utils.getBundleString(bundle, "captive"), Unit.ANIMATION_THROWING, 1, .75f);
		scenery_models[3] = new SceneryModel(getViewer().getWorld(), 49*2 + offset, 98*2 + offset, 0, -1, sprite_renderer, shadow_diameter, true, Utils.getBundleString(bundle, "captive"), Unit.ANIMATION_THROWING, 1, .54f);
		scenery_models[4] = new SceneryModel(getViewer().getWorld(), 50*2 + offset, 97*2 + offset, -1, 0, sprite_renderer, shadow_diameter, true, Utils.getBundleString(bundle, "captive"), Unit.ANIMATION_THROWING, 1, .47f);
		scenery_models[5] = new SceneryModel(getViewer().getWorld(), 51*2 + offset, 96*2 + offset, 0, -1, sprite_renderer, shadow_diameter, true, Utils.getBundleString(bundle, "captive"), Unit.ANIMATION_THROWING, 1, .9f);
		scenery_models[6] = new SceneryModel(getViewer().getWorld(), 51*2 + offset, 94*2 + offset, 0, 1, sprite_renderer, shadow_diameter, true, Utils.getBundleString(bundle, "captive"), Unit.ANIMATION_THROWING, 1, .23f);
		scenery_models[7] = new SceneryModel(getViewer().getWorld(), 52*2 + offset, 96*2 + offset, -dir, -dir, sprite_renderer, shadow_diameter, true, Utils.getBundleString(bundle, "captive"), Unit.ANIMATION_THROWING, 1, .7f);
		scenery_models[8] = new SceneryModel(getViewer().getWorld(), 52*2 + offset, 94*2 + offset, -dir, dir, sprite_renderer, shadow_diameter, true, Utils.getBundleString(bundle, "captive"), Unit.ANIMATION_THROWING, 1, 0f);
		scenery_models[9] = new SceneryModel(getViewer().getWorld(), 50*2 + offset, 95*2 + offset, 1, 0, sprite_renderer, shadow_diameter, true, Utils.getBundleString(bundle, "captive"), Unit.ANIMATION_THROWING, 1, .34f);

		// Insert guards
		new Unit(guards, 45*2, 98*2, null, guards.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(guards, 47*2, 92*2, null, guards.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		Unit trigger = new Unit(guards, 54*2, 97*2, null, guards.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));

		// Move start position (for the camera)
		getViewer().getCamera().reset(start_x, start_y);
		getViewer().getCamera().setPos(captive_start_x - 6, captive_start_y);

		// free slaves
		final Runnable free_captives = new Runnable() {
			public final void run() {
				changeObjective(1);
				for (int i = 0; i < scenery_models.length; i++) {
					scenery_models[i].remove();
				}
				if (!local_player.getUnitCountContainer().isSupplyFull())
					new Unit(local_player, 48*2 + offset, 96*2 + offset, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
				if (!local_player.getUnitCountContainer().isSupplyFull())
					new Unit(local_player, 48*2 + offset, 95*2 + offset, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
				if (!local_player.getUnitCountContainer().isSupplyFull())
					new Unit(local_player, 48*2 + offset, 98*2 + offset, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
				if (!local_player.getUnitCountContainer().isSupplyFull())
					new Unit(local_player, 49*2 + offset, 98*2 + offset, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
				if (!local_player.getUnitCountContainer().isSupplyFull())
					new Unit(local_player, 50*2 + offset, 97*2 + offset, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
				if (!local_player.getUnitCountContainer().isSupplyFull())
					new Unit(local_player, 51*2 + offset, 96*2 + offset, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
				if (!local_player.getUnitCountContainer().isSupplyFull())
					new Unit(local_player, 51*2 + offset, 94*2 + offset, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
				if (!local_player.getUnitCountContainer().isSupplyFull())
					new Unit(local_player, 52*2 + offset, 96*2 + offset, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
				if (!local_player.getUnitCountContainer().isSupplyFull())
					new Unit(local_player, 52*2 + offset, 94*2 + offset, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
				if (!local_player.getUnitCountContainer().isSupplyFull())
					new Unit(local_player, 50*2 + offset, 95*2 + offset, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
			}
		};
		final Runnable dialog5 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header5"),
						Utils.getBundleString(bundle, "dialog5"),
						getCampaign().getIcons().getFaces()[3],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						free_captives);
				addModalForm(dialog);
			}
		};
		final Runnable dialog4 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header4"),
						Utils.getBundleString(bundle, "dialog4"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						dialog5);
				addModalForm(dialog);
			}
		};
		final Runnable dialog3 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header3"),
						Utils.getBundleString(bundle, "dialog3"),
						getCampaign().getIcons().getFaces()[3],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						dialog4);
				addModalForm(dialog);
			}
		};
		final Runnable dialog2 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header2"),
						Utils.getBundleString(bundle, "dialog2"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						dialog3);
				addModalForm(dialog);
			}
		};
		new DeathTrigger(trigger, dialog2);

		// Winner prize
		final Runnable prize = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(1, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setIslandState(2, CampaignState.ISLAND_AVAILABLE);
				getCampaign().getState().setIslandState(5, CampaignState.ISLAND_AVAILABLE);
				getCampaign().getState().setNumPeons(getCampaign().getState().getNumPeons() + NUM_CAPTIVES);
				getCampaign().victory(getViewer());
			}
		};
		// Winning condition
		new VictoryTrigger(getViewer(), prize);

		final int attack1 = 5;
		final int attack2 = 10;
		final int defense = 20;

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

		// Attack2...
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

		// Insert enemy
		enemy.setStartX(106*2);
		enemy.setStartY(54*2);
		enemy.buildBuilding(Race.BUILDING_QUARTERS, 106, 54);
		enemy.buildBuilding(Race.BUILDING_ARMORY, 97, 50);

		new Unit(enemy, enemy.getStartX(), enemy.getStartY(), null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, enemy.getStartX(), enemy.getStartY(), null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, enemy.getStartX(), enemy.getStartY(), null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, enemy.getStartX(), enemy.getStartY(), null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, enemy.getStartX(), enemy.getStartY(), null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, enemy.getStartX(), enemy.getStartY(), null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, enemy.getStartX(), enemy.getStartY(), null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, enemy.getStartX(), enemy.getStartY(), null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, enemy.getStartX(), enemy.getStartY(), null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, enemy.getStartX(), enemy.getStartY(), null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));

		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 97, 39);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 90, 52);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 96, 63);
	}

	public final CharSequence getHeader() {
		return Utils.getBundleString(bundle, "header");
	}

	public final CharSequence getDescription() {
		return Utils.getBundleString(bundle, "description");
	}

	public final CharSequence getCurrentObjective() {
		return Utils.getBundleString(bundle, "objective" + objective);
	}

	private final void changeObjective(int objective) {
		this.objective = objective;
	}
}
