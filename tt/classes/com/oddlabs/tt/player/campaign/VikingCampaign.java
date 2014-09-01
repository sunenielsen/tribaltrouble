package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.DemoForm;
import com.oddlabs.tt.gui.CampaignIcons;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIImage;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.gui.VikingCampaignIcons;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;

public final strictfp class VikingCampaign extends Campaign {
	public final static int MAX_UNITS = 46;
	private final static int[] INITIAL_STATES = new int[]{
/*
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_AVAILABLE};
*/
		CampaignState.ISLAND_AVAILABLE,
		CampaignState.ISLAND_UNAVAILABLE,
		CampaignState.ISLAND_UNAVAILABLE,
		CampaignState.ISLAND_UNAVAILABLE,
		CampaignState.ISLAND_UNAVAILABLE,
		CampaignState.ISLAND_UNAVAILABLE,
		CampaignState.ISLAND_UNAVAILABLE,
		CampaignState.ISLAND_UNAVAILABLE,
		CampaignState.ISLAND_UNAVAILABLE,
		CampaignState.ISLAND_UNAVAILABLE,
		CampaignState.ISLAND_HIDDEN,
		CampaignState.ISLAND_UNAVAILABLE,
		CampaignState.ISLAND_UNAVAILABLE,
		CampaignState.ISLAND_UNAVAILABLE,
		CampaignState.ISLAND_UNAVAILABLE};

	private final Island[] islands;

	static {
		VikingCampaignIcons.load();
	}

	public VikingCampaign(NetworkSelector network, GUIRoot gui_root) {
		this(network, gui_root, new CampaignState(INITIAL_STATES));
	}

	public VikingCampaign(NetworkSelector network, GUIRoot gui_root, CampaignState campaign_state) {
		super(campaign_state);
		islands = new Island[VikingCampaignIcons.getIcons().getNumIslands()];
		islands[0] = new VikingIsland0(this);
		islands[1] = new VikingIsland1(this);
		islands[2] = new VikingIsland2(this);
		islands[3] = new VikingIsland3(this);
		islands[4] = new VikingIsland4(this);
		islands[5] = new VikingIsland5(this);
		islands[6] = new VikingIsland6(this);
		islands[7] = new VikingIsland7(this);
		islands[8] = new VikingIsland8(this);
		islands[9] = new VikingIsland9(this);
		islands[10] = new VikingIsland10(this);
		islands[11] = new VikingIsland11(this);
		islands[12] = new VikingIsland12(this);
		islands[13] = new VikingIsland13(this);
		islands[14] = new VikingIsland14(this);
		if (getState().getCurrentIsland() == -1) {
			startIsland(network, gui_root, 0);
		}
	}

	public final CampaignIcons getIcons() {
		return VikingCampaignIcons.getIcons();
	}

	public final void islandChosen(NetworkSelector network, GUIRoot gui_root, int number) {
		if (Renderer.isRegistered() || number == 1 || number == 2) {
			Form dialog = new CampaignDialogForm(islands[number].getHeader(),
					islands[number].getDescription(),
					null,
					CampaignDialogForm.ALIGN_IMAGE_LEFT,
					new IslandListener(network, gui_root, number), true);
			gui_root.addModalForm(dialog);
		} else {
			ResourceBundle db = ResourceBundle.getBundle(DemoForm.class.getName());
			Form demo_form = new DemoForm(gui_root, Utils.getBundleString(db, "limited_campaign_header"), new GUIImage(512, 256, 0f, 0f, 1f, 1f, "/textures/gui/demo_campaign"), Utils.getBundleString(db, "limited_campaign"));
			gui_root.addModalForm(demo_form);
		}
	}

	public final CharSequence getCurrentObjective() {
		if (getState().getCurrentIsland() != -1) {
			return islands[getState().getCurrentIsland()].getCurrentObjective();
		}
		throw new RuntimeException();
	}

	public final void defeated(WorldViewer viewer, String game_over_message) {
		if (getState().getCurrentIsland() == 13)
			((VikingIsland13)islands[13]).removeCounter();
		super.defeated(viewer, game_over_message);
	}

	public final void startIsland(NetworkSelector network, GUIRoot gui_root, int number) {
		getState().setCurrentIsland(number);
		islands[number].chosen(network, gui_root);
	}

	private final strictfp class IslandListener implements Runnable {
		private final int number;
		private final GUIRoot gui_root;
		private final NetworkSelector network;

		public IslandListener(NetworkSelector network, GUIRoot gui_root, int number) {
			this.number = number;
			this.gui_root = gui_root;
			this.network = network;
		}

		public final void run() {
			startIsland(network, gui_root, number);
		}
	}

}
