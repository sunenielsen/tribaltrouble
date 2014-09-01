package com.oddlabs.tt.global;

import org.lwjgl.opengl.*;

public final strictfp class Globals {
	public final static String SUPPORT_ADDRESS = "http://oddlabs.com/support";

	public final static int BOUNDING_NONE = 0;
	public final static int BOUNDING_UNIT_GRID = 1;
	public final static int BOUNDING_LANDSCAPE = 2;
	public final static int BOUNDING_TREES = 3;
	public final static int BOUNDING_PLAYERS = 4;
	public final static int BOUNDING_OCCUPATION = 5;
	public final static int BOUNDING_REGIONS = 6;
	public final static int BOUNDING_ALL = 7;

	public final static int DETAIL_LOW = 0;
	public final static int DETAIL_NORMAL = 1;
	public final static int DETAIL_HIGH = 2;

	public final static int[] TEXTURE_MIP_SHIFT = new int[]{1, 0, 0};
	public final static int[] UNIT_HIGH_POLY_COUNT = new int[]{7500, 20000, 40000};
	public final static int[] LANDSCAPE_POLY_COUNT = new int[]{5000, 10000, 20000};
	public final static boolean[] INSERT_PLANTS = new boolean[]{false, false, true};
	
	public final static String GAME_NAME = "TribalTrouble";
	public final static String REG_FILE_NAME = "registration";
	public final static String SETTINGS_FILE_NAME = "settings";
	
	public final static String AFFILIATE_ID_KEY = "affiliate_id";

	public static boolean run_ai = true;

	public static int gamespeed = 2;

	public static boolean process_landscape = true;
	public static boolean process_trees = true;
	public static boolean process_misc = true;
	public static boolean process_shadows = true;
	
	public static boolean draw_status = false;
	public static boolean draw_landscape = true;
	public static boolean draw_trees = true;
	public static boolean draw_misc = true;
	public static boolean draw_particles = true;
	public static boolean draw_water = true;
	public static boolean draw_sky = true;
	public static boolean draw_axes = false;
	public static boolean draw_detail = true;
	public static boolean draw_shadows = true;
	public static boolean draw_light = true;
	public static boolean draw_plants = true;
	
	public static boolean line_mode = false;
	public static boolean clear_frame_buffer = false;
	public static boolean frustum_freeze = false;
	
	public static boolean slowmotion = false;
	
	public static boolean checksum_error_in_last_game = false;
	
	private static int bounding = BOUNDING_NONE;

	public final static void switchBoundingMode() {
		bounding = (bounding + 1)%(BOUNDING_ALL + 1);
	}

	public final static boolean isBoundsEnabled(int bounding_type) {
		return bounding == bounding_type || bounding == BOUNDING_ALL;
	}

	public static int COMPRESSED_RGB_FORMAT = GL13.GL_COMPRESSED_RGB;
	public static int COMPRESSED_RGBA_FORMAT = GL13.GL_COMPRESSED_RGBA;
	public static int COMPRESSED_A_FORMAT = GL13.GL_COMPRESSED_ALPHA;
/*	public static int COMPRESSED_LUMINANCE_FORMAT = GL13.GL_COMPRESSED_LUMINANCE;
*/	
	
/*	public static int COMPRESSED_RGB_FORMAT = GL11.GL_RGB;
	public static int COMPRESSED_RGBA_FORMAT = GL11.GL_RGBA;
	public static int COMPRESSED_A_FORMAT = GL11.GL_ALPHA8;*/
	public static int COMPRESSED_LUMINANCE_FORMAT = GL11.GL_LUMINANCE;
	public static int LOW_DETAIL_TEXTURE_SHIFT = 1;

	public final static void disableTextureCompression() {
		System.out.println("Disabling texture compression");
		COMPRESSED_RGB_FORMAT = GL11.GL_RGB;
		COMPRESSED_RGBA_FORMAT = GL11.GL_RGBA;
		COMPRESSED_A_FORMAT = GL11.GL_ALPHA;
		COMPRESSED_LUMINANCE_FORMAT = GL11.GL_LUMINANCE;
	}

	public final static float LANDSCAPE_HILLS = 1f;
	public final static float LANDSCAPE_VEGETATION = 2f;
	public final static float LANDSCAPE_RESOURCES = 0f;
	public final static int LANDSCAPE_SEED = 1;
	
	public final static int VIEW_BIT_DEPTH = 16;
	public final static float FOV = 45.0f;
	public final static float VIEW_MIN = 2f;
	public final static float VIEW_MAX = 8000.0f;
	public final static float GUI_Z = VIEW_MIN + 0.1f;
	
	public final static int NET_PORT = 21000;

	public final static int NO_MIPMAP_CUTOFF = 1000;

	public final static int STRUCTURE_SIZE = 256;
	public final static int DETAIL_SIZE = 256;
	public final static int TEXELS_PER_GRID_UNIT = 8;
		
	public final static float LANDSCAPE_DETAIL_REPEAT_RATE = 0.25f;
	public final static float WATER_REPEAT_RATE = 0.001f;
	public final static float WATER_DETAIL_REPEAT_RATE = 0.01f;
	public final static int LANDSCAPE_DETAIL_FADEOUT_BASE_LEVEL = 2;
	public final static float LANDSCAPE_DETAIL_FADEOUT_FACTOR = 0.75f;

	public final static int MAX_RENDERNODE_DEPTH = 5;

	public final static String SCREENSHOT_DEFAULT = "screenshot";

	public final static float[][] SEA_BOTTOM_COLOR = {{0.45f, 0.25f, 0.6f}, {0f, 0f, 0f}};
	
	public final static float TREE_ERROR_DISTANCE = 100f;

	public final static float WHEEL_SCALE = 0.01f;

	public final static int CURSOR_BLINK_TIME = 1000;

	public final static int MENU_HOVER_DELAY = 500;

	public final static int FPS_WIDTH = 800;

	public final static int SHELL_HISTORY_SIZE = 50;
	public final static int SHELL_HISTORY_PAGE_SIZE = 10;

	// max texture size (for generated textures)
	public final static int MIN_TEXTURE_POWER = 2;
	public final static int MIN_TEXTURE_SIZE = 1 << MIN_TEXTURE_POWER;
	public static int MAX_TEXTURE_POWER;
	public static int MAX_TEXTURE_SIZE;
	// How to divide images in 2^n textures - 1 means split most memory preserving 0 means split least
	public final static float TEXTURE_WEIGHT = 0.5f;
	public static int[] TEXTURE_SIZES;
	public static byte[] TEXTURE_SPLITS;
	public static int[] BEST_SIZES;

	public final static float SEA_LEVEL = .1f;
	public final static int TEXELS_PER_CHUNK_BORDER = 4;

	public final static int BLOCK_SCROLL_AMOUNT = 20;

	public final static float ERROR_TOLERANCE = 10f;
}
