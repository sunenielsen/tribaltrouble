package com.oddlabs.tt.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.audio.Audio;
import com.oddlabs.tt.audio.AudioFile;
import com.oddlabs.tt.form.ProgressForm;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.gui.Icons;
import com.oddlabs.tt.landscape.AbstractTreeGroup;
import com.oddlabs.tt.landscape.LandscapeTargetRespond;
import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.weapon.InstantHitFactory;
import com.oddlabs.tt.model.weapon.IronAxeWeapon;
import com.oddlabs.tt.model.weapon.IronSpearWeapon;
import com.oddlabs.tt.model.weapon.LightningCloudFactory;
import com.oddlabs.tt.model.weapon.MagicFactory;
import com.oddlabs.tt.model.weapon.PoisonFogFactory;
import com.oddlabs.tt.model.weapon.RockAxeWeapon;
import com.oddlabs.tt.model.weapon.RockSpearWeapon;
import com.oddlabs.tt.model.weapon.RubberAxeWeapon;
import com.oddlabs.tt.model.weapon.RubberSpearWeapon;
import com.oddlabs.tt.model.weapon.SonicBlastFactory;
import com.oddlabs.tt.model.weapon.StunFactory;
import com.oddlabs.tt.model.weapon.ThrowingFactory;
import com.oddlabs.tt.model.weapon.WeaponFactory;
import com.oddlabs.tt.player.NativeChieftainAI;
import com.oddlabs.tt.player.VikingChieftainAI;
import com.oddlabs.tt.procedural.GeneratorDamageSmoke;
import com.oddlabs.tt.procedural.GeneratorLightning;
import com.oddlabs.tt.procedural.GeneratorPoison;
import com.oddlabs.tt.procedural.GeneratorSmoke;
import com.oddlabs.tt.procedural.GeneratorSonic;
import com.oddlabs.tt.render.ShadowListKey;
import com.oddlabs.tt.render.TargetRespondRenderer;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.render.TextureKey;
import com.oddlabs.tt.render.RenderQueues;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.resource.SpriteFile;
import com.oddlabs.tt.resource.TextureFile;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.resource.ResourceDescriptor;
import com.oddlabs.tt.procedural.GeneratorHalos;

