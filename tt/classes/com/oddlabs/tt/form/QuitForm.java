package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.gui.GUIRoot;

public final strictfp class QuitForm extends QuestionForm {
	private final static String getI18N(String key) {
		ResourceBundle bundle = ResourceBundle.getBundle(QuitForm.class.getName());
		return Utils.getBundleString(bundle, key);
	}

	public QuitForm(GUIRoot gui_root) {
		this(gui_root, true);
	}
	
	public QuitForm(final GUIRoot gui_root, boolean show_quit_screen) {
		super(!PeerHub.isWaitingForAck() ? getI18N("confirm_quit") : getI18N("confirm_quit_waiting_for_ack"), show_quit_screen ?
				(MouseClickListener)new MouseClickListener() {
					public final void mouseClicked(int button, int x, int y, int clicks) {
						Renderer.shutdownWithQuitScreen(gui_root);
					}
				}
				:
				(MouseClickListener)new MouseClickListener() {
					public final void mouseClicked(int button, int x, int y, int clicks) {
						Renderer.shutdown();
					}
				});
	}
}
