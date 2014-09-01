package com.oddlabs.tt.landscape;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.form.ProgressForm;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Icons;
import com.oddlabs.tt.model.AbstractElementNode;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.model.SupplyManager;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.pathfinder.RegionBuilder;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.resource.WorldGenerator;
import com.oddlabs.tt.resource.WorldInfo;
import com.oddlabs.tt.scenery.Sky;
import com.oddlabs.tt.scenery.Water;
import com.oddlabs.tt.trigger.GameOverTrigger;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.render.RenderQueues;
import com.oddlabs.tt.player.UnitInfo;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.net.Network;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.resource.FogInfo;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.model.SupplyManagers;
import com.oddlabs.tt.resource.NativeResource;
import com.oddlabs.geometry.LowDetailModel;

public final strictfp class World {
	public final static int GAMESPEED_DONTCARE = -2;

	private final static float[] GAMESPEEDS = new float[]{
		0f,
			AnimationManager.ANIMATION_SECONDS_PER_TICK/2,
			AnimationManager.ANIMATION_SECONDS_PER_TICK,
			AnimationManager.ANIMATION_SECONDS_PER_TICK*1.75f,
			AnimationManager.ANIMATION_SECONDS_PER_TICK*4};

	private final HeightMap world;
	private final Random random;
	private final AnimationManager animation_manager_game_time;
	private final AnimationManager animation_manager_real_time;
	private final AudioImplementation audio_impl;

	private final int max_unit_count;
	private final NotificationListener notification_listener;

	private final Player[] players;
	private final SupplyManagers supply_managers;
	private final UnitGrid unit_grid;
	private final LandscapeTileIndices landscape_indices;
	private final AbstractPatchGroup patch_root;
	private final AbstractTreeGroup tree_root;
	private final AbstractElementNode element_root;
	private final RacesResources races_resources;
	private final LandscapeResources landscape_resources;

	private int global_checksum;
	private int gamespeed;

	public static LandscapeResources loadCommon(RenderQueues queues) {
		LandscapeResources landscape_resources = new LandscapeResources(queues);
		ProgressForm.progress();
		return landscape_resources;
	}

	public static RacesResources loadInGame(RenderQueues queues) {
		Icons.load();
		return new RacesResources(queues);
	}

	public final static World newWorld(AudioImplementation audio_implementation, LandscapeResources landscape_resources, RacesResources races_resources, LowDetailModel[] tree_low_details, NotificationListener notification_listener, WorldParameters world_params, WorldInfo world_info, int terrain_type, PlayerInfo[] player_infos, float[][] colors) {
		NativeResource.gc();
		ProgressForm.progress();
		World world = new World(audio_implementation, landscape_resources, races_resources, tree_low_details, notification_listener, world_params, world_info, terrain_type, player_infos, colors);
		ProgressForm.progress();
		ProgressForm.progress(1/5f);
		ProgressForm.progress();
		Player[] players = world.getPlayers();
		for (short i = 0; i < players.length; i++) {
			Player player = players[i];
			assert player != null;
			player.init(world_info.starting_locations[i]);
		}
		return world;
	}

	public final LandscapeResources getLandscapeResources() {
		return landscape_resources;
	}

	public final RacesResources getRacesResources() {
		return races_resources;
	}

	public final AudioImplementation getAudio() {
		return audio_impl;
	}

	public final int getChecksum() {
		return global_checksum;
	}

	public final void updateGlobalChecksum(int value) {
		global_checksum += value;
	}

	public final int getGamespeed() {
		return gamespeed;
	}

	public final float getSecondsPerTick() {
		return GAMESPEEDS[gamespeed];
	}

	public static boolean isValidPreferredGamespeed(int speed) {
		return speed == GAMESPEED_DONTCARE || isValidGamespeed(speed);
	}

	public static boolean isValidGamespeed(int speed) {
		return speed >= 0 && speed < GAMESPEEDS.length;
	}

	public final void gamespeedChanged() {
		int new_gamespeed = GAMESPEED_DONTCARE;
		for (int i = 0; i < players.length; i++) {
			int gamespeed = players[i].getPreferredGamespeed();
			if (gamespeed != GAMESPEED_DONTCARE) {
				if (new_gamespeed != GAMESPEED_DONTCARE && gamespeed != new_gamespeed)
					return;
				new_gamespeed = gamespeed;
			}
		}
		if (new_gamespeed != GAMESPEED_DONTCARE && new_gamespeed != gamespeed) {
			gamespeed = new_gamespeed;
			getNotificationListener().gamespeedChanged(gamespeed);
		}
	}

	public final void tick(float t) {
		getAnimationManagerGameTime().runAnimations(getSecondsPerTick()*t/AnimationManager.ANIMATION_SECONDS_PER_TICK);
		getAnimationManagerRealTime().runAnimations(t/*AnimationManager.ANIMATION_SECONDS_PER_TICK*/);
	}

	public final int getTick() {
		return getAnimationManagerRealTime().getTick();
	}

	private World(AudioImplementation audio_implementation, LandscapeResources landscape_resources, RacesResources races_resources, LowDetailModel[] tree_low_details, NotificationListener notification_listener, WorldParameters world_params, WorldInfo world_info, int terrain_type, PlayerInfo[] player_infos, float[][] colors) {
		System.out.println("****************** Generating landscape at tick " + LocalEventQueue.getQueue().getHighPrecisionManager().getTick() + " ********************");
		this.landscape_resources = landscape_resources;
		this.races_resources = races_resources;
		this.audio_impl = audio_implementation;
		this.max_unit_count = world_params.getMaxUnitCount();
		this.notification_listener = notification_listener;
		this.gamespeed = world_params.getInitialGameSpeed();
		long time_start = System.currentTimeMillis();

		int num_players = player_infos.length;

		world = new HeightMap(this, world_info.meters_per_world, world_info.sea_level_meters, world_info.texels_per_colormap, world_info.chunks_per_colormap, world_info.heightmap, world_info.trees, world_info.access_grid, world_info.build_grid);
		animation_manager_game_time = new AnimationManager();
		animation_manager_real_time = new AnimationManager();
		random = new Random(42);

		List participant_list = new ArrayList();
		List player_list = new ArrayList();
		for (short i = 0; i < player_infos.length; i++) {
//			slot_to_participant_index[i] = -1;
			Player player = new Player(this, player_infos[i], colors[i]);
			player_list.add(player);
		}

		players = new Player[player_list.size()];
		player_list.toArray(players);

		long time_stop = System.currentTimeMillis();
		System.out.println("****************** Finished landscape in " + ((time_stop - time_start)/1000f) +" sec ********************");
		this.supply_managers = new SupplyManagers(this);
		this.unit_grid = new UnitGrid(world);
		RegionBuilder.buildRegions(unit_grid, world_info.starting_locations[0][0], world_info.starting_locations[0][1]);
		this.landscape_indices = new LandscapeTileIndices(world, HeightMap.GRID_UNITS_PER_PATCH_EXP);
		this.patch_root = new PatchGroup(this);
		this.tree_root = AbstractTreeGroup.newRoot(this, tree_low_details, world_info.trees, world_info.palm_trees, terrain_type);
		this.element_root = AbstractElementNode.newRoot(world);
		AbstractElementNode.buildSupplies(this, world_info.iron, world_info.rocks, world_info.plants, terrain_type);
	}

	public final AbstractElementNode getElementRoot() {
		return element_root;
	}

	public final AbstractTreeGroup getTreeRoot() {
		return tree_root;
	}

	public final LandscapeTileIndices getLandscapeIndices() {
		return landscape_indices;
	}

	public final AbstractPatchGroup getPatchRoot() {
		return patch_root;
	}

	public final UnitGrid getUnitGrid() {
		return unit_grid;
	}

	public final SupplyManager getSupplyManager(Class cl) {
		return supply_managers.getSupplyManager(cl);
	}

	public final Player[] getPlayers() {
		return players;
	}

	public int getMaxUnitCount() {
		return max_unit_count;
	}

	public final NotificationListener getNotificationListener() {
		return notification_listener;
	}

	public final HeightMap getHeightMap() {
		return world;
	}
	
	public final AnimationManager getAnimationManagerGameTime() {
		return animation_manager_game_time;
	}

	public final AnimationManager getAnimationManagerRealTime() {
		return animation_manager_real_time;
	}

	public final Random getRandom() {
		return random;
	}
}
