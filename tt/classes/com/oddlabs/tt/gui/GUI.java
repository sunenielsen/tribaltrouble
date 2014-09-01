package com.oddlabs.tt.gui;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.viewer.AmbientAudio;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.delegate.NullDelegate;
import com.oddlabs.tt.render.UIRenderer;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.render.Picker;
import com.oddlabs.tt.util.ToolTip;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.BufferUtils;

public final strictfp class GUI implements Animated {
	private final Languages languages;
	private GUIRoot current_root = createRoot();
	private Fade fade;
	private UIRenderer renderer;

	public GUI(Languages languages) {
		this.languages = languages;
	}

	public final GUIRoot newFade() {
		return newFade(null, null);
	}

	public final GUIRoot newFade(Fadable fadable, UIRenderer renderer) {
		GUIRoot gui_root = createRoot();
		newFade(fadable, gui_root, renderer);
		return gui_root;
	}

	public final void newFade(Fadable fadable, GUIRoot gui_root, UIRenderer renderer) {
		fade = new Fade(fadable, gui_root, renderer);
		LocalEventQueue.getQueue().getManager().registerAnimation(this);
	}

	public final GUIRoot createRoot() {
		GUIRoot gui_root = new GUIRoot(this);
		gui_root.displayChanged();
		return gui_root;
	}

	public final Languages getLanguages() {
		return languages;
	}

	public final void animate(float t) {
		fade.animate(this, t);
	}

	final void stopFade() {
		fade = null;
		LocalEventQueue.getQueue().getManager().removeAnimation(this);
	}

	public final void updateChecksum(StateChecksum checksum) {
	}

	final void switchRoot(GUIRoot gui_root, UIRenderer renderer) {
		if (current_root != null)
			current_root.removeTree();
		current_root = gui_root;
		this.renderer = renderer;
	}

	public final GUIRoot getGUIRoot() {
		return current_root;
	}

	public final void render(AmbientAudio ambient, CameraState frustum_state) {
		boolean clear_color = renderer != null ? renderer.clearColorBuffer() : true;
		if (clear_color)
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		else
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		if (renderer != null)
			renderer.render(ambient, frustum_state, current_root);
		renderGUI();
	}

	public final void pickHover() {
		CameraState camera = getGUIRoot().getDelegate().getCamera().getState();
		GUIObject gui_hit = getGUIRoot().getCurrentGUIObject();
		if (renderer != null)
			renderer.pickHover(gui_hit.canHoverBehind(), camera, LocalInput.getMouseX(), LocalInput.getMouseY());
	}

	public final void renderGUI() {
		current_root.setupGUIView();
		current_root.render();
		if (current_root.showToolTip()) {
			ToolTip tooltip = current_root.getToolTip();
			if (tooltip == null && renderer != null)
				tooltip = renderer.getToolTip();
			if (tooltip != null)
				current_root.renderToolTip(tooltip);
		}
		if (renderer != null)
			renderer.renderGUI(current_root);
		current_root.renderTopmost();
		if (fade != null)
			fade.render();
		GUIRoot.resetGUIView();
	}
}
