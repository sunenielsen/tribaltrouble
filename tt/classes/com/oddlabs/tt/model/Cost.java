package com.oddlabs.tt.model;

import com.oddlabs.tt.gui.*;
import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.util.Quad;

public final strictfp class Cost {
	private final Class[] supply_types;
	private final int[] supply_amounts;

	public Cost(Class[] supply_types, int[] supply_amounts) {
		this.supply_types = supply_types;
		this.supply_amounts = supply_amounts;
		assert supply_types.length == supply_amounts.length;
	}
	
	public final Class[] getSupplyTypes() {
		return supply_types;
	}

	public final int[] getSupplyAmounts() {
		return supply_amounts;
	}

	public final Quad[] toIconArray() {
		int size = 0;
		for (int i = 0; i < supply_amounts.length; i++)
			size += supply_amounts[i];
		Quad[] result = new Quad[size];
		int index = 0;
		for (int i = 0; i < supply_types.length; i++) {
			Class type = supply_types[i];
			Quad icon;
			if (type == TreeSupply.class) {
				icon = Icons.getIcons().getTreeStatusIcon();
			} else if (type == RockSupply.class) {
				icon = Icons.getIcons().getRockStatusIcon();
			} else if (type == IronSupply.class) {
				icon = Icons.getIcons().getIronStatusIcon();
			} else if (type == RubberSupply.class) {
				icon = Icons.getIcons().getRubberStatusIcon();
			} else {
				throw new RuntimeException("Wrong supply_type");
			}
			for (int j = 0; j < supply_amounts[i]; j++)
				result[index++] = icon;
		}
		assert index == result.length;
		return result;
	}
}
