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
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.player.UnitInfo;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.trigger.campaign.DeathTrigger;
import com.oddlabs.tt.trigger.campaign.GameStartedTrigger;
import com.oddlabs.tt.trigger.campaign.MagicUsedTrigger;
import com.oddlabs.tt.trigger.campaign.NearArmyTrigger;
import com.oddlabs.tt.trigger.campaign.NearPointTrigger;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.net.GameNetwork;

public final strictfp class VikingIsland8 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(VikingIsland8.class.getName());

	private int objective = 0;
	
	public VikingIsland8(Campaign campaign) {
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
		GameNetwork game_network = startNewGame(network, gui_root, 1024, Landscape.NATIVE, 1f, 1f, 0f, 285914281, 8, VikingCampaign.MAX_UNITS, ai_names);
		game_network.getClient().getServerInterface().setPlayerSlot(0,
				PlayerSlot.HUMAN,
				RacesResources.RACE_VIKINGS,
				0,
				true,
				PlayerSlot.AI_NONE);
		game_network.getClient().setUnitInfo(0, new UnitInfo(false, false, 0, false, 0, 0, 0, 0));
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
				PlayerSlot.AI_NEUTRAL_CAMPAIGN);
		game_network.getClient().setUnitInfo(2, new UnitInfo(false, false, 0, false, 0, 0, 0, 1));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		Runnable runnable;
		final Player local_player = getViewer().getLocalPlayer();
		final Player lost = getViewer().getWorld().getPlayers()[1];
		final Player enemy = getViewer().getWorld().getPlayers()[2];

		// First reset camera direction and then move to rallypoint
		getViewer().getCamera().reset(170*2, 160*2);
		getViewer().getCamera().setPos(358*2, 484*2);

		// Introduction
		final Runnable camera_jump = new Runnable() {
			public final void run() {
				getViewer().getGUIRoot().pushDelegate(new JumpDelegate(getViewer(), getViewer().getCamera(), 170*2, 160*2, 200f, 3f));
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

		// Insert viking men
		ResourceBundle player_bundle = ResourceBundle.getBundle(Player.class.getName());
		local_player.setActiveChieftain(new Unit(local_player, 170*2, 160*2, null, local_player.getRace().getUnitTemplate(Race.UNIT_CHIEFTAIN), Utils.getBundleString(player_bundle, "chieftain_name"), false));
		local_player.getChieftain().increaseMagicEnergy(0, 1000);
		local_player.getChieftain().increaseMagicEnergy(1, 1000);
		int unit_count = getCampaign().getState().getNumPeons()
			+ getCampaign().getState().getNumRockWarriors()
			+ getCampaign().getState().getNumIronWarriors()
			+ getCampaign().getState().getNumRubberWarriors();
		for (int i = 0; i < unit_count; i++) {
			new Unit(local_player, 170*2, 160*2, null, local_player.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		}

		// Winner prize
		// Winning condition
		// See setMagicUsedTrigger()

		// Give blast when arrived
		runnable = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header1"),
						Utils.getBundleString(bundle, "dialog1"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT);
				addModalForm(dialog);
				getViewer().getLocalPlayer().enableMagic(0, true);
				local_player.getChieftain().increaseMagicEnergy(0, 1000);
				local_player.getChieftain().increaseMagicEnergy(1, 1000);
				setMagicUsedTrigger();
				changeObjective(1);
			}
		};
		new NearPointTrigger(354, 478, 4, local_player.getChieftain(), runnable);

		// Insert rally point
		new SceneryModel(getViewer().getWorld(), 354*2, 478*2, 0, -1, local_player.getRace().getRallyPoint());

		// Insert native towers
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 208, 210);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 124, 211);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 139, 223);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 171, 250);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 155, 244);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 68, 189);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 56, 187);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 44, 190);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 180, 124);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 224, 269);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 302, 370);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 312, 394);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 301, 388);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 253, 236);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 278, 316);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_ROCK, 261, 166);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 308, 467);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 192, 252);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 203, 267);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_IRON, 316, 435);

		// Insert first blocking army
		Unit bait = new Unit(enemy, 240*2, 137*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		final Selectable[] army = new Selectable[25];
		army[0] = new Unit(enemy, 213*2, 122*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		army[1] = new Unit(enemy, 213*2, 122*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		army[2] = new Unit(enemy, 213*2, 122*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[3] = new Unit(enemy, 213*2, 122*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[4] = new Unit(enemy, 213*2, 122*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[5] = new Unit(enemy, 245*2, 170*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		army[6] = new Unit(enemy, 245*2, 170*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		army[7] = new Unit(enemy, 245*2, 170*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[8] = new Unit(enemy, 245*2, 170*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[9] = new Unit(enemy, 245*2, 170*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[10] = new Unit(enemy, 239*2, 177*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		army[11] = new Unit(enemy, 239*2, 177*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		army[12] = new Unit(enemy, 239*2, 177*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[13] = new Unit(enemy, 239*2, 177*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[14] = new Unit(enemy, 239*2, 177*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[15] = new Unit(enemy, 286*2, 113*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		army[16] = new Unit(enemy, 286*2, 113*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		army[17] = new Unit(enemy, 286*2, 113*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[18] = new Unit(enemy, 286*2, 113*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[19] = new Unit(enemy, 286*2, 113*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[20] = new Unit(enemy, 289*2, 141*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		army[21] = new Unit(enemy, 289*2, 141*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		army[22] = new Unit(enemy, 289*2, 141*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[23] = new Unit(enemy, 289*2, 141*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army[24] = new Unit(enemy, 289*2, 141*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		runnable = new Runnable() {
			public final void run() {
				enemy.setLandscapeTarget(army, 238, 136, Target.ACTION_ATTACK, true);
			}
		};
		new DeathTrigger(bait, runnable);

		// Insert second blocking army
		Unit bait2 = new Unit(enemy, 347*2, 455*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		int num_hidden;
		switch (getCampaign().getState().getDifficulty()) {
			case CampaignState.DIFFICULTY_EASY:
				num_hidden = 7;
				break;
			case CampaignState.DIFFICULTY_NORMAL:
				num_hidden = 13;
				break;
			case CampaignState.DIFFICULTY_HARD:
				num_hidden = 20;
				break;
			default:
				throw new RuntimeException();
		}
		final Selectable[] army2 = new Selectable[num_hidden];
		army2[0] = new Unit(enemy, 364*2, 440*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		army2[1] = new Unit(enemy, 364*2, 440*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		army2[2] = new Unit(enemy, 364*2, 440*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army2[3] = new Unit(enemy, 364*2, 440*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		army2[4] = new Unit(enemy, 364*2, 440*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		army2[5] = new Unit(enemy, 364*2, 440*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		army2[6] = new Unit(enemy, 364*2, 440*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));

		if (getCampaign().getState().getDifficulty() == CampaignState.DIFFICULTY_HARD || getCampaign().getState().getDifficulty() == CampaignState.DIFFICULTY_NORMAL) {
			army2[7] = new Unit(enemy, 365*2, 427*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
			army2[8] = new Unit(enemy, 365*2, 427*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
			army2[9] = new Unit(enemy, 365*2, 427*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
			army2[10] = new Unit(enemy, 365*2, 427*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
			army2[11] = new Unit(enemy, 365*2, 427*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
			army2[12] = new Unit(enemy, 365*2, 427*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		}
		if (getCampaign().getState().getDifficulty() == CampaignState.DIFFICULTY_HARD) {
			army2[13] = new Unit(enemy, 366*2, 419*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
			army2[14] = new Unit(enemy, 366*2, 419*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
			army2[15] = new Unit(enemy, 366*2, 419*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
			army2[16] = new Unit(enemy, 366*2, 419*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
			army2[17] = new Unit(enemy, 366*2, 419*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
			army2[18] = new Unit(enemy, 366*2, 419*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
			army2[19] = new Unit(enemy, 366*2, 419*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		}
		runnable = new Runnable() {
			public final void run() {
				enemy.setLandscapeTarget(army2, 352, 480, Target.ACTION_ATTACK, true);
			}
		};
		new DeathTrigger(bait2, runnable);

		// Insert scattered resistance
		new Unit(enemy, 348*2, 315*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 348*2, 315*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 348*2, 315*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 348*2, 315*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));

		new Unit(enemy, 299*2, 321*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 299*2, 321*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 299*2, 321*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));

		new Unit(enemy, 300*2, 453*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 300*2, 453*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
		new Unit(enemy, 300*2, 453*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 300*2, 453*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 300*2, 453*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 300*2, 453*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 300*2, 453*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));

		new Unit(enemy, 352*2, 456*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 355*2, 459*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 360*2, 461*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 365*2, 466*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 367*2, 474*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 369*2, 479*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 348*2, 455*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 342*2, 459*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 335*2, 467*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 334*2, 475*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 345*2, 465*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 346*2, 460*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 347*2, 467*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));

		new Unit(enemy, 331*2, 374*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 331*2, 374*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 331*2, 374*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 331*2, 374*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));

		new Unit(enemy, 348*2, 371*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 348*2, 371*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));

		new Unit(enemy, 399*2, 399*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 399*2, 399*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 399*2, 399*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 399*2, 399*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 399*2, 399*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));

		new Unit(enemy, 354*2, 474*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 354*2, 474*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		new Unit(enemy, 354*2, 474*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 354*2, 474*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 354*2, 474*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		new Unit(enemy, 354*2, 474*2, null, enemy.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));

		// Insert Neurtal units
		int num_neutrals;
		switch (getCampaign().getState().getDifficulty()) {
			case CampaignState.DIFFICULTY_EASY:
				num_neutrals = 15;
				break;
			case CampaignState.DIFFICULTY_NORMAL:
				num_neutrals = 9;
				break;
			case CampaignState.DIFFICULTY_HARD:
				num_neutrals = 6;
				break;
			default:
				throw new RuntimeException();
		}
		final Unit[] neutrals = new Unit[num_neutrals];
		neutrals[0] = new Unit(lost, 267*2, 325*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		neutrals[1] = new Unit(lost, 268*2, 324*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		neutrals[2] = new Unit(lost, 267*2, 323*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		neutrals[3] = new Unit(lost, 265*2, 325*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		neutrals[4] = new Unit(lost, 265*2, 324*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		neutrals[5] = new Unit(lost, 266*2, 326*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
		if (getCampaign().getState().getDifficulty() == CampaignState.DIFFICULTY_EASY || getCampaign().getState().getDifficulty() == CampaignState.DIFFICULTY_NORMAL) {
			neutrals[6] = new Unit(lost, 267*2, 327*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
			neutrals[7] = new Unit(lost, 269*2, 327*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
			neutrals[8] = new Unit(lost, 270*2, 327*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		}
		if (getCampaign().getState().getDifficulty() == CampaignState.DIFFICULTY_EASY) {
			neutrals[9] = new Unit(lost, 268*2, 326*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
			neutrals[10] = new Unit(lost, 271*2, 325*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
			neutrals[11] = new Unit(lost, 270*2, 324*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
			neutrals[12] = new Unit(lost, 271*2, 328*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
			neutrals[13] = new Unit(lost, 269*2, 330*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
			neutrals[14] = new Unit(lost, 272*2, 323*2, null, lost.getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
		}
		runnable = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header2"),
						Utils.getBundleString(bundle, "dialog2"),
						getCampaign().getIcons().getFaces()[4],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT);
				addModalForm(dialog);
				for (int i = 0; i < neutrals.length; i++) {
					if (!neutrals[i].isDead())
						changeOwner(neutrals[i], local_player);
				}
			}
		};
		new NearArmyTrigger(neutrals, 10f, local_player, runnable);
	}

	private final void setMagicUsedTrigger() {
		Runnable runnable = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(8, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setIslandState(7, CampaignState.ISLAND_AVAILABLE);
				getCampaign().getState().setIslandState(9, CampaignState.ISLAND_SEMI_AVAILABLE);
				getCampaign().getState().setIslandState(11, CampaignState.ISLAND_SEMI_AVAILABLE);
				getCampaign().victory(getViewer());
			}
		};

		new MagicUsedTrigger(getViewer().getLocalPlayer().getChieftain(), 354*2, 478*2, 15, 0, runnable);
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
