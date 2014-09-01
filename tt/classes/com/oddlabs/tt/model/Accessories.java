package com.oddlabs.tt.model;

import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.landscape.World;

public abstract strictfp class Accessories extends Model {
	private final SpriteKey sprite_renderer;

	public Accessories(World world, SpriteKey sprite_renderer) {
		super(world);
		this.sprite_renderer = sprite_renderer;
		register();
	}

	public final SpriteKey getSpriteRenderer() {
		return sprite_renderer;
	}

	public final float getShadowDiameter() {
		return 0f;
	}
}
