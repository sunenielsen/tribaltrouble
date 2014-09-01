package com.oddlabs.tt.delegate;

import java.util.ResourceBundle;

import com.oddlabs.tt.animation.TimerAnimation;
import com.oddlabs.tt.animation.Updatable;
import com.oddlabs.tt.camera.Camera;
import com.oddlabs.tt.camera.StaticCamera;
import com.oddlabs.tt.form.TutorialForm;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.tutorial.TutorialInGameInfo;
import com.oddlabs.tt.gui.*;

public final strictfp class TutorialOverDelegate extends CameraDelegate implements Updatable {
	private final static float DELAY = 1f;
	private final TimerAnimation delay_timer = new TimerAnimation(this, DELAY);
	private final Group group_buttons;
	private final TutorialInGameInfo tutorial_info;
	private final WorldViewer viewer;

	public TutorialOverDelegate(final WorldViewer viewer, TutorialInGameInfo tutorial_info, Camera old_camera, int tutorial_number) {
		super(viewer.getGUIRoot(), new StaticCamera(old_camera.getState()));
		this.viewer = viewer;
		this.tutorial_info = tutorial_info;
		ResourceBundle bundle = ResourceBundle.getBundle(TutorialOverDelegate.class.getName());
		setDim(LocalInput.getViewWidth(), LocalInput.getViewHeight());
		String tutorial_completed_str = Utils.getBundleString(bundle, "tutorial_completed", new Object[]{new Integer(tutorial_number)});
		Label label = new Label(tutorial_completed_str, Skin.getSkin().getHeadlineFont());
		addChild(label);
		label.setPos((getWidth() - label.getWidth())/2, (getHeight() - label.getHeight())*2/3);

		group_buttons = new Group();
		HorizButton button_next = new HorizButton(Utils.getBundleString(bundle, "next_tutorial"), 170);
		button_next.addMouseClickListener(new StartTutorialListener(tutorial_number + 1));
		HorizButton button_restart = new HorizButton(Utils.getBundleString(bundle, "restart_tutorial"), 170);
		button_restart.addMouseClickListener(new StartTutorialListener(tutorial_number));
		HorizButton button_end = new HorizButton(Utils.getBundleString(bundle, "main_menu"), 170);
		button_end.addMouseClickListener(new MouseClickListener() {
			public final void mouseClicked(int button, int x, int y, int clicks) {
				viewer.close();
				TutorialOverDelegate.this.setDisabled(true);
			} 
		});

		if (tutorial_number < TutorialForm.NUM_TUTORIALS)
			group_buttons.addChild(button_next);
		group_buttons.addChild(button_end);
		group_buttons.addChild(button_restart);

		button_end.place();
		button_restart.place(button_end, LEFT_MID);
		
		if (tutorial_number < TutorialForm.NUM_TUTORIALS)
			button_next.place(button_restart, LEFT_MID);
		group_buttons.compileCanvas();
		group_buttons.setPos((getWidth() - group_buttons.getWidth())/2, (getHeight() - group_buttons.getHeight())*1/3);

		delay_timer.start();
	}

	protected final void renderGeometry() {
		renderBackgroundAlpha();
	}

	public final void update(Object anim) {
		addChild(group_buttons);
		delay_timer.stop();
	}
	
	private final strictfp class StartTutorialListener implements MouseClickListener {
		private final int tutorial_number;
		
		public StartTutorialListener(int tutorial_number) {
			this.tutorial_number = tutorial_number;
		}
		
		public final void mouseClicked(int button, int x, int y, int clicks) {
			if (tutorial_info.setNextTutorial(viewer.getGUIRoot(), tutorial_number))
				viewer.close();
		}
	}
	
}
