package com.oddlabs.tt.trigger.campaign;

import java.util.ResourceBundle;

import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.campaign.Campaign;
import com.oddlabs.tt.trigger.IntervalTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class DefeatTrigger extends IntervalTrigger {
	private final Campaign campaign;
	private final Unit chieftain;
	private final Runnable runnable;
	private final WorldViewer viewer;

	private boolean triggered_by_chieftain_death = false;

	public DefeatTrigger(WorldViewer viewer, Campaign campaign, Unit chieftain) {
		this(viewer, campaign, chieftain, null);
	}

	public DefeatTrigger(WorldViewer viewer, Campaign campaign, Unit chieftain, Runnable runnable) {
		super(viewer.getWorld(), .5f, 0f);
		this.viewer = viewer;
		this.campaign = campaign;
		this.chieftain = chieftain;
		this.runnable = runnable;
	}

	protected final void check() {
		Player current = viewer.getLocalPlayer();
		if (chieftain != current.getChieftain()) {
			triggered_by_chieftain_death = true;
			triggered();
		}

		int units = current.getUnitCountContainer().getNumSupplies();
		if (units == 0 && !current.hasActiveChieftain()) {
			triggered();
		}
	}

	protected final void done() {
		if (runnable == null) {
			GUIRoot gui_root = viewer.getGUIRoot();
			ResourceBundle bundle = ResourceBundle.getBundle(DefeatTrigger.class.getName());
			String game_over_message;
			if (triggered_by_chieftain_death)
				game_over_message = Utils.getBundleString(bundle, "defeat_by_chieftain");
			else
				game_over_message = Utils.getBundleString(bundle, "defeat");
			campaign.defeated(viewer, game_over_message);
		} else {
			runnable.run();
		}
	}
}
