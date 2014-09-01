package com.oddlabs.tt.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.model.AbstractElementNode;
import com.oddlabs.tt.model.Model;

public final strictfp class SpriteRenderer {
	public final static int HIGH_POLY = 0;
	public final static int LOW_POLY = 1;
	
	private final SpriteList sprite_list;
	private final SpriteListRenderer sprite_list_renderer;
	private final int tex_index;
	private final List no_detail_render_list = new ArrayList();

	public SpriteRenderer(SpriteList sprite_list, int tex_index) {
		this.sprite_list = sprite_list;
		this.tex_index = tex_index;
		sprite_list_renderer = new SpriteListRenderer(sprite_list);
	}

	public final void renderNoDetail(ModelState model) {
		float[] color = model.getTeamColor();
		GL11.glColor3f(color[0], color[1], color[2]);
		GL11.glBegin(GL11.GL_QUADS);
		float x = model.getModel().getPositionX();
		float y = model.getModel().getPositionY();
		float z = model.getModel().getPositionZ();
		float r = model.getModel().getNoDetailSize();
		GL11.glVertex3f(x - r, y - r, z);
		GL11.glVertex3f(x + r, y - r, z);
		GL11.glVertex3f(x + r, y + r, z);
		GL11.glVertex3f(x - r, y + r, z);
		GL11.glEnd();
	}

	public final SpriteList getSpriteList() {
		return sprite_list;
	}

	public final void setupWithColor(int index, FloatBuffer material_color, boolean respond, boolean modulate_tex1) {
		getSpriteList().getSprite(index).setupWithColor(material_color, tex_index, respond, modulate_tex1);
	}

	public final void setup(int index, boolean respond) {
		getSpriteList().getSprite(index).setup(tex_index, respond);
	}

	public final void addToNoDetailList(ModelState model) {
		no_detail_render_list.add(model);
	}

	public final void addToRenderList(int index, ModelState model, boolean respond) {
		index = StrictMath.min(sprite_list.getNumSprites() - 1, index);
		if (respond) {
			sprite_list_renderer.addToRespondRenderList(model, index, tex_index);
		} else {
			sprite_list_renderer.addToRenderList(model, index, tex_index);
		}
	}

	public final int getTriangleCount(int index) {
		index = StrictMath.min(sprite_list.getNumSprites() - 1, index);
		return sprite_list.getSprite(index).getTriangleCount();
	}

	private final void clearRenderLists() {
		no_detail_render_list.clear();
	}

	public final void getAllPicks(List pick_list) {
		for (int i = 0; i < sprite_list.getNumSprites(); i++)
			sprite_list_renderer.getAllPicks(pick_list, i, tex_index);
		for (int i = 0; i < no_detail_render_list.size(); i++) {
			ModelState model = (ModelState)no_detail_render_list.get(i);
			no_detail_render_list.set(i, null);
			pick_list.add(model.getModel());
		}
		clearRenderLists();
	}

	public final void renderAll() {
		for (int i = 0; i < sprite_list.getNumSprites(); i++) {
			sprite_list_renderer.renderAll(i, tex_index);
		}
		setupNoDetail();
		for (int i = 0; i < no_detail_render_list.size(); i++) {
			ModelState model = (ModelState)no_detail_render_list.get(i);
			no_detail_render_list.set(i, null);
			if (Globals.draw_misc)
				renderNoDetail(model);
		}
		finishNoDetail();
		clearRenderLists();
	}
	
	private static void setupNoDetail() {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	private static void finishNoDetail() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
