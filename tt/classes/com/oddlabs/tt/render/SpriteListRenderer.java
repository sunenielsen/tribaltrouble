package com.oddlabs.tt.render;

import com.oddlabs.tt.model.Model;

import java.util.List;
import java.util.ArrayList;

final strictfp class SpriteListRenderer {
	private final SpriteList sprite_list;
	private final List render_lists[][];
	private final List respond_render_lists[][];

	SpriteListRenderer(SpriteList sprite_list) {
		this.sprite_list = sprite_list;
		int num_sprites = sprite_list.getNumSprites();
		render_lists = new ArrayList[num_sprites][];
		respond_render_lists = new ArrayList[num_sprites][];
		for (int i = 0; i < num_sprites; i++) {
			Sprite sprite = sprite_list.getSprite(i);
			render_lists[i] = new ArrayList[sprite.getNumTextures()];
			respond_render_lists[i] = new ArrayList[sprite.getNumTextures()];
			for (int j = 0; j < render_lists[i].length; j++) {
				render_lists[i][j] = new ArrayList();
				respond_render_lists[i][j] = new ArrayList();
			}
		}
	}

	public final void addToRenderList(ModelState model, int sprite_index, int tex_index) {
		render_lists[sprite_index][tex_index].add(model);
	}

	public final void addToRespondRenderList(ModelState model, int sprite_index, int tex_index) {
		respond_render_lists[sprite_index][tex_index].add(model);
	}

	public final void getAllPicks(List pick_list, int sprite_index, int tex_index) {
		List render_list = render_lists[sprite_index][tex_index];
		pickFromList(render_list, pick_list);
		render_list.clear();

		render_list = respond_render_lists[sprite_index][tex_index];
		pickFromList(render_list, pick_list);
		render_list.clear();
	}

	private final void pickFromList(List render_list, List pick_list) {
		for (int i = 0; i < render_list.size(); i++) {
			ModelState model = (ModelState)render_list.get(i);
			render_list.set(i, null);
			pick_list.add(model.getModel());
		}
	}

	public final void renderAll(int index, int tex_index) {
		List render_list = render_lists[index][tex_index];
		Sprite sprite = sprite_list.getSprite(index);
		sprite.setup(tex_index, false);
		sprite.renderAll(render_list, tex_index, false);
		sprite.reset(false, sprite.modulateColor());
		render_list.clear();

		render_list = respond_render_lists[index][tex_index];
		if (render_list.size() > 0) {
			sprite.setup(tex_index, true);
			sprite.renderAll(render_list, tex_index, true);
			sprite.reset(true, sprite.modulateColor());
			render_list.clear();
		}
	}
}
