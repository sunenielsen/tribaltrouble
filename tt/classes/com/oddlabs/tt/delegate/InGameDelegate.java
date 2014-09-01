package com.oddlabs.tt.delegate;

import com.oddlabs.tt.camera.Camera;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.camera.StaticCamera;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.render.Picker;
import com.oddlabs.tt.render.LandscapeLocation;
import com.oddlabs.tt.gui.KeyboardEvent;
import com.oddlabs.tt.delegate.InGameMainMenu;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.viewer.Cheat;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.global.Settings;

import org.lwjgl.input.Keyboard;
import java.util.Set;

public abstract strictfp class InGameDelegate extends CameraDelegate {
	private final WorldViewer viewer;

	protected InGameDelegate(WorldViewer viewer, Camera camera) {
		super(viewer.getGUIRoot(), camera);
		this.viewer = viewer;
	}

	private boolean cheat(KeyboardEvent event) {
		// cheats
		Cheat cheat = viewer.getCheat();
		if (!cheat.isEnabled())
			return false;
		LandscapeLocation landscape_hit = new LandscapeLocation();
		viewer.getPicker().pickLocation(getCamera().getState(), landscape_hit);
		float landscape_x = landscape_hit.x;
		float landscape_y = landscape_hit.y;
		switch (event.getKeyCode()) {
			case Keyboard.KEY_F1:
				if (viewer.getLocalPlayer().getUnitCountContainer().getNumSupplies() != viewer.getParameters().getMaxUnitCount()) {
					new Unit(viewer.getLocalPlayer(), landscape_x, landscape_y, null,
							viewer.getLocalPlayer().getRace().getUnitTemplate(Race.UNIT_PEON));
					return true;
				}
				break;
			case Keyboard.KEY_F2:
				if (viewer.getLocalPlayer().getUnitCountContainer().getNumSupplies() != viewer.getParameters().getMaxUnitCount()) {
					new Unit(viewer.getLocalPlayer(), landscape_x, landscape_y, null,
							viewer.getLocalPlayer().getRace().getUnitTemplate(Race.UNIT_WARRIOR_ROCK));
					return true;
				}
				break;
			case Keyboard.KEY_F3:
				if (viewer.getLocalPlayer().getUnitCountContainer().getNumSupplies() != viewer.getParameters().getMaxUnitCount()) {
					new Unit(viewer.getLocalPlayer(), landscape_x, landscape_y, null,
							viewer.getLocalPlayer().getRace().getUnitTemplate(Race.UNIT_WARRIOR_IRON));
					return true;
				}
				break;
			case Keyboard.KEY_F4:
				if (viewer.getLocalPlayer().getUnitCountContainer().getNumSupplies() != viewer.getParameters().getMaxUnitCount()) {
					new Unit(viewer.getLocalPlayer(), landscape_x, landscape_y, null,
							viewer.getLocalPlayer().getRace().getUnitTemplate(Race.UNIT_WARRIOR_RUBBER));
					return true;
				}
				break;
			case Keyboard.KEY_F5:
				if (!viewer.getLocalPlayer().hasActiveChieftain() && !viewer.getLocalPlayer().isTrainingChieftain()) {
					Unit chieftain = new Unit(viewer.getLocalPlayer(), landscape_x, landscape_y, null,
							viewer.getLocalPlayer().getRace().getUnitTemplate(Race.UNIT_CHIEFTAIN));
					viewer.getLocalPlayer().setActiveChieftain(chieftain);
					return true;
				}
				break;
			case Keyboard.KEY_F6:
				viewer.getLocalPlayer().killSelection(viewer.getSelection().getCurrentSelection().filter(Abilities.NONE));
				return true;
			case Keyboard.KEY_F7:
				cheat.draw_trees = !cheat.draw_trees;
				return true;
			case Keyboard.KEY_F8:
				cheat.line_mode = !cheat.line_mode;
				return true;
			default:
				break;
		}

		if (!Settings.getSettings().inDeveloperMode())
			return false;

		switch (event.getKeyCode()) {
			case Keyboard.KEY_I:
				if (event.isControlDown()) {
					Set set = viewer.getSelection().getCurrentSelection().getSet();
					if (set.size() > 0) {
						Selectable s = (Selectable)set.iterator().next();
						if (s instanceof Building) {
							Building building = (Building)s;
							if (!building.isDead() && !building.getAbilities().hasAbilities(Abilities.ATTACK))
								building.printDebugInfo();
						} else if (s instanceof Unit) {
							Unit unit = (Unit)s;
							if (!unit.isDead())
								unit.printDebugInfo();
						}
					}
					return true;
				}
				break;
			default:
				break;
		}
		return false;
	}

	protected void keyPressed(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_ESCAPE:
				getGUIRoot().pushDelegate(new InGameMainMenu(viewer, new StaticCamera(getCamera().getState()),
							viewer.getParameters()));
				break;
			default:
				if (!cheat(event))
					super.keyPressed(event);
				break;
		}
	}

	public final WorldViewer getViewer() {
		return viewer;
	}
}
