package com.oddlabs.tt.model;

import java.util.ResourceBundle;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.audio.AudioPlayer;
import com.oddlabs.tt.audio.AudioParameters;
import com.oddlabs.tt.gui.Icons;
import com.oddlabs.tt.gui.ToolTipBox;
import com.oddlabs.tt.landscape.LandscapeResources;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.pathfinder.Movable;
import com.oddlabs.tt.pathfinder.PathTracker;
import com.oddlabs.tt.pathfinder.Region;
import com.oddlabs.tt.pathfinder.TargetTrackerAlgorithm;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.util.Utils;

public final strictfp class RubberSupply extends SupplyModel implements Animated, Movable {
	private final static float MIN_TREE_FALL_HEIGHT = 4f;
	private final static float MAX_TREE_FALL_HEIGHT = 8f;

	private final static int ANIMATION_IDLING = 0;
	private final static int ANIMATION_PECKING = 1;
	private final static int ANIMATION_DYING = 2;
	private final static int ANIMATION_RUNNING = 3;
	private final static int ANIMATION_FLYING = 4;

	private final static float[] ANIMATION_SPEEDS;
	
	private final static int INITIAL_SUPPLIES = 1;
	private final static float METERS_PER_SECOND = 8f;
	private final static int MAX_MOVE_GRIDS = 5;

	private final PathTracker path_tracker;
	private final int start_grid_x;
	private final int start_grid_y;
	private final float spawn_x;
	private final float spawn_y;
	private final float spawn_z;

	private final RubberGroup group;

	private float anim_time = 0;
	private int animation = ANIMATION_IDLING;
	private boolean is_hit = false;
	private boolean spawning;
	private float offset_z;

	static {
		float SPEED_IDLE = 1f/(50f/25f);
		float SPEED_PECK = 1f/(120f/50f);
		float SPEED_DIE = 1f/(150f/50f);
		float SPEED_MOVE = METERS_PER_SECOND;
		ANIMATION_SPEEDS = new float[]{SPEED_IDLE, SPEED_PECK, SPEED_DIE, SPEED_MOVE, SPEED_MOVE};
	}
	
	public RubberSupply(World world, SpriteKey sprite_renderer, float size, int grid_x, int grid_y, float x, float y, float rotation, RubberGroup group, float spawn_x, float spawn_y) {
		super(world, sprite_renderer, size, grid_x, grid_y, x, y, rotation, INITIAL_SUPPLIES, false);
		this.path_tracker = new PathTracker(world.getUnitGrid(), this);
		this.group = group;
		start_grid_x = grid_x;
		start_grid_y = grid_y;
		this.spawn_x = spawn_x;
		this.spawn_y = spawn_y;
		this.spawn_z = offset_z = world.getRandom().nextFloat()*(MAX_TREE_FALL_HEIGHT - MIN_TREE_FALL_HEIGHT) + MIN_TREE_FALL_HEIGHT;
		spawning = true;
		float dx = x - spawn_x;
		float dy = y - spawn_y;
		float inv_len = 1f/(float)StrictMath.sqrt(dx*dx + dy*dy);
		setDirection(dx*inv_len, dy*inv_len);
		setNewAnimation(ANIMATION_FLYING);
	}
	
	protected final float getZError() {
		return getLandscapeError();
	}

	public final float getShadowDiameter() {
		return 1.2f;
	}

	public final void animateSpawn(float t, float progress) {
		anim_time += ANIMATION_SPEEDS[animation]*t;
		float x = spawn_x + (UnitGrid.coordinateFromGrid(getGridX()) - spawn_x)*progress;
		float y = spawn_y + (UnitGrid.coordinateFromGrid(getGridY()) - spawn_y)*progress;
		setPosition(x, y);
		offset_z = spawn_z - spawn_z*progress*progress;
		reinsert();
	}

	public final void spawnComplete() {
		offset_z = 0;
		spawning = false;
		setNewAnimation(ANIMATION_IDLING);
	}
	
	public final Supply respawn() {
		throw new RuntimeException();
	}

	public final PathTracker getTracker() {
		return path_tracker;
	}

	public boolean isMoving() {
		return false;
	}

/*	public void moveNextAnimate() {
		throw new RuntimeException("Chickens should not rotate.");
	}
*/
	public final void free() {
		getWorld().getUnitGrid().freeGrid(getGridX(), getGridY(), this);
	}
	
	public final void occupy() {
		getWorld().getUnitGrid().occupyGrid(getGridX(), getGridY(), this);
	}

	public final void setGridPosition(int grid_x, int grid_y) {
		Region current_region = getWorld().getUnitGrid().getRegion(getGridX(), getGridY());
		Region new_region = getWorld().getUnitGrid().getRegion(grid_x, grid_y);
		if (current_region != new_region) {
			current_region.unregisterObject(getClass(), this);
			new_region.registerObject(getClass(), this);
		}
		super.setGridPosition(grid_x, grid_y);
	}
	
	public final void markBlocking() {
	}

	public final boolean isHit() {
		return is_hit;
	}

	public final void animate(float t) {
		if (spawning)
			return;
		anim_time += ANIMATION_SPEEDS[animation]*t;
		if (animation == ANIMATION_FLYING || animation == ANIMATION_RUNNING) {
			fly(t);
		} else if (!is_hit && anim_time >= 1f) {
			float random = getWorld().getRandom().nextFloat();
			if (random < .75) {
				setNewAnimation(ANIMATION_IDLING);
				if (random < .05)
					getWorld().getAudio().newAudio(new AudioParameters(getWorld().getLandscapeResources().getBirdIdleSound(getWorld().getRandom()), getPositionX(), getPositionY(), getPositionZ(),
							AudioPlayer.AUDIO_RANK_CHICKEN,
							AudioPlayer.AUDIO_DISTANCE_CHICKEN,
							AudioPlayer.AUDIO_GAIN_CHICKEN_IDLE,
							AudioPlayer.AUDIO_RADIUS_CHICKEN_IDLE));
			} else if (random < .85) {
				// fly
				int new_grid_x = start_grid_x + (int)((getWorld().getRandom().nextFloat()*2 - 1) * MAX_MOVE_GRIDS);
				int new_grid_y = start_grid_y + (int)((getWorld().getRandom().nextFloat()*2 - 1) * MAX_MOVE_GRIDS);
				Target target = getWorld().getUnitGrid().findGridTargets(new_grid_x, new_grid_y, 1, false)[0];
				path_tracker.setTarget(new TargetTrackerAlgorithm(getWorld().getUnitGrid(), 0f, target));
				float move_random = getWorld().getRandom().nextFloat();
				if (move_random < .25f) {
					setNewAnimation(ANIMATION_FLYING);
					getWorld().getAudio().newAudio(new AudioParameters(getWorld().getLandscapeResources().getBirdPeckSound(), getPositionX(), getPositionY(), getPositionZ(),
							AudioPlayer.AUDIO_RANK_CHICKEN,
							AudioPlayer.AUDIO_DISTANCE_CHICKEN,
							AudioPlayer.AUDIO_GAIN_CHICKEN_PECK,
							AudioPlayer.AUDIO_RADIUS_CHICKEN_PECK));
				} else {
					setNewAnimation(ANIMATION_RUNNING);
				}
			} else {
				setNewAnimation(ANIMATION_PECKING);
				if (random > .98f)
					getWorld().getAudio().newAudio(new AudioParameters(getWorld().getLandscapeResources().getBirdPeckSound(), getPositionX(), getPositionY(), getPositionZ(),
							AudioPlayer.AUDIO_RANK_CHICKEN,
							AudioPlayer.AUDIO_DISTANCE_CHICKEN,
							AudioPlayer.AUDIO_GAIN_CHICKEN_PECK,
							AudioPlayer.AUDIO_RADIUS_CHICKEN_PECK));
				
			}
		}
	}

	private final void fly(float t) {
		int state = path_tracker.animate(METERS_PER_SECOND*t);
		switch (state) {
			case PathTracker.OK:
			case PathTracker.OK_INTERRUPTIBLE:
				return;
			case PathTracker.DONE:
			case PathTracker.BLOCKED:
			case PathTracker.SOFTBLOCKED:
				setNewAnimation(ANIMATION_IDLING);
				return;
			default:
				throw new RuntimeException("Invalid tracker state: " + state);
		}
	}

	public final float getOffsetZ() {
		return offset_z;
	}
	
	private void setNewAnimation(int animation_index) {
		anim_time = 0;
		animation = animation_index;
	}

	public final int getAnimation() {
		return animation;
	}

	public final float getAnimationTicks() {
		return anim_time;
	}

	public final boolean hit() {
		if (!is_hit) {
			is_hit = true;
			setNewAnimation(ANIMATION_DYING);
			getWorld().getAudio().newAudio(new AudioParameters(getWorld().getLandscapeResources().getBirdDeathSound(), getPositionX(), getPositionY(), getPositionZ(),
					AudioPlayer.AUDIO_RANK_DEATH,
					AudioPlayer.AUDIO_DISTANCE_DEATH,
					AudioPlayer.AUDIO_GAIN_CHICKEN_DEATH,
					AudioPlayer.AUDIO_RADIUS_CHICKEN_DEATH));
			group.remove(this);
		}
		return super.hit();
	}

	protected final void register() {
		super.register();
		getWorld().getAnimationManagerGameTime().registerAnimation(this);
	}

	protected void remove() {
		getWorld().getAnimationManagerGameTime().removeAnimation(this);
		super.remove();
	}

	public final void visit(ElementVisitor visitor) {
		visitor.visitRubberSupply(this);
	}

	public final void updateChecksum(StateChecksum checksum) {
	}
}
