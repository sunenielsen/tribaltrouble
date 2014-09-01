package com.oddlabs.tt.player.campaign;

import java.util.ResourceBundle;

import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.form.DemoForm;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.gui.CampaignIcons;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIImage;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.NativeCampaignIcons;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class NativeCampaign extends Campaign {
	public final static int MAX_UNITS = 41;
	private final static int[] INITIAL_STATES = new int[]{
/*
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
		CampaignState.ISLAND_HIDDEN};

	private final Island[] islands;

	static {
		NativeCampaignIcons.load();
	}

	public NativeCampaign(NetworkSelector network, GUIRoot gui_root) {
		this(network, gui_root, new CampaignState(INITIAL_STATES));
	}

	public NativeCampaign(NetworkSelector network, GUIRoot gui_root, CampaignState campaign_state) {
		super(campaign_state);
		islands = new Island[NativeCampaignIcons.getIcons().getNumIslands()];
		islands[0] = new NativeIsland0(this);
		islands[1] = new NativeIsland1(this);
		islands[2] = new NativeIsland2(this);
		islands[3] = new NativeIsland3(this);
		islands[4] = new NativeIsland4(this);
		islands[5] = new NativeIsland5(this);
		islands[6] = new NativeIsland6(this);
		islands[7] = new NativeIsland7(this);
		if (getState().getCurrentIsland() == -1) {
			startIsland(network, gui_root, 0);
		}
		getState().setHasMagic0(true);
		getState().setHasRubberWeapons(true);
	}

	public final CampaignIcons getIcons() {
		return NativeCampaignIcons.getIcons();
	}

	public final void islandChosen(NetworkSelector network, GUIRoot gui_root, int number) {
		if (Renderer.isRegistered()) {
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
		if (getState().getCurrentIsland() == 4)
			((NativeIsland4)islands[4]).removeCounter();
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
