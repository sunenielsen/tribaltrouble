package com.oddlabs.tt.procedural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.oddlabs.procedural.Channel;
import com.oddlabs.procedural.Layer;
import com.oddlabs.procedural.Tools;
import com.oddlabs.tt.form.ProgressForm;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.resource.BlendInfo;
import com.oddlabs.tt.resource.FogInfo;
import com.oddlabs.tt.resource.BlendLighting;
import com.oddlabs.tt.resource.GLByteImage;
import com.oddlabs.tt.resource.GLIntImage;
import com.oddlabs.tt.resource.StructureBlend;
import com.oddlabs.util.Utils;
import com.oddlabs.tt.landscape.HeightMap;

public final strictfp class Landscape {
	public final static boolean DEBUG = false;
	private final static int STRUCTURE_SEED = 42; // must be constant; otherwise distinct repeating patterns might appear
	
	private final static int NUM_PLANT_TYPES = 4;
	public final static int NATIVE = 0;
	public final static int VIKING = 1;
	
	private Random random;
	private BlendInfo[] blend_infos;
	private GLIntImage[] structures;
	private GLIntImage detail;
	private GLByteImage[] alpha_maps;
	
	private Channel height;
	private Channel slope;
	private Channel access;
	private Channel access_exported;
	private Channel relheight;
	private Channel highlight;
	private Channel shadow;
	private Channel trees;
	private Channel palmtrees;
	private Channel rock;
	private Channel iron;

	private final int size_multiplier;
	private final int num_players;
	private final int meters_per_world;
	private final int meters_per_height_unit;
	private final int unit_grids_per_world;
	private final int height_scale;
	private final float sea_level_meters;
	private final int detail_size;
	private final int structure_size;
	private final float detail_alpha_value;
	private final int features;
	private final float hills;
	private final float vegetation_amount;
	private final float supplies_amount;
	private final int seed;
	private final float area;
	private final int max_trees;
	private final int max_palmtrees;
	private final int max_rock;
	private final int max_iron;
	private final int max_plants;
	private final float access_threshold;
	private final float build_threshold;
	private final int terrain_type;
	
	private byte[][] build;
	private float[][] player_locations;
	private int[][] supply_locations;
	private float[][] plants;

	public Landscape(int num_players, int meters_per_world, int terrain_type, float detail_alpha_value, float hills, float vegetation_amount, float supplies_amount, int seed, int initial_unit_count, float random_start_pos) {

		this.terrain_type = terrain_type;
		hills = (float)StrictMath.sqrt(hills);
		this.num_players = num_players;
		this.features = 4;
		this.hills = hills;
		this.vegetation_amount = 0.25f + 0.75f*vegetation_amount;
		this.supplies_amount = 0.25f + 0.75f*supplies_amount;
		this.seed = seed;
		this.meters_per_world = meters_per_world;
		this.unit_grids_per_world = meters_per_world/HeightMap.METERS_PER_UNIT_GRID;
		this.meters_per_height_unit = meters_per_world/unit_grids_per_world;
		int height_scale = 0;
		float access_threshold = 0f;
		switch (meters_per_world) {
			case 256:
				size_multiplier = 1;
				height_scale = 32;
				access_threshold = 0.05f;
				break;
			case 512:
				size_multiplier = 4;
				height_scale = 48;
				access_threshold = 0.0375f;
				break;
			case 1024:
				size_multiplier = 16;
				height_scale = 64;
				access_threshold = 0.025f;
				break;
			case 2048:
				size_multiplier = 64;
				height_scale = 80;
				access_threshold = 0.0145f;
				break;
			default:
				size_multiplier = 0;
				assert false : "illegal meters_per_world";
				break;
		}
		this.height_scale = height_scale;
		this.access_threshold = access_threshold;
		this.build_threshold = access_threshold/2f;
		this.sea_level_meters = height_scale*Globals.SEA_LEVEL;
		this.detail_size = Globals.DETAIL_SIZE;
		this.structure_size = Globals.STRUCTURE_SIZE;
		this.detail_alpha_value = detail_alpha_value;
		
		area = size_multiplier*10000f;
		max_plants = size_multiplier*64;
		
		if (terrain_type == NATIVE) {
			max_trees = (int)StrictMath.pow(2, 2*Utils.powerOf2Log2(meters_per_world) - 9);
			max_palmtrees = max_trees>>1;
		} else {
			max_trees = (int)(.75f*StrictMath.pow(2, 2*Utils.powerOf2Log2(meters_per_world) - 9));
			max_palmtrees = max_trees;
		}
		
		max_rock = max_trees>>3;
		max_iron = max_trees>>4;
		random = new Random(seed);

		// generate shared voronoi and noise maps
		float c1 = -1f;
		float c2 = 1f;
		float c3 = 0f;
		Voronoi voronoi;
		Channel voronoi4 = new Voronoi(structure_size, 4, 4, 1, 1f, STRUCTURE_SEED).getDistance(c1, c2, c3);
		voronoi = new Voronoi(structure_size, 8, 8, 1, 1f, STRUCTURE_SEED);
		Channel voronoi8 = voronoi.getDistance(c1, c2, c3);
		Channel voronoi8_hit = voronoi.getHitpoint();
		voronoi = new Voronoi(structure_size, 16, 16, 1, 1f, STRUCTURE_SEED);
		Channel voronoi16 = voronoi.getDistance(c1, c2, c3);
		Channel voronoi16_hit = voronoi.getHitpoint();
		voronoi = new Voronoi(structure_size, 32, 32, 1, 1f, STRUCTURE_SEED);
		Channel voronoi32 = voronoi.getDistance(c1, c2, c3);
		Channel voronoi32_hit = voronoi.getHitpoint();
		Channel noise8 = new Midpoint(structure_size, 3, 0.45f, STRUCTURE_SEED).toChannel();
		Channel noise256 = new Midpoint(structure_size, 8, 1f, STRUCTURE_SEED).toChannel();

		switch (terrain_type) {
			case NATIVE:
				generateStructuresNative(voronoi4, voronoi8, voronoi8_hit, voronoi16, voronoi16_hit, voronoi32, voronoi32_hit, noise8, noise256);
				ProgressForm.progress();
				generateTerrainNative();
				break;
			case VIKING:
				generateStructuresViking(voronoi4, voronoi8, voronoi8_hit, voronoi16, voronoi16_hit, voronoi32, voronoi32_hit, noise8, noise256);
				ProgressForm.progress();
				generateTerrainViking();
				break;
			default:
				assert false : "illegal terrain_type";
				break;
		}
		
		if (DEBUG) height.toLayer().saveAsPNG("height");
		ProgressForm.progress();
		Channel grass_alpha = generateAlphas();
		ProgressForm.progress();
		generateUnitLocations(initial_unit_count, random_start_pos);
		generateSupplies(grass_alpha);
		
		// scale height map vertically
		for (int y = 0; y < unit_grids_per_world; y++) {
			for (int x = 0; x < unit_grids_per_world; x++) {
				height.putPixel(x, y, height_scale*height.getPixel(x, y));
			}
		}
		
		if (DEBUG) access.toLayer().saveAsPNG("access_connected");
		
		// create blend infos
		blend_infos = new BlendInfo[]{
			new StructureBlend(structures[0], new GLByteImage(new Channel(1, 1).fill(1f))),
			new StructureBlend(structures[1], alpha_maps[0]),
			new StructureBlend(structures[2], alpha_maps[1]),
			new StructureBlend(structures[3], alpha_maps[2]),
			new StructureBlend(structures[4], alpha_maps[3]),
			new BlendLighting(alpha_maps[4], 1f, 0.9f, 0.6f),
			new StructureBlend(structures[5], alpha_maps[5]),
			new StructureBlend(structures[6], alpha_maps[6])
		};
	}

	// **************
	// * STRUCTURES *
	// **************
	private final void generateStructuresNative(Channel voronoi4, Channel voronoi8, Channel voronoi8_hit, Channel voronoi16, Channel voronoi16_hit, Channel voronoi32, Channel voronoi32_hit, Channel noise8, Channel noise256) {
		structures = new GLIntImage[7];
		ProgressForm.progress(1/8f);

		Layer structure_sand = genSand(structure_size, noise8.copy(), noise256.copy());
		structures[0] = new GLIntImage(structure_sand);

		Layer structure_dirt = genDirt(structure_size, noise8.copy(), noise256.copy(), voronoi32.copy());
		structures[1] = new GLIntImage(structure_dirt);

		Layer structure_rubble = genRubble(structure_size, noise8.copy(), voronoi4.copy(), voronoi8.copy(), voronoi16.copy(), structure_dirt.copy());
		structures[2] = new GLIntImage(structure_rubble);

		Layer structure_grass = genGrass(structure_size, noise8.copy(), noise256.copy());
		structures[4] = new GLIntImage(structure_grass);

		Layer structure_rock = genRock(structure_size, noise8.copy(), noise256.copy(), voronoi4.copy(), voronoi8.copy(), voronoi16.copy(), structure_rubble.copy(), structure_grass.copy());
		structures[3] = new GLIntImage(structure_rock);

		Layer structure_black = genBlack();
		structures[5] = new GLIntImage(structure_black);

		Layer structure_seabottom = genSeabottom(structure_size);
		structures[6] = new GLIntImage(structure_seabottom);

		Layer structure_detail = genDetail(detail_size, detail_alpha_value, STRUCTURE_SEED, noise8.copy());
		detail = new GLIntImage(structure_detail);
	}
	
	private final void generateStructuresViking(Channel voronoi4, Channel voronoi8, Channel voronoi8_hit, Channel voronoi16, Channel voronoi16_hit, Channel voronoi32, Channel voronoi32_hit, Channel noise8, Channel noise256) {
		structures = new GLIntImage[7];
		ProgressForm.progress(1/8f);

		Layer structure_gravel = genGravel(structure_size, noise8.copy(), noise256.copy());
		structures[0] = new GLIntImage(structure_gravel);

		Layer structure_soil = genSoil(structure_size, noise8.copy(), noise256.copy(), voronoi32.copy());
		structures[1] = new GLIntImage(structure_soil);

		Layer structure_grass = genGrass(structure_size, noise8.copy(), noise256.copy()).multiply(.75f, .9f, 1.1f);
		structures[3] = new GLIntImage(structure_grass);
		
		Layer structure_cliff = genCliff(structure_size, noise8.copy(), noise256.copy(), voronoi4.copy(), voronoi8.copy(), voronoi16.copy(), structure_soil.copy(), structure_grass.copy());
		structures[2] = new GLIntImage(structure_cliff);
		
		Layer structure_snow = genSnow(structure_size, noise8.copy(), noise256.copy());
		structures[4] = new GLIntImage(structure_snow);

		Layer structure_black = genBlack();
		structures[5] = new GLIntImage(structure_black);

		Layer structure_seabottom = genSeabottom(structure_size);
		structures[6] = new GLIntImage(structure_seabottom);

		Layer structure_detail = genDetail(detail_size, detail_alpha_value, STRUCTURE_SEED, noise8.copy());
		detail = new GLIntImage(structure_detail);
	}

	private final Layer genSand(int size, Channel noise8, Channel noise256) {
		Channel empty = new Channel(size, size).fill(1f);
		Channel sand_bump1 = noise8.brightness(0.75f);
		Channel sand_bump2 = noise256.brightness(0.25f);
		Layer sand = new Layer(empty.copy(), empty.copy().fill(0.9f), empty.copy().fill(0.8f));
		sand.bump(sand_bump1.channelAdd(sand_bump2), size/1024f, 0f, 0.1f, 1f, 1f, 1f, 0f, 0f, 0f);
		if (DEBUG) sand.saveAsPNG("structure_sand");
		return sand;
	}
	
	private final Layer genGravel(int size, Channel noise8, Channel noise256) {
		Channel empty = new Channel(size, size).fill(1f);
		Channel gravel_bump1 = noise8.brightness(0.5f);
		Channel gravel_bump2 = noise256.brightness(0.5f);
		Layer gravel = new Layer(empty.copy().fill(0.7f), empty.copy().fill(0.55f), empty.copy().fill(0.4f));
		gravel.bump(gravel_bump1.channelAdd(gravel_bump2), size/1024f, 0f, 0.1f, 1f, 1f, 1f, 0f, 0f, 0f);
		if (DEBUG) gravel.saveAsPNG("structure_gravel");
		return gravel;
	}

	private final Layer genDirt(int size, Channel noise8, Channel noise256, Channel voronoi32) {
		Channel empty = new Channel(size, size).fill(1f);
		Channel dirt_bump1 = noise8.brightness(0.8f);
		Channel dirt_bump2 = noise256.brightness(0.1f);
		Channel dirt_bump3 = voronoi32.brightness(0.1f);
		Layer dirt = new Layer(empty.copy(), empty.copy().fill(.7f), empty.copy().fill(0.5f));
		Channel dirt_bump = dirt_bump1.channelAdd(dirt_bump2).channelAdd(dirt_bump3);
		dirt_bump.perturb(noise8, 0.05f);
		dirt.bump(dirt_bump, size/128f, 0f, 0.5f, 1f, 1f, 1f, 0f, 0f, 0f);
		if (DEBUG) dirt.saveAsPNG("structure_dirt");
		return dirt;
	}
	
	private final Layer genSoil(int size, Channel noise8, Channel noise256, Channel voronoi32) {
		Channel empty = new Channel(size, size).fill(1f);
		Channel soil_bump1 = noise8.brightness(0.8f);
		Channel soil_bump2 = noise256.brightness(0.1f);
		Channel soil_bump3 = voronoi32.brightness(0.1f);
		Layer soil = new Layer(empty.copy().fill(.65f), empty.copy().fill(.5f), empty.copy().fill(0.35f));
		Channel soil_bump = soil_bump1.channelAdd(soil_bump2).channelAdd(soil_bump3);
		soil_bump.perturb(noise8, 0.05f);
		soil.bump(soil_bump, size/128f, 0f, 0.5f, 1f, 1f, 1f, 0f, 0f, 0f);
		if (DEBUG) soil.saveAsPNG("structure_soil");
		return soil;
	}

	private final Layer genGrass(int size, Channel noise8, Channel noise256) {
		Channel empty = new Channel(size, size).fill(1f);
		Channel grass_bump = noise8.copy().rotate(90).channelAdd(noise256.brightness(0.05f));
		Layer grass = new Layer(empty.copy().fill(0.2f), empty.copy().fill(0.45f), empty.copy().fill(0f));
		grass.r.channelAdd(noise8.brightness(0.2f));
		grass.bump(grass_bump, size/256f, 0f, 0.6f, 1f, 1f, 1f, 0f, 0f, 0f);
		if (DEBUG) grass.saveAsPNG("structure_grass");
		return grass;
	}

	private final Layer genRubble(int size, Channel noise8, Channel voronoi4, Channel voronoi8, Channel voronoi16, Layer rubble) {
		Channel rubble_bump1 = voronoi4.multiply(0.4f);
		Channel rubble_bump2 = voronoi8.multiply(0.3f);
		Channel rubble_bump3 = voronoi16.multiply(0.2f);
		Channel rubble_bump = rubble_bump1.channelAdd(rubble_bump2).channelAdd(rubble_bump3).dynamicRange();
		rubble_bump.perturb(noise8, .1f);
		rubble.multiply(.9f).bump(rubble_bump, size/128f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 0f);
		if (DEBUG) rubble.saveAsPNG("structure_rubble");
		return rubble;
	}

	private final Layer genRock(int size, Channel noise8, Channel noise256, Channel voronoi4, Channel voronoi8, Channel voronoi16, Layer rubble, Layer grass) {
		Channel rock_bump1 = voronoi4.dynamicRange().contrast(1.1f).brightness(2f).gamma2().multiply(0.35f);
		Channel rock_bump2 = voronoi8.dynamicRange().contrast(1.1f).brightness(2f).gamma2().multiply(0.3f);
		Channel rock_bump3 = voronoi16.dynamicRange().contrast(1.1f).brightness(2f).gamma2().multiply(0.2f);
		Channel rock_bump = rock_bump1.channelAdd(rock_bump2).channelAdd(rock_bump3).channelAdd(noise256.multiply(0.15f));
		rock_bump.dynamicRange().perturb(noise8, .1f);
		Layer rock = rubble.copy();
		rock.toHSV();
		rock.r = noise8.copy().dynamicRange(0.05f, 0.1f);
		rock.toRGB();
		rock.layerBlend(rubble.multiply(1f, 0.8f, 0.6f), noise8.gamma8().invert().contrast(4f));
		rock.layerBlend(grass.multiply(0.5f), noise8.rotate(90).multiply(0.5f));
		rock.bump(rock_bump, size/192f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f);
		rock.gamma2().multiply(0.9f);
		if (DEBUG) rock.saveAsPNG("structure_rock");
		return rock;
	}
	
	private final Layer genCliff(int size, Channel noise8, Channel noise256, Channel voronoi4, Channel voronoi8, Channel voronoi16, Layer rubble, Layer grass) {
		Channel cliff_bump1 = voronoi4.dynamicRange().contrast(1.1f).brightness(2f).gamma2().multiply(0.3f);
		Channel cliff_bump2 = voronoi8.dynamicRange().contrast(1.1f).brightness(2f).gamma2().multiply(0.3f);
		Channel cliff_bump3 = voronoi16.dynamicRange().contrast(1.1f).brightness(2f).gamma2().multiply(0.25f);
		Channel cliff_bump = cliff_bump1.channelAdd(cliff_bump2).channelAdd(cliff_bump3).channelAdd(noise256.multiply(0.15f));
		cliff_bump.dynamicRange().perturb(noise8, .1f);
		Layer cliff = rubble.copy();
		cliff.toHSV();
		cliff.r = noise8.copy().dynamicRange(0.05f, 0.1f);
		cliff.g.multiply(0.75f);
		cliff.toRGB();
		cliff.layerBlend(rubble, noise8.gamma8().invert().contrast(4f));
		cliff.layerBlend(grass, noise8.rotate(90).multiply(0.75f));
		cliff.bump(cliff_bump, size/192f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f);
		cliff.gamma2().multiply(0.9f);
		if (DEBUG) cliff.saveAsPNG("structure_cliff");
		return cliff;
	}
	
	private final Layer genSnow(int size, Channel noise8, Channel noise256) {
		Channel empty = new Channel(size, size).fill(.95f);
		Channel snow_bump1 = noise8.brightness(0.75f);
		Channel snow_bump2 = noise256.brightness(0.25f);
		Layer snow = new Layer(empty.copy(), empty.copy(), empty.copy());
		snow.bump(snow_bump1.channelAdd(snow_bump2), size/1024f, 0f, 0.1f, 1f, 1f, 1f, 0f, 0f, 0f);
		if (DEBUG) snow.saveAsPNG("structure_snow");
		return snow;
	}

	private final Layer genBlack() {
		return new Channel(1, 1).fill(0f).toLayer();
	}

	private final Layer genSeabottom(int size) {
		Layer seabottom = new Layer(
			new Channel(size, size).fill(Globals.SEA_BOTTOM_COLOR[terrain_type][0]),
			new Channel(size, size).fill(Globals.SEA_BOTTOM_COLOR[terrain_type][1]),
			new Channel(size, size).fill(Globals.SEA_BOTTOM_COLOR[terrain_type][2])
		);
		if (DEBUG) seabottom.saveAsPNG("structure_seabottom");
		return seabottom;
	}

	private final Layer genDetail(int size, float detail_alpha_value, int seed, Channel noise8) {
		Channel detail_noise = new Midpoint(size, 4, 0.4f, seed).toChannel();
		detail_noise.perturb(noise8.scale(size, size), 0.05f);
		Channel detail_grey = new Channel(size, size).fill(0.5f);
		detail_grey.bump(detail_noise, size/64f, 0f, 0f, 1f, 0f).dynamicRange();
		Channel detail_alpha = new Channel(size, size).fill(detail_alpha_value);
		Layer detail = new Layer(detail_grey, detail_grey, detail_grey, detail_alpha);
		if (DEBUG) detail.saveAsPNG("structure_detail");
		return detail;
	}


	// ***********
	// * TERRAIN *
	// ***********
	private final void generateTerrainNative() {
		alpha_maps = new GLByteImage[7];

		// generate height map
		height = new Mountain(unit_grids_per_world, Utils.powerOf2Log2(unit_grids_per_world) - 6, 0.5f, seed).toChannel().multiply(0.67f);
		Voronoi voronoi = new Voronoi(unit_grids_per_world, features, features, 1, 1f, seed);
		Channel cliffs = voronoi.getDistance(-1f, 1f, 0f).brightness(1.5f).multiply(0.33f);
		height.channelAdd(cliffs);
		
		// Fist of God (tm)
		if (unit_grids_per_world > 128) {
			height.channelSubtract(voronoi.getDistance(1f, 0f, 0f).gamma(.5f).flipV().rotate(90));
		} else {
			height.channelSubtract(voronoi.getDistance(-1f, 1f, 0f).gamma(.5f).flipV().rotate(90));
		}
		
		height.perturb(new Midpoint(unit_grids_per_world, 2, 0.5f, seed).toChannel(), 0.25f);
		Channel shape = new Hill(unit_grids_per_world, Hill.OVAL).toChannel();
		height.channelAdd(shape.copy().multiply(0.15f));
		height.channelSubtract(shape.copy().invert().multiply(0.5f));
 		height.erode((24f - hills*12f)/unit_grids_per_world, unit_grids_per_world>>2);
		height.channelMultiply(shape.gamma2());
		height.smooth(1);
		height = beaches(height);
		
		// fix edges
		for (int y = 0; y < unit_grids_per_world; y += (unit_grids_per_world - 1)) {
			for (int x = 0; x < unit_grids_per_world; x++) {
				height.putPixel(x, y, 0f);
			}
		}
		for (int y = 1; y < unit_grids_per_world - 1; y++) {
			for (int x = 0; x < unit_grids_per_world; x += (unit_grids_per_world - 1)) {
				height.putPixel(x, y, 0f);
			}
		}

		slope = height.copy().lineart();
		if (DEBUG) slope.copy().dynamicRange().toLayer().saveAsPNG("slope");
		relheight = height.copy().relativeIntensityNormalized(StrictMath.max(1, unit_grids_per_world>>5));
		if (DEBUG) relheight.toLayer().saveAsPNG("relheight");
		access = generateThresholdMap(slope, access_threshold).largestConnected(1f);
		access_exported = access.copy();
		if (DEBUG) access.toLayer().saveAsPNG("access");
		build = generateBuildMap(generateThresholdMap(slope, build_threshold).channelMultiply(access));
	}
	
	private final void generateTerrainViking() {
		alpha_maps = new GLByteImage[7];
		
		// generate height map
		height = new Mountain(unit_grids_per_world, Utils.powerOf2Log2(unit_grids_per_world) - 6, 0.5f, seed).toChannel().add(.5f).dynamicRange().gamma2().multiply(0.67f);
		
		Voronoi voronoi = new Voronoi(unit_grids_per_world, 8, 8, 1, 1f, seed, true);
		Channel cliffs = voronoi.getDistance(-1f, 1f, 0f).brightness(1.25f).multiply(0.33f);
		height.channelAdd(cliffs).dynamicRange();
		
		// Fist of God (tm)
		Voronoi voronoi2 = new Voronoi(unit_grids_per_world, 4, 4, 1, 1f, seed);
		if (unit_grids_per_world > 128) {
			height.channelSubtract(voronoi2.getDistance(1f, 0f, 0f).gamma(.5f).multiply(.5f));
		} else {
			height.channelSubtract(voronoi2.getDistance(-1f, 1f, 0f).gamma(.5f).multiply(.5f));
		}
		
		Channel hitpoint = voronoi.getHitpoint().smooth(1);
		Channel hitpoint2 = hitpoint.copy().erodeThermal(4f/unit_grids_per_world, unit_grids_per_world>>3);
		Channel noise = new Midpoint(unit_grids_per_world, 3, 0.25f, seed).toChannel().threshold(0.75f*hills, 1f);
		Channel heightcut = hitpoint.channelMultiply(noise.copy().invert()).channelAdd(hitpoint2.copy().channelMultiply(noise));
		height.channelMultiply(heightcut);
		height.perturb(new Midpoint(unit_grids_per_world, 2, 0.5f, seed).toChannel(), 0.25f);
		height.erode((24f - hills*12f)/unit_grids_per_world, unit_grids_per_world>>2);
		
		Channel shape = new Hill(unit_grids_per_world, Hill.SQUARE).toChannel().smoothGain().gamma8();
		height.channelMultiply(shape);
		
		// add roughness to inaccessible areas
		slope = height.copy().lineart();
		Channel peakarea = slope.threshold(0f, access_threshold).largestConnected(1f).invert().channelMultiply(hitpoint2);
		Channel peaks = new Midpoint(unit_grids_per_world, 4, 0.75f, 42).toChannel().channelMultiply(peakarea).multiply(.1f);
		height.channelAdd(peaks);
		height.smooth(1);
		
		// fix edges
		for (int y = 0; y < unit_grids_per_world; y += (unit_grids_per_world - 1)) {
			for (int x = 0; x < unit_grids_per_world; x++) {
				height.putPixel(x, y, 0f);
			}
		}
		for (int y = 1; y < unit_grids_per_world - 1; y++) {
			for (int x = 0; x < unit_grids_per_world; x += (unit_grids_per_world - 1)) {
				height.putPixel(x, y, 0f);
			}
		}

		slope = height.copy().lineart();
		if (DEBUG) slope.copy().dynamicRange().toLayer().saveAsPNG("slope");
		relheight = height.copy().relativeIntensityNormalized(StrictMath.max(1, unit_grids_per_world>>5));
		if (DEBUG) relheight.toLayer().saveAsPNG("relheight");
		access = generateThresholdMap(slope, access_threshold).largestConnected(1f);
		access_exported = access.copy();
		if (DEBUG) access.toLayer().saveAsPNG("access");
		build = generateBuildMap(generateThresholdMap(slope, build_threshold).channelMultiply(access));
	}
	
	// shape beaches
	private final Channel beaches(Channel channel) {
		float sealevel = 1.1f*Globals.SEA_LEVEL;
		float threshold = 2f*sealevel;
		for (int y = 0; y < channel.height; y++) {
			for (int x = 0; x < channel.width; x++) {
				float value = channel.getPixel(x, y);
				if (value < sealevel) {
					value = Tools.interpolateSmooth(0, sealevel, value/sealevel);
					channel.putPixel(x, y, value);
				} else if (value < threshold) {
					value = Tools.interpolateSmooth(sealevel, 2f*threshold - sealevel, 0.5f*(value - sealevel)/(threshold - sealevel));
					channel.putPixel(x, y, value);
				}
			}
		}
		return channel;
	}
	
	// generate threshold map
	private final Channel generateThresholdMap(Channel slopemap, float threshold) {
		Channel channel = slopemap.copy().threshold(0f, threshold).channelSubtract(height.copy().threshold(0f, Globals.SEA_LEVEL));
		// fix edges
		for (int y = 0; y < unit_grids_per_world; y += (unit_grids_per_world - 1)) {
			for (int x = 0; x < unit_grids_per_world; x++) {
				channel.putPixel(x, y, 0f);
			}
		}
		for (int y = 1; y < unit_grids_per_world - 1; y++) {
			for (int x = 0; x < unit_grids_per_world; x += (unit_grids_per_world - 1)) {
				channel.putPixel(x, y, 0f);
			}
		}
		return channel;
	}

	// generate build map
	private final byte[][] generateBuildMap(Channel thresholdmap) {
		if (DEBUG) thresholdmap.toLayer().saveAsPNG("build_tresholdmap");
		int size = thresholdmap.getWidth();
		boolean build_grid[][] = new boolean[size][size];
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				build_grid[y][x] = thresholdmap.getPixel(x, y) > 0;
			}
		}
		byte[][] byte_grid = new byte[build_grid.length][build_grid[0].length];
		byte max = (byte)StrictMath.max(RacesResources.QUARTERS_SIZE, StrictMath.max(RacesResources.ARMORY_SIZE, RacesResources.TOWER_SIZE));
		for (byte i = 0; i < max; i++) {
			for (int y = 1; y < build_grid.length - 1; y++) {
				for (int x = 1; x < build_grid[y].length - 1; x++) {
					if (!build_grid[y][x] && byte_grid[y][x] == i) {
						for (int k = -1; k <= 1; k++) {
							for (int l = -1; l <= 1;  l++) {
								if (build_grid[y + k][x + l]) {
									build_grid[y + k][x + l] = false;
									byte_grid[y + k][x + l] = (byte)(i + 1);
								}
							}
						}
					}
				}
			}
		}
		for (int y = 1; y < build_grid.length - 1; y++) {
			for (int x = 1; x < build_grid[y].length - 1; x++) {
				if (build_grid[y][x])
					byte_grid[y][x] = max;
			}
		}
		return byte_grid;
	}


	// **********
	// * ALPHAS *
	// **********
	private final Channel generateAlphas() {
		int seed = Globals.LANDSCAPE_SEED;
		Channel alpha0, alpha1, alpha2, alpha3;
		Channel grass_alpha = new Channel(1, 1);
		
		switch (terrain_type) {
			case NATIVE:
				alpha0 = generateDirtAlpha();
				if (DEBUG) alpha0.toLayer().saveAsPNG("alpha_dirt");
				alpha1 = generateRubbleAlpha();
				if (DEBUG) alpha1.toLayer().saveAsPNG("alpha_rubble");
				alpha2 = generateRockAlpha();
				if (DEBUG) alpha2.toLayer().saveAsPNG("alpha_rock");
				alpha3 = generateGrassAlpha(unit_grids_per_world, seed);
				if (DEBUG) alpha3.toLayer().saveAsPNG("alpha_grass");
				alpha_maps[0] = new GLByteImage(alpha0);
				alpha_maps[1] = new GLByteImage(alpha1);
				alpha_maps[2] = new GLByteImage(alpha2);
				alpha_maps[3] = new GLByteImage(alpha3);
				grass_alpha = alpha3;
				break;
			case VIKING:
				alpha0 = generateSoilAlpha();
				if (DEBUG) alpha0.toLayer().saveAsPNG("alpha_soil");
				alpha1 = generateCliffAlpha();
				if (DEBUG) alpha1.toLayer().saveAsPNG("alpha_cliff");
				alpha2 = generateGrassAlpha(unit_grids_per_world, seed);
				if (DEBUG) alpha2.toLayer().saveAsPNG("alpha_grass");
				alpha3 = generateSnowAlpha(alpha1.copy());
				if (DEBUG) alpha3.toLayer().saveAsPNG("alpha_snow");
				alpha_maps[0] = new GLByteImage(alpha0);
				alpha_maps[1] = new GLByteImage(alpha1);
				alpha_maps[2] = new GLByteImage(alpha2);
				alpha_maps[3] = new GLByteImage(alpha3);
				grass_alpha = alpha2;
				break;
			default:
				assert false : "illegal terrain_type";
				break;
		}
		Channel seabottom_alpha = generateSeabottomAlpha();
		if (DEBUG) seabottom_alpha.toLayer().saveAsPNG("alpha_seabottom");

		// generate shadow and highlight alpha
		shadow = new Channel(unit_grids_per_world, unit_grids_per_world);
		highlight = new Channel(unit_grids_per_world, unit_grids_per_world);
		float lx = 1;
		float lz = 1;
		float lnorm = 1f/(float)StrictMath.sqrt(lx*lx + lz*lz);
		lx = lx*lnorm;
		lz = lz*lnorm;
		float threshold = (float)StrictMath.sqrt(0.5f);
		float nz = 2f*meters_per_height_unit/height_scale;
		float nzlz = nz*lz;
		float nz2 = nz*nz;
		for (int y = 0; y < unit_grids_per_world; y++) {
			for (int x = 0; x < unit_grids_per_world; x++) {
				float nx = height.getPixelWrap(x + 1, y) - height.getPixelWrap(x - 1, y);
				float ny = height.getPixelWrap(x, y + 1) - height.getPixelWrap(x, y - 1);
				float light = (nx*lx + nzlz)/((float)Math.sqrt(nx*nx + ny*ny + nz2)); // Can use Math here - calculation is not game state affecting
				if (light > threshold) {
					highlight.putPixel(x, y, light);
					shadow.putPixel(x, y, threshold);
				} else {
					highlight.putPixel(x, y, threshold);
					shadow.putPixel(x, y, StrictMath.max(0, light));
				}
			}
		}
		highlight.dynamicRange(0f, 0.25f);
		shadow.invert().dynamicRange(0f, 0.75f);
		ProgressForm.progress(1/14f);

		// generate shadowcasting
		Channel shadowcast = new Channel(unit_grids_per_world, unit_grids_per_world);
		float val = 0;
		float peak = 0;
		float descent = 8f/unit_grids_per_world;
		for (int y = 0; y < unit_grids_per_world; y++) {
			for (int x = 0; x < unit_grids_per_world; x++) {
				val = height.getPixel(x, y);
				peak = peak - descent;
				if (peak > val) {
					shadowcast.putPixel(x, y, 1f);
				} else {
					peak = val;
				}
			}
			peak = 0;
		}
		shadow.channelBrightest(shadowcast.smooth(1).brightness(0.67f));
		if (DEBUG) highlight.toLayer().saveAsPNG("alpha_light");
		if (DEBUG) shadow.toLayer().saveAsPNG("alpha_shadow");
		ProgressForm.progress(1/14f);
		
		alpha_maps[6] = new GLByteImage(seabottom_alpha);

		return grass_alpha;
	}
	
	// generate dirt alpha
	private final Channel generateDirtAlpha() {
		Channel dirt_alpha = height.copy().dynamicRange(1.1f*Globals.SEA_LEVEL, 2f*Globals.SEA_LEVEL, 0f, 1f);
		dirt_alpha.channelSubtract(relheight.copy().invert().dynamicRange(0.5f, 0.6f, 0f, 0.5f));
		return dirt_alpha;
	}

	// generate rubble alpha
	private final Channel generateRubbleAlpha() {
		Channel rubble_alpha = slope.copy().dynamicRange(build_threshold, access_threshold, 0f, 1f);
		rubble_alpha.channelSubtract(height.copy().invert().dynamicRange(0.8f, 1f, 0f, 1f));
		rubble_alpha.channelSubtract(relheight.copy().invert().dynamicRange(0.5f, 0.65f, 0f, 0.5f));
		return rubble_alpha;
	}

	// generate rock alpha
	private final Channel generateRockAlpha() {
		Channel rock_alpha = slope.copy().threshold(access_threshold, 1f);
		return rock_alpha;
	}

	// generate grass alpha
	private final Channel generateGrassAlpha(int size, int seed) {
		Channel grass_alpha = new Midpoint(size, 4, 0.45f, seed).toChannel().dynamicRange(1f - vegetation_amount, 1f, 0f, 1f).gamma2();
		grass_alpha.channelBrightest(slope.copy().dynamicRange(0f, access_threshold, 0f, 1f).invert().dynamicRange(1f - vegetation_amount, 1f, 0f, 1f).gamma2());
		grass_alpha.channelAdd(relheight.copy().invert().add(-0.5f).multiply(2f));
		grass_alpha.channelSubtract(height.copy().invert().dynamicRange(0.6f, 0.8f, 0f, 1f));
		grass_alpha.channelSubtract(slope.copy().threshold(0.75f*access_threshold, 1f).smooth(3));
		grass_alpha.channelSubtract(relheight.copy().invert().dynamicRange(0.5f, 0.7f, 0f, 0.5f));
		return grass_alpha;
	}
	
	// generate soil alpha
	private final Channel generateSoilAlpha() {
		Channel soil_alpha = height.copy().dynamicRange(1.1f*Globals.SEA_LEVEL, 2f*Globals.SEA_LEVEL, 0f, 1f);
		soil_alpha.channelSubtract(relheight.copy().invert().dynamicRange(0.5f, 0.6f, 0f, 0.5f));
		return soil_alpha;
	}
	
	// generate cliff alpha
	private final Channel generateCliffAlpha() {
		Channel cliff_alpha = slope.copy().threshold(access_threshold, 1f);
		return cliff_alpha;
	}
	
	// generate snow alpha
	private final Channel generateSnowAlpha(Channel cliff_alpha) {
		Channel snow_alpha = height.copy().dynamicRange(0.5f, 0.6f, 0f, 1f);
		snow_alpha.channelSubtract(cliff_alpha);
		snow_alpha.smooth(1).smooth(1);
		
		return snow_alpha;
	}

	// generate seabottom alpha
	private final Channel generateSeabottomAlpha() {
		Channel seabottom_alpha = height.copy().invert().dynamicRange(1f - Globals.SEA_LEVEL, 1f, 0f, 1f);
		switch (terrain_type) {
			case NATIVE:
				seabottom_alpha.grow(0f, 1).gamma(0.5f);
				break;
			case VIKING:
				seabottom_alpha.gamma(0.5f);
				break;
			default:
				assert false : "illegal terrain_type";
				break;
		}
		return seabottom_alpha;
	}

	// ************
	// * SUPPLIES *
	// ************
	private final void generateSupplies(Channel grass_alpha) {
		// generate overall probability map for rock and iron resources
		Channel centerprob = new Hill(unit_grids_per_world, Hill.CIRCLE).toChannel().addClip(-.5f).dynamicRange();
		
		// generate (oak)tree/palmtree(/pine) maps
		Channel noise = new Midpoint(unit_grids_per_world, Utils.powerOf2Log2(unit_grids_per_world) - 3, 0.33f, seed).toChannel();
		Channel tree_channel = grass_alpha.copy();
		Channel palmtree_channel = height.copy();
		ProgressForm.progress(1/14f);
		
		switch (terrain_type) {
			case NATIVE:
				tree_channel.threshold(0.5f, 1f);
				tree_channel.channelAdd(noise.rotate(90).copy().threshold(0.9f, 1f));
				tree_channel.channelSubtract(slope.copy().threshold(access_threshold, 1f));
				tree_channel.channelSubtract(noise.copy().dynamicRange(0.1f, 1f));
				
				palmtree_channel.invert().dynamicRange(0.6f, 0.89f, 0f, 1f);
				palmtree_channel.channelSubtract(height.copy().invert().dynamicRange(0.89f, 0.9f, 0f, 1f));
				palmtree_channel.channelSubtract(noise.rotate(90)).channelAdd(noise.rotate(90).copy().threshold(0.9f, 1f)).channelSubtract(slope.copy().threshold(access_threshold, 1f));
				break;
			case VIKING:
				tree_channel.gamma8();
				tree_channel.channelMultiply(height.copy().dynamicRange(0.55f, 0.65f, 1f, 0f));
				tree_channel.channelSubtract(slope.copy().threshold(access_threshold, 1f));
				tree_channel.channelSubtract(noise.copy());
				
				palmtree_channel = grass_alpha.copy().channelMultiply(height.copy().dynamicRange(0.5f, 0.6f, 1f, 0f)).invert();
				palmtree_channel.channelSubtract(height.copy().invert().dynamicRange(0.8f, 0.875f, 0f, 1f));
				palmtree_channel.channelSubtract(slope.copy().threshold(access_threshold, 1f));
				palmtree_channel.channelSubtract(noise.rotate(90));
				break;
			default:
				assert false : "illegal terrain_type";
				break;
		}
		
		if (DEBUG) tree_channel.toLayer().saveAsPNG("supplies_trees");
		if (DEBUG) palmtree_channel.toLayer().saveAsPNG("supplies_palmtrees");
		ProgressForm.progress(1/14f);
		
		// generate rock and iron supplies map
		Channel rock_channel = relheight.copy();
		
		switch (terrain_type) {
			case NATIVE:
				rock_channel.invert().threshold(0.5f, 1f).channelMultiply(noise.rotate(90).copy().gamma8().invert()).channelMultiply(centerprob);
				break;
			case VIKING:
				rock_channel.threshold(0f, 0.5f).channelMultiply(noise.rotate(90).copy().gamma8().invert());
				break;
			default:
				assert false : "illegal terrain_type";
				break;
		}
		
		Channel iron_channel = rock_channel.copy().rotate(90).flipV();
		if (DEBUG) rock_channel.toLayer().saveAsPNG("supplies_rocks");
		if (DEBUG) iron_channel.toLayer().saveAsPNG("supplies_iron");
		
		Channel supplies = access.copy();
		float accessible = supplies.sum();
		
		// place trees
		trees = placeSupplies(tree_channel, supplies, 64, (int)(vegetation_amount*max_trees*(accessible/area)), 0.33f);
		access.channelSubtract(trees);
		if (DEBUG) trees.toLayer().saveAsPNG("supplies_trees_placed");
		
		// place palmtrees
		palmtrees = placeSupplies(palmtree_channel, supplies, 64, (int)(vegetation_amount*max_palmtrees*(accessible/area)), 0.25f);
		access.channelSubtract(palmtrees);
		if (DEBUG) palmtrees.toLayer().saveAsPNG("supplies_palmtrees_placed");

		// place rock
		rock = placeSupplies(rock_channel, supplies, 64, (int)(supplies_amount*max_rock), 0f);
		access.channelSubtract(rock);
		shadow.channelBrightest(rock.copy().multiply(0.5f));
		if (DEBUG) rock.toLayer().saveAsPNG("supplies_rock_placed");

		// place iron
		iron = placeSupplies(iron_channel, supplies, 64, (int)(supplies_amount*max_iron), 0f);
		access.channelSubtract(iron);
		shadow.channelBrightest(iron.copy().multiply(0.5f));
		if (DEBUG) iron.toLayer().saveAsPNG("supplies_iron_placed");

		/* Team Penguin */
		//if (DEBUG) {
			System.out.println("Number of trees placed: " + trees.count(1f));
			System.out.println("Number of palmtrees placed: " + palmtrees.count(1f));
			System.out.println("Number of rocks placed: " + rock.count(1f));
			System.out.println("Number of iron ore placed: " + iron.count(1f));
		//}
		/* End Penguin */
		
		// place extra supplies around starting locations
		int num_rock = 2;
		int num_iron = 1;
		for (int p = 0; p < num_players; p++) {
			for (int r = 0; r < num_rock; r++) {
				int[] location = access.find((unit_grids_per_world>>1), supply_locations[p][0], supply_locations[p][1], 1f);
				rock.putPixel(location[0], location[1], 1f);
				access.putPixel(location[0], location[1], 0f);
			}
			for (int i = 0; i < num_iron; i++) {
				int[] location = access.find((unit_grids_per_world>>1), supply_locations[p][0], supply_locations[p][1], 1f);
				iron.putPixel(location[0], location[1], 1f);
				access.putPixel(location[0], location[1], 0f);
			}
		}
		
		// shadow and highlight are changed by supply placement
		alpha_maps[4] = new GLByteImage(highlight, GL11.GL_LUMINANCE);
		alpha_maps[5] = new GLByteImage(shadow);
		ProgressForm.progress(1/14f);
		
		// generate plant maps
		plants = new float[NUM_PLANT_TYPES][max_plants<<1];
		tree_channel.channelBrightest(palmtree_channel).brightness(.5f);
		noise.scaleFast(noise.width>>1, noise.height>>1).tileDouble();
		
		Channel plantsmap = grass_alpha.copy();
		plantsmap.multiply(0.25f).add(0.75f);
		plantsmap.channelMultiply(slope.copy().threshold(0f, access_threshold));
		plantsmap.channelMultiply(height.copy().threshold(Globals.SEA_LEVEL, 1f));
		
		Channel plants1 = plantsmap.copy().channelMultiply(noise.copy());
		Channel plants2 = plantsmap.copy().channelMultiply(noise.rotate(90));
		Channel plants3 = plantsmap.copy().channelMultiply(noise.rotate(90));
		Channel plants4 = plantsmap.copy().channelMultiply(noise.rotate(90));
		Channel place = new Channel(unit_grids_per_world, unit_grids_per_world);
		place = placePlants(plants1, place, 64, max_plants>>2, 0);
		place = placePlants(plants2, place, 64, max_plants>>2, 1);
		place = placePlants(plants3, place, 64, max_plants>>2, 2);
		place = placePlants(plants4, place, 64, max_plants>>2, 3);
	}

	// place supplies on map
	private final Channel placeSupplies(Channel probability, Channel supplies, int intervals, int max_count, float shadow_alpha_val) {
		max_count = StrictMath.min(probability.width*probability.height, max_count);
		int scaleshift = Utils.powerOf2Log2(unit_grids_per_world*meters_per_height_unit/meters_per_world);
		int i = 0;
		float interval_size = 1f/intervals;
		float upper_bound = 1f;
		float lower_bound = upper_bound - interval_size;
		int supplyshadow_size = StrictMath.max(unit_grids_per_world>>7, 2);
		Channel supplyshadow_alpha = new Channel(supplyshadow_size<<1, supplyshadow_size<<1).place(new Channel(supplyshadow_size, supplyshadow_size).fill(1f), supplyshadow_size>>1, supplyshadow_size>>1).smoothFast();
		Channel supplyshadow = new Channel(supplyshadow_size<<1, supplyshadow_size<<1);
		Channel supplyshadow_alpha2 = supplyshadow_alpha.copy().brightness(shadow_alpha_val);
		Channel place = new Channel(probability.width, probability.height);

		// place supplies
		out:
		while (i < max_count && lower_bound > interval_size) {
			for (int y = 1; y < probability.height - 1; y++) {
				for (int x = 1; x < probability.width - 1; x++) {
					float val = probability.getPixel(x, y);
					if (val <= upper_bound && val > lower_bound) {
						// place resource if grid and its 4 neighbours are unoccupied
						if (supplies.getPixel(x, y) > 0
							&& supplies.getPixel(x - 1, y) > 0
							&& supplies.getPixel(x + 1, y) > 0
							&& supplies.getPixel(x, y - 1) > 0
							&& supplies.getPixel(x, y - 1) > 0
						) {
							place.putPixel(x, y, 1f);
							// place shadow
							if (shadow_alpha_val > 0f) {
								int x_pixel = (x - supplyshadow_size + 1)>>scaleshift;
								int y_pixel = (y - supplyshadow_size + 1)>>scaleshift;
								highlight.place(supplyshadow, supplyshadow_alpha, x_pixel, y_pixel);
								shadow.placeBrightest(supplyshadow_alpha2, x_pixel, y_pixel);
							}
							// make node neighbourhood inaccessible
							for (int k = -1; k <= 1; k++) {
								for (int l = -1; l <= 1; l++) {
									supplies.putPixelWrap(x + k, y + l, 0f);
								}
							}
							
							i++;
							if (i >= max_count) break out;
						}
					}
				}
			}
			lower_bound -= interval_size;
			upper_bound -= interval_size;
		}
		return place;
	}
	
	// place plants on map
	private final Channel placePlants(Channel probability, Channel place, int intervals, int max_count, int plant_type) {
		max_count = StrictMath.min(probability.width*probability.height, max_count);
		int i = 0;
		float interval_size = 1f/intervals;
		float upper_bound = 1f;
		float lower_bound = upper_bound - interval_size;

		// place plants
		out:
		while (i < max_count && lower_bound > interval_size) {
			for (int y = 2; y < probability.height - 2; y++) {
				for (int x = 2; x < probability.width - 2; x++) {
					float val = probability.getPixel(x, y);
					if (val <= upper_bound && val > lower_bound && place.getPixel(x, y) < 2 && random.nextFloat() < .25) {
						// place plant
						plants[plant_type][2*i] = meters_per_height_unit*(x + random.nextFloat());
						plants[plant_type][2*i+1] = meters_per_height_unit*(y + random.nextFloat());
						
						place.putPixelWrap(x, y, place.getPixel(x, y) + 1f);
						
						/*
						// make node neighbourhood inaccessible
						for (int k = -1; k <= 1; k++) {
							for (int l = -1; l <= 1; l++) {
								place.putPixelWrap(x + k, y + l, place.getPixel(x + k, y + l) + 1f);
							}
						}
						*/
						
						i++;
						if (i >= max_count) break out;
					}
				}
			}
			lower_bound -= interval_size;
			upper_bound -= interval_size;
		}
		return place;
	}


	// ******************
	// * UNIT LOCATIONS *
	// ******************
	private final void generateUnitLocations(int initial_unit_count, float random_start_pos) {
		// create building placement map
		Channel buildmap = new Channel(access.width, access.height);
		Channel buildmap_debug = new Channel(access.width, access.height);
		for (int y = 0; y < access.height; y++) {
			for (int x = 0; x < access.width; x++) {
				if (build[y][x] == RacesResources.QUARTERS_SIZE) {
					buildmap.putPixel(x, y, 1f);
				}
				if (DEBUG) buildmap_debug.putPixel(x, y, .2f*build[y][x]);
			}
		}
		if (DEBUG) buildmap.toLayer().saveAsPNG("buildmap");
		if (DEBUG) buildmap_debug.toLayer().saveAsPNG("buildmap_debug");
		// find initial starting locations
		player_locations = new float[num_players][2*initial_unit_count];
		supply_locations = new int[num_players][2];
		float angle = 0.5f*(float)StrictMath.PI;
		angle += random_start_pos*(float)StrictMath.PI*2; // random start for multiplayer games
		float angle_step = 2f*(float)StrictMath.PI/num_players;
		float radius = 0.35f*unit_grids_per_world;
		int scale = meters_per_world/unit_grids_per_world;
		int[] location_quarters = new int[2];
		int[] location_armory = new int[2];
		for (int i = 0; i < num_players; i++) {
			int x = (int)(radius*(float)StrictMath.cos(angle) + (unit_grids_per_world>>1) + 0.5f);
			int y = (int)(radius*(float)StrictMath.sin(angle) + (unit_grids_per_world>>1) + 0.5f);
			angle += angle_step;
			location_quarters = buildmap.findNoWrap((unit_grids_per_world>>1), x, y, 1f);
			for (int k = -(RacesResources.QUARTERS_SIZE/* - 1*/); k <= (RacesResources.QUARTERS_SIZE/* - 1*/); k++) {
				for (int l = -(RacesResources.QUARTERS_SIZE/* - 1*/); l <= (RacesResources.QUARTERS_SIZE/* - 1*/); l++) {
					access.putPixelWrap(location_quarters[0] + k, location_quarters[1] + l, 0f);
					buildmap.putPixelWrap(location_quarters[0] + k, location_quarters[1] + l, 0f);
				}
			}
			location_armory = buildmap.find((unit_grids_per_world>>1), location_quarters[0], location_quarters[1], 1f);
			for (int k = -(RacesResources.ARMORY_SIZE/* - 1*/); k <= (RacesResources.ARMORY_SIZE/* - 1*/); k++) {
				for (int l = -(RacesResources.ARMORY_SIZE/* - 1*/); l <= (RacesResources.ARMORY_SIZE/* - 1*/); l++) {
					access.putPixelWrap(location_armory[0] + k, location_armory[1] + l, 0f);
					buildmap.putPixelWrap(location_armory[0] + k, location_armory[1] + l, 0f);
				}
			}
			int[] location_unit_start = access.find((unit_grids_per_world>>1), location_quarters[0], location_quarters[1], 1f);
			supply_locations[i][0] = location_armory[0];
			supply_locations[i][1] = location_armory[1];
			int[] location_unit = new int[2];
			for (int u = 0; u < initial_unit_count; u++) {
				location_unit = access.find((unit_grids_per_world>>1), location_unit_start[0], location_unit_start[1], 1f);
				access.putPixelWrap(location_unit[0], location_unit[1], 0f);
				player_locations[i][2*u] = (float)(location_unit[0]*scale);
				player_locations[i][2*u + 1] = (float)(location_unit[1]*scale);
			}
		}
		
		// shuffle player starting locations
		List player_locations_list = Arrays.asList(player_locations);
		Collections.shuffle(player_locations_list, random);
	}


	// ***************
	// * GET METHODS *
	// ***************

	public static FogInfo getFogInfo(int terrain_type, int meters_per_world) {
		switch (terrain_type) {
			case NATIVE:
				return new FogInfo(new float[] {.65f, .75f, 1f, 1f}, GL11.GL_EXP2, 1.3f*meters_per_world, .0015f, 0f, meters_per_world>>2);
			case VIKING:
				return new FogInfo(new float[] {.2f, .4f, .55f, 1f}, GL11.GL_EXP2, 1.3f*meters_per_world, .0015f, 0f, meters_per_world>>2);
			default:
				throw new RuntimeException("Unknown terrain type: " + terrain_type);
		}
	}
	
	public final BlendInfo[] getBlendInfos() {
		return blend_infos;
	}

	public final GLIntImage getDetail() {
		return detail;
	}

	public final float[][] getHeight() {
		return height.getPixels();
	}

	public final boolean[][] getAccessGrid() {
		int size = access_exported.getWidth();
		boolean access_grid[][] = new boolean[size][size];
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				access_grid[y][x] = access_exported.getPixel(x, y) > 0;
			}
		}
		return access_grid;
	}

	public final byte[][] getBuildGrid() {
		return build;
	}

	public final List getTrees() {
		List list = new ArrayList();
		for (int y = 0; y < trees.height; y++) {
			for (int x = 0; x < trees.width; x++) {
				if (trees.getPixel(x, y) == 1f) {
					list.add(new int[]{x, y});
				}
			}
		}
		return list;
	}

	public final List getPalmtrees() {
		List list = new ArrayList();
		for (int y = 0; y < palmtrees.height; y++) {
			for (int x = 0; x < palmtrees.width; x++) {
				if (palmtrees.getPixel(x, y) == 1f) {
					list.add(new int[]{x, y});
				}
			}
		}
		return list;
	}

	public final List getRock() {
		List list = new ArrayList();
		for (int y = 0; y < rock.height; y++) {
			for (int x = 0; x < rock.width; x++) {
				if (rock.getPixel(x, y) == 1f) {
					list.add(new int[]{x, y});
				}
			}
		}
		return list;
	}

	public final List getIron() {
		List list = new ArrayList();
		for (int y = 0; y < iron.height; y++) {
			for (int x = 0; x < iron.width; x++) {
				if (iron.getPixel(x, y) == 1f) {
					list.add(new int[]{x, y});
				}
			}
		}
		return list;
	}
	
	public final float[][] getPlants() {
		return plants;
	}

	public final float[][] getStartingLocations() {
		return player_locations;
	}

	public final float getSeaLevelMeters() {
		return sea_level_meters;
	}

}
