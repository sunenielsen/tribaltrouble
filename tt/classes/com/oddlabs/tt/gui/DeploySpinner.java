package com.oddlabs.tt.gui;

import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.DeployContainer;
import com.oddlabs.tt.player.PlayerInterface;
import com.oddlabs.util.Quad;

public final strictfp class DeploySpinner extends IconSpinner {
	private final PlayerInterface player_interface;
	private Class supply_type;
	private int deploy_type;
	private Building current_building;
	private int num_orders = 0;
	private int order_size = 0;

	public DeploySpinner(WorldViewer viewer, PlayerInterface player_interface, IconQuad[] icon_quad, String tool_tip, Quad[] tool_tip_icons, String shortcut_key) {
		super(viewer, icon_quad, tool_tip, tool_tip_icons, shortcut_key);
		this.player_interface = player_interface;
	}

	public void setContainers(Building current_building, int deploy_type, Class supply_type) {
		this.current_building = current_building;
		this.deploy_type = deploy_type;
		this.supply_type = supply_type;
		if (!current_building.isDead())
			num_orders = current_building.getDeployContainer(deploy_type).getNumOrders();
	}

	public final int computeCount() {
		if (current_building != null && !current_building.isDead()) {
			DeployContainer deploy_container = current_building.getDeployContainer(deploy_type);
			return StrictMath.min(deploy_container.getMaxSupplyCount(),
					StrictMath.max(0, deploy_container.getNumSupplies() + getOrderDiff()));
		} else
			return 0;
	}

	public final boolean renderInfinite() {
		return false;
	}

	public final int getOrderDiff() {
		if (current_building != null && !current_building.isDead()) {
			return num_orders - current_building.getDeployContainer(deploy_type).getNumOrders();
		} else {
			return 0;
		}
	}

	private final void order(int num) {
		if (!current_building.isDead())
			player_interface.deployUnits(current_building, deploy_type, num);
	}

	protected final void increase(int amount) {
		if (!current_building.isDead()) {
			int num_units = current_building.getUnitContainer().getNumSupplies();
			int num_supplies = Integer.MAX_VALUE;
			if (supply_type != null) {
				num_supplies = current_building.getSupplyContainer(supply_type).getNumSupplies();
			}

			if (num_units > getOrderDiff() && num_supplies > getOrderDiff()) {
				if (amount > num_units - getOrderDiff()) {
					amount = num_units - getOrderDiff();
				}
				if (supply_type != null && amount > num_supplies - getOrderDiff()) {
					amount = num_supplies - getOrderDiff();
				}
				order_size += amount;
				num_orders += amount;
			}
		}
	}

	protected final void decrease(int amount) {
		if (!current_building.isDead() && computeCount() > 0) {
			int num_units = current_building.getDeployContainer(deploy_type).getNumSupplies();

			if (num_units > -getOrderDiff()/* && num_supplies > -getOrderDiff()*/) {
				if (amount > num_units + getOrderDiff()) {
					amount = num_units + getOrderDiff();
				}
				/*
				if (supply_type != null && amount > num_supplies + getOrderDiff()) {
					amount = num_supplies + getOrderDiff();
				}
				*/
				order_size -= amount;
				num_orders -= amount;
			}
		}
	}

	protected final void release() {
		order(order_size);
		order_size = 0;
	}

	protected final int getOrderSize() {
		return order_size;
	}

	protected final float getProgress() {
		if (!current_building.isDead())
			return current_building.getDeployContainer(deploy_type).getBuildProgress();
		else
			return 0;
	}
}
