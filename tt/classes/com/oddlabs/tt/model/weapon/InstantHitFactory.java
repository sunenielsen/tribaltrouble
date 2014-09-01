package com.oddlabs.tt.model.weapon;

import com.oddlabs.tt.audio.Audio;
import com.oddlabs.tt.audio.AudioPlayer;
import com.oddlabs.tt.audio.AudioParameters;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.UnitTemplate;

public final strictfp class InstantHitFactory extends WeaponFactory {
	private final Audio[] sounds;

	public InstantHitFactory(float hit_chance, float range, float release_ratio, Audio[] sounds) {
		super(hit_chance, range, release_ratio);
		this.sounds = sounds;
	}

	protected final void doAttack(boolean hit, Unit src, Selectable target) {
		int damage = 1;
		if (target instanceof Building && target.getTemplate().getAbilities().hasAbilities(Abilities.ATTACK))
			damage = 6;
		else if (!hit)
			return;
		float dx = target.getPositionX() - src.getPositionX();
		float dy = target.getPositionY() - src.getPositionY();
		float dir_len_inv = 1f/(float)StrictMath.sqrt(dx*dx + dy*dy);
		if (target instanceof Unit) {
			World world = src.getOwner().getWorld();
			world.getAudio().newAudio(new AudioParameters(sounds[world.getRandom().nextInt(sounds.length)], target.getPositionX(), target.getPositionY(), target.getPositionZ(),
					AudioPlayer.AUDIO_RANK_DEATH,
					AudioPlayer.AUDIO_DISTANCE_DEATH,
					AudioPlayer.AUDIO_GAIN_DEATH,
					AudioPlayer.AUDIO_RADIUS_DEATH,
					1f + (world.getRandom().nextFloat() - .5f)*((UnitTemplate)target.getTemplate()).getDeathPitch()));
		}
		target.hit(damage, dx*dir_len_inv, dy*dir_len_inv, src.getOwner());
	}

	public final Class getType() {
		return null;
	}
}
