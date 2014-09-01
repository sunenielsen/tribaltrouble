package com.oddlabs.tt.viewer;

import java.util.ArrayList;
import java.util.List;
import com.oddlabs.tt.gui.GUIRoot;

import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.landscape.NotificationListener;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.model.Selectable;

public final strictfp class NotificationManager {
	private final List attack_notifies = new ArrayList();
	private final List notifies = new ArrayList();
	private final GUIRoot gui_root;
	private Notification latest_notification = null;

	public NotificationManager(GUIRoot gui_root) {
		this.gui_root = gui_root;
	}

	public final Notification getLatestNotification() {
		return latest_notification;
	}
	
	public final void newAttackNotification(AnimationManager manager, Selectable target, Player local_player) {
		for (int i = 0; i < attack_notifies.size(); i++) {
			AttackNotification current = (AttackNotification)attack_notifies.get(i);
			if (current.contains(target)) {
				current.restartTimer();
				return;
			}
		}
		addNotification(new AttackNotification(local_player, gui_root, target, this, manager), attack_notifies);
	}

	public final void newSelectableNotification(Selectable s, AnimationManager manager, Player local_player) {
		newNotification(manager, local_player, s.getPositionX(), s.getPositionY(), 0f, 1f, 0f, false);
	}

	public final void newBeacon(AnimationManager manager, Player local_player, float x, float y) {
		newNotification(manager, local_player, x, y, 0f, 0f, 1f, true);
	}

	private final void newNotification(AnimationManager manager, Player local_player, float x, float y, float r, float g, float b, boolean show_always) {
		addNotification(new Notification(local_player.getWorld(), gui_root, x, y, this, r, g, b, local_player.getRace().getBuildingNotificationAudio(), show_always, manager), notifies);
	}
	
	private final void addNotification(Notification notification, List list) {
		list.add(notification);
		latest_notification = notification;
	}

	public final void removeAttackNotification(AttackNotification current) {
		attack_notifies.remove(current);
	}
	
	public final void removeNotification(Notification current) {
		notifies.remove(current);
	}
}
