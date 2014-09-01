package com.oddlabs.tt.model;

import com.oddlabs.tt.net.DistributableTable;
import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.gui.ToolTipBox;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.pathfinder.Occupant;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.camera.CameraState;

public strictfp class SceneryModel extends Model implements Occupant, ModelToolTip, Animated {
	private final SpriteKey sprite_renderer;
	private final float shadow_diameter;
	private final boolean occupy;
	private final String name;
	private final int animation;
	private final float seconds_per_animation_cycle;
	private float anim_time = 0;

	public SceneryModel(World world, float x, float y, float dir_x, float dir_y, SpriteKey sprite_renderer) {
		this(world, x, y, dir_x, dir_y, sprite_renderer, 0f, false, null);
	}

	public SceneryModel(World world, float x, float y, float dir_x, float dir_y, SpriteKey sprite_renderer, float shadow_diameter, boolean occupy, String name) {
		this(world, x, y, dir_x, dir_y, sprite_renderer, shadow_diameter, occupy, name, -1, -1, 0);
	}

	public SceneryModel(World world, float x, float y, float dir_x, float dir_y, SpriteKey sprite_renderer, float shadow_diameter, boolean occupy, String name, int animation, float seconds_per_animation_cycle, float anim_offset) {
		super(world);
		this.sprite_renderer = sprite_renderer;
		this.shadow_diameter = shadow_diameter;
		this.occupy = occupy;
		this.name = name;
		this.animation = animation;
		this.seconds_per_animation_cycle = seconds_per_animation_cycle;
		anim_time = anim_offset;
		setPosition(x, y);
		setDirection(dir_x, dir_y);
		doRegister();
		if (occupy) {
			world.getUnitGrid().occupyGrid(getGridX(), getGridY(), this);
		}
	}

	public final String getName() {
		return name;
	}

	public final float getShadowDiameter() {
		return shadow_diameter;
	}

	protected void doRegister() {
		register();
		reinsert();
		getWorld().getNotificationListener().registerTarget(this);
		if (animation > -1)
			getWorld().getAnimationManagerGameTime().registerAnimation(this);
	}

	public final void remove() {
		if (occupy) {
			getWorld().getUnitGrid().freeGrid(getGridX(), getGridY(), this);
		}
		super.remove();
		getWorld().getNotificationListener().unregisterTarget(this);
		if (animation > -1)
			getWorld().getAnimationManagerGameTime().removeAnimation(this);
	}

	public final void visit(ToolTipVisitor visitor) {
		visitor.visitSceneryModel(this);
	}

	public final float getOffsetZ() {
		return 0;
	}

	public final void animate(float t) {
		anim_time += t/2.5f;
		if (seconds_per_animation_cycle > -1 && anim_time > seconds_per_animation_cycle)
			anim_time = 0;
		reinsert();
	}
		
	public final int getAnimation() {
		if (animation > -1)
			return animation;
		else
			return 0;
	}
	
	public final float getAnimationTicks() {
		if (animation > -1) {
			return anim_time;
		} else {
			return 0;
		}
	}

	public final void updateChecksum(StateChecksum checksum) {
	}

	public final float getNoDetailSize() {
		return 0f;
	}

	public int getPenalty() {
		return Occupant.STATIC;
	}

	public final int getGridX() {
		return UnitGrid.toGridCoordinate(getPositionX());
	}

	public final int getGridY() {
		return UnitGrid.toGridCoordinate(getPositionY());
	}

	public final float getSize() {
		throw new RuntimeException();
	}

	public final boolean isDead() {
		return false;
	}

	public final boolean isOccupying() {
		return occupy;
	}

	public final SpriteKey getSpriteRenderer() {
		return sprite_renderer;
	}

	public void visit(ElementVisitor visitor) {
		visitor.visitSceneryModel(this);
	}
}
