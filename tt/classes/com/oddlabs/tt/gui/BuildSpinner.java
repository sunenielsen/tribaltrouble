package com.oddlabs.tt.gui;

import com.oddlabs.tt.model.BuildProductionContainer;
import com.oddlabs.tt.model.BuildSupplyContainer;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.model.weapon.IronAxeWeapon;
import com.oddlabs.tt.model.weapon.RockAxeWeapon;
import com.oddlabs.tt.model.weapon.RubberAxeWeapon;
import com.oddlabs.tt.player.PlayerInterface;
import com.oddlabs.util.Quad;

public final strictfp class BuildSpinner extends IconSpinner {
	public final static int INFINITE_LIMIT = 30;

	private final PlayerInterface player_interface;

	private Building current_building;
	private Class type;
	private int num_orders;
	private int order_size;
	private boolean infinite;

	public BuildSpinner(WorldViewer viewer, PlayerInterface player_interface, IconQuad[] icon_quad, String tool_tip, Quad[] tool_tip_icons, String shortcut_key) {
		super(viewer, icon_quad, tool_tip, tool_tip_icons, shortcut_key);
		this.player_interface = player_interface;
	}

	public void setBuildSupplyContainer(Building current_building, Class type) {
		this.current_building = current_building;
		this.type = type;
		if (!current_building.isDead())
			num_orders = current_building.getBuildSupplyContainer(type).getNumOrders();
	}

	public final int computeCount() {
		if (!current_building.isDead()) {
			BuildSupplyContainer build_container = current_building.getBuildSupplyContainer(type);
			int count = StrictMath.min(build_container.getMaxSupplyCount(),
					StrictMath.max(0, build_container.getNumSupplies() + getOrderDiff()));
			infinite = count >= INFINITE_LIMIT;
			return count;
		} else
			return 0;
	}

	public final boolean renderInfinite() {
		return infinite;
	}

	private final void order(int num) {
		if (!current_building.isDead()) {
			if (type == RockAxeWeapon.class) {
				player_interface.buildRockWeapons(current_building, num, infinite);
			} else if (type == IronAxeWeapon.class) {
				player_interface.buildIronWeapons(current_building, num, infinite);
			} else if (type == RubberAxeWeapon.class) {
				player_interface.buildRubberWeapons(current_building, num, infinite);
			} else {
				throw new RuntimeException();
			}
		}
	}

	public final void appendToolTip(ToolTipBox tool_tip_box) {
		if (!isDisabled())
			super.appendToolTip(tool_tip_box);
	}

	protected final float getProgress() {
		if (!current_building.isDead())
			return ((BuildProductionContainer)current_building.getBuildSupplyContainer(type)).getBuildProgress();
		else
			return 0;
	}

	protected final Building getBuilding() {
		return current_building;
	}

	protected final int getOrderDiff() {
		if (!current_building.isDead())
			return num_orders - current_building.getBuildSupplyContainer(type).getNumOrders();
		else
			return 0;
	}

	protected void increase(int amount) {
		order_size += amount;
		num_orders += amount;
	}

	protected void decrease(int amount) {
		order_size -= amount;
		num_orders -= amount;
	}

	protected final void release() {
		order(order_size);
		order_size = 0;
	}

	protected final int getOrderSize() {
		return order_size;
	}

	protected void postRender() {
		if (renderInfinite())
			Icons.getIcons().getInfinite().render(0,  0);		
	}
}
