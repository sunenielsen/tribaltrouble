package com.oddlabs.tt.viewer;

import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.player.Player;

final strictfp class AttackNotification extends Notification {
	private final static float RADIUS = 30f;
	private final static float FADE_OUT = 5f;
	
	private boolean active = true;

	public AttackNotification(Player local_player, GUIRoot gui_root, Selectable center, NotificationManager manager, AnimationManager animation_manager) {
		super(local_player.getWorld(), gui_root, center.getPositionX(), center.getPositionY(), manager, 1f, 0f, 0f, local_player.getRace().getAttackNotificationAudio(), false, animation_manager);
	}

	public final boolean contains(Target target) {
		float dx = getX() - target.getPositionX();
		float dy = getY() - target.getPositionY();
		float dist = dx*dx + dy*dy;
		return dist <= RADIUS*RADIUS;
	}

	public final void restartTimer() {
		if (!active)
			getTimer().resetTime();
	}

	public void update(Object anim) {
		if (active) {
			active = false;
			getArrow().remove();
			getTimer().setTimerInterval(FADE_OUT);
		} else {
			getTimer().stop();
			getManager().removeAttackNotification(this);
		}
	}
}
