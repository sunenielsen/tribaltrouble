package com.oddlabs.tt.render;

import com.oddlabs.tt.model.*;
import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.util.ToolTip;
import com.oddlabs.tt.gui.ToolTipBox;
import com.oddlabs.tt.gui.Icons;
import com.oddlabs.util.Quad;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.model.behaviour.Controller;
import com.oddlabs.tt.model.behaviour.GatherController;

import java.util.ResourceBundle;

final strictfp class ToolTipAdapter implements ToolTipVisitor, ToolTip {
	private final static Quad[] icon = new Quad[1];

	private final ModelToolTip model;
	private final Player local_player;
	private ToolTipBox tool_tip_box;

	ToolTipAdapter(ModelToolTip model, Player local_player) {
		this.local_player = local_player;
		this.model = model;
	}

	private void visitPlayer(Player player) {
		tool_tip_box.append(player.getPlayerInfo().getName());
		tool_tip_box.append(" - ");
		//      tool_tip_box.append(team_tip);
		//      tool_tip_box.append(" ");
		//      if (Settings.getSettings().inDeveloperMode()) {
		//          tool_tip_box.append("total_units=");
		//          tool_tip_box.append(unit_count.getNumSupplies());
		//          tool_tip_box.append(" ");
		//      }
	}

	private void visitSelectable(Selectable selectable) {
		assert !selectable.isDead();
		visitPlayer(selectable.getOwner());
		/*      if (Settings.getSettings().developer_mode) {
				if (getCurrentBehaviour() instanceof WalkBehaviour)
				((WalkBehaviour)getCurrentBehaviour()).appendToolTip(tool_tip_box);
				else*/
		//tool_tip_box.append(getPrimaryController().getClass().getName());
		//}
	}

	public final void appendToolTip(ToolTipBox tool_tip) {
		tool_tip_box = tool_tip;
		model.visit(this);
	}

	public final void visitSceneryModel(SceneryModel model) {
		String name = model.getName();
		if (name != null)
			tool_tip_box.append(name);
	}

	public final void visitSupply(Supply model) {
		tool_tip_box.append(Utils.getBundleString(ResourceBundle.getBundle(model.getClass().getName()), "name"));
		tool_tip_box.append(Icons.getIcons().getToolTipIcon(model.getClass()));
	}

	public final void visitBuilding(Building building) {
		visitSelectable(building);
		tool_tip_box.append(building.getTemplate().getName());
		Quad[] watch = Icons.getIcons().getWatch();
		icon[0] = watch[(int)((watch.length - 1)*building.getHitPoints()/building.getBuildingTemplate().getMaxHitPoints())];
		tool_tip_box.append(icon);
		//      if (getUnitContainer() != null && Settings.getSettings().developer_mode) {
		//          tool_tip_box.append(" units_in_building ");
		//          tool_tip_box.append(getUnitContainer().getNumSupplies());
		//      }

	}

	public final void visitUnit(Unit unit) {
		visitSelectable(unit);
		String name = unit.getName();
		if (name != null)
			tool_tip_box.append(name);
		else
			tool_tip_box.append(unit.getTemplate().getName());
		Controller c = unit.getPrimaryController();
		if (unit.getAbilities().hasAbilities(Abilities.MAGIC)) {
			Quad[] watch = Icons.getIcons().getWatch();
			int hit_points = unit.getHitPoints();
			int index = (int)((watch.length - 1)*hit_points/unit.getUnitTemplate().getMaxHitPoints());
			assert hit_points > 0 && hit_points <= unit.getUnitTemplate().getMaxHitPoints(): "Invalid hit points";
			icon[0] = watch[index];
			tool_tip_box.append(icon);
		} else if (unit.getOwner() == local_player && c instanceof GatherController) {
			GatherController gc = (GatherController)c;
			tool_tip_box.append(Icons.getIcons().getToolTipIcon(gc.getSupplyType()));
		}
		/*      if (getCurrentBehaviour() instanceof WalkBehaviour)
				((WalkBehaviour)getCurrentBehaviour()).appendToolTip(tool_tip_box);*/

	}
}
