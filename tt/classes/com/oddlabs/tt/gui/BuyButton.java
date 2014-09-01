package com.oddlabs.tt.gui;

import java.util.ResourceBundle;

import org.lwjgl.Sys;

import com.oddlabs.tt.form.QuestionForm;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.regclient.ReflexiveRegistrationClient;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.util.Utils;

public final strictfp class BuyButton extends HorizButton {
	public BuyButton(GUIRoot gui_root, String caption, int width) {
		super(caption, width);
		addListener(gui_root, this);
	}

	public final static void addListener(GUIRoot gui_root, ButtonObject button) {
		button.addMouseClickListener(new BuyListener(gui_root));
	}
	
	private final static strictfp class BuyListener implements MouseClickListener {
		private final GUIRoot gui_root;

		public BuyListener(GUIRoot gui_root) {
			this.gui_root = gui_root;
		}

		public final void mouseClicked(int button, int x, int y, int clicks) {
			if (Settings.getSettings().buy_now_only_quit) {
				Renderer.shutdown();
			} else {
				ResourceBundle bundle = ResourceBundle.getBundle(BuyButton.class.getName());
				String buy_quit_message = Utils.getBundleString(bundle, "buy_quit_message");
				gui_root.addModalForm(new QuestionForm(buy_quit_message, new ActionBuyListener()));
			}
		}
	}

	private final static strictfp class ActionBuyListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			Renderer.shutdown();
			Sys.openURL(Settings.getSettings().buy_url);
		}
	}
}
