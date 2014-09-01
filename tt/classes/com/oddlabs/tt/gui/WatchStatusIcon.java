package com.oddlabs.tt.gui;

import com.oddlabs.tt.model.*;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.util.Quad;

import org.lwjgl.opengl.*;

public final strictfp class WatchStatusIcon extends StatusIcon {
	private Building building;

	public WatchStatusIcon(int label_width, Quad icon, String tooltip) {
		super(label_width, icon, tooltip);
	}

	public void setUnitContainerBuilding(Building building) {
		this.building = building;
	}

	protected final void renderGeometry() {
		super.renderGeometry();
		if (!building.isDead() && !building.getChieftainContainer().isTraining() && building.getOwner().getUnitCountContainer().getNumSupplies() < building.getOwner().getWorld().getMaxUnitCount()) {
			// Radeon 9200 problem
			GL11.glEnd();
			GL11.glColor4f(1f, 1f, 1f, .75f);
			GL11.glBegin(GL11.GL_QUADS);
			IconQuad[] watch = Icons.getIcons().getWatch();
			float progress = ((ReproduceUnitContainer)(building.getUnitContainer())).getBuildProgress();
			int index = (int)(progress*(watch.length - 1));
			int x = getWidth() - watch[0].getWidth();
			int y = (getHeight() - watch[0].getHeight())/2;
			x -= 5; // visual HAX
			watch[index].render(x,  y);
			GL11.glEnd();
			GL11.glColor4f(1f, 1f, 1f, 1f);
			GL11.glBegin(GL11.GL_QUADS);
		}
	}
}
