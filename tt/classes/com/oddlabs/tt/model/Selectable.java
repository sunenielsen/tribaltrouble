package com.oddlabs.tt.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.oddlabs.tt.net.DistributableTable;
import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.gui.ToolTipBox;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.behaviour.Behaviour;
import com.oddlabs.tt.model.behaviour.Controller;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.pathfinder.Occupant;
import com.oddlabs.tt.pathfinder.ScanFilter;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.viewer.Selection;
import com.oddlabs.tt.render.Picker;
import com.oddlabs.tt.util.BoundingBox;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.util.ToolTip;

public abstract class Selectable extends Model implements Target, Animated, ModelToolTip {
	public final static int UNINTERRUPTIBLE = 1;
	public final static int INTERRUPTIBLE = 2;
	public final static int DONE = 3;

	private final Player owner;
	private Behaviour current_behaviour;
	private final Abilities abilities = new Abilities(Abilities.NONE);
	private final Template template;
	private final List controller_stack = new ArrayList();

	private boolean dead;
	private boolean should_decide;
	private int last_behaviour_state;

	private int grid_x;
	private int grid_y;

	protected Selectable(Player owner, Template template) {
		super(owner.getWorld());
		this.owner = owner;
		this.template = template;
	}

	public final float getShadowDiameter() {
		return template.getShadowDiameter();
	}

	public final Template getTemplate() {
		return template;
	}

	public float getDefenseChance() {
		return template.getDefenseChance();
	}

	public final float getNoDetailSize() {
		return template.getNoDetailSize();
	}

	public abstract boolean isEnabled();

	public float getHitOffsetZ() {
		return template.getHitOffsetZ(0);
	}

	public final Controller getCurrentController() {
		return (Controller)controller_stack.get(controller_stack.size() - 1);
	}

	public final void animate(float t) {
		last_behaviour_state = current_behaviour.animate(t);
		switch (last_behaviour_state) {
			case UNINTERRUPTIBLE:
				break;
			case INTERRUPTIBLE:
				if (should_decide)
					decide();
				break;
			case DONE:
				decide();
				break;
			default:
				throw new RuntimeException();
		}
		doAnimate(t);
		owner.getWorld().updateGlobalChecksum(grid_x + grid_y);
	}

	protected void doAnimate(float t) {
	}

	protected final boolean isBlocking() {
		return current_behaviour.isBlocking();
	}

	public final Behaviour getCurrentBehaviour() {
		return current_behaviour;
	}
	
	public final UnitGrid getUnitGrid() {
		return owner.getWorld().getUnitGrid();
	}

	public final void scanVicinity(ScanFilter filter) {
		assert !isDead();
		getUnitGrid().scan(filter, getGridX(), getGridY());
	}

	private final static boolean isAdjacent(UnitGrid unit_grid, int grid_x, int grid_y, Occupant occ) {
		int t_x = occ.getGridX();
		int t_y = occ.getGridY();
		int dx = 0;
		int dy = 0;
		if (t_x > grid_x)
			dx = 1;
		else if (t_x < grid_x)
			dx = -1;
		if (t_y > grid_y)
			dy = 1;
		else if (t_y < grid_y)
			dy = -1;
		assert dx != 0 || dy != 0: "occ = " + occ;
		return unit_grid.getOccupant(grid_x + dx, grid_y + dy) == occ;
	}
	
	public final boolean isCloseEnough(float max_dist, Target target) {
		assert !isDead();
		return isCloseEnough(getUnitGrid(), max_dist, getGridX(), getGridY(), target);
	}

	public final static boolean isCloseEnough(UnitGrid unit_grid, float max_dist, int grid_x, int grid_y, Target target) {
		if (max_dist == 0f && target instanceof Occupant) {
			return isAdjacent(unit_grid, grid_x, grid_y, (Occupant)target);
		} else {
			int dx = grid_x - target.getGridX();
			int dy = grid_y - target.getGridY();
			int dist_squared = dx*dx + dy*dy;
			float max_dist_squared = max_dist*max_dist;
			return dist_squared <= max_dist_squared;
		}
	}

