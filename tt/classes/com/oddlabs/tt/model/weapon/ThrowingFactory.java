package com.oddlabs.tt.model.weapon;

import com.oddlabs.tt.audio.Audio;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;

public final strictfp class ThrowingFactory extends WeaponFactory {
	private final static Class[] types = new Class[]{boolean.class, Unit.class, Selectable.class, SpriteKey.class, Audio.class, Audio[].class};
	private final Class weapon_type;
	private final SpriteKey weapon_sprite;
	private final Audio throw_sound;
	private final Audio[] hit_sounds;

	public ThrowingFactory(Class weapon_type, float hit_chance, float range, float release_ratio, SpriteKey weapon_sprite, Audio throw_sound, Audio[] hit_sounds) {
		super(hit_chance, range, release_ratio);
		this.weapon_type = weapon_type;
		this.weapon_sprite = weapon_sprite;
		this.throw_sound = throw_sound;
		this.hit_sounds = hit_sounds;
	}

	protected final void doAttack(boolean hit, Unit src, Selectable target) {
		Object[] args = new Object[]{new Boolean(hit), src, target, weapon_sprite, throw_sound, hit_sounds};
		try {
			weapon_type.getConstructor(types).newInstance(args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public final Class getType() {
		return weapon_type;
	}
}
