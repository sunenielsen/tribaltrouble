package com.oddlabs.tt.global;

import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.render.Display;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.GLUtils;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

public final strictfp class Settings implements Serializable {
    private static final long serialVersionUID = 1l;

    private static Settings settings;

    // event logging
    public String last_event_log_dir = "";
    public int last_revision = -1;
    public boolean crashed = false;

    // network
    // TODO: Why is domain name stuck on tribaltrouble.org?
    // when it is loaded from the settings file?
    private String domain_name = "tribaltrouble.org";
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
    public boolean show_compass = true;
    private boolean use_native_cursor = false;

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

    // affiliate
    public String affiliate_id = "oddlabs.com";
    public String affiliate_logo_file = "";
    public String buy_url = "http://tribaltrouble.com/order";

    // reg key
    public boolean online = true;
    public String reg_key = "";

    // portal stuff
    public boolean hide_update = false;
    public boolean hide_register = false;
    public boolean hide_multiplayer = false;
    public boolean hide_bugreporter = false;
    public boolean hide_regkey = false;
    public boolean buy_now_only_quit = false;

    /* optional extensions */
    public boolean use_vbo_draw_range_elements = false;
    private boolean use_vbo = false;
    private boolean use_pbuffer = false;
    private boolean use_fbo = true;
    public boolean use_copyteximage = false;
    private boolean use_texture_compression = true;

    public int frame_grab_milliseconds_per_frame = 40;

    public static final void setSettings(Settings new_settings) {
        settings = new_settings;
        settings.setDomain(settings.getDomainName());
    }

    public static final Settings getSettings() {
        return settings;
    }

    public final boolean useFBO() {
        return use_fbo
                && GL.getCapabilities().GL_EXT_framebuffer_object
                && !GLUtils.isIntelGMA950();
    }

    public final boolean usePbuffer() {
        return false;
    }

    public final boolean useTextureCompression() {
        return use_texture_compression
                && (GL.getCapabilities().GL_ARB_texture_compression
                        || GL.getCapabilities().OpenGL13);
    }

    public final boolean useVBO() {
        return use_vbo && GL.getCapabilities().GL_ARB_vertex_buffer_object;
    }

    public final boolean inDeveloperMode() {
        return developer_mode.equals("randomgryf") && Renderer.isRegistered();
    }

    public final boolean inBetaMode() {
        return beta_mode.equals("mythol");
    }

    public final void save() {
        if (LocalEventQueue.getQueue().getDeterministic().isPlayback()) return;
        Settings original_settings = new Settings();
        Properties props = new Properties();
        Field[] pref_fields = Settings.class.getDeclaredFields();
        for (int i = 0; i < pref_fields.length; i++) {
            Field field = pref_fields[i];
            int mods = field.getModifiers();
            if (!hasValidModifiers(mods)) continue;
            assert !Modifier.isStatic(mods);
            Class field_type = field.getType();
            try {
                if (field_type.equals(boolean.class)) {
                    boolean field_value = field.getBoolean(this);
                    if (field_value != field.getBoolean(original_settings))
                        props.setProperty(field.getName(), "" + field_value);
                } else if (field_type.equals(int.class)) {
                    int field_value = field.getInt(this);
                    if (field_value != field.getInt(original_settings))
                        props.setProperty(field.getName(), "" + field_value);
                } else if (field_type.equals(float.class)) {
                    float field_value = field.getFloat(this);
                    if (field_value != field.getFloat(original_settings))
                        props.setProperty(field.getName(), "" + field_value);
                } else if (field_type.equals(String.class)) {
                    String field_value = (String) field.get(this);
                    if (!field_value.equals(field.get(original_settings)))
                        props.setProperty(field.getName(), "" + field_value);
                } else throw new RuntimeException("Unsupported Settings type " + field_type);
            } catch (IllegalAccessException e) {
                System.out.println("Exception: " + e);
                throw new RuntimeException(e);
            }
        }

        String settings_save_file = Globals.getSettingsFileName();
        System.out.println("Saving settings to " + LocalInput.getGameDir() + settings_save_file);
        File settings_file = new File(LocalInput.getGameDir(), settings_save_file);
        try {
            OutputStream out = new FileOutputStream(settings_file);
            props.store(out, "comment");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            System.err.println("Failed to write settings to " + settings_file + " exception: " + e);
        }
    }

    /** Updates settings related to the domain name for the client */
    public void setDomain(String new_domain) {
        domain_name = new_domain;
    }

    public String getDomainName() {
        return domain_name;
    }

    public String getRegistrationAddress() {
        return "registration." + domain_name;
    }

    public String getMatchmakingAddress() {
        return "matchmaking." + domain_name;
    }

    public String getBugReportAddress() {
        return "bugreport." + domain_name;
    }

    public String getRouterAddress() {
        return "router." + domain_name;
    }

    public void setNativeCursor(boolean use_native_cursor) {
        this.use_native_cursor = use_native_cursor;
        int cursor_mode = use_native_cursor ? GLFW.GLFW_CURSOR_NORMAL : GLFW.GLFW_CURSOR_DISABLED;
        System.out.println(
                "Cursor mode: "
                        + (cursor_mode == GLFW.GLFW_CURSOR_NORMAL
                                ? "Normal (native)"
                                : "Disabled (game)"));
        GLFW.glfwSetInputMode(Display.getWindow(), GLFW.GLFW_CURSOR, cursor_mode);
    }

    public boolean getNativeCursor() {
        return use_native_cursor;
    }

    public final void load(File game_dir) {
        System.out.println("Loading settings from " + game_dir);
        Field[] pref_fields = getClass().getDeclaredFields();
        Properties props = new Properties();
        File settings_file = new File(game_dir, Globals.getSettingsFileName());
        try {
            InputStream in = new FileInputStream(settings_file);
            props.load(in);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            System.err.println("Could not read settings from " + settings_file);
            return;
        }

        for (int i = 0; i < pref_fields.length; i++) {
            Field field = pref_fields[i];
            int mods = field.getModifiers();
            if (!hasValidModifiers(mods)) continue;
            assert !Modifier.isStatic(mods);
            String value = props.getProperty(field.getName());
            if (value == null) continue;

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
                } else throw new RuntimeException("Unsupported Settings type " + field_type);
            } catch (Exception e) {
                System.out.println("Exception: " + e);
                System.out.println(
                        "WARNING: "
                                + field.getName()
                                + " is not of type: "
                                + field.getType()
                                + ". Skipped");
            }
        }
    }

    private static final boolean hasValidModifiers(int mods) {
        return !Modifier.isStatic(mods) && !Modifier.isFinal(mods);
    }
}
