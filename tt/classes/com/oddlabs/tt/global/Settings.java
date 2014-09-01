package com.oddlabs.tt.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Pbuffer;

import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.util.GLUtils;

public final strictfp class Settings implements Serializable {
	private final static long serialVersionUID = 1l;

	private static Settings settings;

	// event logging
	public String last_event_log_dir = "";
	public int last_revision = -1;
	public boolean crashed = false;

	// network
	public String registration_address = "registration.oddlabs.com";
	public String matchmaking_address = "matchmaking.oddlabs.com";
	public String bugreport_address = "bugreport.oddlabs.com";
	public String router_address = "router.oddlabs.com";
	public String username = "";
	public String pw_digest = "";
	public boolean remember_login = false;

	public int graphic_detail = Globals.DETAIL_NORMAL;

	// sound
	public boolean play_music = true;
	public boolean play_sfx = true;
	public float music_gain = .5f;
	public float sound_gain = 1f;

	// language
	public String language = "default";

	// window
	public int view_width = 800;
	public int view_height = 600;
	public int view_freq = 75;

	public int new_view_width = view_width;
	public int new_view_height = view_height;
	public int new_view_freq = view_freq;
	
	public boolean fullscreen = true;
	public boolean vsync = true;
//	public int view_bpp = 32;
	public int samples = 0;
	
	// control
	public boolean invert_camera_pitch = false;
	public boolean aggressive_units = false;
	public boolean use_native_cursor = true;
	
	public float mapmode_delay = .5f;
	public float tooltip_delay = .5f;
	private String developer_mode = "";
	private String beta_mode = "";
	public boolean has_native_campaign = false;
	
	public boolean save_event_log = true;
	public boolean fullscreen_depth_workaround = true;
	public boolean generate_dummy_worlds = false;
	public boolean first_run = true;
	
	public boolean warning_no_sound = true;

	//affiliate
	public String affiliate_id = "oddlabs.com";
	public String affiliate_logo_file = "";
	public String buy_url = "http://tribaltrouble.com/order";

	//reg key
	public boolean online = true;
	public String reg_key = "";

	//portal stuff
	public boolean hide_update = false;
	public boolean hide_register = false;
	public boolean hide_multiplayer = false;
	public boolean hide_bugreporter = false;
	public boolean hide_regkey = false;
	public boolean buy_now_only_quit = false;

	/* optional extensions */
	public boolean use_vbo_draw_range_elements = false;
	private boolean use_vbo = false;
	private boolean use_pbuffer = true;
	private boolean use_fbo = true;
	public boolean use_copyteximage = false;
	private boolean use_texture_compression = true;

	public int frame_grab_milliseconds_per_frame = 40;

	public final static void setSettings(Settings new_settings) {
		settings = new_settings;
	}

	public final static Settings getSettings() {
		return settings;
	}

	public final boolean useFBO() {
		return use_fbo && GLContext.getCapabilities().GL_EXT_framebuffer_object && !GLUtils.isIntelGMA950();
	}
	
	public final boolean usePbuffer() {
		return use_pbuffer && ((Pbuffer.getCapabilities() & Pbuffer.PBUFFER_SUPPORTED) != 0);
	}
	
	public final boolean useTextureCompression() {
		return use_texture_compression && (GLContext.getCapabilities().GL_ARB_texture_compression || GLContext.getCapabilities().OpenGL13);
	}

	public final boolean useVBO() {
		return use_vbo && GLContext.getCapabilities().GL_ARB_vertex_buffer_object;
	}

	public final boolean inDeveloperMode() {
		return developer_mode.equals("randomgryf") && Renderer.isRegistered();
	}
	
	public final boolean inBetaMode() {
		return beta_mode.equals("mythol");
	}
	
	public final void save() {
		if (LocalEventQueue.getQueue().getDeterministic().isPlayback())
			return;
		Settings original_settings = new Settings();
		Properties props = new Properties();
		Field[] pref_fields = Settings.class.getDeclaredFields();
		for (int i = 0; i < pref_fields.length; i++) {
			Field field = pref_fields[i];
			int mods = field.getModifiers();
			if (!hasValidModifiers(mods))
				continue;
			assert !Modifier.isStatic(mods);
			Class field_type = field.getType();
			try {
				if (field_type.equals(boolean.class)) {
					boolean field_value = field.getBoolean(this);
					if (field_value != field.getBoolean(original_settings))
						props.setProperty(field.getName(), ""+field_value);
				} else if (field_type.equals(int.class)) {
					int field_value = field.getInt(this);
					if (field_value != field.getInt(original_settings))
						props.setProperty(field.getName(), ""+field_value);
				} else if (field_type.equals(float.class)) {
					float field_value = field.getFloat(this);
					if (field_value != field.getFloat(original_settings))
						props.setProperty(field.getName(), ""+field_value);
				} else if (field_type.equals(String.class)) {
					String field_value = (String)field.get(this);
					if (!field_value.equals(field.get(original_settings)))
						props.setProperty(field.getName(), ""+field_value);
				} else
					throw new RuntimeException("Unsupported Settings type " + field_type);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		File settings_file = new File(LocalInput.getGameDir(), Globals.SETTINGS_FILE_NAME);
		try {
			OutputStream out = new FileOutputStream(settings_file);
			props.store(out, "comment");
		} catch (Exception e) {
			System.err.println("Failed to write settings to " + settings_file + " exception: " + e);
		}
	}

	public final void load(File game_dir) {
		Field[] pref_fields = getClass().getDeclaredFields();
		Properties props = new Properties();
		File settings_file = new File(game_dir, Globals.SETTINGS_FILE_NAME);
		try {
			InputStream in = new FileInputStream(settings_file);
			props.load(in);
		} catch (Exception e) {
			System.err.println("Could not read settings from " + settings_file);
			return;
		}
		
		for (int i = 0; i < pref_fields.length; i++) {
			Field field = pref_fields[i];
			int mods = field.getModifiers();
			if (!hasValidModifiers(mods))
				continue;
			assert !Modifier.isStatic(mods);
			String value = props.getProperty(field.getName());
			if (value == null)
				continue;

			Class field_type = field.getType();
			try {
				if (field_type.equals(boolean.class)) {
					boolean field_value = (new Boolean(value)).booleanValue();
					field.setBoolean(this, field_value);
				} else if (field_type.equals(int.class)) {
					int field_value = (new Integer(value)).intValue();
					field.setInt(this, field_value);
				} else if (field_type.equals(float.class)) {
					float field_value = (new Float(value)).floatValue();
					field.setFloat(this, field_value);
				} else if (field_type.equals(String.class)) {
					field.set(this, value);
				} else
					throw new RuntimeException("Unsupported Settings type " + field_type);
			} catch (Exception e) {
				System.out.println("WARNING: " + field.getName() + " is not of type: " + field.getType() + ". Skipped");
			}
		}
	}

	private final static boolean hasValidModifiers(int mods) {
		return !Modifier.isStatic(mods) && !Modifier.isFinal(mods);
	}
}
