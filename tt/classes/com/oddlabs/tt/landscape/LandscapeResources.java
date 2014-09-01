package com.oddlabs.tt.landscape;

import com.oddlabs.geometry.LowDetailModel;
import com.oddlabs.tt.audio.Audio;
import com.oddlabs.tt.audio.AudioFile;
import com.oddlabs.tt.form.ProgressForm;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.procedural.GeneratorRespond;
import com.oddlabs.tt.render.RenderQueues;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.resource.SpriteFile;
import com.oddlabs.tt.resource.TextureFile;
import com.oddlabs.util.Utils;

import java.util.Random;

public final strictfp class LandscapeResources {
	private final SpriteKey[] rock_fragment_sprites = new SpriteKey[5];
	private final SpriteKey[] iron_fragment_sprites = new SpriteKey[5];
	private final SpriteKey[][] plant_sprites;
	private final SpriteKey chicken;
	private final Audio[] bird_idle_sound;
	private final Audio bird_peck_sound;
	private final Audio bird_death_sound;
	
	public LandscapeResources(RenderQueues queues) {
		int num_progress = 13;
		ProgressForm.progress(10f/num_progress);
		
		SpriteFile fragment1 = new SpriteFile("/geometry/misc/rock_1.binsprite", Globals.NO_MIPMAP_CUTOFF, true, true, true, false);
		SpriteFile fragment2 = new SpriteFile("/geometry/misc/rock_2.binsprite", Globals.NO_MIPMAP_CUTOFF, true, true, true, false);
		SpriteFile fragment3 = new SpriteFile("/geometry/misc/rock_3.binsprite", Globals.NO_MIPMAP_CUTOFF, true, true, true, false);
		SpriteFile fragment4 = new SpriteFile("/geometry/misc/rock_4.binsprite", Globals.NO_MIPMAP_CUTOFF, true, true, true, false);
		SpriteFile fragment5 = new SpriteFile("/geometry/misc/rock_5.binsprite", Globals.NO_MIPMAP_CUTOFF, true, true, true, false);

		SpriteFile native_plant1 = new SpriteFile("/geometry/misc/plant_1.binsprite", Globals.NO_MIPMAP_CUTOFF, true, false, true, true, true);
		SpriteFile native_plant2 = new SpriteFile("/geometry/misc/plant_2.binsprite", Globals.NO_MIPMAP_CUTOFF, true, false, true, true, true);
		SpriteFile native_plant3 = new SpriteFile("/geometry/misc/plant_3.binsprite", Globals.NO_MIPMAP_CUTOFF, true, false, true, true, true);
		SpriteFile native_plant4 = new SpriteFile("/geometry/misc/plant_4.binsprite", Globals.NO_MIPMAP_CUTOFF, true, false, true, true, true);
		SpriteFile viking_plant1 = new SpriteFile("/geometry/misc/viking_plant_1.binsprite", Globals.NO_MIPMAP_CUTOFF, true, false, true, true, true);
		SpriteFile viking_plant2 = new SpriteFile("/geometry/misc/viking_plant_2.binsprite", Globals.NO_MIPMAP_CUTOFF, true, false, true, true, true);
		SpriteFile viking_plant3 = new SpriteFile("/geometry/misc/viking_plant_3.binsprite", Globals.NO_MIPMAP_CUTOFF, true, false, true, true, true);
		SpriteFile viking_plant4 = new SpriteFile("/geometry/misc/viking_plant_4.binsprite", Globals.NO_MIPMAP_CUTOFF, true, false, true, true, true);
		ProgressForm.progress(1f/num_progress);

		plant_sprites = new SpriteKey[][]{{
			queues.register(native_plant1),
			queues.register(native_plant2),
			queues.register(native_plant3),
			queues.register(native_plant4)},{
			queues.register(viking_plant1),
			queues.register(viking_plant2),
			queues.register(viking_plant3),
			queues.register(viking_plant4)}};

		rock_fragment_sprites[0] = queues.register(fragment1);
		rock_fragment_sprites[1] = queues.register(fragment2);
		rock_fragment_sprites[2] = queues.register(fragment3);
		rock_fragment_sprites[3] = queues.register(fragment4);
		rock_fragment_sprites[4] = queues.register(fragment5);
		
		iron_fragment_sprites[0] = queues.register(fragment1, 1);
		iron_fragment_sprites[1] = queues.register(fragment2, 1);
		iron_fragment_sprites[2] = queues.register(fragment3, 1);
		iron_fragment_sprites[3] = queues.register(fragment4, 1);
		iron_fragment_sprites[4] = queues.register(fragment5, 1);

		ProgressForm.progress(1f/num_progress);
		SpriteFile sprite_list_chicken = new SpriteFile("/geometry/misc/chicken.binsprite",
				Globals.NO_MIPMAP_CUTOFF,
				true, true, true, false);
		chicken = queues.register(sprite_list_chicken);
		ProgressForm.progress(1f/num_progress);
		
		bird_idle_sound = new Audio[4];
		bird_idle_sound[0] = (Audio)Resources.findResource(new AudioFile("/sfx/chicken_idle1.ogg"));
		bird_idle_sound[1] = (Audio)Resources.findResource(new AudioFile("/sfx/chicken_idle2.ogg"));
		bird_idle_sound[2] = (Audio)Resources.findResource(new AudioFile("/sfx/chicken_idle3.ogg"));
		bird_idle_sound[3] = (Audio)Resources.findResource(new AudioFile("/sfx/chicken_idle4.ogg"));
		bird_peck_sound = (Audio)Resources.findResource(new AudioFile("/sfx/chicken_peck.ogg"));
		bird_death_sound = (Audio)Resources.findResource(new AudioFile("/sfx/chicken_death.ogg"));
	}
	
	public static LowDetailModel[] loadTreeLowDetails() {
		LowDetailModel jungle_lowdetail = (LowDetailModel)Utils.loadObject(Utils.makeURL("/geometry/misc/tree_low.binlowdetail"));
		LowDetailModel palm_lowdetail = (LowDetailModel)Utils.loadObject(Utils.makeURL("/geometry/misc/palm_low.binlowdetail"));
		LowDetailModel oak_lowdetail = (LowDetailModel)Utils.loadObject(Utils.makeURL("/geometry/misc/oak_tree_low.binlowdetail"));
		LowDetailModel pine_lowdetail = (LowDetailModel)Utils.loadObject(Utils.makeURL("/geometry/misc/pine_tree_low.binlowdetail"));
		return new LowDetailModel[]{jungle_lowdetail, palm_lowdetail, oak_lowdetail, pine_lowdetail};
	}

	public final SpriteKey[] getRockFragments() {
		return rock_fragment_sprites;
	}
	
	public final SpriteKey[] getIronFragments() {
		return iron_fragment_sprites;
	}

	public final SpriteKey[][] getPlants() {
		return plant_sprites;
	}

	public final SpriteKey getChicken() {
		return chicken;
	}

	public final Audio getBirdIdleSound(Random random) {
		return bird_idle_sound[random.nextInt(bird_idle_sound.length)];
	}
	
	public final Audio getBirdPeckSound() {
		return bird_peck_sound;
	}
	
	public final Audio getBirdDeathSound() {
		return bird_death_sound;
	}
}
