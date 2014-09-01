package com.oddlabs.tt.render;

import com.oddlabs.geometry.AnimationInfo;
import com.oddlabs.geometry.SpriteInfo;
import com.oddlabs.tt.resource.SpriteFile;
import com.oddlabs.tt.util.BoundingBox;
import com.oddlabs.util.Utils;

public final strictfp class SpriteList {
	private final BoundingBox[] bounds;
	private final Sprite[] sprites;
	private final int[] type_array;

	public SpriteList(SpriteFile sprite_file) {
		Object[] sprites_and_animations = (Object[])Utils.loadObject(sprite_file.getURL());
		SpriteInfo[] sprite_infos = (SpriteInfo[])sprites_and_animations[0];
		AnimationInfo[] animation_infos = (AnimationInfo[])sprites_and_animations[1];
		bounds = new BoundingBox[animation_infos.length];
		for (int i = 0; i < bounds.length; i++)
			bounds[i] = new BoundingBox();
		sprites = new Sprite[sprite_infos.length];
		float[] cpw_array = new float[animation_infos.length];
		type_array = new int[animation_infos.length];
		int[] animation_length_array = new int[animation_infos.length];
		for (int i = 0; i < animation_infos.length; i++) {
			cpw_array[i] = 1f/animation_infos[i].getWPC();
			type_array[i] = animation_infos[i].getType();
			animation_length_array[i] = animation_infos[i].getFrames().length;
		}
		for (int i = 0; i < sprites.length; i++)
			sprites[i] = new Sprite(sprite_infos[i], animation_infos, sprite_file.hasAlpha(), sprite_file.isLighted(), sprite_file.isCulled(), sprite_file.hasModulateColor(), sprite_file.hasMaxAlpha(), sprite_file.getMipmapCutoff(), bounds, cpw_array, type_array, animation_length_array);
		for (int i = 0; i < bounds.length; i++)
			bounds[i].maximizeXYPlane();
	}

	public final void reset(int index, boolean respond, boolean modulate_tex1) {
		getSprite(index).reset(respond, modulate_tex1);
	}

	public final void render(int index, int animation, float anim_ticks) {
		getSprite(index).render(animation, anim_ticks);
	}

	public final float[] getClearColor() {
		return getSprite(0).getClearColor();
	}

	public final void renderModel(int tex_index) {
		getSprite(0).renderModel(tex_index);
	}

	public final BoundingBox[] getBounds() {
		return bounds;
	}

	public final int getNumSprites() {
		return sprites.length;
	}

	public final Sprite getSprite(int index) {
		return sprites[index];
	}

	public final int[] getAnimationTypes() {
		return type_array;
	}
}
