package com.oddlabs.tt.tutorial;

import java.util.ResourceBundle;

import com.oddlabs.tt.animation.TimerAnimation;
import com.oddlabs.tt.animation.Updatable;
import com.oddlabs.tt.audio.AudioPlayer;
import com.oddlabs.tt.audio.AudioParameters;
import com.oddlabs.tt.form.TutorialForm;
import com.oddlabs.tt.gui.GUIObject;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.LabelBox;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.delegate.TutorialOverDelegate;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class Tutorial {
	private final static int BORDER_OFFSET = 90;
	
	private final WorldViewer viewer;
	private final TutorialInGameInfo tutorial_info;
	private GUIObject info;
	private TimerAnimation timer;
	private float old_after_done_time;
	
	public Tutorial(WorldViewer viewer, TutorialInGameInfo tutorial_info, TutorialTrigger first_trigger) {
		this.viewer = viewer;
		this.tutorial_info = tutorial_info;
		next0(first_trigger);
		old_after_done_time = first_trigger.getAfterDoneTime();
	}

	final WorldViewer getViewer() {
		return viewer;
	}

	private void removeInfo() {
		if (info != null)
			info.remove();
	}

	final void done(int next_tutorial) {
		timer.stop();
		removeInfo();
		viewer.getGUIRoot().pushDelegate(new TutorialOverDelegate(viewer, tutorial_info, viewer.getGUIRoot().getDelegate().getCamera(), next_tutorial));
	}

	final void next(final TutorialTrigger trigger) {
		timer.stop();
		TimerAnimation delay_timer = new TimerAnimation(viewer.getAnimationManagerLocal(), new Updatable() {
			public final void update(Object anim) {
				((TimerAnimation)anim).stop();
				next0(trigger);
			}
		}, old_after_done_time);
		delay_timer.start();
		old_after_done_time = trigger.getAfterDoneTime();
	}

	private void next0(final TutorialTrigger trigger) {
		removeInfo();
		TimerAnimation delay_timer = new TimerAnimation(viewer.getAnimationManagerLocal(), new Updatable() {
			public final void update(Object anim) {
				((TimerAnimation)anim).stop();
				next1(trigger);
			}
		}, .5f);
		delay_timer.start();
	}

	private void next1(final TutorialTrigger trigger) {
		String text = Utils.getBundleString(ResourceBundle.getBundle(TutorialTrigger.class.getName()), trigger.getTextKey(), trigger.getFormatArgs());
		info =  new LabelBox(text, Skin.getSkin().getEditFont(), 400);
		info.setPos(BORDER_OFFSET, viewer.getGUIRoot().getHeight() - BORDER_OFFSET - info.getHeight());	
		viewer.getGUIRoot().addChild(info);
		viewer.getWorld().getAudio().newAudio(new AudioParameters(viewer.getLocalPlayer().getRace().getBuildingNotificationAudio(), 0f, 0f, 0f, AudioPlayer.AUDIO_RANK_NOTIFICATION, AudioPlayer.AUDIO_DISTANCE_NOTIFICATION, .25f, 1f, 1f, false, true));
		timer = new TimerAnimation(viewer.getAnimationManagerLocal(), new Updatable() {
			public final void update(Object anim) {
				trigger.run(Tutorial.this);
			}
		}, trigger.getCheckInterval());
		timer.start();
	}
}
