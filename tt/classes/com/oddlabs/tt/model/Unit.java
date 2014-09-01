package com.oddlabs.tt.model;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.oddlabs.geometry.AnimationInfo;
import com.oddlabs.tt.audio.AudioPlayer;
import com.oddlabs.tt.audio.AudioParameters;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.gui.Icons;
import com.oddlabs.tt.gui.ToolTipBox;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.landscape.LandscapeTarget;
import com.oddlabs.tt.model.behaviour.Controller;
import com.oddlabs.tt.model.behaviour.DefendController;
import com.oddlabs.tt.model.behaviour.DieBehaviour;
import com.oddlabs.tt.model.behaviour.DieController;
import com.oddlabs.tt.model.behaviour.EnterController;
import com.oddlabs.tt.model.behaviour.GatherController;
import com.oddlabs.tt.model.behaviour.HuntController;
import com.oddlabs.tt.model.behaviour.IdleController;
import com.oddlabs.tt.model.behaviour.MagicController;
import com.oddlabs.tt.model.behaviour.PlaceBuildingController;
import com.oddlabs.tt.model.behaviour.RepairController;
import com.oddlabs.tt.model.behaviour.StunController;
import com.oddlabs.tt.model.behaviour.WalkBehaviour;
import com.oddlabs.tt.model.behaviour.WalkController;
import com.oddlabs.tt.model.weapon.WeaponFactory;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.particle.BalancedParametricEmitter;
import com.oddlabs.tt.particle.StunFunction;
import com.oddlabs.tt.pathfinder.Movable;
import com.oddlabs.tt.pathfinder.Occupant;
import com.oddlabs.tt.pathfinder.PathTracker;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.util.Target;
import com.oddlabs.util.Quad;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