public final strictfp class RacesResources {
	public final static int QUARTERS_SIZE = 5;
	public final static int ARMORY_SIZE = 5;
	public final static int TOWER_SIZE = 3;
	public final static int QUARTERS_HIT_POINTS = 200;
	public final static int ARMORY_HIT_POINTS = 200;
	public final static int TOWER_HIT_POINTS = 100;
	public final static int VIKING_CHIEFTAIN_HIT_POINTS = 60;
	public final static int NATIVE_CHIEFTAIN_HIT_POINTS = 40;

	public final static int RACE_NATIVES = 0;
	public final static int RACE_VIKINGS = 1;
	
	public final static int NUM_MAGIC = 2;
	public final static int INDEX_MAGIC_POISON = 0;
	public final static int INDEX_MAGIC_LIGHTNING = 1;
	public final static int INDEX_MAGIC_STUN = 0;
	public final static int INDEX_MAGIC_BLAST = 1;
	public final static float THROW_RANGE = 6f;
	
	public final static ResourceDescriptor DEFAULT_SHADOW_DESC = new GeneratorHalos(128, new float[][]{{0f, 0.75f}, {0.5f, 0f}}, new float[][]{{0.40f, 0f}, {0.41f, 1f}, {0.48f, 1f}, {0.49f, 0f}});

	private final static ResourceBundle bundle = ResourceBundle.getBundle(RacesResources.class.getName());
	private final static String[] race_names = new String[]{Utils.getBundleString(bundle, "natives"), Utils.getBundleString(bundle, "vikings")};
	private final static int MAX_UNIT_RESOURCES = 1;

	private final TextureKey[] smoke_textures = new TextureKey[1];
	private final TextureKey[] damage_smoke_textures = new TextureKey[1];
	private final TextureKey[] poison_textures = new TextureKey[1];
	private final TextureKey lightning_texture;
	private final TextureKey[] sonic_textures = new TextureKey[1];
	private final TextureKey[] note_textures = new TextureKey[8];
	private final TextureKey[] star_textures = new TextureKey[1];
	private final Audio[] tree_fall_sound;
	private final Audio[] building_hit_sound;
	private final Audio gas_sound;
	private final Audio bubbling_sound;
	private final Audio lightning_sound;
	private final Audio cloud_sound;
	private final Audio[] stun_sound;
	private final Audio[] blast_lur_sound;
	private final Audio blast_rumble_sound;
	private final Audio blast_blast_sound;
	private final Audio armory_sound;
	private final Audio building_collapse_sound;
	private final Map harvest_sounds = new HashMap();
	private final SpriteKey[] wood_fragment_sprites = new SpriteKey[4];
	private final SpriteKey[] treasure_sprites = new SpriteKey[6];
	private final Race[] races;

	public final static boolean isValidRace(int race) {
		return race == RACE_NATIVES || race == RACE_VIKINGS;
	}

	private final static BuildingTemplate createBuildingTemplate(
			RenderQueues queues,
			int template_id,
			String built_name,
			float built_selection_radius,
			float built_selection_height,
			String halfbuilt_name,
			float halfbuilt_selection_radius,
			float halfbuilt_selection_height,
			String start_name,
			float start_selection_radius,
			float start_selection_height,
			float shadow_diameter,
			float ring_thickness,
			int placing_size,
			float smoke_radius,
			float smoke_height,
			int num_fragments,
			int max_hit_points,
			UnitContainerFactory unit_container_factory,
			Abilities abilities,
			float[] hit_offset_z,
			float mount_offset,
			float no_detail_size,
			float rally_x,
			float rally_y,
			float rally_z,
			float chimney_x,
			float chimney_y,
			float chimney_z, String name) {
		assert hit_offset_z.length == 3;
		
		final float ring_mid = 0.38f;
		final float fadeout = 0.005f;
		ResourceDescriptor building_shadow_desc = new GeneratorHalos(256, new float[][]{{0.15f, 0.5f}, {0.5f, 0f}}, new float[][]{{ring_mid - ring_thickness/2 - fadeout, 0f}, {ring_mid - ring_thickness/2, 1f}, {ring_mid + ring_thickness/2, 1f}, {ring_mid + ring_thickness/2 + fadeout, 0f}});
		ShadowListKey shadow_renderer = queues.registerSelectableShadowList(building_shadow_desc);
		SpriteFile building = new SpriteFile(built_name,
																 Globals.NO_MIPMAP_CUTOFF,
																 true, true, true, false);
		SpriteFile building_halfbuilt = new SpriteFile(halfbuilt_name,
																		   Globals.NO_MIPMAP_CUTOFF,
																		   true, true, true, false);
		SpriteFile building_start = new SpriteFile(start_name,
																		   Globals.NO_MIPMAP_CUTOFF,
																		   true, true, true, false);
		return new BuildingTemplate(template_id,
									placing_size,
									smoke_radius,
									smoke_height,
									num_fragments,
									shadow_diameter,
									shadow_renderer,
									queues.register(building),
									built_selection_radius,
									built_selection_height,
									queues.register(building_halfbuilt),
									halfbuilt_selection_radius,
									halfbuilt_selection_height,
									queues.register(building_start),
									start_selection_radius,
									start_selection_height,
									max_hit_points,
									unit_container_factory,
									abilities,
									hit_offset_z,
									mount_offset,
									no_detail_size,
									0f,
									rally_x,
									rally_y,
									rally_z,
									chimney_x,
									chimney_y,
									chimney_z, name);
	}

	public RacesResources(RenderQueues queues) {
		int num_progress = 23;
		SpriteFile native_rock_sprite = new SpriteFile("/geometry/natives/rock_resource.binsprite",
																						   Globals.NO_MIPMAP_CUTOFF,
																						   true, true, true, false);
		ProgressForm.progress(1f/num_progress);
		SpriteFile native_wood_sprite = new SpriteFile("/geometry/natives/wood_resource.binsprite",
																						   Globals.NO_MIPMAP_CUTOFF,
																						   true, true, true, false);
		SpriteFile native_rubber_sprite = new SpriteFile("/geometry/natives/rubber_resource.binsprite",
																						   Globals.NO_MIPMAP_CUTOFF,
																						   true, true, true, false);
		ProgressForm.progress(1f/num_progress);
		Map native_supply_sprite_lists = new HashMap();
		native_supply_sprite_lists.put(TreeSupply.class, queues.register(native_wood_sprite));
		native_supply_sprite_lists.put(RockSupply.class, queues.register(native_rock_sprite));
		native_supply_sprite_lists.put(IronSupply.class, queues.register(native_rock_sprite, 1));
		native_supply_sprite_lists.put(RubberSupply.class, queues.register(native_rubber_sprite));

		SpriteFile viking_wood_sprite = new SpriteFile("/geometry/vikings/wood_resource.binsprite",
																						   Globals.NO_MIPMAP_CUTOFF,
																						   true, true, true, false);
		SpriteFile viking_rubber_sprite = new SpriteFile("/geometry/vikings/rubber_resource.binsprite",
																						   Globals.NO_MIPMAP_CUTOFF,
																						   true, true, true, false);
		ProgressForm.progress(1f/num_progress);
		SpriteFile viking_rock_sprite = new SpriteFile("/geometry/vikings/rock_resource.binsprite",
																						   Globals.NO_MIPMAP_CUTOFF,
																						   true, true, true, false);
		ProgressForm.progress(1f/num_progress);
		Map viking_supply_sprite_lists = new HashMap();
		viking_supply_sprite_lists.put(TreeSupply.class, queues.register(viking_wood_sprite));
		viking_supply_sprite_lists.put(RockSupply.class, queues.register(viking_rock_sprite));
		viking_supply_sprite_lists.put(IronSupply.class, queues.register(viking_rock_sprite, 1));
		viking_supply_sprite_lists.put(RubberSupply.class, queues.register(viking_rubber_sprite));


		smoke_textures[0] = queues.registerTexture(new GeneratorSmoke(), 0);
		damage_smoke_textures[0] = queues.registerTexture(new GeneratorDamageSmoke(), 0);
		poison_textures[0] = queues.registerTexture(new GeneratorPoison(), 0);
		lightning_texture = queues.registerTexture(new GeneratorLightning(), 0);
		sonic_textures[0] = queues.registerTexture(new GeneratorSonic(), 0);
		
		note_textures[0] = queues.registerTexture(new TextureFile("/textures/effects/note1",
					Globals.COMPRESSED_RGBA_FORMAT,
					GL11.GL_LINEAR_MIPMAP_NEAREST,
					GL11.GL_LINEAR,
					GL11.GL_CLAMP,
					GL11.GL_CLAMP));
		note_textures[1] = queues.registerTexture(new TextureFile("/textures/effects/note2",
					Globals.COMPRESSED_RGBA_FORMAT,
					GL11.GL_LINEAR_MIPMAP_NEAREST,
					GL11.GL_LINEAR,
					GL11.GL_CLAMP,
					GL11.GL_CLAMP));
		note_textures[2] = queues.registerTexture(new TextureFile("/textures/effects/note3",
					Globals.COMPRESSED_RGBA_FORMAT,
					GL11.GL_LINEAR_MIPMAP_NEAREST,
					GL11.GL_LINEAR,
					GL11.GL_CLAMP,
					GL11.GL_CLAMP));
		note_textures[3] = queues.registerTexture(new TextureFile("/textures/effects/note4",
					Globals.COMPRESSED_RGBA_FORMAT,
					GL11.GL_LINEAR_MIPMAP_NEAREST,
					GL11.GL_LINEAR,
					GL11.GL_CLAMP,
					GL11.GL_CLAMP));
		note_textures[4] = queues.registerTexture(new TextureFile("/textures/effects/note5",
					Globals.COMPRESSED_RGBA_FORMAT,
					GL11.GL_LINEAR_MIPMAP_NEAREST,
					GL11.GL_LINEAR,
					GL11.GL_CLAMP,
					GL11.GL_CLAMP));
		note_textures[5] = queues.registerTexture(new TextureFile("/textures/effects/note6",
					Globals.COMPRESSED_RGBA_FORMAT,
					GL11.GL_LINEAR_MIPMAP_NEAREST,
					GL11.GL_LINEAR,
					GL11.GL_CLAMP,
					GL11.GL_CLAMP));
		note_textures[6] = queues.registerTexture(new TextureFile("/textures/effects/note7",
					Globals.COMPRESSED_RGBA_FORMAT,
					GL11.GL_LINEAR_MIPMAP_NEAREST,
					GL11.GL_LINEAR,
					GL11.GL_CLAMP,
					GL11.GL_CLAMP));
		note_textures[7] = queues.registerTexture(new TextureFile("/textures/effects/note8",
					Globals.COMPRESSED_RGBA_FORMAT,
					GL11.GL_LINEAR_MIPMAP_NEAREST,
					GL11.GL_LINEAR,
					GL11.GL_CLAMP,
					GL11.GL_CLAMP));
		
		star_textures[0] = queues.registerTexture(new TextureFile("/textures/effects/star",
					Globals.COMPRESSED_RGBA_FORMAT,
					GL11.GL_LINEAR_MIPMAP_NEAREST,
					GL11.GL_LINEAR,
					GL11.GL_CLAMP,
					GL11.GL_CLAMP));
		
		Audio death_peon_sound = (Audio)Resources.findResource(new AudioFile("/sfx/death_peon.ogg"));
		Audio death_viking1_sound = (Audio)Resources.findResource(new AudioFile("/sfx/death_viking_warrior1.ogg"));
		Audio death_viking2_sound = (Audio)Resources.findResource(new AudioFile("/sfx/death_viking_warrior2.ogg"));
		Audio death_native1_sound = (Audio)Resources.findResource(new AudioFile("/sfx/death_native_warrior1.ogg"));
		Audio death_native2_sound = (Audio)Resources.findResource(new AudioFile("/sfx/death_native_warrior2.ogg"));
		
		Audio axe_throw_sound = (Audio)Resources.findResource(new AudioFile("/sfx/weapon_axe.ogg"));
		Audio spear_throw_sound = (Audio)Resources.findResource(new AudioFile("/sfx/weapon_spear.ogg"));

		tree_fall_sound = new Audio[2];
		tree_fall_sound[AbstractTreeGroup.TREE_INDEX] = (Audio)Resources.findResource(new AudioFile("/sfx/felling_tree.ogg"));
		tree_fall_sound[AbstractTreeGroup.PALMTREE_INDEX] = (Audio)Resources.findResource(new AudioFile("/sfx/felling_palmtree.ogg"));
		
		ProgressForm.progress(1f/num_progress);
		
		building_hit_sound = new Audio[]{
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_wood1.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_wood2.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_wood3.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_wood4.ogg"))
		};
		
		gas_sound = (Audio)Resources.findResource(new AudioFile("/sfx/gas.ogg"));
		bubbling_sound = (Audio)Resources.findResource(new AudioFile("/sfx/bubbling.ogg"));
		lightning_sound = (Audio)Resources.findResource(new AudioFile("/sfx/flash.ogg"));
		cloud_sound = (Audio)Resources.findResource(new AudioFile("/sfx/crackling_cloud.ogg"));

		armory_sound = (Audio)Resources.findResource(new AudioFile("/sfx/armory.ogg"));
		
		building_collapse_sound = (Audio)Resources.findResource(new AudioFile("/sfx/building_crash.ogg"));

		stun_sound = new Audio[]{
			(Audio)Resources.findResource(new AudioFile("/sfx/lur_stun1.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/lur_stun2.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/lur_stun3.ogg"))
		};

		blast_lur_sound = new Audio[]{
			(Audio)Resources.findResource(new AudioFile("/sfx/lur_blast1.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/lur_blast2.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/lur_blast3.ogg"))
		};
		blast_rumble_sound = (Audio)Resources.findResource(new AudioFile("/sfx/rumble.ogg"));
		blast_blast_sound = (Audio)Resources.findResource(new AudioFile("/sfx/lurblast.ogg"));
		
		Audio[] tree_cut_sound = new Audio[]{
			(Audio)Resources.findResource(new AudioFile("/sfx/axe_cutting_wood1.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/axe_cutting_wood2.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/axe_cutting_wood3.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/axe_cutting_wood4.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/axe_cutting_wood5.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/axe_cutting_wood6.ogg"))
		};
		
		Audio[] rock_cut_sound = new Audio[]{
			(Audio)Resources.findResource(new AudioFile("/sfx/axe_cutting_stone1.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/axe_cutting_stone2.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/axe_cutting_stone3.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/axe_cutting_stone4.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/axe_cutting_stone5.ogg"))
		};
		
		Audio[] meat_cut_sound = new Audio[]{
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_meat1.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_meat2.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_meat3.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_meat4.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_meat5.ogg"))
		};
		
		ProgressForm.progress(1f/num_progress);
		harvest_sounds.put(TreeSupply.class, tree_cut_sound);
		harvest_sounds.put(RockSupply.class, rock_cut_sound);
		harvest_sounds.put(IronSupply.class, rock_cut_sound);
		harvest_sounds.put(RubberSupply.class, meat_cut_sound);

		BuildingTemplate viking_quarters_template = createBuildingTemplate(
				queues,
				Race.BUILDING_QUARTERS,
				"/geometry/vikings/quarters.binsprite",
				3.5f, 7f,
				"/geometry/vikings/quarters_halfbuilt.binsprite",
				3.5f, 6f,
				"/geometry/vikings/quarters_start.binsprite",
				5f, 1f,
				22f, .001f, QUARTERS_SIZE, 6f, 9f, 30, QUARTERS_HIT_POINTS,
				new ReproduceUnitContainerFactory(),
				new Abilities(Abilities.REPRODUCE | Abilities.RALLY_TO | Abilities.TARGET),
				new float[]{0f, 1f, 3f}, 0f, 6f,
				3.65f, .25f, 8f,
				0f, 0f, 0f,
				Utils.getBundleString(bundle, "quarters"));
		ProgressForm.progress(1f/num_progress);
		BuildingTemplate viking_armory_template = createBuildingTemplate(
				queues,
				Race.BUILDING_ARMORY,
				"/geometry/vikings/armory.binsprite",
				3.5f, 7f,
				"/geometry/vikings/armory_halfbuilt.binsprite",
				3.5f, 6f,
				"/geometry/vikings/armory_start.binsprite",
				5f, 1f,
				22f, .001f, ARMORY_SIZE, 6f, 9f, 30, ARMORY_HIT_POINTS,
				new WorkerUnitContainerFactory(),
				new Abilities(Abilities.SUPPLY_CONTAINER | Abilities.BUILD_ARMIES | Abilities.RALLY_TO | Abilities.TARGET),
				new float[]{0f, 1f, 3f}, 0f, 6f,
				0f, 2.25f, 10f,
				.25f, -2.8f, 13.1f,
				Utils.getBundleString(bundle, "armory"));
		ProgressForm.progress(1f/num_progress);
		BuildingTemplate viking_tower_template = createBuildingTemplate(
				queues,
				Race.BUILDING_TOWER,
				"/geometry/vikings/tower.binsprite",
				1.25f, 11f,
				"/geometry/vikings/tower_halfbuilt.binsprite",
				2f, 7f,
				"/geometry/vikings/tower_start.binsprite",
				2.5f, 1f,
				10f, .009f, TOWER_SIZE, 3f, 12f, 20, TOWER_HIT_POINTS,
				new MountUnitContainerFactory(),
				new Abilities(Abilities.ATTACK | Abilities.RALLY_TO | Abilities.TARGET),
				new float[]{0f, 2f, 7.5f}, 9.55f, 2.5f,
				.85f, .85f, 9.5f,
				0f, 0f, 0f,
				Utils.getBundleString(bundle, "tower"));
		ProgressForm.progress(1f/num_progress);
		BuildingTemplate native_quarters_template = createBuildingTemplate(
				queues,
				Race.BUILDING_QUARTERS,
				"/geometry/natives/quarters.binsprite",
				4f, 8f,
				"/geometry/natives/quarters_halfbuilt.binsprite",
				4f, 6f,
				"/geometry/natives/quarters_start.binsprite",
				5f, 1f,
				16f, .004f, QUARTERS_SIZE, 6f, 9f, 30, QUARTERS_HIT_POINTS,
				new ReproduceUnitContainerFactory(),
				new Abilities(Abilities.REPRODUCE | Abilities.RALLY_TO | Abilities.TARGET),
				new float[]{0f, 1f, 3f}, 0f, 6f,
				-1.15f, -.77f, 11f,
				0f, 0f, 0f,
				Utils.getBundleString(bundle, "quarters"));
		ProgressForm.progress(1f/num_progress);
		BuildingTemplate native_armory_template = createBuildingTemplate(
				queues,
				Race.BUILDING_ARMORY,
				"/geometry/natives/armory.binsprite",
				4f, 8f,
				"/geometry/natives/armory_halfbuilt.binsprite",
				4f, 6f,
				"/geometry/natives/armory_start.binsprite",
				5f, 1f,
				16f, .004f, ARMORY_SIZE, 6f, 9f, 30, ARMORY_HIT_POINTS,
				new WorkerUnitContainerFactory(),
				new Abilities(Abilities.SUPPLY_CONTAINER | Abilities.BUILD_ARMIES | Abilities.RALLY_TO | Abilities.TARGET),
				new float[]{0f, 1f, 3f}, 0f, 6f,
				0f, -.4f, 12f,
				0f, -1f, 11.5f,
				Utils.getBundleString(bundle, "armory"));
		ProgressForm.progress(1f/num_progress);
		BuildingTemplate native_tower_template = createBuildingTemplate(
				queues,
				Race.BUILDING_TOWER,
				"/geometry/natives/tower.binsprite",
				1f, 14f,
				"/geometry/natives/tower_halfbuilt.binsprite",
				1f, 14f,
				"/geometry/natives/tower_start.binsprite",
				1.5f, 2f,
				5f, .025f, TOWER_SIZE, 3f, 12f, 20, TOWER_HIT_POINTS,
				new MountUnitContainerFactory(),
				new Abilities(Abilities.ATTACK | Abilities.RALLY_TO | Abilities.TARGET),
				new float[]{0f, 11.5f, 11.5f}, 13f, 2.5f,
				.95f, 0f, 13f,
				0f, 0f, 0f,
				Utils.getBundleString(bundle, "tower"));
		ProgressForm.progress(1f/num_progress);
		final float shadow_diameter_warrior = 1.9f;
		final float shadow_diameter_peon = 1.6f;
		final float shadow_diameter_chieftain = 2.2f;
		ProgressForm.progress(1f/num_progress);

		SpriteFile sprite_list_warrior = new SpriteFile("/geometry/vikings/warrior.binsprite",
																						   Globals.NO_MIPMAP_CUTOFF,
																						   true, true, true, false);
		ProgressForm.progress(1f/num_progress);

		SpriteFile sprite_list_chieftain = new SpriteFile("/geometry/vikings/chieftain.binsprite",
																						Globals.NO_MIPMAP_CUTOFF,
																						true, true, true, false);
		ProgressForm.progress(1f/num_progress);
		SpriteFile sprite_list_native_chieftain = new SpriteFile("/geometry/natives/chieftain.binsprite",
																							   Globals.NO_MIPMAP_CUTOFF,
																							   true, true, true, false);
		SpriteFile sprite_list_peon = new SpriteFile("/geometry/vikings/peon.binsprite",
																						Globals.NO_MIPMAP_CUTOFF,
																						true, true, true, false);
		ProgressForm.progress(1f/num_progress);
		SpriteFile sprite_list_native_peon = new SpriteFile("/geometry/natives/peon.binsprite",
																							   Globals.NO_MIPMAP_CUTOFF,
																							   true, true, true, false);
		ProgressForm.progress(1f/num_progress);
		SpriteFile sprite_list_native_warrior = new SpriteFile("/geometry/natives/warrior.binsprite",
																								  Globals.NO_MIPMAP_CUTOFF,
																								  true, true, true, false);
		ProgressForm.progress(1f/num_progress);
		SpriteFile viking_warrior_axe = new SpriteFile("/geometry/vikings/axe.binsprite",
																						  Globals.NO_MIPMAP_CUTOFF,
																						  true, true, true, false);
		ProgressForm.progress(1f/num_progress);
		SpriteFile native_warrior_spear = new SpriteFile("/geometry/natives/spear.binsprite",
																							Globals.NO_MIPMAP_CUTOFF,
																							true, true, true, false);
		ProgressForm.progress(1f/num_progress);

		Audio[] unit_hit_sounds = new Audio[]{
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_meat1.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_meat2.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_meat3.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_meat4.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/impact_meat5.ogg"))
		};
		WeaponFactory viking_warrior_rock_weapon = new ThrowingFactory(RockAxeWeapon.class, 0.5f, THROW_RANGE, 29f/58f,
																	   queues.register(viking_warrior_axe, Race.UNIT_WARRIOR_ROCK),
																	   axe_throw_sound,
																	   unit_hit_sounds);
		WeaponFactory viking_warrior_iron_weapon = new ThrowingFactory(IronAxeWeapon.class, 0.75f, THROW_RANGE, 29f/58f,
																	   queues.register(viking_warrior_axe, Race.UNIT_WARRIOR_IRON),
																	   axe_throw_sound,
																	   unit_hit_sounds);
		WeaponFactory viking_warrior_rubber_weapon = new ThrowingFactory(RubberAxeWeapon.class, 0.95f, THROW_RANGE, 29f/58f,
																	   queues.register(viking_warrior_axe, Race.UNIT_WARRIOR_RUBBER),
																	   axe_throw_sound,
																	   unit_hit_sounds);
		WeaponFactory native_warrior_rock_weapon = new ThrowingFactory(RockSpearWeapon.class, 0.5f, THROW_RANGE, 46f/100f,
																	   queues.register(native_warrior_spear, Race.UNIT_WARRIOR_ROCK),
																	   spear_throw_sound,
																	   unit_hit_sounds);
		WeaponFactory native_warrior_iron_weapon = new ThrowingFactory(IronSpearWeapon.class, 0.75f, THROW_RANGE, 46f/100f,
																	   queues.register(native_warrior_spear, Race.UNIT_WARRIOR_IRON),
																	   spear_throw_sound,
																	   unit_hit_sounds);
		WeaponFactory native_warrior_rubber_weapon = new ThrowingFactory(RubberSpearWeapon.class, 0.95f, THROW_RANGE, 46f/100f,
																	   queues.register(native_warrior_spear, Race.UNIT_WARRIOR_RUBBER),
																	   spear_throw_sound,
																	   unit_hit_sounds);
		
		Audio[] native_chieftain_hit_sounds = new Audio[]{
			(Audio)Resources.findResource(new AudioFile("/sfx/hit3.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/hit4.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/hit5.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/hit6.ogg"))
		};
		Audio[] viking_chieftain_hit_sounds = new Audio[]{
			(Audio)Resources.findResource(new AudioFile("/sfx/hit1.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/hit2.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/hit6.ogg")),
			(Audio)Resources.findResource(new AudioFile("/sfx/hit7.ogg"))
		};

		ProgressForm.progress(1f/num_progress);
		ShadowListKey default_shadow_list = queues.registerSelectableShadowList(DEFAULT_SHADOW_DESC);
		UnitTemplate viking_warrior_rock_template = new UnitTemplate(.4f,
																	 1.2f,
																	 new Abilities(Abilities.ATTACK | Abilities.TARGET | Abilities.THROW),
																	 4f,
																	 viking_warrior_rock_weapon,
																	 queues.register(sprite_list_warrior, Race.UNIT_WARRIOR_ROCK),
																	 shadow_diameter_warrior,
																	 default_shadow_list,
																	 null,
																	 death_viking1_sound,
																	 .25f,
																	 new float[]{1.2f},
																	 1f,
																	 .5f,
																	 Utils.getBundleString(bundle, "rock_warrior"),
																	 1,
																	 0f, 0f, 2f,
																	 3);
		UnitTemplate viking_warrior_iron_template = new UnitTemplate(.4f,
																	 1.2f,
																	 new Abilities(Abilities.ATTACK | Abilities.TARGET | Abilities.THROW),
																	 4f,
																	 viking_warrior_iron_weapon,
																	 queues.register(sprite_list_warrior, Race.UNIT_WARRIOR_IRON),
																	 shadow_diameter_warrior,
																	 default_shadow_list,
																	 null,
																	 death_viking2_sound,
																	 .25f,
																	 new float[]{1.2f},
																	 1f,
																	 .7f,
																	 Utils.getBundleString(bundle, "iron_warrior"),
																	 1,
																	 0f, 0f, 2f,
																	 5);
		UnitTemplate viking_warrior_rubber_template = new UnitTemplate(.4f,
																	   1.2f,
																	   new Abilities(Abilities.ATTACK | Abilities.TARGET | Abilities.THROW),
																	   4f,
																	   viking_warrior_rubber_weapon,
																	   queues.register(sprite_list_warrior, Race.UNIT_WARRIOR_RUBBER),
																	 shadow_diameter_warrior,
																	 default_shadow_list,
																	   null,
																	   death_viking2_sound,
																	   .25f,
																	   new float[]{1.2f},
																	   1f,
																	   .7f,
																	   Utils.getBundleString(bundle, "chicken_warrior"),
																	   1,
																	   0f, 0f, 2f,
																	   10);
		UnitTemplate native_warrior_rock_template = new UnitTemplate(.4f,
																	 1.2f,
																	 new Abilities(Abilities.ATTACK | Abilities.TARGET | Abilities.THROW),
																	 4f,
																	 native_warrior_rock_weapon,
																	 queues.register(sprite_list_native_warrior, Race.UNIT_WARRIOR_ROCK),
																	 shadow_diameter_warrior,
																	 default_shadow_list,
																	 null,
																	 death_native1_sound,
																	 .25f,
																	 new float[]{1.2f},
																	 1f,
																	 .5f,
																	 Utils.getBundleString(bundle, "rock_warrior"),
																	 1,
																	 0f, 0f, 2f,
																	 3);
		UnitTemplate native_warrior_iron_template = new UnitTemplate(.4f,
																	 1.2f,
																	 new Abilities(Abilities.ATTACK | Abilities.TARGET | Abilities.THROW),
																	 4f,
																	 native_warrior_iron_weapon,
																	 queues.register(sprite_list_native_warrior, Race.UNIT_WARRIOR_IRON),
																	 shadow_diameter_warrior,
																	 default_shadow_list,
																	 null,
																	 death_native2_sound,
																	 .25f,
																	 new float[]{1.2f},
																	 1f,
																	 .7f,
																	 Utils.getBundleString(bundle, "iron_warrior"),
																	 1,
																	 0f, 0f, 2f,
																	 5);
		UnitTemplate native_warrior_rubber_template = new UnitTemplate(.4f,
																	   1.2f,
																	   new Abilities(Abilities.ATTACK | Abilities.TARGET | Abilities.THROW),
																	   4f,
																	   native_warrior_rubber_weapon,
																	   queues.register(sprite_list_native_warrior, Race.UNIT_WARRIOR_RUBBER),
																	 shadow_diameter_warrior,
																	 default_shadow_list,
																	   null,
																	   death_native2_sound,
																	   .25f,
																	   new float[]{1.2f},
																	   1f,
																	   .7f,
																	   Utils.getBundleString(bundle, "chicken_warrior"),
																	   1,
																	   0f, 0f, 2f,
																	   10);
		UnitTemplate viking_peon_template = new UnitTemplate(.4f,
															 1.1f,
															 new Abilities(Abilities.BUILD | Abilities.HARVEST | Abilities.ATTACK | Abilities.TARGET),
															 5f,
															 new InstantHitFactory(1/5f, 0f, 11f/38f, unit_hit_sounds),
															 queues.register(sprite_list_peon),
															 shadow_diameter_peon,
															 default_shadow_list,
															 new UnitSupplyContainerFactory(MAX_UNIT_RESOURCES, viking_supply_sprite_lists),
															 death_peon_sound,
															 .25f,
															 new float[]{.7f},
															 1f,
															 0f,
															 Utils.getBundleString(bundle, "peon"),
															 1,
															 .1f, 0f, 1.75f,
															 1);
		UnitTemplate native_peon_template = new UnitTemplate(.4f,
															 1.1f,
															 new Abilities(Abilities.BUILD | Abilities.HARVEST | Abilities.ATTACK | Abilities.TARGET),
															 5f,
															 new InstantHitFactory(1/5f, 0f, 51f/83f, unit_hit_sounds),
															 queues.register(sprite_list_native_peon),
															 shadow_diameter_peon,
															 default_shadow_list,
															 new UnitSupplyContainerFactory(MAX_UNIT_RESOURCES, native_supply_sprite_lists),
															 death_peon_sound,
															 .25f,
															 new float[]{.7f},
															 1f,
															 0f,
															 Utils.getBundleString(bundle, "peon"),
															 1,
															 0f, 0f, 1.75f,
															 1);
		UnitTemplate viking_chieftain_template = new UnitTemplate(.4f,
																  1.4f,
																  new Abilities(Abilities.ATTACK | Abilities.TARGET | Abilities.MAGIC),
																  4f,
																  new InstantHitFactory(3/4f, 0f, 75f/119f, viking_chieftain_hit_sounds),
																  queues.register(sprite_list_chieftain),
																  shadow_diameter_chieftain,
															 	  default_shadow_list,
																  null,
																  death_viking2_sound,
																  .15f,
																  new float[]{1.7f},
																  1f,
																  0.5f,
																  Utils.getBundleString(bundle, "chieftain"),
																  VIKING_CHIEFTAIN_HIT_POINTS,
																  -.07f, .312f, 2.7f,
																  40);
		UnitTemplate native_chieftain_template = new UnitTemplate(.4f,
																  1.4f,
																  new Abilities(Abilities.ATTACK | Abilities.TARGET | Abilities.MAGIC),
																  4f,
																  new InstantHitFactory(3/4f, 0f, 75f/129f, native_chieftain_hit_sounds),
																  queues.register(sprite_list_native_chieftain),
																  shadow_diameter_chieftain,
															 	  default_shadow_list,
																  null,
																  death_native2_sound,
																  .15f,
																  new float[]{1.7f},
																  1f,
																  0.5f,
																  Utils.getBundleString(bundle, "chieftain"),
																  NATIVE_CHIEFTAIN_HIT_POINTS,
																  .878f, .151f, 2.8f,
																  40);

		MagicFactory[] native_magic = new MagicFactory[NUM_MAGIC];
		native_magic[INDEX_MAGIC_POISON] = new PoisonFogFactory(0.9f, 0f, 0.55f, 26f, .5f, 2f, 20f, 10, 5f, 80f/224f, 163f/224f);
		native_magic[INDEX_MAGIC_LIGHTNING] = new LightningCloudFactory(0.9f, 0f, 0.55f, 22f, 1f, 8f, 1f, 30, 18f, 5f, 80f/224f, 163f/224f);

		MagicFactory[] viking_magic = new MagicFactory[NUM_MAGIC];
		viking_magic[INDEX_MAGIC_STUN] = new StunFactory(2.57f, 0f, 3.8f, 36f, 30f, 10f, 6f, 57f/159f, 100f/159f);
		viking_magic[INDEX_MAGIC_BLAST] = new SonicBlastFactory(2.57f, 0f, 3.8f, 36f, 17f, 2f, 150, 30, .8f, 6f, 57f/159f, 100f/159f);

		ProgressForm.progress(1f/num_progress);
		Icons icons = Icons.getIcons();
		Race natives_race = new Race(native_quarters_template,
				native_armory_template,
				native_tower_template,
				native_warrior_rock_template,
				native_warrior_iron_template,
				native_warrior_rubber_template,
				native_peon_template,
				native_chieftain_template,
				queues.register(new SpriteFile("/geometry/natives/rally_point.binsprite",
							Globals.NO_MIPMAP_CUTOFF,
							true, true, true, false)),
				icons.getNativeIcons(),
				(Audio)Resources.findResource(new AudioFile("/sfx/attacknotify_native.ogg")),
				(Audio)Resources.findResource(new AudioFile("/sfx/buildingnotify_native.ogg")),
				native_magic,
				new NativeChieftainAI(),
				"/music/native.ogg");
		Race vikings_race = new Race(viking_quarters_template,
				viking_armory_template,
				viking_tower_template,
				viking_warrior_rock_template,
				viking_warrior_iron_template,
				viking_warrior_rubber_template,
				viking_peon_template,
				viking_chieftain_template,
				queues.register(new SpriteFile("/geometry/vikings/rally_point.binsprite",
							Globals.NO_MIPMAP_CUTOFF,
							true, true, true, false)),
				icons.getVikingIcons(),
				(Audio)Resources.findResource(new AudioFile("/sfx/attacknotify_viking.ogg")),
				(Audio)Resources.findResource(new AudioFile("/sfx/buildingnotify_viking.ogg")),
				viking_magic,
				new VikingChieftainAI(),
				"/music/viking.ogg");
		races = new Race[]{natives_race, vikings_race};

		wood_fragment_sprites[0] = queues.register(new SpriteFile("/geometry/misc/wood_2.binsprite",
						Globals.NO_MIPMAP_CUTOFF,
						true, true, true, false), 0);
		wood_fragment_sprites[1] = queues.register(new SpriteFile("/geometry/misc/wood_3.binsprite",
						Globals.NO_MIPMAP_CUTOFF,
						true, true, true, false));
		wood_fragment_sprites[2] = queues.register(new SpriteFile("/geometry/misc/wood_4.binsprite",
						Globals.NO_MIPMAP_CUTOFF,
						true, true, true, false));
		wood_fragment_sprites[3] = queues.register(new SpriteFile("/geometry/misc/wood_5.binsprite",
						Globals.NO_MIPMAP_CUTOFF,
						true, true, true, false));

		treasure_sprites[0] = queues.register(new SpriteFile("/geometry/misc/icon.binsprite",
						Globals.NO_MIPMAP_CUTOFF,
						true, true, true, false));
		treasure_sprites[1] = queues.register(new SpriteFile("/geometry/misc/treasure_1.binsprite",
						Globals.NO_MIPMAP_CUTOFF,
						true, true, true, false));
		treasure_sprites[2] = queues.register(new SpriteFile("/geometry/misc/treasure_2.binsprite",
						Globals.NO_MIPMAP_CUTOFF,
						true, true, true, false));
		treasure_sprites[3] = queues.register(new SpriteFile("/geometry/misc/treasure_3.binsprite",
						Globals.NO_MIPMAP_CUTOFF,
						true, true, true, false));
		treasure_sprites[4] = queues.register(new SpriteFile("/geometry/misc/treasure_4.binsprite",
						Globals.NO_MIPMAP_CUTOFF,
						true, true, true, false));
		treasure_sprites[5] = queues.register(new SpriteFile("/geometry/misc/treasure_5.binsprite",
						Globals.NO_MIPMAP_CUTOFF,
						true, true, true, false));

		ProgressForm.progress(1f/num_progress);
		ProgressForm.progress(1f/num_progress);
	}
	
	public final TextureKey[] getSmokeTextures() {
		return smoke_textures;
	}

	public final TextureKey[] getDamageSmokeTextures() {
		return damage_smoke_textures;
	}

	public final TextureKey[] getPoisonTextures() {
		return poison_textures;
	}

	public final TextureKey getLightningTexture() {
		return lightning_texture;
	}

	public final TextureKey[] getSonicTextures() {
		return sonic_textures;
	}

	public final TextureKey[] getNoteTextures() {
		return note_textures;
	}

	public final TextureKey[] getStarTextures() {
		return star_textures;
	}

	public final Audio getHarvestSound(Class key, Random random) {
		Audio[] sounds = (Audio[])harvest_sounds.get(key);
		return sounds[random.nextInt(sounds.length)];
	}

	public final Audio[] getTreeFallSound() {
		return tree_fall_sound;
	}

	public final Audio getBuildingHitSound(Random random) {
		return building_hit_sound[random.nextInt(building_hit_sound.length)];
	}
	
	public final Audio getGasSound() {
		return gas_sound;
	}

	public final Audio getBubblingSound() {
		return bubbling_sound;
	}

	public final Audio getLightningSound() {
		return lightning_sound;
	}

	public final Audio getCloudSound() {
		return cloud_sound;
	}

	public final Audio getStunSound(Random random) {
		return stun_sound[random.nextInt(stun_sound.length)];
	}

	public final Audio getBlastLurSound(Random random) {
		return blast_lur_sound[random.nextInt(blast_lur_sound.length)];
	}

	public final Audio getBlastRumbleSound() {
		return blast_rumble_sound;
	}

	public final Audio getBlastBlastSound() {
		return blast_blast_sound;
	}

	public final Audio getArmorySound() {
		return armory_sound;
	}

	public final Audio getBuildingCollapseSound() {
		return building_collapse_sound;
	}

	public final Race getRace(int i) {
		return races[i];
	}
	
	public final static String getRaceName(int i) {
		return race_names[i];
	}

	public final static int getNumRaces() {
		return race_names.length;
	}

	public final SpriteKey[] getWoodFragments() {
		return wood_fragment_sprites;
	}

	public final SpriteKey[] getTreasures() {
		return treasure_sprites;
	}
}
