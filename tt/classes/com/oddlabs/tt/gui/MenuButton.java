package com.oddlabs.tt.gui;

import com.oddlabs.tt.font.*;
import com.oddlabs.tt.event.*;

import org.lwjgl.opengl.*;

public final strictfp class MenuButton extends ButtonObject {
	private final static float SECONDS_PER_HOVER_CYCLE = 1.5f;
	private final static float HOVER_SCALE_FACTOR = 0.06f;

	private final TextLineRenderer text_renderer;
	private final CharSequence text;
	private final float[] color_normal;
	private final float[] color_active;
	
	private float start_hover_time;

	public MenuButton(String caption, float[] color_normal, float[] color_active) {
		this(caption, Skin.getSkin().getHeadlineFont(), color_normal, color_active);
	}

	private MenuButton(CharSequence text, Font font, float[] color_normal, float[] color_active) {
		setDim(font.getWidth(text), font.getHeight());
		this.text = text;
		this.color_normal = color_normal;
		this.color_active = color_active;
		text_renderer = new TextLineRenderer(font);
	}

	private final void scaleHovered() {
		float time = (LocalEventQueue.getQueue().getTime() - start_hover_time)%SECONDS_PER_HOVER_CYCLE;
		float cycle_position = time/SECONDS_PER_HOVER_CYCLE;
		float scale = 1f + HOVER_SCALE_FACTOR*(float)StrictMath.sin(cycle_position*2*StrictMath.PI);
		GL11.glScalef(scale, scale, 1f);
	}

	protected final void renderGeometry(float clip_left, float clip_right, float clip_bottom, float clip_top) {
		GL11.glEnd();
		GL11.glPushMatrix();
		GL11.glTranslatef(getWidth()/2, getHeight()/2, 0);
		clip_left -= getWidth()/2;
		clip_right -= getWidth()/2;
		clip_top -= getHeight()/2;
		clip_bottom -= getHeight()/2;
		if (isActive()) {
			GL11.glColor3f(color_active[0], color_active[1], color_active[2]);
			scaleHovered();
		} else if (isDisabled()) {
			GL11.glColor4f(Label.DISABLED_COLOR[0], Label.DISABLED_COLOR[1], Label.DISABLED_COLOR[2], Label.DISABLED_COLOR[3]);
		} else {
			GL11.glColor3f(color_normal[0], color_normal[1], color_normal[2]);
		}
		GL11.glBegin(GL11.GL_QUADS);

		text_renderer.render(-getWidth()/2, -getHeight()/2, clip_left, clip_right, clip_bottom, clip_top, text);
		GL11.glEnd();
		GL11.glPopMatrix();
		GL11.glColor3f(1f, 1f, 1f);
		GL11.glBegin(GL11.GL_QUADS);
	}

	protected final void mouseEntered() {
		if (!isActive()) {
			start_hover_time = LocalEventQueue.getQueue().getTime()%SECONDS_PER_HOVER_CYCLE;
			setFocus();
		}
	}
}
