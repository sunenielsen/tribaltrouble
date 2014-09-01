package com.oddlabs.tt.viewer;

import com.oddlabs.tt.form.WaitingForPlayersForm;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.net.StallHandler;

final strictfp class ViewerStallHandler implements StallHandler {
	private final static float SHOW_WAITING_DELAY_SECONDS = 3f;

	private final WorldViewer viewer;

	private float local_stall_time;
	private int stall_tick;
	private WaitingForPlayersForm waiting_for_players_form;

	ViewerStallHandler(WorldViewer viewer) {
		this.viewer = viewer;
	}

	private void resetStallTime() {
		local_stall_time = LocalEventQueue.getQueue().getTime();
	}

	public final void stopStall() {
		if (waiting_for_players_form != null) {
			waiting_for_players_form.remove();
			waiting_for_players_form = null;
		}
	}

	public final void peerhubFailed() {
		viewer.close();
	}

	public final void processStall(int tick) {
		if (stall_tick != tick) {
			System.out.println("Stalled on tick " + tick);
			stall_tick = tick;
			resetStallTime();
		}
		float elapsed_time = LocalEventQueue.getQueue().getTime() - local_stall_time;
		if (tick == 0 || elapsed_time > SHOW_WAITING_DELAY_SECONDS) {
			if (waiting_for_players_form == null) {
				waiting_for_players_form = new WaitingForPlayersForm(viewer);
				viewer.getGUIRoot().addModalForm(waiting_for_players_form);
			}
		}
	}
}
