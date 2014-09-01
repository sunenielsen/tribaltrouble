package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.delegate.JumpDelegate;
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
import com.oddlabs.tt.trigger.campaign.MagicUsedTrigger;
import com.oddlabs.tt.trigger.campaign.NearPointTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.net.GameNetwork;

public final strictfp class VikingIsland10 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(VikingIsland10.class.getName());

	private int objective = 0;
	
	public VikingIsland10(Campaign campaign) {
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
		GameNetwork game_network = startNewGame(network, gui_root, 512, Landscape.NATIVE, 1f, 1f, 0f, -1442873271, 10, VikingCampaign.MAX_UNITS, ai_names);
		game_network.getClient().getServerInterface().setPlayerSlot(0,
				PlayerSlot.HUMAN,
				RacesResources.RACE_VIKINGS,
				0,
				true,
				PlayerSlot.AI_NONE);
		game_network.getClient().setUnitInfo(0, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
		game_network.getClient().getServerInterface().setPlayerSlot(2,
				PlayerSlot.AI,
				RacesResources.RACE_NATIVES,
				1,
				true,
				PlayerSlot.AI_NEUTRAL_CAMPAIGN);
		game_network.getClient().setUnitInfo(2, new UnitInfo(false, false, 0, false, 0, 0, 0, 1));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		Runnable runnable;
		final Player local_player = getViewer().getLocalPlayer();
		final Player enemy = getViewer().getWorld().getPlayers()[1];

		// First reset camera direction and then move to rallypoint
		getViewer().getCamera().reset(142*2, 182*2);
		getViewer().getCamera().setPos(177*2, 156*2);

		// Introduction
		final Runnable camera_jump = new Runnable() {
			public final void run() {
				getViewer().getGUIRoot().pushDelegate(new JumpDelegate(getViewer(), getViewer().getCamera(), 142*2, 182*2, 200f, 3f));
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

		// Insert viking men
		ResourceBundle player_bundle = ResourceBundle.getBundle(Player.class.getName());
		local_player.setActiveChieftain(new Unit(local_player, 142*2, 182*2, null, local_player.getRace().getUnitTemplate(Race.UNIT_CHIEFTAIN), Utils.getBundleString(player_bundle, "chieftain_name"), false));
		local_player.getChieftain().increaseMagicEnergy(0, 1000);
		local_player.getChieftain().increaseMagicEnergy(1, 1000);
		// 5 peons
		for (int i = 0; i < 5; i++) {
			new Unit(local_player, 142*2, 182*2, null, local_player.getRace().getUnitTemplate(Race.UNIT_PEON));
		}
		// rest as warriors
		int unit_count = getCampaign().getState().getNumPeons()
			+ getCampaign().getState().getNumRockWarriors()
			+ getCampaign().getState().getNumIronWarriors()
			+ getCampaign().getState().getNumRubberWarriors() - 5;
		for (int i = 0; i < unit_count; i++) {
			if (getCampaign().getState().getDifficulty() == CampaignState.DIFFICULTY_EASY)
				new Unit(local_player, 142*2, 182*2, null, local_player.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
			else 
				new Unit(local_player, 142*2, 182*2, null, local_player.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		}

		// Winner prize
		runnable = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(10, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setIslandState(13, CampaignState.ISLAND_AVAILABLE);
				getCampaign().getState().setHasMagic1(true);
				getCampaign().victory(getViewer());
			}
		};

		// Winning condition
		new MagicUsedTrigger(local_player.getChieftain(), 173*2, 153*2, 7, 1, runnable);

		// Give blast when arrived
		final Runnable dialog11 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header11"),
						Utils.getBundleString(bundle, "dialog11"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT);
				addModalForm(dialog);
				changeObjective(1);
			}
		};
		final Runnable dialog10 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header10"),
						Utils.getBundleString(bundle, "dialog10"),
						getCampaign().getIcons().getFaces()[8],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						dialog11);
				addModalForm(dialog);
			}
		};
		final Runnable dialog9 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header9"),
						Utils.getBundleString(bundle, "dialog9"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						dialog10);
				addModalForm(dialog);
			}
		};
		final Runnable dialog8 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header8"),
						Utils.getBundleString(bundle, "dialog8"),
						getCampaign().getIcons().getFaces()[8],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						dialog9);
				addModalForm(dialog);
			}
		};
		final Runnable dialog7 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header7"),
						Utils.getBundleString(bundle, "dialog7"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						dialog8);
				addModalForm(dialog);
			}
		};
		final Runnable dialog6 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header6"),
						Utils.getBundleString(bundle, "dialog6"),
						getCampaign().getIcons().getFaces()[8],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						dialog7);
				addModalForm(dialog);
			}
		};
		final Runnable dialog5 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header5"),
						Utils.getBundleString(bundle, "dialog5"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						dialog6);
				addModalForm(dialog);
			}
		};
		final Runnable dialog4 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header4"),
						Utils.getBundleString(bundle, "dialog4"),
						getCampaign().getIcons().getFaces()[8],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						dialog5);
				addModalForm(dialog);
			}
		};
		final Runnable dialog3 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header3"),
						Utils.getBundleString(bundle, "dialog3"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						dialog4);
				addModalForm(dialog);
			}
		};
		final Runnable dialog2 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header2"),
						Utils.getBundleString(bundle, "dialog2"),
						getCampaign().getIcons().getFaces()[8],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						dialog3);
				addModalForm(dialog);
			}
		};
		final Runnable dialog1 = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header1"),
						Utils.getBundleString(bundle, "dialog1"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						dialog2);
				addModalForm(dialog);
				getViewer().getLocalPlayer().enableMagic(1, true);
				local_player.getChieftain().increaseMagicEnergy(0, 1000);
				local_player.getChieftain().increaseMagicEnergy(1, 1000);
			}
		};
		new NearPointTrigger(173, 153, 3, local_player.getChieftain(), dialog1);

		// Insert statue
		float shadow_diameter = 2.6f;
		float offset = HeightMap.METERS_PER_UNIT_GRID/2f;
		new SceneryModel(getViewer().getWorld(), 173*2 + offset, 153*2 + offset, 0, 1, getViewer().getWorld().getRacesResources().getTreasures()[2], shadow_diameter, true, Utils.getBundleString(bundle, "statue"));

		// Insert native towers
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 177, 159);//*
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 180, 176);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 165, 195);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 169, 198);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 152, 209);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 200, 197);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 199, 169);

		// Blocking army
		new Unit(enemy, 173*2, 188*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 173*2, 188*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 173*2, 188*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 173*2, 188*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 173*2, 188*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 175*2, 190*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 175*2, 190*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 175*2, 190*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 175*2, 190*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 175*2, 190*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 178*2, 192*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 178*2, 192*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 178*2, 192*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 178*2, 192*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 178*2, 192*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 185*2, 194*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 185*2, 194*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 185*2, 194*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 185*2, 194*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 185*2, 194*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 181*2, 195*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 181*2, 195*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 181*2, 195*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 181*2, 195*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 181*2, 195*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 181*2, 195*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 181*2, 195*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 181*2, 195*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 181*2, 195*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 164*2, 203*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 164*2, 203*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 164*2, 203*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 164*2, 203*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 164*2, 203*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 164*2, 203*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 164*2, 203*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 164*2, 203*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));

		// Scattered resistance
		new Unit(enemy, 114*2, 163*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 114*2, 163*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 118*2, 170*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 118*2, 170*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 109*2, 153*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 109*2, 153*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 122*2, 151*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 122*2, 151*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy,  98*2, 137*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy,  98*2, 137*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy,  93*2, 130*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy,  93*2, 130*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy,  86*2, 132*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy,  86*2, 132*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy,  72*2, 146*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy,  72*2, 146*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 158*2,  97*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 158*2,  97*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 132*2, 118*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 132*2, 118*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 157*2, 135*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 157*2, 135*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
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
