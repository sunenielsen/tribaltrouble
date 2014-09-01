package com.oddlabs.tt.model.weapon;

import com.oddlabs.tt.model.*;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.audio.Audio;

public abstract strictfp class RotatingThrowingWeapon extends ThrowingWeapon {
	private float angle = 0;

	public RotatingThrowingWeapon(boolean hit, Unit src, Selectable target, SpriteKey sprite_renderer, Audio throw_sound, Audio[] hit_sounds) {
		super(hit, src, target, sprite_renderer, throw_sound, hit_sounds);
	}

	private final void setAngle(float angle) {
		this.angle = angle;
	}

	public final float getAngle() {
		return angle;
	}

	public final void animate(float t) {
		super.animate(t);
		setAngle(getAngle() + getAngleVelocity()*t);
	}

	protected abstract float getAngleVelocity();

	public final void visit(ElementVisitor visitor) {
		visitor.visitRotatingThrowingWeapon(this);
	}
}
