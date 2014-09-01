package com.oddlabs.tt.gui;

import com.oddlabs.tt.event.*;
import com.oddlabs.tt.util.*;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.render.UIRenderer;

import org.lwjgl.opengl.*;

final strictfp class Fade {
	private final static float FADE_TIME = 1f;
	
	private final Fadable fadable;
	private final GUIRoot gui_root;
	private final UIRenderer renderer;

	private float time = 0;
	private boolean image_switched = false;
	
	Fade(Fadable fadable, GUIRoot gui_root, UIRenderer renderer) {
		this.fadable = fadable;
		this.gui_root = gui_root;
		this.renderer = renderer;
	}

	public final void animate(GUI gui, float t) {
		time += t;
		if (!image_switched && time >= FADE_TIME/2) {
			image_switched = true;
			gui.switchRoot(gui_root, renderer);
		}
		
		if (time >= FADE_TIME) {
			gui.stopFade();
			if (fadable != null)
				fadable.fadingDone();
		}
	}

	public final void updateChecksum(StateChecksum checksum) {
	}

	protected final void render() {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		float alpha = (float)StrictMath.sin(StrictMath.PI*time/FADE_TIME);
		GL11.glColor4f(0f, 0f, 0f, alpha);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex3f(0, 0, 0f);
		GL11.glVertex3f(LocalInput.getViewWidth(), 0, 0f);
		GL11.glVertex3f(LocalInput.getViewWidth(), LocalInput.getViewHeight(), 0f);
		GL11.glVertex3f(0, LocalInput.getViewHeight(), 0f);
		GL11.glEnd();
		GL11.glColor4f(1f, 1f, 1f, 1f);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
