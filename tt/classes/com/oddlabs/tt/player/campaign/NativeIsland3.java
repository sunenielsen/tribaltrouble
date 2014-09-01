package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.delegate.JumpDelegate;
import com.oddlabs.tt.landscape.LandscapeTarget;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.SceneryModel;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.player.UnitInfo;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.trigger.campaign.GameStartedTrigger;
import com.oddlabs.tt.trigger.campaign.MagicUsedTrigger;
import com.oddlabs.tt.trigger.campaign.NearPointTrigger;
import com.oddlabs.tt.trigger.campaign.TimeTrigger;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.net.GameNetwork;

public final strictfp class NativeIsland3 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(NativeIsland3.class.getName());

	private int objective = 0;
	
	public NativeIsland3(Campaign campaign) {
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
		GameNetwork game_network = startNewGame(network, gui_root, 512, Landscape.VIKING, 1f, 1f, 0f, 808208041, 3, NativeCampaign.MAX_UNITS, ai_names);
		game_network.getClient().getServerInterface().setPlayerSlot(0,
				PlayerSlot.HUMAN,
				RacesResources.RACE_NATIVES,
				0,
				true,
				PlayerSlot.AI_NONE);
		game_network.getClient().setUnitInfo(0, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().setPlayerSlot(2,
				PlayerSlot.AI,
				RacesResources.RACE_VIKINGS,
				1,
				true,
				PlayerSlot.AI_NEUTRAL_CAMPAIGN);
		game_network.getClient().setUnitInfo(2, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().setPlayerSlot(4,
				PlayerSlot.AI,
				RacesResources.RACE_VIKINGS,
				1,
				true,
				PlayerSlot.AI_PASSIVE_CAMPAIGN);
		game_network.getClient().setUnitInfo(4, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		Runnable runnable;
		final Player local_player = getViewer().getLocalPlayer();
		final Player enemy = getViewer().getWorld().getPlayers()[1];
		final Player reinforcements = getViewer().getWorld().getPlayers()[2];

		final int start_x = 125*2;
		final int start_y = 222*2;
		final int thor_x = 40*2;
		final int thor_y = 40*2;

		// First reset camera direction and then move to rallypoint
		getViewer().getCamera().reset(start_x, start_y);
		getViewer().getCamera().setPos(thor_x, thor_y + 9);

		// Introduction
		final Runnable camera_jump = new Runnable() {
			public final void run() {
				getViewer().getGUIRoot().pushDelegate(new JumpDelegate(getViewer(), getViewer().getCamera(), start_x, start_y, 200f, 3f));
			}
		};
		runnable = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header0"),
						Utils.getBundleString(bundle, "dialog0"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						camera_jump);
				addModalForm(dialog);
			}
		};
		new GameStartedTrigger(getViewer().getWorld(), runnable);

		// Disable construction
		getViewer().getLocalPlayer().enableBuilding(Race.BUILDING_QUARTERS, false);
		getViewer().getLocalPlayer().enableBuilding(Race.BUILDING_ARMORY, false);
		getViewer().getLocalPlayer().enableBuilding(Race.BUILDING_TOWER, false);

		// Insert native men
		ResourceBundle player_bundle = ResourceBundle.getBundle(Player.class.getName());
		local_player.setActiveChieftain(new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_CHIEFTAIN), Utils.getBundleString(player_bundle, "native_chieftain_name"), false));
		local_player.getChieftain().increaseMagicEnergy(0, 1000);
		local_player.getChieftain().increaseMagicEnergy(1, 1000);
		// 5 peons
		for (int i = 0; i < 5; i++) {
			new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
		}
		// rest as warriors
		int unit_count = getCampaign().getState().getNumPeons()
			+ getCampaign().getState().getNumRockWarriors()
			+ getCampaign().getState().getNumIronWarriors()
			+ getCampaign().getState().getNumRubberWarriors() - 5;
		for (int i = 0; i < unit_count; i++) {
			if (getCampaign().getState().getDifficulty() == CampaignState.DIFFICULTY_EASY)
				new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
			else 
				new Unit(local_player, start_x, start_y, null, local_player.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		}

		// Winner prize
		final Runnable prize = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(3, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setIslandState(4, CampaignState.ISLAND_AVAILABLE);
				getCampaign().getState().setHasMagic1(true);
				getCampaign().victory(getViewer());
			}
		};

		// Ask for Stinking Stew
		final Runnable dialog8 = new Runnable() {
			public final void run() {
				// Winning condition
				new MagicUsedTrigger(local_player.getChieftain(), thor_x, thor_y, 20, 0, prize);
				changeObjective(1);

				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header8"),
						Utils.getBundleString(bundle, "dialog8"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT);
				addModalForm(dialog);
			}
		};
		final Runnable dialog7 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header7"),
						Utils.getBundleString(bundle, "dialog7"),
						getCampaign().getIcons().getFaces()[7],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						dialog8);
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
						getCampaign().getIcons().getFaces()[7],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						dialog6);
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
						getCampaign().getIcons().getFaces()[7],
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
		final Runnable dialog1 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header1"),
						Utils.getBundleString(bundle, "dialog1"),
						getCampaign().getIcons().getFaces()[7],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						dialog2);
				addModalForm(dialog);
				local_player.getChieftain().increaseMagicEnergy(0, 1000);
				local_player.getChieftain().increaseMagicEnergy(1, 1000);
			}
		};
		new NearPointTrigger(thor_x/2, thor_x/2, 8, local_player.getChieftain(), dialog1);

		// Insert Thor
		float shadow_diameter = 4.5f;

		float dir = (float)StrictMath.sin(StrictMath.PI/4);
		new SceneryModel(getViewer().getWorld(), thor_x, thor_y, dir, dir, enemy.getRace().getUnitTemplate(Race.UNIT_CHIEFTAIN).getSpriteRenderer(), shadow_diameter, true, Utils.getBundleString(bundle, "god"), Unit.ANIMATION_THOR, -1f, 0f);


		// Insert reinforcements
		reinforcements.setStartX(126*2);
		reinforcements.setStartY(135*2);
		reinforcements.buildBuilding(Race.BUILDING_QUARTERS, 96, 145);
		reinforcements.buildBuilding(Race.BUILDING_ARMORY, 126, 135);
		for (int i = 0; i < 30; i++) {
			new Unit(reinforcements, 126*2, 135*2, null, reinforcements.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		}

		// Insert native towers
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 60, 60);//*
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 110, 167);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 102, 127);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 67, 163);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 71, 189);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 92, 205);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 128, 186);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 191, 185);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 145, 128);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 173, 90);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 161, 82);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 120, 140);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 124, 127);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 135, 140);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 100, 139);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 80, 97);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 112, 76);

		// Scattered resistance
		new Unit(enemy, 180*2, 155*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 178*2, 145*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 178*2, 151*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 180*2, 153*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 103*2, 93*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 99*2, 99*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 113*2, 98*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 116*2, 98*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 83*2, 170*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 110*2, 199*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 186*2, 215*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 186*2, 211*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 53*2, 105*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 51*2, 107*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 53*2, 110*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 53*2, 101*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));

		// Send reinforcements
		final Runnable reinforce = new Runnable() {
			public final void run() {
				Unit unit = getWarrior(reinforcements);
				if (unit != null && !unit.isDead()) {
					Unit new_unit = changeOwner(unit, enemy);
					if (new_unit != null && !new_unit.isDead())
						new_unit.setTarget(new LandscapeTarget(62, 62), Target.ACTION_DEFAULT, true);
				}
			}
		};
		float interval;
		switch (getCampaign().getState().getDifficulty()) {
			case CampaignState.DIFFICULTY_EASY:
				interval = 120f;
				break;
			case CampaignState.DIFFICULTY_NORMAL:
				interval = 60f;
				break;
			case CampaignState.DIFFICULTY_HARD:
				interval = 30f;
				break;
			default:
				throw new RuntimeException();
		}
		float time = interval;
		for (int i = 0;i < 10; i++) {
			new TimeTrigger(getViewer().getWorld(), time, reinforce);
			time += interval;
		}
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
