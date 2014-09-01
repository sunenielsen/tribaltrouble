package com.oddlabs.tt.render;

import com.oddlabs.tt.resource.SpriteFile;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.resource.ResourceDescriptor;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public final strictfp class RenderQueues {
	private final List sprite_renderers = new ArrayList();
	private final List blend_sprite_renderers = new ArrayList();

	private final List sprite_list_lookup = new ArrayList();
	private final List shadow_renderer_lookup = new ArrayList();
	private final Map desc_to_shadow_key = new HashMap();
	private final List texture_lookup = new ArrayList();

	public final TextureKey registerTexture(ResourceDescriptor desc, int index) {
		TextureKey key = new TextureKey(texture_lookup.size());
		Texture[] textures = (Texture[])Resources.findResource(desc);
		texture_lookup.add(textures[index]);
		return key;
	}

	public final TextureKey registerTexture(ResourceDescriptor desc) {
		TextureKey key = new TextureKey(texture_lookup.size());
		texture_lookup.add((Texture)Resources.findResource(desc));
		return key;
	}

	final Texture getTexture(TextureKey key) {
		return (Texture)texture_lookup.get(key.getKey());
	}

	public final ShadowListKey registerRespondRenderer(ResourceDescriptor desc) {
		ShadowListKey key = (ShadowListKey)desc_to_shadow_key.get(desc);
		if (key != null)
			return key;
		ShadowListRenderer renderer = new TargetRespondRenderer(desc);
		return register(desc, renderer);
	}

	private ShadowListKey register(ResourceDescriptor desc, ShadowListRenderer renderer) {
		int index = shadow_renderer_lookup.size();
		shadow_renderer_lookup.add(renderer);
		ShadowListKey key = new ShadowListKey(index);
		desc_to_shadow_key.put(desc, key);
		return key;
	}

	public final ShadowListKey registerSelectableShadowList(ResourceDescriptor desc) {
		ShadowListKey key = (ShadowListKey)desc_to_shadow_key.get(desc);
		if (key != null)
			return key;
		ShadowListRenderer renderer = new SelectableShadowRenderer(desc);
		return register(desc, renderer);
	}

	final ShadowListRenderer getShadowRenderer(ShadowListKey key) {
		return (ShadowListRenderer)shadow_renderer_lookup.get(key.getKey());
	}

	public final SpriteKey register(SpriteFile sprite_file) {
		return register(sprite_file, 0);
	}

	public final SpriteKey register(SpriteFile sprite_file, int tex_index) {
		int index = sprite_list_lookup.size();
		SpriteList sprite_list = (SpriteList)Resources.findResource(sprite_file);
		SpriteRenderer sprite_renderer = new SpriteRenderer(sprite_list, tex_index);
		sprite_list_lookup.add(sprite_renderer);
		registerSpriteRenderer(sprite_renderer);
		return new SpriteKey(index, sprite_list.getBounds(), sprite_list.getAnimationTypes());
	}

	public final SpriteRenderer getRenderer(SpriteKey key) {
		return (SpriteRenderer)sprite_list_lookup.get(key.getKey());
	}

	private void registerSpriteRenderer(SpriteRenderer sprite_renderer) {
		if (sprite_renderer.getSpriteList().getSprite(0).modulateColor()) {
			blend_sprite_renderers.add(sprite_renderer);
		} else {
			sprite_renderers.add(sprite_renderer);
		}
	}

	final void getAllPicks(List pick_list) {
		for (int j = 0; j < sprite_renderers.size(); j++)
			((SpriteRenderer)sprite_renderers.get(j)).getAllPicks(pick_list);
	}

	final void renderAll() {
		for (int j = 0; j < sprite_renderers.size(); j++)
			((SpriteRenderer)sprite_renderers.get(j)).renderAll();
	}

	final void renderBlends() {
		for (int j = 0; j < blend_sprite_renderers.size(); j++)
			((SpriteRenderer)blend_sprite_renderers.get(j)).renderAll();
	}

	final void renderShadows(LandscapeRenderer renderer) {
		for (int j = 0; j < shadow_renderer_lookup.size(); j++)
			((ShadowListRenderer)shadow_renderer_lookup.get(j)).renderShadows(renderer);
	}
}
