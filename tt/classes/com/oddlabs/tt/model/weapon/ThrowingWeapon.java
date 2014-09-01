package com.oddlabs.tt.model.weapon;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.audio.Audio;
import com.oddlabs.tt.audio.AudioPlayer;
import com.oddlabs.tt.audio.AbstractAudioPlayer;
import com.oddlabs.tt.audio.AudioParameters;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.Accessories;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.UnitTemplate;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.model.AbstractElementNode;
import com.oddlabs.tt.render.SpriteKey;

public abstract strictfp class ThrowingWeapon extends Accessories implements Animated {
	private final static float GRAVITY = -6*9.82f;
	private final static float NO_DETAIL_SIZE = .5f;

	private final static float OFFSET_X = 1.316f;
	private final static float OFFSET_Y = -.347f;
	private final static float OFFSET_Z = 1.382f;
	private final AbstractAudioPlayer audio_player;
	private final Audio[] hit_sounds;
	private final Player owner;
	private final boolean hit;

	private Selectable target;
	private float start_x;
	private float start_y;
	private float end_x;
	private float end_y;
	private float dir_x;
	private float dir_y;
	private float time_limit;
	private float time;
	private float z_speed;
	private float deterministic_z;

	public ThrowingWeapon(boolean hit, Unit src, Selectable target, SpriteKey sprite_renderer, Audio throw_sound, Audio[] hit_sounds) {
		super(target.getOwner().getWorld(), sprite_renderer);
		this.hit = hit;
		this.hit_sounds = hit_sounds;

		owner = src.getOwner();

		setPosition(src.getPositionX() + OFFSET_X*src.getDirectionX() - OFFSET_Y*src.getDirectionY(), src.getPositionY() + OFFSET_X*src.getDirectionY() - OFFSET_Y*src.getDirectionX());
		deterministic_z = OFFSET_Z + src.getMountOffset();
		
		setTarget(target);
		
		reinsert();
		audio_player = target.getOwner().getWorld().getAudio().newAudio(new AudioParameters(
			throw_sound,
			getPositionX(),
			getPositionY(),
			getPositionZ(),
			AudioPlayer.AUDIO_RANK_WEAPON_ATTACK,
			AudioPlayer.AUDIO_DISTANCE_WEAPON_ATTACK,
			AudioPlayer.AUDIO_GAIN_WEAPON_ATTACK,
			AudioPlayer.AUDIO_RADIUS_WEAPON_ATTACK,
			target.getOwner().getWorld().getRandom().nextFloat()*.2f + .9f));
		target.getOwner().getWorld().getAnimationManagerGameTime().registerAnimation(this);

		// stats
		src.getOwner().weaponThrown();
	}

	public String toString() {
		return "ThrowingWeapon: start_x = " + start_x + " | start_y = " + start_y + " | end_x = " + end_x + " | end_y = " + end_y + " | target = " + target + "  "  + super.toString();
	}

	protected final void setTarget(Selectable target) {
		this.target = target;
		updateDirection();
		calcNumUpdatesAndZSpeed();
	}

	private final void calcNumUpdatesAndZSpeed() {
		start_x = getPositionX();
		start_y = getPositionY();
		updateTarget();
		float dx = end_x - start_x;
		float dy = end_y - start_y;
		float len = (float)StrictMath.sqrt(dx*dx + dy*dy);
		time_limit = len/getMetersPerSecond();
		time = 0;
		float dest_vec_z = owner.getWorld().getHeightMap().getNearestHeight(end_x, end_y) + target.getHitOffsetZ() - (getPositionZ() + deterministic_z);
		z_speed = (dest_vec_z)/time_limit - GRAVITY*time_limit/2f;
	}

	protected abstract float getMetersPerSecond();

	private final void updateTarget() {
		end_x = target.getPositionX();
		end_y = target.getPositionY();
	}
	
	private final void updateDirection() {
		float dx = target.getPositionX() - getPositionX();
		float dy = target.getPositionY() - getPositionY();
		float len = (float)StrictMath.sqrt(dx*dx + dy*dy);
		if (len < .01f)
			len = .01f;
		float len_inv = 1f/len;
		dir_x = dx*len_inv;
		dir_y = dy*len_inv;
		setDirection(dir_x, dir_y);
	}

	public final void updateChecksum(StateChecksum checksum) {
		checksum.update(time);
	}

	public final float getOffsetZ() {
		return deterministic_z;
	}

	public final float getAnimationTicks() {
		return 0;
	}

	public final int getAnimation() {
		return 0;
	}

	public void animate(float t) {
		if (time >= time_limit) {
			hitTarget(hit, owner, target);
			return;
		}
		
		if (hit) {
			updateTarget();
		}
		time += t;
		float progress = time/time_limit;
		
		float x;
		float y;
		if (progress < 1f) {
			x = start_x + (target.getPositionX() - start_x)*progress;
			y = start_y + (target.getPositionY() - start_y)*progress;
		} else {
			x = end_x;
			y = end_y;
		}
		deterministic_z += z_speed*t;
		z_speed += GRAVITY*t;
		setPosition(x, y);
		reinsert();
		audio_player.setPos(getPositionX(), getPositionY(), getPositionZ());
	}

	protected void hitTarget(boolean hit, Player owner, Selectable target) {
		owner.getWorld().getAnimationManagerGameTime().removeAnimation(this);
		audio_player.stop();
		remove();
		if (hit)
			damageTarget(target);
	}

	protected final void damageTarget(Selectable target) {
		if (target instanceof Unit) {
			owner.getWorld().getAudio().newAudio(new AudioParameters(hit_sounds[owner.getWorld().getRandom().nextInt(hit_sounds.length)], target.getPositionX(), target.getPositionY(), target.getPositionZ(),
					AudioPlayer.AUDIO_RANK_DEATH,
					AudioPlayer.AUDIO_DISTANCE_DEATH,
					AudioPlayer.AUDIO_GAIN_DEATH,
					AudioPlayer.AUDIO_RADIUS_DEATH,
					1f + (owner.getWorld().getRandom().nextFloat() - .5f)*((UnitTemplate)target.getTemplate()).getDeathPitch()));
		}
		target.hit(getDamage(), dir_x, dir_y, owner);
	}

	protected abstract int getDamage();

	public final float getZSpeed() {
		return z_speed;
	}

	public final float getNoDetailSize() {
		return NO_DETAIL_SIZE;
	}
}
