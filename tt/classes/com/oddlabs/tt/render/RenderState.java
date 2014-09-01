package com.oddlabs.tt.render;

import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.particle.Emitter;
import com.oddlabs.tt.particle.Lightning;
import com.oddlabs.tt.landscape.LandscapeTargetRespond;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.landscape.LandscapeLeaf;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.procedural.GeneratorRing;
import com.oddlabs.tt.model.*;
import com.oddlabs.tt.model.weapon.*;
import com.oddlabs.tt.viewer.Selection;
import com.oddlabs.tt.util.*;

import java.util.*;

import org.lwjgl.opengl.GL11;

public final strictfp class RenderState implements ElementVisitor {
	private final List emitter_queue = new ArrayList();
	private final List lightning_queue = new ArrayList();
	private final SpriteSorter sprite_sorter;
	private final RenderStateCache render_state_cache;
	private final RenderQueues render_queues;
	private final TargetRespondRenderer target_respond_renderer;
	private final SelectableShadowRenderer default_shadow_renderer;
	private final Picker picker;
	private final Selection selection;
	private final LandscapeRenderer landscape_renderer;
	private final Player local_player;

	private boolean picking;
	private boolean visible_override;
	private CameraState camera;

	public RenderState(Player local_player, LandscapeRenderer renderer, SpriteSorter sprite_sorter, RenderQueues render_queues, Picker picker, Selection selection) {
		this.local_player = local_player;
		this.landscape_renderer = renderer;
		this.selection = selection;
		this.picker = picker;
		this.sprite_sorter = sprite_sorter;
		this.render_queues = render_queues;
		ShadowListKey key = render_queues.registerRespondRenderer(new GeneratorRing(LandscapeTargetRespond.SIZE, new float[][]{{0.40f, 0f}, {0.41f, 1f}, {0.48f, 1f}, {0.49f, 0f}}));
		this.target_respond_renderer = (TargetRespondRenderer)render_queues.getShadowRenderer(key);
		this.default_shadow_renderer = (SelectableShadowRenderer)render_queues.getShadowRenderer(
				render_queues.registerSelectableShadowList(RacesResources.DEFAULT_SHADOW_DESC));
		this.render_state_cache = new RenderStateCache(new RenderStateFactory() {
			public final Object create() {
				return new ElementRenderState(RenderState.this);
			}
		});
	}

	final Player getLocalPlayer() {
		return local_player;
	}

	final boolean isResponding(Object target) {
		return picker.getRespondManager().isResponding(target);
	}

	final RenderQueues getRenderQueues() {
		return render_queues;
	}

	public final void setVisibleOverride(boolean override) {
		this.visible_override = visible_override;
	}

	public final void setup(boolean picking, CameraState camera_state) {
		this.picking = picking;
		this.camera = camera_state;
		render_state_cache.clear();
	}

	final CameraState getCamera() {
		return camera;
	}

	final boolean isPicking() {
		return picking;
	}

	final boolean overrideVisibility() {
		return visible_override;
	}

	private final static ModelVisitor unit_visitor = new SelectableVisitor() {
		public final void markDetailPolygon(ElementRenderState render_state, int level) {
			Unit unit = (Unit)render_state.model;
			super.markDetailPolygon(render_state, level);
			UnitSupplyContainer supply_container = unit.getSupplyContainer();
			if (!render_state.render_state.isPicking() && unit.getAbilities().hasAbilities(Abilities.BUILD) && supply_container.getSupplyType() != null) {
				if (supply_container.getNumSupplies() > 0) {
					SpriteRenderer supply_sprite = render_state.getRenderer(supply_container.getSupplySpriteRenderer(supply_container.getSupplyType()));
					supply_sprite.addToRenderList(level, render_state, false);
				}
			}
		}
	};
	public final void visitUnit(final Unit unit) {
		float z_offset = getVisuallyCorrectHeight(unit.getPositionX(), unit.getPositionY()) + unit.getOffsetZ();
		visitSelectable(unit_visitor, unit, z_offset, unit.getUnitTemplate().getSelectionRadius(), unit.getUnitTemplate().getSelectionHeight());
	}

	private ElementRenderState doGetCachedState() {
		return (ElementRenderState)render_state_cache.get();
	}

	private ModelState getCachedState(ModelVisitor visitor, Model model) {
		ElementRenderState state = doGetCachedState();
		state.setup(visitor, model);
		return state;
	}

	private ModelState getCachedState(ModelVisitor visitor, Model model, float dist_squared) {
		ElementRenderState state = doGetCachedState();
		state.setup(visitor, model, dist_squared);
		return state;
	}

	private final static BoundingBox picking_selection_box = new BoundingBox();
	private static boolean pickingInFrustum(Selectable selectable, float[][] frustum, float z_offset, float selection_radius, float selection_height) {
		picking_selection_box.setBounds(-selection_radius + selectable.getPositionX(), selection_radius + selectable.getPositionX(), -selection_radius + selectable.getPositionY(), selection_radius + selectable.getPositionY(), z_offset, z_offset + selection_height);
		return RenderTools.inFrustum(picking_selection_box, frustum) >= RenderTools.IN_FRUSTUM;
	}

	boolean isHovered(Selectable selectable) {
		return selectable == picker.getCurrentHovered();
	}

	boolean isSelected(Selectable selectable) {
		return selection.getCurrentSelection().contains(selectable);
	}

	private void visitSelectable(ModelVisitor visitor, Selectable selectable, float z_offset, float selection_radius, float selection_height) {
		boolean in_view = !picking || (selectable.isEnabled() && (visible_override || pickingInFrustum(selectable, camera.getFrustum(), z_offset, selection_radius, selection_height)));
		if (in_view) {
			Player owner = selectable.getOwnerNoCheck();
			boolean point_on_map = !local_player.isEnemy(owner) || (!owner.teamHasBuilding() && PeerHub.getFreeQuitTimeLeft(local_player.getWorld()) < 0f);
			ModelState state = getCachedState(visitor, selectable, z_offset);
			int sort_status = addToRenderList(state, point_on_map);
			if (!picking && selectable.isEnabled() && sort_status == SpriteSorter.DETAIL_POLYGON) {
				SelectableShadowRenderer shadow_renderer = (SelectableShadowRenderer)render_queues.getShadowRenderer(selectable.getTemplate().getSelectableShadowRenderer());
				if (isHovered(selectable) || isSelected(selectable)) {
					shadow_renderer.addToSelectionList(state);
				} else {
					shadow_renderer.addToShadowList(state);
				}
			}
		}
	}

	private float getVisuallyCorrectHeight(float x_f, float y_f) {
		int patch_level = landscape_renderer.getPatchLevelFromCoordinates(x_f, y_f).getLevel();
		return local_player.getWorld().getHeightMap().computeInterpolatedHeight(patch_level, x_f, y_f);
	}

	private static float getBuildingSelectionRadius(Building building) {
		int render_level = building.getRenderLevel();
		switch (render_level) {
			case Building.RENDER_START:
				return building.getBuildingTemplate().getStartSelectionRadius();
			case Building.RENDER_HALFBUILT:
				return building.getBuildingTemplate().getHalfbuiltSelectionRadius();
			case Building.RENDER_BUILT:
				return building.getBuildingTemplate().getBuiltSelectionRadius();
			default:
				throw new RuntimeException();
		}
	}

	private static float getBuildingSelectionHeight(Building building) {
		int render_level = building.getRenderLevel();
		switch (render_level) {
			case Building.RENDER_START:
				return building.getBuildingTemplate().getStartSelectionHeight();
			case Building.RENDER_HALFBUILT:
				return building.getBuildingTemplate().getHalfbuiltSelectionHeight();
			case Building.RENDER_BUILT:
				return building.getBuildingTemplate().getBuiltSelectionHeight();
			default:
				throw new RuntimeException();
		}
	}

	private final static ModelVisitor building_visitor = new SelectableVisitor();
	public final void visitBuilding(final Building building) {
		visitSelectable(building_visitor, building, building.getPositionZ(), getBuildingSelectionRadius(building), getBuildingSelectionHeight(building));
	}

	final int addToRenderList(LODObject model) {
		return addToRenderList(model, false);
	}

	final int addToRenderList(LODObject model, boolean point_on_map) {
		return sprite_sorter.add(model, camera, point_on_map);
	}

	public final void visitEmitter(final Emitter emitter) {
		if (!picking)
			emitter_queue.add(emitter);
	}

	public final void visitLightning(final Lightning lightning) {
		if (!picking)
			lightning_queue.add(lightning);
	}

	public final void visitRespond(final LandscapeTargetRespond respond) {
		if (!picking)
			target_respond_renderer.addToTargetList(respond);
	}

	private final static ModelVisitor supply_model_visitor = new SupplyModelVisitor() {
		public final void transform(ElementRenderState render_state) {
			SupplyModel model = (SupplyModel)render_state.getModel();
			GL11.glTranslatef(model.getPositionX(), model.getPositionY(), model.getPositionZ());
			GL11.glRotatef(model.getRotation(), 0f, 0f, 1f);
		}
	};
	public final void visitSupplyModel(final SupplyModel model) {
		addToRenderList(getCachedState(supply_model_visitor, model));
	}

	private final static ModelVisitor rubber_model_visitor = new SupplyModelVisitor() {
		public final void transform(ElementRenderState render_state) {
			Model model = render_state.model;
			RenderTools.translateAndRotate(model.getPositionX(), model.getPositionY(), render_state.f, model.getDirectionX(), model.getDirectionY());
		}
	};
	public final void visitRubberSupply(final RubberSupply model) {
		float z_offset = getVisuallyCorrectHeight(model.getPositionX(), model.getPositionY()) + model.getOffsetZ();
		ModelState state = getCachedState(rubber_model_visitor, model, z_offset);
		addToRenderList(state);
		if (!picking && !model.isHit())
			default_shadow_renderer.addToShadowList(state);
	}

	private final static ModelVisitor scenery_model_visitor = new WhiteModelVisitor() {
		public void transform(ElementRenderState render_state) {
			RenderTools.translateAndRotate(render_state.getModel());
			GL11.glColor4f(1f, 1f, 1f, 1f);
		}
	};
	public final void visitSceneryModel(final SceneryModel model) {
		ModelState state = getCachedState(scenery_model_visitor, model);
		addToRenderList(state);
		if (!picking) {
			if (model.getShadowDiameter() > 0f)
				default_shadow_renderer.addToShadowList(state);
		}
	}

	private final static float PLANTS_CUT_DIST = 200;
	private final static ModelVisitor plants_model_visitor = new WhiteModelVisitor() {
		private final static float START_FADE_DIST = 100;

		public final void transform(ElementRenderState render_state) {
			Plants plants = (Plants)render_state.getModel();
			RenderTools.translateAndRotate(plants);
			float dist_squared = render_state.f;
			if (dist_squared > START_FADE_DIST*START_FADE_DIST) {
				float camera_dist = (float)Math.sqrt(dist_squared);
				float alpha = 1f - ((camera_dist - START_FADE_DIST)/(PLANTS_CUT_DIST - START_FADE_DIST));
				GL11.glColor4f(1f, 1f, 1f, alpha);
			}
		}
	};
	public final void visitPlants(final Plants plants) {
		if (!picking && Globals.draw_plants) {
			float camera_dist_sqr = RenderTools.getEyeDistanceSquared(plants, camera.getCurrentX(), camera.getCurrentY(), camera.getCurrentZ());
			if (camera_dist_sqr <= PLANTS_CUT_DIST*PLANTS_CUT_DIST)
				addToRenderList(getCachedState(plants_model_visitor, plants, camera_dist_sqr));
		}
	}

	private final static ModelVisitor directed_weapon_model_visitor = new WhiteModelVisitor() {
		public final void transform(ElementRenderState render_state) {
			DirectedThrowingWeapon model = (DirectedThrowingWeapon)render_state.getModel();
			RenderTools.translateAndRotate(render_state.getModel());
			GL11.glRotatef(-model.getZSpeed(), 0f, 1f, 0f);
		}
	};
	public final void visitDirectedThrowingWeapon(final DirectedThrowingWeapon model) {
		if (!picking) {
			addToRenderList(getCachedState(directed_weapon_model_visitor, model));
		}
	}

	private final static ModelVisitor rotating_weapon_model_visitor = new WhiteModelVisitor() {
		public final void transform(ElementRenderState render_state) {
			RotatingThrowingWeapon model = (RotatingThrowingWeapon)render_state.getModel();
			RenderTools.translateAndRotate(render_state.getModel());
			GL11.glRotatef(model.getAngle(), 0f, 1f, 0f);
		}
	};
	public final void visitRotatingThrowingWeapon(final RotatingThrowingWeapon model) {
		if (!picking) {
			addToRenderList(getCachedState(rotating_weapon_model_visitor, model));
		}
	}

	public final List getEmitterQueue() {
		return emitter_queue;
	}

	public final List getLightningQueue() {
		return lightning_queue;
	}
}