public strictfp class Unit extends Selectable implements Occupant, Movable {
	private final static float IDLE_SPEED = 1f/2.5f;
	private final static float TRANSPORT_SPEED_SCALE = 4f/5f;

	private final static int PENALTY_INCREMENT = 3;
	private final static int INITIAL_PATH_PENALTY = 5;
	private final static float[] MAX_MAGIC_ENERGY = new float[]{40f, 70f};

	private final static int ANIMATION_IDLING = 0;
	public final static int ANIMATION_MOVING = 1;
	public final static int ANIMATION_THROWING = 2;
	public final static int ANIMATION_DYING = 3;
	public final static int ANIMATION_MAGIC = 4;
	public final static int ANIMATION_THOR = 5;
	public final static int SPEAR_RELEASE_FRAME = 29;

	private final static Quad[] icon = new Quad[1];

	private final UnitSupplyContainer supply_container;
	private final String name;
	private final PathTracker path_tracker;
	private final float[] magic_energy = new float[2];
	private int last_magic_index = -1;

	private BalancedParametricEmitter stun_marker;
	private int hit_points;
	private int animation;
	private float anim_speed;
	private float anim_time;
	private int path_penalty;
	private boolean mounted;
	private float mount_offset = 0;
	private Building mounted_building;
	private float range_bonus;

	public Unit(Player owner, float x, float y, Target rally_point, UnitTemplate unit_template) {
		this(owner, x, y, rally_point, unit_template, null);
	}

	public Unit(Player owner, float x, float y, Target rally_point, UnitTemplate unit_template, String name) {
		this(owner, x, y, rally_point, unit_template, name, true);
	}

	public Unit(Player owner, float x, float y, Target rally_point, UnitTemplate unit_template, String name, boolean notify_by_chieftain) {
		this(owner, x, y, rally_point, unit_template, name, notify_by_chieftain, false);
	}

	public Unit(Player owner, float x, float y, Target rally_point, UnitTemplate unit_template, String name, boolean notify_by_chieftain, boolean grid_targets_only) {
		super(owner, unit_template);
		this.name = name;
		getAbilities().addAbilities(unit_template.getAbilities());
		register();
		hit_points = unit_template.getMaxHitPoints();
		this.path_tracker = new PathTracker(getUnitGrid(), this);
		UnitSupplyContainerFactory factory = unit_template.getUnitSupplyContainerFactory();
		if (factory != null)
			supply_container = (UnitSupplyContainer)factory.createContainer(this);
		else
			supply_container = null;

		findInitialPosition(x, y, grid_targets_only);
		pushController(new IdleController(this, new AttackScanFilter(getOwner(), AttackScanFilter.UNIT_RANGE), true));
		if (!getAbilities().hasAbilities(Abilities.MAGIC)) {
			int result = getOwner().getUnitCountContainer().increaseSupply(1);
			assert (result == 1): "No room for new unit in player unit container.";
		} else if (notify_by_chieftain) {
			owner.getWorld().getNotificationListener().newSelectableNotification(this);
		}
		if (rally_point != null) {
			Target unit_target;
			if (rally_point instanceof LandscapeTarget) {
				UnitGrid grid = getUnitGrid();
				List temp_occupants = new ArrayList();
				Set units = getOwner().getUnits().getSet();
				Iterator it = units.iterator();
				while (it.hasNext()) {
					Selectable s = (Selectable)it.next();
					if (s.getCurrentController() instanceof WalkController) {
						Target target = ((WalkController)s.getCurrentController()).getTarget();
						if (!grid.isGridOccupied(target.getGridX(), target.getGridY())) {
							grid.occupyGrid(target.getGridX(), target.getGridY(), this);
							temp_occupants.add(target);
						}
					}
				}
				unit_target = grid.findGridTargets(rally_point.getGridX(), rally_point.getGridY(), 1, true)[0];
				for (int i = 0; i < temp_occupants.size(); i++) {
					Target target = (Target)temp_occupants.get(i);
					grid.freeGrid(target.getGridX(), target.getGridY(), this);
				}
			} else
				unit_target = rally_point;
				
			boolean aggressive = unit_template.getAbilities().hasAbilities(Abilities.THROW);
			setTarget(unit_target, Target.ACTION_DEFAULT, aggressive);
		}
	}

	protected final float getZError() {
		return getLandscapeError();
	}

	public final void visit(ElementVisitor visitor) {
		visitor.visitUnit(this);
	}

	public final UnitTemplate getUnitTemplate() {
		return (UnitTemplate)getTemplate();
	}

	public final UnitSupplyContainer getSupplyContainer() {
		return supply_container;
	}

	public final String toString() {
		if (!isDead())
			return "Unit: " + hashCode() + " | getOwner() = " + getOwner() + " | mounted = " + mounted + " | getGridX() = " + getGridX() + " | getGridY() = " + getGridY();
		else
			return super.toString();
	}

	private final void findInitialPosition(float x, float y, boolean grid_targets_only) {
		UnitGrid unit_grid = getUnitGrid();
		Target reserved_target = unit_grid.findGridTargets(UnitGrid.toGridCoordinate(x), UnitGrid.toGridCoordinate(y), 1, grid_targets_only)[0];
		setGridPosition(reserved_target.getGridX(), reserved_target.getGridY());
		setPosition(reserved_target.getPositionX(), reserved_target.getPositionY());
		occupy();
		reinsert();
	}

	public final int getStatusValue() {
		int tower_factor = 1;
		if (mounted)
			tower_factor = 3;
		return getUnitTemplate().getStatusValue()*tower_factor;
	}

	public final void increaseRange(float amount) {
		assert !isDead();
		range_bonus += amount;
	}

	public final int getAttackPriority() {
		assert !isDead();
		if (getAbilities().hasAbilities(Abilities.BUILD))
			return AttackScanFilter.PRIORITY_PEON;
		else
			return AttackScanFilter.PRIORITY_WARRIOR;
	}

	public final void visit(ToolTipVisitor visitor) {
		visitor.visitUnit(this);
	}

	public final String getName() {
		return name;
	}
	
	public final int getHitPoints() {
		return hit_points;
	}

	public final void unmount() {
		assert !isDead();
		clearControllerStack();
		swapController(new IdleController(this, new AttackScanFilter(getOwner(), AttackScanFilter.UNIT_RANGE), true));
		mounted = false;
		mount_offset = 0;
		enable();
		findInitialPosition(getPositionX(), getPositionY(), true);
	}

	public final void mount(Building building) {
		assert !isDead();
		mounted_building = building;
		mount_offset = building.getBuildingTemplate().getMountOffset();
		disable();
		free();
		setPosition(building.getPositionX(), building.getPositionY());
		mounted = true;
		clearControllerStack();
		swapController(new IdleController(this, new AttackScanFilter(getOwner(), AttackScanFilter.TOWER_RANGE), false));
	}

	public final boolean isMounted() {
		return mounted;
	}

	public final boolean isEnabled() {
		return !isDead() && !mounted;
	}

	public final float getMetersPerSecond() {
		assert !isDead();
		if (getAbilities().hasAbilities(Abilities.HARVEST) && supply_container.getNumSupplies() > 0)
			return TRANSPORT_SPEED_SCALE*getUnitTemplate().getMetersPerSecond();
		else
			return getUnitTemplate().getMetersPerSecond();
	}

	public final void aimAtTarget(Target target) {
		assert !isDead();
		float dx = target.getPositionX() - getPositionX();
		float dy = target.getPositionY() - getPositionY();
		float dir_len_inv = 1f/(float)StrictMath.sqrt(dx*dx + dy*dy);
		dx *= dir_len_inv;
		dy *= dir_len_inv;
		setDirection(dx, dy);
	}

	public final void switchToIdleAnimation() {
		assert !isDead();
		switchAnimation(IDLE_SPEED, ANIMATION_IDLING);
	}

	public final WeaponFactory getWeaponFactory() {
		assert !isDead();
		return getUnitTemplate().getWeaponFactory();
	}

	public final float getRange(Target target) {
		assert !isDead();
		return getWeaponFactory().getRange() + range_bonus + target.getSize();
	}

	public final float getSize() {
		assert !isDead();
		return 1.9f;
	}

	public final SpriteKey getSpriteRenderer() {
		return getUnitTemplate().getSpriteRenderer();
	}

	public final void doAnimate(float t) {
		anim_time += anim_speed*t;
		if (isDead() || mounted)
			reinsert();
		getOwner().getWorld().updateGlobalChecksum(animation);

		if (getAbilities().hasAbilities(Abilities.MAGIC)) {
			for (int i = 0; i < magic_energy.length; i++) {
				increaseMagicEnergy(i, t);
			}
		}
	}

	public final void increaseMagicEnergy(int index, float amount) {
		magic_energy[index] += amount;
		if (magic_energy[index] > MAX_MAGIC_ENERGY[index]) {
			magic_energy[index] = MAX_MAGIC_ENERGY[index];
		}
	}

	public final PathTracker getTracker() {
		assert !isDead();
		return path_tracker;
	}

	public final void markBlocking() {
		assert !isDead();
		path_penalty = StrictMath.min(path_penalty + PENALTY_INCREMENT, STATIC - 1); // never gets STATIC
	}

	public final int getPenalty() {
		assert !isDead();
		if (isBlocking())
			return Occupant.STATIC;
		else
			return path_penalty;
	}

	protected final void removeDying() {
		if (getAbilities().hasAbilities(Abilities.MAGIC)) {
			getOwner().setActiveChieftain(null);
		}
		free();
		if (!getAbilities().hasAbilities(Abilities.MAGIC)) {
			int result = getOwner().getUnitCountContainer().increaseSupply(-1);
			assert result == -1;
		}
		if (stun_marker != null) {
			stun_marker.done();
			stun_marker = null;
		}
		super.removeDying();
	}

	public final void removeNow() {
		assert !isDead();
		removeDying();
		remove();
	}

	public final void free() {
		assert !isDead();
		UnitGrid unit_grid = getUnitGrid();
		unit_grid.freeGrid(getGridX(), getGridY(), this);
		path_penalty = INITIAL_PATH_PENALTY;
	}

	public final void occupy() {
		assert !isDead();
		UnitGrid unit_grid = getUnitGrid();
		unit_grid.occupyGrid(getGridX(), getGridY(), this);

		// stats
		getOwner().unitMoved();
	}

	public final boolean isMoving() {
		return (getCurrentBehaviour() instanceof WalkBehaviour);
	}

/*	public final void moveNextAnimate() {
		WalkBehaviour behaviour = (WalkBehaviour)getCurrentBehaviour();
		behaviour.moveNextAnimate();
	}
*/
	public final void hit(int damage, float direction_x, float direction_y, Player owner) {
		super.hit(damage, direction_x, direction_y, owner);
		if (mounted) {
			mounted_building.hit(damage, direction_x, direction_y, owner);
		} else if (!isDead()) {
			hit_points = StrictMath.max(StrictMath.min(hit_points - damage, getUnitTemplate().getMaxHitPoints()), 0);
			if (hit_points == 0) {
				// stats
				owner.unitKilled();
				getOwner().unitLost();
				
				pushController(new DieController(this));
				forceDecide();
				/*
				new AudioPlayer(getPositionX(), getPositionY(), getPositionZ(),
						RacesResources.getUnitHitSound(),
						AudioPlayer.AUDIO_RANK_DEATH,
						AudioPlayer.AUDIO_DISTANCE_DEATH,
						AudioPlayer.AUDIO_GAIN_DEATH,
						AudioPlayer.AUDIO_RADIUS_DEATH,
						1f + (World.getRandom().nextFloat() - .5f)*getUnitTemplate().getDeathPitch());
				*/
				getOwner().getWorld().getAudio().newAudio(new AudioParameters(getUnitTemplate().getDeathSound(), getPositionX(), getPositionY(), getPositionZ(),
						AudioPlayer.AUDIO_RANK_DEATH,
						AudioPlayer.AUDIO_DISTANCE_DEATH,
						AudioPlayer.AUDIO_GAIN_DEATH,
						AudioPlayer.AUDIO_RADIUS_DEATH,
						1f + (getOwner().getWorld().getRandom().nextFloat() - .5f)*getUnitTemplate().getDeathPitch()));
				setDirection(-direction_x, -direction_y);
				removeDying();
			}
		}
	}

	public final void stun(float time) {
		float x = getPositionX() + getUnitTemplate().getStunX()*getDirectionX() + getUnitTemplate().getStunY()*(-getDirectionY());
		float y = getPositionY() + getUnitTemplate().getStunX()*getDirectionY() + getUnitTemplate().getStunY()*getDirectionX();
		float z = getOwner().getWorld().getHeightMap().getNearestHeight(x, y) + getUnitTemplate().getStunZ() + mount_offset;

		if (stun_marker != null) {
			stun_marker.done();
		}
		stun_marker = createStunStar(x, y, z, time, (float)StrictMath.PI/2);
		pushController(new StunController(this, time));
		forceDecide();
	}

	private final BalancedParametricEmitter createStunStar(float x, float y, float z, float time, float velocity) {
		int num_particles = 5;
		return new BalancedParametricEmitter(getOwner().getWorld(), new StunFunction(.4f, .15f), new Vector3f(x, y, z),
				velocity, 5f, (float)StrictMath.PI*2, (float)StrictMath.PI*2,
				num_particles, 0f, 2f,
				new Vector4f(1f, 1f, 1f, 1f), new Vector4f(0f, 0f, 0f, 0f),
				new Vector3f(.1f, .1f, .1f), new Vector3f(0f, 0f, 0f), time,
				GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, getOwner().getWorld().getRacesResources().getStarTextures(),
				getOwner().getWorld().getAnimationManagerGameTime());
	}

	public final boolean canAttack(Target target, boolean kill_friendly) {
		assert !isDead();
		if (!(target instanceof Selectable) || !getAbilities().hasAbilities(Abilities.ATTACK))
			return false;
		Selectable selectable = (Selectable)target;
		Player target_player = selectable.getOwner();
		return kill_friendly || getOwner().isEnemy(target_player);
	}

	private final boolean canBuild(Target target) {
		if (!(target instanceof Building) || !getAbilities().hasAbilities(Abilities.BUILD))
			return false;
		Building building = (Building)target;
		return !building.isPlaced();
	}

	private final boolean canGather(Target target) {
		return target instanceof Supply && getAbilities().hasAbilities(Abilities.BUILD);
	}

	private final boolean canRepair(Target target, boolean action_repair) {
		if (!(target instanceof Building) || !getAbilities().hasAbilities(Abilities.BUILD))
			return false;
		Building building = (Building)target;
		if (!action_repair && building.getAbilities().hasAbilities(Abilities.SUPPLY_CONTAINER) && building.isComplete())
			return false;
		//return getOwner() == building.getOwner() && building.isPlaced() && building.isDamaged();
		return !getOwner().isEnemy(building.getOwner()) && building.isPlaced() && building.isDamaged();
	}

	private final boolean canEnter(Target target) {
		if (!(target instanceof Building) || getAbilities().hasAbilities(Abilities.MAGIC))
			return false;
		Building building = (Building)target;
		return building.getUnitContainer() != null && getOwner() == building.getOwner() && building.getUnitContainer().canEnter(this);
	}

	public final float getDefenseChance() {
		if (getCurrentController() instanceof StunController)
			return 0;
		else
			return super.getDefenseChance();
	}

	private final void walkToTarget(Target target, boolean scan_attack) {
		Target walkable_target = getUnitGrid().findGridTargets(target.getGridX(), target.getGridY(), 1, false)[0];
		pushController(new WalkController(this, walkable_target, scan_attack));
	}

	public final void setTarget(Target target, int action, boolean aggressive) {
		if (target == this)
			return;
		assert !target.isDead(): "Setting dead target";
		assert !mounted;
		switch (action) {
			case Target.ACTION_DEFAULT:
				if (canBuild(target)) {
					pushController(new PlaceBuildingController(this, (Building)target));
				} else if (canGather(target)) {
					pushController(new GatherController(this, (Supply)target, target.getClass()));
				} else if (canRepair(target, false)) {
					pushController(new RepairController(this, (Building)target));
				} else if (canEnter(target)) {
					pushController(new EnterController(this, (Building)target));
				} else if (canAttack(target, false)) {
					pushController(new HuntController(this, (Selectable)target));
				} else {
					walkToTarget(target, aggressive);
				}
				break;
			case Target.ACTION_MOVE:
				if (canEnter(target)) {
					pushController(new EnterController(this, (Building)target));
				} else {
					walkToTarget(target, false);
				}
				break;
			case Target.ACTION_ATTACK:	
				if (canAttack(target, true)) {
					pushController(new HuntController(this, (Selectable)target));
				} else {
					walkToTarget(target, true);
				}
				break;
			case Target.ACTION_GATHER_REPAIR:
				if (canGather(target)) {
					pushController(new GatherController(this, (Supply)target, target.getClass()));
				} else if (canRepair(target, true)) {
					pushController(new RepairController(this, (Building)target));
				}
				break;
			case Target.ACTION_DEFEND:
				pushController(new DefendController(this, target));
				break;
			default:
				System.out.println("Invalid action: " + action);
				break;
		}
	}

	public final void printDebugInfo() {
		System.out.println("-----------------------------------");
		System.out.println("Primary Controller = " + getPrimaryController());
		if (getAbilities().hasAbilities(Abilities.MAGIC)) {
			System.out.println("Hit Points = " + hit_points);
			System.out.println("Magic Energy 0 = " + magic_energy[0]);
			System.out.println("Magic Energy 1 = " + magic_energy[1]);
			System.out.println("Controller = " + getPrimaryController());
		}
	}

	public final boolean canDoMagic(int magic_index) {
		return !isDead() && magic_index >= 0 && magic_index < RacesResources.NUM_MAGIC && getOwner().canDoMagic(magic_index) && magic_energy[magic_index] == MAX_MAGIC_ENERGY[magic_index];
	}

	public final void doMagic(int magic_index, boolean clear_stack) {
		if (canDoMagic(magic_index)) {
			if (clear_stack)
				clearControllerStack();
			pushController(new MagicController(this, getOwner().getRace().getMagicFactory(magic_index)));
			for (int i = 0; i < magic_energy.length; i++) {
				magic_energy[i] = 0f;
			}
			last_magic_index = magic_index;

			// stats
			getOwner().magicCast();
		}
	}

	public final int getLastMagicIndex() {
		return last_magic_index; // for tutorial
	}

	public final float getMagicProgress(int magic_index) {
		return magic_energy[magic_index]/MAX_MAGIC_ENERGY[magic_index];
	}

	public final void switchAnimation(float anim_speed, int animation) {
		assert !isDead();
		this.anim_speed = anim_speed;
		if (this.animation != animation || getUnitTemplate().getSpriteRenderer().getAnimationType(animation) == AnimationInfo.ANIM_PLAIN) {
			this.animation = animation;
			assert animation != -1;
			this.anim_time = 0f;
		}
	}

	public final int getAnimation() {
		assert animation != -1;
		return animation;
	}

	public final float getAnimationTicks() {
		return anim_time;
	}

	public final float getMountOffset() {
		assert !isDead();
		return mount_offset;
	}

	public final float getOffsetZ() {
		if (mounted)
			return mounted_building.getOffsetZ() + mount_offset;
		else {
			if (isDead()) {
				DieBehaviour die_behaviour = (DieBehaviour)getCurrentBehaviour();
				return die_behaviour.getOffsetZ();
			} else
				return 0;
		}
	}

	public final void debugRender() {
		path_tracker.debugRender();
	}
}
