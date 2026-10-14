package com.oddlabs.tt.global;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.matchmaking.MatchmakingServerInterface;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.util.Color;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Settings implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static Settings settings;

    // event logging
    private static final Logger logger = Logger.getLogger(Settings.class.getName());
    public transient @NonNull Path last_event_log_dir = Path.of("");
    public int last_revision = -1;
    public boolean crashed = false;

    // network
    public @NonNull String username = "";
    public @NonNull String pw_digest = "";
    public boolean remember_login = false;

    public int graphic_detail = Globals.DETAIL_NORMAL;

    // sound
    public boolean play_music = true;
    public boolean play_sfx = true;
    public boolean headphone_mode = false;
    public boolean classic_audio = true;
    public float music_gain = .5f;
    public float sound_gain = 1f;

    // language
    public @NonNull String language = "default";

    // window
    public int view_width = -1;
    public int view_height = -1;
    public int view_freq = -1;

    public int new_view_width = view_width;
    public int new_view_height = view_height;
    public int new_view_freq = view_freq;
    public int new_view_samples = 4;

    public boolean fullscreen = true;
    public final boolean vsync = true;
    public int view_samples = 4;
//	public int view_bpp = 32;

    // control
    public boolean invert_camera_pitch = false;
    public boolean invert_camera_yaw = false;
    public boolean aggressive_units = false;
    public boolean show_compass = true;
    public float camera_pan_speed = 1f;
    public boolean camera_pan_acceleration = true;
    public float camera_rotate_speed = 1f;
    public float camera_zoom_speed = 1f;
    public float cinematic_camera_speed = .4f;
    public boolean cinematic_unlock_limits = true;
    public boolean confine_cursor = true;

    // gameplay
    public int gamespeed = Game.GAMESPEED_NORMAL;

    public float mapmode_delay = .5f;
    public float tooltip_delay = .5f;
    public float ui_scale = 0.0f; // 0.0 = 100% (Scale 1.0), 1.0 = Max Scale
    private final boolean developer_mode = Boolean.getBoolean("com.oddlabs.tt.developer");
    public boolean has_native_campaign = developer_mode;

    public final boolean save_event_log = true;
    public boolean generate_dummy_worlds = false;
    public boolean first_run = true;

    public boolean warning_no_sound = true;
    public String patch_notes_seen = "";

    public boolean hide_multiplayer = false;

    // Server domain
    public static final String OFFICIAL_DOMAIN = "tribaltrouble.org";
    private String domain_name = OFFICIAL_DOMAIN;

    // Community links
    public static final String DISCORD_URL = "https://discord.gg/TribalTrouble";
    public static final String GITHUB_URL = "https://github.com/OmarAMokhtar/tribaltrouble";

    public final int frame_grab_milliseconds_per_frame = 40;

    // Accessibility
    public int cvd_mode = 0; // 0=None, 1=Protanopia, 2=Deuteranopia, 3=Tritanopia
    public float cvd_intensity = 1.0f;
    public boolean high_contrast = false;
    public float contrast_intensity = 0.5f;
    public boolean team_stencil = false;

    public static final Vector4f[] DEFAULT_TEAM_COLOURS = generateDefaultColours();

    private static Vector4f[] generateDefaultColours() {
        // 18 hand-picked colours that are visually distinct on terrain
        Vector4f[] handPicked = {Color.argb4v(0xFFFFBF00), /*  0 Orange */
                Color.argb4v(0xFF007FFF), /*  1 Royal Blue */
                Color.argb4v(0xFFFF0040), /*  2 Red */
                Color.argb4v(0xFF00FFBF), /*  3 Teal */
                Color.argb4v(0xFFBF00FF), /*  4 Purple */
                Color.argb4v(0xFFBFFF00), /*  5 Lime */
                Color.argb4v(0xFFFFFF00), /*  6 Yellow */
                Color.argb4v(0xFFFF69B4), /*  7 Hot Pink */
                Color.argb4v(0xFF006400), /*  8 Dark Green */
                Color.argb4v(0xFF8B0000), /*  9 Maroon */
                Color.argb4v(0xFF00BFFF), /* 10 Sky Blue */
                Color.argb4v(0xFFFFFFFF), /* 11 White */
                Color.argb4v(0xFF000080), /* 12 Navy */
                Color.argb4v(0xFFFF7F50), /* 13 Coral */
                Color.argb4v(0xFFFFD700), /* 14 Gold */
                Color.argb4v(0xFFFF00FF), /* 15 Magenta */
                Color.argb4v(0xFF228B22), /* 16 Forest Green */
                Color.argb4v(0xFF708090), /* 17 Slate */
        };
        // Use the hand-picked palette up to MAX_PLAYERS; if MAX_PLAYERS exceeds the
        // hand-picked count, fill remaining slots with evenly-spaced HSB-generated colours.
        Vector4f[] all = new Vector4f[MatchmakingServerInterface.MAX_PLAYERS];
        int copyCount = Math.min(handPicked.length, all.length);
        System.arraycopy(handPicked, 0, all, 0, copyCount);
        for (int i = copyCount; i < all.length; i++) {
            float hue = (i - handPicked.length) / (float) (all.length - handPicked.length);
            int rgb = java.awt.Color.HSBtoRGB(hue, 0.8f, 0.9f);
            all[i] = Color.argb4v(0xFF000000 | (rgb & 0x00FFFFFF));
        }
        return all;
    }

    public Vector4f @NonNull [] team_colours = new Vector4f[DEFAULT_TEAM_COLOURS.length];

    public Settings() {
        for (int i = 0; i < DEFAULT_TEAM_COLOURS.length; i++) {
            team_colours[i] = new Vector4f(DEFAULT_TEAM_COLOURS[i]);
        }
    }

    public static void setSettings(Settings new_settings) {
        settings = new_settings;
    }

    public static Settings getSettings() {
        return settings;
    }

    public boolean inDeveloperMode() {
        return developer_mode;
    }

    public void setDomain(String new_domain) {
        domain_name = new_domain;
    }

    public String getDomainName() {
        return domain_name;
    }

    public boolean isOfficialServer() {
        if (OFFICIAL_DOMAIN.equals(domain_name) || domain_name.endsWith("." + OFFICIAL_DOMAIN)) {
            return true;
        }
        // Also allow Steam auth on localhost if debug flag is enabled
        if (Globals.debug_steam_auth_localhost
                && ("localhost".equals(domain_name) || "local".equals(domain_name))) {
            return true;
        }
        return false;
    }

    public String getMatchmakingAddress() {
        return "matchmaking." + domain_name;
    }

    public String getRouterAddress() {
        return "router." + domain_name;
    }

    public void save() {
        if (LocalEventQueue.getQueue().getDeterministic().isPlayback())
            return;
        Settings defaults = new Settings();
        Properties props = new Properties();

        setProperty(props, "last_event_log_dir", last_event_log_dir, defaults.last_event_log_dir);
        setProperty(props, "last_revision", last_revision, defaults.last_revision);
        setProperty(props, "crashed", crashed, defaults.crashed);
        setProperty(props, "username", username, defaults.username);
        setProperty(props, "pw_digest", pw_digest, defaults.pw_digest);
        setProperty(props, "remember_login", remember_login, defaults.remember_login);
        setProperty(props, "graphic_detail", graphic_detail, defaults.graphic_detail);
        setProperty(props, "play_music", play_music, defaults.play_music);
        setProperty(props, "play_sfx", play_sfx, defaults.play_sfx);
        setProperty(props, "headphone_mode", headphone_mode, defaults.headphone_mode);
        setProperty(props, "classic_audio", classic_audio, defaults.classic_audio);
        setProperty(props, "music_gain", music_gain, defaults.music_gain);
        setProperty(props, "sound_gain", sound_gain, defaults.sound_gain);
        setProperty(props, "language", language, defaults.language);
        setProperty(props, "view_width", view_width, defaults.view_width);
        setProperty(props, "view_height", view_height, defaults.view_height);
        setProperty(props, "view_freq", view_freq, defaults.view_freq);
        setProperty(props, "new_view_width", new_view_width, defaults.new_view_width);
        setProperty(props, "new_view_height", new_view_height, defaults.new_view_height);
        setProperty(props, "new_view_freq", new_view_freq, defaults.new_view_freq);
        setProperty(props, "new_view_samples", new_view_samples, defaults.new_view_samples);
        setProperty(props, "fullscreen", fullscreen, defaults.fullscreen);
        setProperty(props, "view_samples", view_samples, defaults.view_samples);
        setProperty(props, "invert_camera_pitch", invert_camera_pitch, defaults.invert_camera_pitch);
        setProperty(props, "invert_camera_yaw", invert_camera_yaw, defaults.invert_camera_yaw);
        setProperty(props, "aggressive_units", aggressive_units, defaults.aggressive_units);
        setProperty(props, "show_compass", show_compass, defaults.show_compass);
        setProperty(props, "camera_pan_speed", camera_pan_speed, defaults.camera_pan_speed);
        setProperty(props, "camera_pan_acceleration", camera_pan_acceleration, defaults.camera_pan_acceleration);
        setProperty(props, "camera_rotate_speed", camera_rotate_speed, defaults.camera_rotate_speed);
        setProperty(props, "camera_zoom_speed", camera_zoom_speed, defaults.camera_zoom_speed);
        setProperty(props, "cinematic_camera_speed", cinematic_camera_speed, defaults.cinematic_camera_speed);
        setProperty(props, "cinematic_unlock_limits", cinematic_unlock_limits, defaults.cinematic_unlock_limits);
        setProperty(props, "confine_cursor", confine_cursor, defaults.confine_cursor);
        setProperty(props, "gamespeed", gamespeed, defaults.gamespeed);
        setProperty(props, "mapmode_delay", mapmode_delay, defaults.mapmode_delay);
        setProperty(props, "tooltip_delay", tooltip_delay, defaults.tooltip_delay);
        setProperty(props, "ui_scale", ui_scale, defaults.ui_scale);
        setProperty(props, "first_run", first_run, defaults.first_run);
        setProperty(props, "warning_no_sound", warning_no_sound, defaults.warning_no_sound);
        setProperty(props, "patch_notes_seen", patch_notes_seen, defaults.patch_notes_seen);
        setProperty(props, "domain_name", domain_name, defaults.domain_name);

        setProperty(props, "cvd_mode", cvd_mode, defaults.cvd_mode);
        setProperty(props, "cvd_intensity", cvd_intensity, defaults.cvd_intensity);
        setProperty(props, "high_contrast", high_contrast, defaults.high_contrast);
        setProperty(props, "contrast_intensity", contrast_intensity, defaults.contrast_intensity);
        setProperty(props, "team_stencil", team_stencil, defaults.team_stencil);
        setProperty(props, "team_colours", team_colours, defaults.team_colours);

        Renderer.getLocalInput().getInputManager().saveBindings(props);

        Path settings_file = Renderer.getLocalInput().getGameDir().resolve(Globals.getSettingsFileName());
        try (OutputStream out = Files.newOutputStream(settings_file)) {
            props.store(out, Instant.now().toString());
        } catch (IOException e) {
            logger.warning("Failed to write settings to " + settings_file + " exception: " + e);
        }
    }

    public void load(@NonNull Path game_dir) {
        Properties props = new Properties();
        Path settings_file = game_dir.resolve(Globals.getSettingsFileName());
        if (!Files.exists(settings_file)) {
            return;
        }
        try (InputStream in = Files.newInputStream(settings_file)) {
            props.load(in);
        } catch (IOException _) {
            logger.warning("WARNING: Could not read settings from " + settings_file + ". Using defaults.");
            return;
        }

        last_event_log_dir = getPath(props, "last_event_log_dir", last_event_log_dir);
        last_revision = getInt(props, "last_revision", last_revision);
        crashed = getBoolean(props, "crashed", crashed);
        username = props.getProperty("username", username);
        pw_digest = props.getProperty("pw_digest", pw_digest);
        remember_login = getBoolean(props, "remember_login", remember_login);
        graphic_detail = getInt(props, "graphic_detail", graphic_detail);
        play_music = getBoolean(props, "play_music", play_music);
        play_sfx = getBoolean(props, "play_sfx", play_sfx);
        headphone_mode = getBoolean(props, "headphone_mode", headphone_mode);
        classic_audio = getBoolean(props, "classic_audio", classic_audio);
        music_gain = getFloat(props, "music_gain", music_gain);
        sound_gain = getFloat(props, "sound_gain", sound_gain);
        language = props.getProperty("language", language);
        view_width = getInt(props, "view_width", view_width);
        view_height = getInt(props, "view_height", view_height);
        view_freq = getInt(props, "view_freq", view_freq);
        new_view_width = getInt(props, "new_view_width", new_view_width);
        new_view_height = getInt(props, "new_view_height", new_view_height);
        new_view_freq = getInt(props, "new_view_freq", new_view_freq);
        new_view_samples = getInt(props, "new_view_samples", new_view_samples);
        fullscreen = getBoolean(props, "fullscreen", fullscreen);
        view_samples = getInt(props, "view_samples", view_samples);
        invert_camera_pitch = getBoolean(props, "invert_camera_pitch", invert_camera_pitch);
        invert_camera_yaw = getBoolean(props, "invert_camera_yaw", invert_camera_yaw);
        aggressive_units = getBoolean(props, "aggressive_units", aggressive_units);
        show_compass = getBoolean(props, "show_compass", show_compass);
        camera_pan_speed = getFloat(props, "camera_pan_speed", camera_pan_speed);
        camera_pan_acceleration = getBoolean(props, "camera_pan_acceleration", camera_pan_acceleration);
        camera_rotate_speed = getFloat(props, "camera_rotate_speed", camera_rotate_speed);
        camera_zoom_speed = getFloat(props, "camera_zoom_speed", camera_zoom_speed);
        cinematic_camera_speed = getFloat(props, "cinematic_camera_speed", cinematic_camera_speed);
        cinematic_unlock_limits = getBoolean(props, "cinematic_unlock_limits", cinematic_unlock_limits);
        confine_cursor = getBoolean(props, "confine_cursor", confine_cursor);
        gamespeed = getInt(props, "gamespeed", gamespeed);
        mapmode_delay = getFloat(props, "mapmode_delay", mapmode_delay);
        tooltip_delay = getFloat(props, "tooltip_delay", tooltip_delay);
        ui_scale = getFloat(props, "ui_scale", ui_scale);
        first_run = getBoolean(props, "first_run", first_run);
        warning_no_sound = getBoolean(props, "warning_no_sound", warning_no_sound);
        patch_notes_seen = props.getProperty("patch_notes_seen", patch_notes_seen);
        domain_name = props.getProperty("domain_name", domain_name);

        cvd_mode = getInt(props, "cvd_mode", cvd_mode);
        cvd_intensity = getFloat(props, "cvd_intensity", cvd_intensity);
        high_contrast = getBoolean(props, "high_contrast", high_contrast);
        contrast_intensity = getFloat(props, "contrast_intensity", contrast_intensity);
        team_stencil = getBoolean(props, "team_stencil", team_stencil);
        team_colours = getColours(props, "team_colours", team_colours);

        Renderer.getLocalInput().getInputManager().loadBindings(props);
    }

    // --- Save Helpers ---
    private void setProperty(@NonNull Properties props, @NonNull String key, @NonNull Path value, Path defaultValue) {
        if (!value.equals(defaultValue)) {
            props.setProperty(key, value.toString());
        }
    }

    private void setProperty(@NonNull Properties props, @NonNull String key, @NonNull String value,
            String defaultValue) {
        if (!value.equals(defaultValue)) {
            props.setProperty(key, value);
        }
    }

    private void setProperty(@NonNull Properties props, @NonNull String key, int value, int defaultValue) {
        if (value != defaultValue) {
            props.setProperty(key, String.valueOf(value));
        }
    }

    private void setProperty(@NonNull Properties props, @NonNull String key, float value, float defaultValue) {
        if (value != defaultValue) {
            props.setProperty(key, String.valueOf(value));
        }
    }

    private void setProperty(@NonNull Properties props, @NonNull String key, boolean value, boolean defaultValue) {
        if (value != defaultValue) {
            props.setProperty(key, String.valueOf(value));
        }
    }

    private void setProperty(@NonNull Properties props, @NonNull String key, @NonNull Vector4fc @NonNull [] value,
            @NonNull Vector4fc @NonNull [] defaultValue) {
        if (!Arrays.equals(value, defaultValue)) {
            String colors = Arrays.stream(value).mapToInt(Color::argbi).mapToObj(Integer::toHexString).collect(
                    Collectors.joining(","));

            props.setProperty(key, colors);
        }
    }

    // --- Load Helpers ---
    private boolean getBoolean(@NonNull Properties props, @NonNull String key, boolean defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        // Boolean.parseBoolean is robust and doesn't throw exceptions
        return Boolean.parseBoolean(value);
    }

    private int getInt(@NonNull Properties props, @NonNull String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException _) {
            logger.warning(
                    "WARNING: Invalid value for setting '" + key + "': '" + value + "'. Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
    }

    private float getFloat(@NonNull Properties props, @NonNull String key, float defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException _) {
            logger.warning(
                    "WARNING: Invalid value for setting '" + key + "': '" + value + "'. Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
    }

    private static Path getPath(@NonNull Properties props, @NonNull String key, Path defaultValue) {
        String value = props.getProperty(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Path.of(value);
        } catch (InvalidPathException _) {
            logger.warning(
                    "Invalid path for setting '" + key + "': '" + value + "'. Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
    }

    private static Vector4f @NonNull [] getColours(@NonNull Properties props, @NonNull String key,
            Vector4f @NonNull [] defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            String[] hexStrings = value.split(",");
            Vector4f[] result = new Vector4f[DEFAULT_TEAM_COLOURS.length];
            for (int i = 0; i < DEFAULT_TEAM_COLOURS.length; i++) {
                result[i] = new Vector4f(DEFAULT_TEAM_COLOURS[i]);
            }
            for (int i = 0; i < Math.min(DEFAULT_TEAM_COLOURS.length, hexStrings.length); i++) {
                int argb = (int) Long.parseLong(hexStrings[i], 16);
                result[i] = Color.argb4v(argb);
            }
            return result;
        } catch (Exception e) {
            logger.warning(
                    "WARNING: Invalid value for setting '" + key + "': '" + value + "'. Using default value. Error: " + e);
            return defaultValue;
        }
    }

    @Serial
    private void writeObject(@NonNull ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(last_event_log_dir.toString());
    }

    @Serial
    private void readObject(@NonNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        last_event_log_dir = Path.of((String) in.readObject());
    }
}