	protected final void register() {
		super.register();
		owner.getWorld().getAnimationManagerGameTime().registerAnimation(this);
		enable();
	}

	public final void pushControllers(Controller[] controllers) {
		assert !isDead();
		for (int i = 0; i < controllers.length; i++)
			controller_stack.add(controllers[i]);
		decide();
	}

	private final void decide() {
		if (last_behaviour_state != UNINTERRUPTIBLE) {
			doDecide();
		} else {
			should_decide = true;
		}
	}

	private final void doDecide() {
		should_decide = false;
		current_behaviour = null;
		getCurrentController().decide();
	}

	protected final void forceDecide() {
		current_behaviour.forceInterrupted();
		last_behaviour_state = INTERRUPTIBLE;
		doDecide();
	}

	public final void pushController(Controller controller) {
		assert !isDead();
		controller_stack.add(controller);
		decide();
	}

	public final void swapController(Controller controller) {
		assert !isDead();
		controller_stack.remove(controller_stack.size() - 1);
		pushController(controller);
	}

	public final void popController() {
		assert !isDead();
		controller_stack.remove(controller_stack.size() - 1);
		decide();
	}

	public final void setBehaviour(Behaviour behaviour) {
		assert !isDead();
		assert last_behaviour_state != UNINTERRUPTIBLE: "Invalid behaviour state";
		current_behaviour = behaviour;
	}

	public final Controller getPrimaryController() {
		assert !isDead();
		if (controller_stack.size() > 1)
			return (Controller)controller_stack.get(1); // Jump over the default controller
		else
			return (Controller)controller_stack.get(0);
	}

	protected final void clearControllerStack() {
		Controller default_controller = (Controller)controller_stack.get(0);
		controller_stack.clear();
		controller_stack.add(default_controller);
	}

	public final void initTarget(Target target, int action, boolean aggressive) {
		assert !isDead();
		if (target == null)
			return;
		clearControllerStack();
		setTarget(target, action, aggressive);
	}

	protected abstract void setTarget(Target target, int action, boolean aggressive);
	public abstract int getAttackPriority();
	public abstract int getStatusValue();

	public final Abilities getAbilities() {
		return abilities;
	}

	public final int getGridX() {
		return grid_x;
	}

	public final int getGridY() {
		return grid_y;
	}

	public final void setGridPosition(int grid_x, int grid_y) {
		assert !isDead();
		assert owner.getWorld().getHeightMap().isGridInside(grid_x, grid_y): grid_x + " " + grid_y + " " + this.grid_x + " " + this.grid_y;
		this.grid_x = grid_x;
		this.grid_y = grid_y;
	}

	public final Player getOwnerNoCheck() {
		return owner;
	}

	public final Player getOwner() {
		return getOwnerNoCheck();
	}

	public void remove() {
		super.remove();
		owner.getWorld().getAnimationManagerGameTime().removeAnimation(this);
	}

	public final void updateChecksum(StateChecksum checksum) {
/*		checksum.update(getGridX());
		checksum.update(getGridY());
		checksum.update(getPositionX());
		checksum.update(getPositionY());*/
	}

	protected final void disable() {
		owner.getWorld().getNotificationListener().unregisterTarget(this);
		owner.getUnits().remove(this);
	}

	protected final void enable() {
		owner.getWorld().getNotificationListener().registerTarget(this);
		owner.getUnits().add(this);
//(new Throwable()).printStackTrace();
	}

	protected void removeDying() {
		disable();
		dead = true;
	}

	public final boolean isDead() {
		return dead;
	}

	public void hit(int damage, float direction_x, float direction_y, Player attacker) {
		if (owner.isEnemy(attacker))
			owner.getWorld().getNotificationListener().newAttackNotification(this);
	}
}
