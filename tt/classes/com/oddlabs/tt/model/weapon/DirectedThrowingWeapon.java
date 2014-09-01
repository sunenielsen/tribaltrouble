package com.oddlabs.tt.model.weapon;

import com.oddlabs.tt.model.*;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.audio.Audio;

public abstract strictfp class DirectedThrowingWeapon extends ThrowingWeapon {
	public DirectedThrowingWeapon(boolean hit, Unit src, Selectable target, SpriteKey sprite_renderer, Audio throw_sound, Audio[] hit_sounds) {
		super(hit, src, target, sprite_renderer, throw_sound, hit_sounds);
	}

	public final void visit(ElementVisitor visitor) {
		visitor.visitDirectedThrowingWeapon(this);
	}
}
