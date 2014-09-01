package com.oddlabs.tt.model;

import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.render.ShadowListKey;

public final strictfp class BuildingTemplate extends Template {
	private final int template_id;
	private final int placing_size;
	private final float smoke_radius;
	private final float smoke_height;
	private final int num_fragments;
	private final SpriteKey built_renderer;
	private final SpriteKey halfbuilt_renderer;
	private final SpriteKey start_renderer;
	private final int max_hit_points;
	private final UnitContainerFactory unit_container_factory;
	private final float mount_offset;
	private final float built_selection_radius;
	private final float built_selection_height;
	private final float halfbuilt_selection_radius;
	private final float halfbuilt_selection_height;
	private final float start_selection_radius;
	private final float start_selection_height;
	private final float rally_x;
	private final float rally_y;
	private final float rally_z;
	private final float chimney_x;
	private final float chimney_y;
	private final float chimney_z;

	public BuildingTemplate(
			int template_id,
			int placing_size,
			float smoke_radius,
			float smoke_height,
			int num_fragments,
			float shadow_diameter,
			ShadowListKey shadow_renderer,
			SpriteKey built_renderer,
			float built_selection_radius,
			float built_selection_height,
			SpriteKey halfbuilt_renderer,
			float halfbuilt_selection_radius,
			float halfbuilt_selection_height,
			SpriteKey start_renderer,
			float start_selection_radius,
			float start_selection_height,
			int max_hit_points,
			UnitContainerFactory unit_container_factory,
			Abilities abilities,
			float[] hit_offset_z,
			float mount_offset,
			float no_detail_size,
			float defense_chance,
			float rally_x,
			float rally_y,
			float rally_z,
			float chimney_x,
			float chimney_y,
			float chimney_z,
			String name) {
		super(abilities, shadow_diameter, shadow_renderer, hit_offset_z, no_detail_size, defense_chance, name);
		this.template_id = template_id;
		this.built_selection_radius = built_selection_radius;
		this.built_selection_height = built_selection_height;
		this.halfbuilt_selection_radius = halfbuilt_selection_radius;
		this.halfbuilt_selection_height = halfbuilt_selection_height;
		this.start_selection_radius = start_selection_radius;
		this.start_selection_height = start_selection_height;
		this.placing_size = placing_size;
		this.smoke_radius = smoke_radius;
		this.smoke_height = smoke_height;
		this.num_fragments = num_fragments;
		this.built_renderer = built_renderer;
		this.halfbuilt_renderer = halfbuilt_renderer;
		this.start_renderer = start_renderer;
		this.max_hit_points = max_hit_points;
		this.unit_container_factory = unit_container_factory;
		this.mount_offset = mount_offset;
		this.rally_x = rally_x;
		this.rally_y = rally_y;
		this.rally_z = rally_z;
		this.chimney_x = chimney_x;
		this.chimney_y = chimney_y;
		this.chimney_z = chimney_z;
	}

	public final int getTemplateID() {
		return template_id;
	}

	public final float getBuiltSelectionRadius() {
		return built_selection_radius;
	}

	public final float getBuiltSelectionHeight() {
		return built_selection_height;
	}

	public final float getHalfbuiltSelectionRadius() {
		return halfbuilt_selection_radius;
	}

	public final float getHalfbuiltSelectionHeight() {
		return halfbuilt_selection_height;
	}

	public final float getStartSelectionRadius() {
		return start_selection_radius;
	}

	public final float getStartSelectionHeight() {
		return start_selection_height;
	}

	public final int getPlacingSize() {
		return placing_size;
	}

	public final float getSmokeRadius() {
		return smoke_radius;
	}
	
	public final float getSmokeHeight() {
		return smoke_height;
	}

	public final int getNumFragments() {
		return num_fragments;
	}
	
	public final SpriteKey getBuiltRenderer() {
		return built_renderer;
	}

	public final SpriteKey getStartRenderer() {
		return start_renderer;
	}

	public final SpriteKey getHalfbuiltRenderer() {
		return halfbuilt_renderer;
	}

	public final int getMaxHitPoints() {
		return max_hit_points;
	}

	public final UnitContainerFactory getUnitContainerFactory() {
		return unit_container_factory;
	}

	public final float getMountOffset() {
		return mount_offset;
	}

	public final float getRallyX() {
		return rally_x;
	}

	public final float getRallyY() {
		return rally_y;
	}

	public final float getRallyZ() {
		return rally_z;
	}

	public final float getChimneyX() {
		return chimney_x;
	}

	public final float getChimneyY() {
		return chimney_y;
	}

	public final float getChimneyZ() {
		return chimney_z;
	}
}
