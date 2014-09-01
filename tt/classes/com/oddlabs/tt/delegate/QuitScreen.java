package com.oddlabs.tt.delegate;

import java.util.ResourceBundle;

import com.oddlabs.tt.animation.TimerAnimation;
import com.oddlabs.tt.animation.Updatable;
import com.oddlabs.tt.camera.Camera;
import com.oddlabs.tt.gui.ButtonObject;
import com.oddlabs.tt.delegate.CameraDelegate;
import com.oddlabs.tt.gui.Fadable;
import com.oddlabs.tt.gui.GUIImage;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.ImageBuyButton;
import com.oddlabs.tt.gui.KeyboardEvent;
import com.oddlabs.tt.gui.LabelBox;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.Utils;

public final strictfp class QuitScreen extends CameraDelegate implements Updatable {
	private final static float DELAY = 5f;
	private final static int OFFSET = 20;
	private final static int BUY_OFFSET = 10;
	
	private final static int overlay_texture_width = 1024;
	private final static int overlay_texture_height = 1024;
	private final static int overlay_image_width = 800;
	private final static int overlay_image_height = 600;
	private final static String overlay_texture_name = "/textures/gui/quitscreen";

	private final GUIImage overlay;
	private final TimerAnimation delay_timer = new TimerAnimation(this, DELAY);
	private boolean key_pressed = false;
	private boolean time_out = false;
	
	public QuitScreen(GUIRoot gui_root, Camera camera) {
		super(gui_root, camera);
		setCanFocus(true);
		setFocusCycle(true);
		
		int screen_width = LocalInput.getViewWidth();
		int screen_height = LocalInput.getViewHeight();
		overlay = new GUIImage(screen_width, screen_height, 0f, 0f, (float)overlay_image_width/overlay_texture_width, (float)overlay_image_height/overlay_texture_height, overlay_texture_name);
		overlay.setPos(0, 0);
		addChild(overlay);

		ResourceBundle bundle = ResourceBundle.getBundle(QuitScreen.class.getName());
		String text = Utils.getBundleString(bundle, "buy_now") + "\n" +
			Utils.getBundleString(bundle, "incentive0") + "\n" +
			Utils.getBundleString(bundle, "incentive1") + "\n" +
			Utils.getBundleString(bundle, "incentive2") + "\n" +
			Utils.getBundleString(bundle, "incentive3") + "\n" +
			Utils.getBundleString(bundle, "incentive4") + "\n" +
			Utils.getBundleString(bundle, "incentive5");

		LabelBox text_box = new LabelBox(text, Skin.getSkin().getHeadlineFont(), 400);
		text_box.setPos(OFFSET, screen_height - (text_box.getHeight() + OFFSET));
		addChild(text_box);
		
		GUIRoot quit_root = gui_root.getGUI().newFade();

		ButtonObject button_buy = new ImageBuyButton(quit_root);
		addChild(button_buy);
		button_buy.setPos(screen_width - (button_buy.getWidth() + BUY_OFFSET), BUY_OFFSET);
		button_buy.addMouseClickListener(new ResetListener());

		delay_timer.start();

		quit_root.pushDelegate(this);
	}

	public final void displayChangedNotify(int width, int height) {
		setDim(width, height);
		overlay.setDim(width, height);
	}

	public final void update(Object anim) {
		delay_timer.stop();
		time_out = true;
		quit();
	}

	private final void quit() {
		if (key_pressed && time_out)
			Renderer.shutdown();
	}

	protected final void keyPressed(KeyboardEvent event) {
		key_pressed = true;
		quit();
	}

	protected void mouseClicked(int button, int x, int y, int clicks) {
		key_pressed = true;
		quit();
	}

	private final strictfp class ResetListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			key_pressed = false;
		}
	}
}
