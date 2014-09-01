package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.camera.Camera;
import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.camera.JumpCamera;
import com.oddlabs.tt.camera.MapCamera;
import com.oddlabs.tt.camera.FirstPersonCamera;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.delegate.JumpDelegate;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.SceneryModel;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.net.GameNetwork;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.player.UnitInfo;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.landscape.HeightMap;
import com.oddlabs.tt.trigger.campaign.GameStartedTrigger;
import com.oddlabs.tt.trigger.campaign.NearArmyTrigger;
import com.oddlabs.tt.trigger.campaign.VictoryTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;

public final strictfp class NativeIsland0 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(NativeIsland0.class.getName());

	private int objective = 0;
	
	public NativeIsland0(Campaign campaign) {
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
		GameNetwork game_network = startNewGame(network, gui_root, 1024, Landscape.NATIVE, .75f, .65f, .85f, 25, 0, NativeCampaign.MAX_UNITS, ai_names);
		game_network.getClient().getServerInterface().setPlayerSlot(0,
				PlayerSlot.HUMAN,
				RacesResources.RACE_NATIVES,
				0,
				true,
				PlayerSlot.AI_NONE);
		game_network.getClient().setUnitInfo(0, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().setPlayerSlot(1,
				PlayerSlot.AI,
				RacesResources.RACE_NATIVES,
				PlayerInfo.TEAM_NEUTRAL,
				true,
				PlayerSlot.AI_NEUTRAL_CAMPAIGN);
		game_network.getClient().setUnitInfo(1, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		switch (getCampaign().getState().getDifficulty()) {
			case CampaignState.DIFFICULTY_EASY:
				break;
			case CampaignState.DIFFICULTY_NORMAL:
				break;
			case CampaignState.DIFFICULTY_HARD:
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
		game_network.getClient().setUnitInfo(2, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().setPlayerSlot(3,
				PlayerSlot.AI,
				RacesResources.RACE_NATIVES,
				0,
				true,
				PlayerSlot.AI_NEUTRAL_CAMPAIGN);
		game_network.getClient().setUnitInfo(3, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		final Player local_player = getViewer().getLocalPlayer();
		final Player reinforcements = getViewer().getWorld().getPlayers()[1];
		final Player enemy = getViewer().getWorld().getPlayers()[2];
		final Player natives = getViewer().getWorld().getPlayers()[3];

		final int chief_start_x = 140*2;
		final int chief_start_y = 117*2;
		final int viking_start_x = 179*2;//236*2;
		final int viking_start_y = 195*2;//362*2;

		// Move start position (for the camera)
		getViewer().getCamera().reset(viking_start_x, viking_start_y);

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
		final Runnable camera_jump0 = new Runnable() {
			public final void run() {
				getViewer().getGUIRoot().pushDelegate(new JumpDelegate(getViewer(), getViewer().getCamera(), chief_start_x + 7, chief_start_y + 7, 200f, 3f, dialog1));
			}
		};
		final Runnable dialog0 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header0"),
						Utils.getBundleString(bundle, "dialog0"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						camera_jump0);
				addModalForm(dialog);
			}
		};
		new GameStartedTrigger(getViewer().getWorld(), dialog0);

		// Insert initial natives
		ResourceBundle player_bundle = ResourceBundle.getBundle(Player.class.getName());
		local_player.setActiveChieftain(new Unit(local_player, chief_start_x, chief_start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_CHIEFTAIN), Utils.getBundleString(player_bundle, "native_chieftain_name"), false));
//		local_player.getChieftain().increaseMagicEnergy(0, 1000);
//		local_player.getChieftain().increaseMagicEnergy(1, 1000);

		natives.buildBuilding(Race.BUILDING_QUARTERS, 135, 128);
		natives.buildBuilding(Race.BUILDING_ARMORY, 143, 124);
		new Unit(natives, 145*2, 127*2, null, natives.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(natives, 149*2, 122*2, null, natives.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(natives, 145*2, 119*2, null, natives.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(natives, 150*2, 125*2, null, natives.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));

		// Insert reinforcements
		int num_reinforcements;
		switch (getCampaign().getState().getDifficulty()) {
			case CampaignState.DIFFICULTY_EASY:
				num_reinforcements = 15;
				break;
			case CampaignState.DIFFICULTY_NORMAL:
				num_reinforcements = 10;
				break;
			case CampaignState.DIFFICULTY_HARD:
				num_reinforcements = 6;
				break;
			default:
				throw new RuntimeException();
		}
		final Unit[] reinforcement_peons = new Unit[num_reinforcements];

		reinforcement_peons[0] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
		reinforcement_peons[1] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
		reinforcement_peons[2] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
		reinforcement_peons[3] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
		reinforcement_peons[4] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
		reinforcement_peons[5] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
		if (getCampaign().getState().getDifficulty() == CampaignState.DIFFICULTY_EASY || getCampaign().getState().getDifficulty() == CampaignState.DIFFICULTY_NORMAL) {
			reinforcement_peons[6] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
			reinforcement_peons[7] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
			reinforcement_peons[8] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
			reinforcement_peons[9] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
		}
		if (getCampaign().getState().getDifficulty() == CampaignState.DIFFICULTY_EASY) {
			reinforcement_peons[10] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
			reinforcement_peons[11] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
			reinforcement_peons[12] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
			reinforcement_peons[13] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
			reinforcement_peons[14] = new Unit(reinforcements, 230*2, 108*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_PEON));
		}

		// Insert viking men
		enemy.setActiveChieftain(new Unit(enemy, viking_start_x, viking_start_y, null, enemy.getRace().getUnitTemplate(Race.UNIT_CHIEFTAIN), Utils.getBundleString(player_bundle, "chieftain_name"), false));
		enemy.getChieftain().increaseMagicEnergy(0, 1000);
		enemy.getChieftain().increaseMagicEnergy(1, 1000);

		int num_iron = 45;
		for (int i = 0; i < num_iron; i++) {
			new Unit(enemy, viking_start_x, viking_start_y, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		}
		int num_rubber = 15;
		for (int i = 0; i < num_rubber; i++) {
			new Unit(enemy, viking_start_x, viking_start_y, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		}
		// Initiate attack
		Building armory = natives.getArmory();
		attack(enemy, armory, num_iron + num_rubber + 1);

		// Winner prize
		final Runnable end_game = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(0, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setIslandState(1, CampaignState.ISLAND_AVAILABLE);
				getCampaign().victory(getViewer());
			}
		};

		// Winning condition
		final Runnable dialog7 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header7"),
						Utils.getBundleString(bundle, "dialog7"),
						getCampaign().getIcons().getFaces()[6],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						end_game);
				addModalForm(dialog);
			}
		};
		final Runnable dialog6 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header6"),
						Utils.getBundleString(bundle, "dialog6"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						dialog7);
				addModalForm(dialog);
			}
		};
		final Runnable dialog5 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header5"),
						Utils.getBundleString(bundle, "dialog5"),
						getCampaign().getIcons().getFaces()[6],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						dialog6);
				addModalForm(dialog);
			}
		};
		new VictoryTrigger(getViewer(), dialog5);

		// Insert treasures
		final SceneryModel[] scenery_models = new SceneryModel[14];
		float dir = (float)StrictMath.sin(StrictMath.PI/4);
		float offset = HeightMap.METERS_PER_UNIT_GRID/2f;
		float shadow_diameter = 4.5f;
		scenery_models[0] = new SceneryModel(getViewer().getWorld(), 163*2 + offset, 126*2 + offset, 0, 1, getViewer().getWorld().getRacesResources().getTreasures()[0], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));

		shadow_diameter = 2.6f;
		scenery_models[1] = new SceneryModel(getViewer().getWorld(), 130*2 + offset, 124*2 + offset, -dir, -dir, getViewer().getWorld().getRacesResources().getTreasures()[3], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		scenery_models[2] = new SceneryModel(getViewer().getWorld(), 152*2 + offset, 138*2 + offset, dir, dir, getViewer().getWorld().getRacesResources().getTreasures()[1], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		scenery_models[3] = new SceneryModel(getViewer().getWorld(), 152*2 + offset, 144*2 + offset, 0, 1, getViewer().getWorld().getRacesResources().getTreasures()[3], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		scenery_models[4] = new SceneryModel(getViewer().getWorld(), 140*2 + offset, 140*2 + offset, 0, 1, getViewer().getWorld().getRacesResources().getTreasures()[4], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		scenery_models[5] = new SceneryModel(getViewer().getWorld(), 143*2 + offset, 116*2 + offset, 0, -1, getViewer().getWorld().getRacesResources().getTreasures()[1], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		scenery_models[6] = new SceneryModel(getViewer().getWorld(), 142*2 + offset, 131*2 + offset, dir, -dir, getViewer().getWorld().getRacesResources().getTreasures()[5], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));

		scenery_models[7] = new SceneryModel(getViewer().getWorld(), 423*2 + offset, 174*2 + offset, 0, 1, getViewer().getWorld().getRacesResources().getTreasures()[1], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		scenery_models[8] = new SceneryModel(getViewer().getWorld(), 408*2 + offset, 161*2 + offset, -1, 0, getViewer().getWorld().getRacesResources().getTreasures()[3], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		scenery_models[9] = new SceneryModel(getViewer().getWorld(), 426*2 + offset, 156*2 + offset, dir, -dir, getViewer().getWorld().getRacesResources().getTreasures()[5], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		scenery_models[10] = new SceneryModel(getViewer().getWorld(), 418*2 + offset, 165*2 + offset, 0, 1, getViewer().getWorld().getRacesResources().getTreasures()[1], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		scenery_models[11] = new SceneryModel(getViewer().getWorld(), 430*2 + offset, 165*2 + offset, 1, 0, getViewer().getWorld().getRacesResources().getTreasures()[3], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		scenery_models[12] = new SceneryModel(getViewer().getWorld(), 419*2 + offset, 170*2 + offset, -dir, dir, getViewer().getWorld().getRacesResources().getTreasures()[4], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));
		scenery_models[13] = new SceneryModel(getViewer().getWorld(), 416*2 + offset, 156*2 + offset, 0, -1, getViewer().getWorld().getRacesResources().getTreasures()[5], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));

		final Runnable dialog4 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header4"),
						Utils.getBundleString(bundle, "dialog4"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT);
				addModalForm(dialog);
				for (int i = 0; i < reinforcement_peons.length; i++) {
					if (!reinforcement_peons[i].isDead())
						changeOwner(reinforcement_peons[i], local_player);
				}
			}
		};
		final Runnable dialog3 = new Runnable() {
			public final void run() {
				changeObjective(1);
				// Remove statues
				for (int i = 0; i < scenery_models.length; i++) {
					scenery_models[i].remove();
				}
				// Remove Vikings
				Unit[] viking_units = new Unit[enemy.getUnits().getSet().size()];
				enemy.getUnits().getSet().toArray(viking_units);
				for (int i = 0; i < viking_units.length; i++) {
					if (!viking_units[i].isDead())
						viking_units[i].removeNow();
				}
				// Insert new Vikings
				int new_viking_start_x = 437*2;
				int new_viking_start_y = 140*2;
				int num_peons;
				switch (getCampaign().getState().getDifficulty()) {
					case CampaignState.DIFFICULTY_EASY:
						num_peons = 5;
						break;
					case CampaignState.DIFFICULTY_NORMAL:
						num_peons = 10;
						break;
					case CampaignState.DIFFICULTY_HARD:
						num_peons = 15;
						break;
					default:
						throw new RuntimeException();
				}
				for (int i = 0; i < num_peons; i++)
					new Unit(enemy, new_viking_start_x, new_viking_start_y, null, enemy.getRace().getUnitTemplate(Race.UNIT_PEON));
				// Remove natives
				Selectable[] native_selectables = new Selectable[natives.getUnits().getSet().size()];
				natives.getUnits().getSet().toArray(native_selectables);
				for (int i = 0; i < native_selectables.length; i++) {
					if (!native_selectables[i].isDead())
						native_selectables[i].hit(10000, 0, 1, enemy);
				}
			CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header3"),
						Utils.getBundleString(bundle, "dialog3"),
						getCampaign().getIcons().getFaces()[2],
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
		final Runnable camera_jump1 = new Runnable() {
			public final void run() {
				int x = 230*2;
				int y = 108*2;
				Camera camera = getViewer().getGUIRoot().getDelegate().getCamera();
				if (camera instanceof GameCamera) {
					getViewer().getGUIRoot().pushDelegate(new JumpDelegate(getViewer(), (GameCamera)camera, x, y, 200f, 3f, dialog2));
				} else if (camera instanceof MapCamera) {
					((MapCamera)camera).mapGoto(x, y, true);
					dialog2.run();
				} else if (camera instanceof JumpCamera || camera instanceof FirstPersonCamera) {
					getViewer().getGUIRoot().getDelegate().pop();
					getViewer().getGUIRoot().pushDelegate(new JumpDelegate(getViewer(), getViewer().getCamera(), x, y, 200f, 3f, dialog2));
				} else {
					throw new RuntimeException("Camera = " + camera);
				}
			}
		};
		new NearArmyTrigger(reinforcement_peons, 10f, local_player, camera_jump1);
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
