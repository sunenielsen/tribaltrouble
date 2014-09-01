package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.InGameCampaignDialogForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.player.UnitInfo;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.trigger.campaign.GameStartedTrigger;
import com.oddlabs.tt.trigger.campaign.VictoryTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.net.GameNetwork;

public final strictfp class VikingIsland11 extends Island {
	private final ResourceBundle bundle = ResourceBundle.getBundle(VikingIsland11.class.getName());
	
	public VikingIsland11(Campaign campaign) {
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
		GameNetwork game_network = startNewGame(network, gui_root, 256, Landscape.NATIVE, .75f, 1f, .85f, 83493473, 11, VikingCampaign.MAX_UNITS, ai_names);
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
		int ai_peons;
		switch (getCampaign().getState().getDifficulty()) {
			case CampaignState.DIFFICULTY_EASY:
				ai_peons = 10;
				break;
			case CampaignState.DIFFICULTY_NORMAL:
				ai_peons = 25;
				break;
			case CampaignState.DIFFICULTY_HARD:
				ai_peons = 40;
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
		game_network.getClient().setUnitInfo(2, new UnitInfo(true, false, 0, false, ai_peons, 0, 2, 0));
		game_network.getClient().getServerInterface().startServer();
	}

	protected final void start() {
		Runnable runnable;
		final Player enemy = getViewer().getWorld().getPlayers()[1];

		// Introduction
		final Runnable dialog8 = new Runnable() {
			public final void run() {
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
						getCampaign().getIcons().getFaces()[3],
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
						getCampaign().getIcons().getFaces()[6],
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
						getCampaign().getIcons().getFaces()[6],
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
						getCampaign().getIcons().getFaces()[6],
						CampaignDialogForm.ALIGN_IMAGE_RIGHT,
						dialog2);
				addModalForm(dialog);
			}
		};
		runnable = new Runnable() {
			public final void run() {
				CampaignDialogForm dialog = new InGameCampaignDialogForm(getViewer(), Utils.getBundleString(bundle, "header0"),
						Utils.getBundleString(bundle, "dialog0"),
						getCampaign().getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						dialog1);
				addModalForm(dialog);
			}
		};
		new GameStartedTrigger(getViewer().getWorld(), runnable);

		// Winner prize
		runnable = new Runnable() {
			public final void run() {
				getCampaign().getState().setIslandState(11, CampaignState.ISLAND_COMPLETED);
				getCampaign().getState().setIslandState(12, CampaignState.ISLAND_AVAILABLE);
				getCampaign().victory(getViewer());
			}
		};

		// Winning condition
		new VictoryTrigger(getViewer(), runnable);

		// Tower
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 47, 22);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 54, 36);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 68, 36);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 80, 36);
		insertGuardTower(enemy, Race.UNIT_WARRIOR_RUBBER, 94, 30);
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
