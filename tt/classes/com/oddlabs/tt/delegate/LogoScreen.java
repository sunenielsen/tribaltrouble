package com.oddlabs.tt.delegate;

import com.oddlabs.tt.animation.TimerAnimation;
import com.oddlabs.tt.animation.Updatable;
import com.oddlabs.tt.camera.Camera;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.camera.StaticCamera;
import com.oddlabs.tt.delegate.CameraDelegate;
import com.oddlabs.tt.gui.Fadable;
import com.oddlabs.tt.gui.GUIImage;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.KeyboardEvent;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.render.UIRenderer;

public final strictfp class LogoScreen extends CameraDelegate implements Updatable {
	private final static float DELAY = 4f;
	private final static int overlay_texture_width = 1024;
	private final static int overlay_texture_height = 1024;
	private final static int overlay_image_width = 800;
	private final static int overlay_image_height = 600;

	private final GUIImage overlay;
	private final TimerAnimation delay_timer = new TimerAnimation(this, DELAY);
	private final GUIRoot client_root;
	private final Fadable fadable;
	private final UIRenderer renderer;
	private boolean fade_started = false;
	
	public LogoScreen(GUIRoot gui_root, Texture logo, Fadable fadable, GUIRoot client_root, UIRenderer renderer) {
		super(gui_root, new StaticCamera(new CameraState()));
		this.client_root = client_root;
		this.fadable = fadable;
		this.renderer = renderer;
		setCanFocus(true);
		setFocusCycle(true);
		
		int screen_width = LocalInput.getViewWidth();
		int screen_height = LocalInput.getViewHeight();
		if (logo != null) { 
			overlay = new GUIImage(screen_width, screen_height, 0f, 0f, (float)overlay_image_width/overlay_texture_width, (float)overlay_image_height/overlay_texture_height, logo);
			overlay.setPos(0, 0);
			addChild(overlay);
		} else
			overlay = null;

		delay_timer.start();
		gui_root.pushDelegate(this);
	}

	public final void displayChangedNotify(int width, int height) {
		setDim(width, height);
		if (overlay != null)
			overlay.setDim(width, height);
	}

	public final void update(Object anim) {
		delay_timer.stop();
		fade();
	}

	public final void fade() {
		if (!fade_started) {
			fade_started = true;
			getGUIRoot().getGUI().newFade(fadable, client_root, renderer);
		}
	}

	public final void switchImage(GUIRoot gui_root) {
		pop();
	}

	public final void fadingDone(GUIRoot gui_root) {
	}
	
	protected final void keyPressed(KeyboardEvent event) {
		fade();
	}

	protected void mouseClicked(int button, int x, int y, int clicks) {
		fade();
	}
}
