package com.oddlabs.tt.model;

import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.landscape.World;

public final strictfp class Plants extends SceneryModel {
	public Plants(World world, float x, float y, float dir_x, float dir_y, SpriteKey sprite_renderer) {
		super(world, x, y, dir_x, dir_y, sprite_renderer);
	}

	protected final void doRegister() {
		if (Globals.INSERT_PLANTS[Settings.getSettings().graphic_detail]) {
			register();
			reinsert();
		}
	}
	
	public final void visit(ElementVisitor visitor) {
		visitor.visitPlants(this);
	}
}
