package com.oddlabs.tt.render;

import com.oddlabs.event.Deterministic;
import com.oddlabs.matchmaking.Game;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.util.BuildInfo;
import com.oddlabs.tt.Main;
import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.animation.TimerAnimation;
import com.oddlabs.tt.animation.Updatable;
import com.oddlabs.tt.audio.AbstractAudioPlayer;
import com.oddlabs.tt.audio.AudioManager;
import com.oddlabs.tt.audio.AudioParameters;
import com.oddlabs.tt.camera.MenuCamera;
import com.oddlabs.tt.delegate.MainMenu;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.form.MessageForm;
import com.oddlabs.tt.form.PatchNotesForm;
import com.oddlabs.tt.form.ProgressForm;
import com.oddlabs.tt.form.WarningForm;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.GlobalsInit;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.GUI;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Languages;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.landscape.LandscapeResources;
import com.oddlabs.tt.landscape.NotificationListener;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.landscape.WorldParameters;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.render.state.GLRenderContext;
import com.oddlabs.tt.render.state.RenderContext;
import com.oddlabs.tt.resource.FogInfo;
import com.oddlabs.tt.steam.SteamManager;
import com.oddlabs.tt.resource.IslandGenerator;
import com.oddlabs.tt.resource.NativeResource;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.resource.WorldGenerator;
import com.oddlabs.tt.resource.WorldInfo;
import com.oddlabs.tt.util.GLUtils;
import com.oddlabs.tt.util.OsPlatform;
import com.oddlabs.tt.util.StatCounter;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.vbo.VBO;
import com.oddlabs.tt.viewer.AmbientAudio;
import com.oddlabs.tt.viewer.Cheat;
import com.oddlabs.tt.viewer.Selection;
import com.oddlabs.tt.window.Window;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class Renderer implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(Renderer.class.getName());
    private static final ResourceBundle bundle = ResourceBundle.getBundle(Renderer.class.getName());

    private static @NonNull String i18n(@NonNull String key, @NonNull Object @NonNull... args) {
        return Utils.getBundleString(bundle, key, args);
    }

    private static final Renderer renderer_instance = new Renderer();
    private final GLRenderContext renderContext = new GLRenderContext();
    private static final StatCounter fps = new StatCounter(10);
    private static int num_triangles_rendered;

    private static boolean grab_frames = false;

    private final Locale default_locale = Locale.of(Locale.getDefault().getLanguage(), Locale.getDefault().getCountry(),
            "default");

    private AbstractAudioPlayer music;
    private String music_path;
    private @Nullable TimerAnimation music_timer;

    private static volatile boolean finished = false;

    private boolean movie_recording_started = false;
    private AmbientAudio ambient;

    private final Window window = new com.oddlabs.tt.window.LWJGL3Window();

    private final LocalInput localInput = new LocalInput(window);

    private @Nullable Cheat cheat = new Cheat();

    public static float getFPS() {
        return fps.getAveragePerUpdate();
    }

    public static boolean isRegistered() {
        return true;
    }

    public static void makeCurrent() {
        try {
            getRenderer().getWindow().makeCurrent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        logger.info("Closing LocalInput...");
        getLocalInput().close();
        logger.info("Closing Window...");
        window.close();
    }

    public @NonNull Window getWindow() {
        return window;
    }

    public static @NonNull Renderer getRenderer() {
        return renderer_instance;
    }

    public @NonNull RenderContext getRenderContext() {
        return renderContext;
    }

    public static @NonNull LocalInput getLocalInput() {
        return getRenderer().localInput;
    }

    private void runGameLoop(@NonNull NetworkSelector network, @NonNull GUI gui) {
        AnimationManager.runGameLoop(network, gui, grab_frames);
    }

    public static void registerTrianglesRendered(int count) {
        num_triangles_rendered += count;
    }

    public static int getTrianglesRendered() {
        return num_triangles_rendered;
    }

    private void display(@NonNull GUI gui) {
        num_triangles_rendered = 0;
        fps.updateDelta(System.currentTimeMillis());
        NativeResource.processGLCleanupTasks();
        GLUtils.checkGLError("After Cleanup");

        gui.render(ambient);
    }

    public void updateProgress(@NonNull GUI gui) {
        renderContext.reset(); // Fix texture bleeding
        display(gui);
        window.update();
        window.pollEvents();
    }

    public static void shutdown() {
        finished = true;
    }

    public static boolean isFinished() {
        return finished;
    }

    private static void deleteLog(@NonNull Path log) throws IOException {
        for (Path LOG_FILES : com.oddlabs.util.Utils.LOG_FILES) {
            Path log_file = log.resolve(LOG_FILES);
            Files.deleteIfExists(log_file);
        }
        Files.deleteIfExists(log);
    }

    private static void deleteOldLogs(File last_log_dir, File new_log_dir, @NonNull File logs_dir) {
        File[] logs = logs_dir.listFiles();
        if (logs == null)
            return;
        for (File log : logs)
            try {
                if (!log.isDirectory() || log.equals(last_log_dir) || log.equals(new_log_dir))
                    continue;
                deleteLog(log.toPath());
            } catch (IOException _) {

            }
    }

    /**
     * Returns a directory path for the specified system property value or, if
     * the property or path is unavailable, the path of a temp directory.
     *
     * @param property name of the system property
     * @return Path to a directory or null if filesystem operations don't
     *         generally seem to work.
     */
    public static @Nullable Path getPropertyPath(@NonNull String property) {
        String propertyValue;
        try {
            propertyValue = System.getProperty(property);
        } catch (SecurityException disallowed) {
            // We are probably sandboxed.
            propertyValue = null;
        }
        Path result = null;
        if (null != propertyValue) {
            try {
                result = Path.of(propertyValue);
                if (Files.notExists(result))
                    result = Files.createDirectories(result);
                if (!Files.isDirectory(result) || !Files.isReadable(result)) {
                    // Path is not something we can use, fall back to temp
                    result = null;
                }
            } catch (IOException badnews) {
                result = null;
            }
        }

        if (null == result) {
            // whelp, let's use a temp directory if we can.
            try {
                result = Files.createTempDirectory(property);
            } catch (IOException | SecurityException totalFailure) {
                result = null;
            }
        }

        return result;
    }

    /**
     * Returns the user's home directory or if that is unavailable a temp
     * directory.
     *
     * @return Path to a directory or null if filesystem operations don't
     *         generally seem to work.
     */
    public static @Nullable Path getUserHomePath() {
        return getPropertyPath("user.home");
    }

    private static @Nullable Path getSteamSaveDir() {
        if (SteamManager.getInstance() == null) return null;
        try {
            java.security.CodeSource codeSource = Renderer.class.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                Path jarPath = Path.of(codeSource.getLocation().toURI());
                Path appDir = jarPath.getParent();
                if (appDir != null) {
                    Path saveDir = appDir.resolve("save_data");
                    Files.createDirectories(saveDir);
                    return saveDir;
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to resolve Steam save path, falling back to default: " + e.getMessage());
        }
        return null;
    }

    private static boolean isUsable(@Nullable Path path) {
        if (path == null) return false;
        try {
            if (!Files.exists(path) || !Files.isDirectory(path) || !Files.isWritable(path)) {
                return false;
            }
            // Try creating a temporary file to be absolutely sure we can actually write.
            // Some sandboxes (like macOS Seatbelt) might allow isWritable to return true
            // but block the actual creation of new files.
            try {
                Path testFile = Files.createTempFile(path, ".tt_write_test", null);
                Files.delete(testFile);
                return true;
            } catch (IOException | SecurityException e) {
                return false;
            }
        } catch (Throwable t) {
            return false;
        }
    }

    public record GamePaths(Path dataDir, Path logDir) {
    }

    private static @NonNull GamePaths setupPaths() throws IOException {
        Path dataDir = null;
        Path logDir = null;
        boolean portable = false;

        // 0. Steam Mode — save data next to the install for Steam Cloud sync
        Path steamDir = getSteamSaveDir();
        if (steamDir != null) {
            dataDir = steamDir;
            portable = true;
        }

        // 1. Check for Portable Mode (CWD)
        // If a game directory exists in the current working directory, use it.
        Path localDir = Path.of(Globals.GAME_NAME);
        if (isUsable(localDir)) {
            dataDir = localDir;
            portable = true;
        }

        // 2. Check for Portable Mode (App/JAR Directory)
        // If the JAR is launched from a different CWD, check next to the JAR.
        if (!portable) {
            try {
                java.security.CodeSource codeSource = Renderer.class.getProtectionDomain().getCodeSource();
                if (codeSource != null) {
                    Path jarPath = Path.of(codeSource.getLocation().toURI());
                    Path appDir = jarPath.getParent();
                    if (appDir != null) {
                        Path appGameDir = appDir.resolve(Globals.GAME_NAME);
                        // Avoid checking the same path twice if CWD == AppDir
                        if (!appGameDir.equals(localDir.toAbsolutePath()) && isUsable(appGameDir)) {
                            dataDir = appGameDir;
                            portable = true;
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore errors determining app directory
            }
        }

        String os_name;
        try {
            os_name = System.getProperty("os.name").toLowerCase();
        } catch (SecurityException e) {
            os_name = "unknown";
        }

        Path userHome = getUserHomePath();

        // 3. Resolve Data Directory (if not portable)
        if (!portable) {
            String xdgConfigHome = null;
            String appData = null;
            try {
                xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
                appData = System.getenv("APPDATA");
            } catch (SecurityException _) {
            }

            Path preferred = null;
            Path fallback = null;
            Path existing = null;

            if (os_name.contains("mac")) {
                if (userHome != null) {
                    Path appSupport = userHome.resolve("Library/Application Support/" + Globals.GAME_NAME);
                    Path config = userHome.resolve(".config/tribaltrouble");

                    if (isUsable(appSupport)) existing = appSupport;
                    else if (isUsable(config)) existing = config;

                    preferred = appSupport;
                    fallback = config;
                }
            } else if (os_name.contains("linux") || os_name.contains("unix")) {
                Path legacyDot = userHome != null ? userHome.resolve(".tribaltrouble") : null;
                Path omarDot = userHome != null ? userHome.resolve("." + Globals.GAME_NAME) : null;
                Path currentDot = Path.of(".tribaltrouble");

                Path xdg;
                if (xdgConfigHome != null && !xdgConfigHome.isEmpty()) {
                    xdg = Path.of(xdgConfigHome).resolve("tribaltrouble");
                } else if (userHome != null) {
                    xdg = userHome.resolve(".config/tribaltrouble");
                } else {
                    xdg = null;
                }

                if (isUsable(legacyDot)) existing = legacyDot;
                else if (isUsable(omarDot)) existing = omarDot;
                else if (isUsable(currentDot)) existing = currentDot;
                else if (isUsable(xdg)) existing = xdg;

                preferred = xdg;
                fallback = legacyDot;
            } else {
                Path roaming = appData != null ? Path.of(appData).resolve(Globals.GAME_NAME) : null;
                Path homeGame = userHome != null ? userHome.resolve(Globals.GAME_NAME) : null;

                if (isUsable(roaming)) existing = roaming;
                else if (isUsable(homeGame)) existing = homeGame;

                preferred = roaming;
                fallback = homeGame;
            }

            if (existing != null) {
                dataDir = existing;
            } else if (preferred != null) {
                try {
                    Files.createDirectories(preferred);
                    if (isUsable(preferred)) dataDir = preferred;
                } catch (IOException | SecurityException e) {
                    // Ignore
                }
            }

            if (dataDir == null && fallback != null) {
                try {
                    Files.createDirectories(fallback);
                    if (isUsable(fallback)) dataDir = fallback;
                } catch (IOException | SecurityException e) {
                    // Ignore
                }
            }

            if (dataDir == null) {
                dataDir = Files.createTempDirectory(Globals.GAME_NAME);
            }
        }

        // 4. Resolve Log Directory
        if (portable) {
            logDir = dataDir.resolve("logs");
        } else {
            // Try standard OS log locations first
            Path preferredLog = null;
            if (os_name.contains("mac")) {
                if (userHome != null) {
                    preferredLog = userHome.resolve("Library/Logs/TribalTrouble");
                }
            } else if (os_name.contains("linux") || os_name.contains("unix")) {
                String xdgStateHome = null;
                try {
                    xdgStateHome = System.getenv("XDG_STATE_HOME");
                } catch (SecurityException _) {
                }

                if (xdgStateHome != null && !xdgStateHome.isEmpty()) {
                    preferredLog = Path.of(xdgStateHome).resolve("tribaltrouble/logs");
                } else if (userHome != null) {
                    preferredLog = userHome.resolve(".local/state/tribaltrouble/logs");
                }
            } else {
                // Windows
                String localAppData = null;
                try {
                    localAppData = System.getenv("LOCALAPPDATA");
                } catch (SecurityException _) {
                }

                if (localAppData != null) {
                    preferredLog = Path.of(localAppData).resolve("TribalTrouble\\logs");
                }
            }

            if (preferredLog != null) {
                try {
                    Files.createDirectories(preferredLog);
                    if (isUsable(preferredLog)) {
                        logDir = preferredLog;
                    }
                } catch (IOException | SecurityException e) {
                    // Ignore
                }
            }

            // Fallback to dataDir/logs if standard location fails
            if (logDir == null) {
                logDir = dataDir.resolve("logs");
            }
        }

        return new GamePaths(dataDir, logDir);
    }

    public void run(@NonNull String @NonNull... args) throws IOException {
        Instant start_time = Instant.now();
        logger.info("CWD: " + System.getProperty("user.dir"));
        boolean first_frame = true;
        // This will be configured by setupLogging, but we need to log before that.
        GamePaths paths = setupPaths();
        Path game_dir = paths.dataDir();
        logger.info("********** Running tt **********");
        logger.info("version: " + BuildInfo.FULL_VERSION);
        logger.info("game dir: " + game_dir);
        logger.info("logs dir: " + paths.logDir());
        boolean eventload = false;
        boolean zipped = false;
        boolean silent = false;
        Path eventload_path = null;
        for (int i = 0; i < args.length; i++)
            switch (args[i]) {
            case "--grabframes" -> grab_frames = true;
            case "--eventload" -> {
                eventload = true;
                i++;
                switch (args[i]) {
                    case "zipped":
                        zipped = true;
                        break;
                    case "normal":
                        break;
                    default:
                        throw new RuntimeException("Unknown event load mode: " + args[i]);
                }
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    i++;
                    eventload_path = Path.of(args[i]);
                }
            }
            case "--silent" -> silent = true;
            default -> throw new IllegalArgumentException("Unknown command line flag: " + args[i]);
            }

        // fetch initial settings
        Settings settings = new Settings();
        settings.load(game_dir);

        if (eventload || grab_frames) {
            Path last_event_log_path = eventload_path != null ? eventload_path : settings.last_event_log_dir.resolve(
                    zipped ? "event.log.gz" : "event.log");
            logger.info("last_event_log_path = " + last_event_log_path);
            // Only use when anal debugging
//			ChecksumLogger.initLogging();
            LocalEventQueue.getQueue().loadEvents(last_event_log_path, zipped);
        }

        Path event_logs_dir = paths.logDir();
        Path event_log_dir = event_logs_dir.resolve(Long.toString(System.currentTimeMillis()));
        if (settings.save_event_log && !eventload) {
            setupLogging(event_log_dir, silent);
            LocalEventQueue.getQueue().setEventsLogged(event_log_dir.resolve(com.oddlabs.util.Utils.EVENT_LOG));
        }
        Deterministic deterministic = LocalEventQueue.getQueue().getDeterministic();
        game_dir = deterministic.log(game_dir);
        event_log_dir = deterministic.log(event_log_dir);
        settings = deterministic.log(settings);
        String default_language = deterministic.log(Locale.getDefault().getLanguage());
        String language = settings.language;
        if (language.equals("default"))
            language = default_language;
        if (!Languages.hasLanguage(language))
            language = "en";
        Locale.setDefault(Locale.of(language));
        Settings.setSettings(settings);
        Globals.gamespeed = settings.gamespeed;
        Path last_event_log_dir = settings.last_event_log_dir;
        boolean crashed = settings.crashed;
        NetworkSelector network = new NetworkSelector(LocalEventQueue.getQueue().getDeterministic(),
                LocalEventQueue.getQueue()::getMillis);
        initNetwork(network);
        Renderer.getLocalInput().settings(game_dir, event_log_dir, settings);
        try {
            initNative(crashed);
        } catch (Exception e) {
            // Let it propagate
            throw new IllegalStateException("Failed initializing natives", e);
        }

        if (!settings.inDeveloperMode() && !deterministic.isPlayback())
            deleteOldLogs(last_event_log_dir.toFile(), event_log_dir.toFile(), event_logs_dir.toFile());
        GlobalsInit.init();
        localInput.init();
        GUI gui = new GUI();

        Duration startup_time_init = Duration.between(start_time, Instant.now());
        logger.info("Init done after " + startup_time_init + "ms");
        ambient = new AmbientAudio(AudioManager.getManager());

        Runnable load_task = setupMainMenu(network, gui, true);

        boolean reset_keyboard = false;
        boolean wasActive = false;
        try {
            while (!finished) {
                window.pollEvents();
                boolean isActive = window.isActive();
                if (isActive && !wasActive) {
                    if (window.isIconified()) window.restore();
                    if (!window.isVisible()) window.show();
                    window.focus();
                }
                wasActive = isActive;

                // Always run simulation and network to avoid freezing multiplayer
                runGameLoop(network, gui);

                if (isActive) {
                    if (reset_keyboard) {
                        reset_keyboard = false;
                        Renderer.getLocalInput().resetKeyboard();
                    }
                } else {
                    reset_keyboard = true;
                }

                if (!window.isIconified()) {
                    if (!first_frame) {
                        window.update();
                    }
                    if (window.wasResized()) {
                        int width = window.getWidth();
                        int height = window.getHeight();
                        // Persist logical (point) dims so the saved value is what glfwCreateWindow
                        // will accept on next launch; using framebuffer dims doubles on retina.
                        Settings.getSettings().view_width = window.getLogicalWidth();
                        Settings.getSettings().view_height = window.getLogicalHeight();
                        GL11.glViewport(0, 0, width, height);
                        initGL();
                        gui.getGUIRoot().displayChanged(width, height);
                    }
                    display(gui);
                    if (first_frame) {
                        Duration startup_time = Duration.between(start_time, Instant.now());
                        logger.info("First frame rendered after " + startup_time);
                        first_frame = false;
                        if (load_task != null) {
                            window.update();
                            LocalEventQueue.getQueue().getDeterministic().setEnabled(true);
                            try {
                                load_task.run();
                            } finally {
                                LocalEventQueue.getQueue().getDeterministic().setEnabled(false);
                                renderContext.reset(); // Fix texture bleeding after loading
                            }
                            load_task = null;
                        }
                    }
                    if (grab_frames && movie_recording_started)
                        GLUtils.takeScreenshot("");
                } else {
                    // Minimized: throttle to save CPU since we can't render anyway
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("woken", e);
                    }
                }
            }
            LocalEventQueue.getQueue().getDeterministic().setEnabled(true);
            Settings.getSettings().save();
        } finally {
            cleanup();
        }
    }

    public @NonNull Locale getDefaultLocale() {
        return default_locale;
    }

    private void setupLogging(@NonNull Path event_log_dir, boolean silent) throws IOException {
        try {
            Files.createDirectories(event_log_dir);
            logger.info("Writing log files in " + event_log_dir);

            // Get the root logger and remove default handlers to prevent duplicate console output
            Logger rootLogger = Logger.getLogger("");
            for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            // Add a file handler
            FileHandler fileHandler = new FileHandler(event_log_dir.resolve("output.log").toString());
            fileHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(fileHandler);

            // Add a console handler unless in silent mode
            if (!silent) {
                ConsoleHandler consoleHandler = new ConsoleHandler();
                consoleHandler.setFormatter(new SimpleFormatter());
                rootLogger.addHandler(consoleHandler);
            }
            logger.info("version: " + BuildInfo.FULL_VERSION);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to setup file logging", e);
        }
    }

    private static void failedOpenGL(@NonNull Exception e) {
        try {
            logger.log(Level.SEVERE, "OpenGL Failure", e);
            Main.fail(e);
        } finally {
            Main.shutdown(1);
        }
    }

    public static void startMenu(@NonNull NetworkSelector network, @NonNull GUI gui) {
        setupMainMenu(network, gui, false);
    }

    private static @Nullable Runnable setupMainMenu(final @NonNull NetworkSelector network, @NonNull GUI gui,
            final boolean first_progress) {
        final WorldGenerator generator = new IslandGenerator(256, Landscape.TerrainType.NATIVE, Globals.LANDSCAPE_HILLS,
                Globals.LANDSCAPE_VEGETATION, Globals.LANDSCAPE_RESOURCES, Globals.LANDSCAPE_SEED, false);
        return ProgressForm.setProgressForm(network, gui, (GUIRoot gui_root) -> finishMainMenu(network, gui_root,
                first_progress, generator), first_progress);
    }

    private static @NonNull UIRenderer finishMainMenu(@NonNull NetworkSelector network, @NonNull GUIRoot gui_root,
            boolean first_progress, @NonNull WorldGenerator generator) {
        AnimationManager.freezeTime();
        PlayerInfo player_info = new PlayerInfo(0, 0, "");
        MatrixStack modelViewStack = new MatrixStack();
        MatrixStack projectionStack = new MatrixStack();
        WorldParameters world_params = new WorldParameters(Game.GAMESPEED_NORMAL, "", 2, Player.DEFAULT_MAX_UNIT_COUNT);
        PlayerInfo[] players = new PlayerInfo[]{player_info};
        WorldInfo world_info = generator.generate(players.length, world_params.getInitialUnitCount(), 0f);
        FogInfo fog_info = generator.getFogInfo();
        RenderQueues render_queues = new RenderQueues();
        LandscapeResources landscape_resources = World.loadCommon(render_queues);
        World world = World.newWorld(AudioManager.getManager(), landscape_resources, null, new NotificationListener() {
        }, world_params, world_info, generator.getTerrainType(), players, fog_info);
        AnimationManager manager = new AnimationManager();
        LandscapeRenderer landscape_renderer = new LandscapeRenderer(world, world_info, manager);
        Player local_player = world.getPlayers()[0];
        Selection selection = new Selection(local_player);
        UIRenderer renderer = new DefaultRenderer(getRenderer().cheat, local_player, render_queues, world_info,
                landscape_renderer, new Picker(manager, local_player, gui_root, render_queues, landscape_renderer,
                        selection), selection, generator, modelViewStack, projectionStack, null);
        Renderer.getRenderer().setMusicPath("/music/menu.ogg", 0f);
        MainMenu main_menu = new MainMenu(network, gui_root, new MenuCamera(world, manager));
        gui_root.pushDelegate(main_menu);
        if (first_progress && !BuildInfo.FULL_VERSION.equals(Settings.getSettings().patch_notes_seen)) {
            Settings.getSettings().patch_notes_seen = BuildInfo.FULL_VERSION;
            Settings.getSettings().save();
            gui_root.addModalForm(new PatchNotesForm());
        }
        if (first_progress && Settings.getSettings().warning_no_sound && !Renderer.getLocalInput().audioIsCreated()) {
            ResourceBundle bundle = ResourceBundle.getBundle(Renderer.class.getName());
            gui_root.addModalForm(new WarningForm(i18n("sound_not_available_caption"), i18n(
                    "sound_not_available_message")));
        }
        if (!initNetwork(network)) {
//			if (true) {
            ResourceBundle bundle = ResourceBundle.getBundle(Renderer.class.getName());
            gui_root.addModalForm(new MessageForm(i18n("network_not_available_caption"),
                    i18n("network_not_available_message"),
                    i18n("quit"), (_, _, _, _) -> shutdown()));
        }
        // We'll leave out the reporting, since checksum errors can happen when a peer is disconnected halfway through it's EOT
        // broadcast
        /*		if (Globals.checksum_error_in_last_game) {
        		Globals.checksum_error_in_last_game = false;
        		ResourceBundle bundle = ResourceBundle.getBundle(Renderer.class.getName());
        		GUIRoot.getGUIRoot().addModalForm(new QuestionForm(i18n("checksum_error_message"), new BugReportListener()));
        		}*/
        return renderer;
    }

    private static boolean initNetwork(@NonNull NetworkSelector network) {
        boolean is_network_created;
        try {
            network.initSelector();
            com.oddlabs.util.Utils.tryGetLoopbackAddress();
            is_network_created = true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize network", e);
            is_network_created = false;
        }
        return LocalEventQueue.getQueue().getDeterministic().log(is_network_created);
    }

    public void startMovieRecording() {
        logger.info("ACTION! Movie recording started.");
        movie_recording_started = true;
    }

    public void cleanup() {
        logger.info("Cleaning up...");
        logger.info("Disposing LocalEventQueue...");
        LocalEventQueue.getQueue().close();
        destroyNative();
        logger.fine("Native resources still registered: " + NativeResource.getCount());
        logger.info("Cleanup complete. Exiting");
    }

    public @NonNull SerializableDisplayMode getCurrentDisplayMode() {
        return LocalEventQueue.getQueue().getDeterministic().log(window.getDisplayMode());
    }

    public static void resetInput() {
        Renderer.getLocalInput().resetKeys();
    }

    public void toggleFullscreen() {
        try {
            boolean fs = !window.isFullscreen() && !LocalEventQueue.getQueue().getDeterministic().isPlayback();
            window.setFullscreen(fs);
            Settings.getSettings().fullscreen = fs;
            resetInput();
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Mode switching failed with exception", e);
            throw new RuntimeException("Mode switching failed");
        }
    }

    public void switchMode(@NonNull SerializableDisplayMode mode, boolean switch_now) {
        if (switch_now) {
            try {
                window.setDisplayMode(mode);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
            modeSwitchedNow(mode);
        } else
            modeSwitchedLater(mode);
    }

    public void setModeToNearest(@NonNull SerializableDisplayMode mode) {
        // Use window create to ensure window is created/resized
        boolean fs = Settings.getSettings().fullscreen && !OsPlatform.IS_MAC;
        window.create(mode, fs);
        modeSwitchedNow(mode);
    }

    private void modeSwitchedLater(@NonNull SerializableDisplayMode new_mode) {
        Settings.getSettings().fullscreen = window.isFullscreen();
        Settings.getSettings().new_view_width = new_mode.getWidth();
        Settings.getSettings().new_view_height = new_mode.getHeight();
        Settings.getSettings().new_view_freq = new_mode.getFrequency();
    }

    private void modeSwitchedNow(@NonNull SerializableDisplayMode new_mode) {
        modeSwitchedLater(new_mode);
        modeSwitched();
    }

    private void modeSwitched() {
        SerializableDisplayMode new_mode = LocalEventQueue.getQueue().getDeterministic().log(window.getDisplayMode());
        logger.info("Switched mode to " + new_mode);
        Settings.getSettings().view_width = new_mode.getWidth();
        Settings.getSettings().view_height = new_mode.getHeight();
        Settings.getSettings().view_freq = new_mode.getFrequency();
    }

    private static void destroyNative() {
        logger.info("Clearing Resources...");
        Resources.clearResources();
        logger.info("Closing AudioManager...");
        AudioManager.getManager().close();
        getRenderer().close();
        logger.info("Renderer Closed.");
    }

    public static void dumpWindowInfo() {
        try {
            GLUtils.checkGLError("Pre-dumpWindowInfo");
            int r = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER, GL11.GL_BACK_LEFT,
                    GL30.GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE);
            int g = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER, GL11.GL_BACK_LEFT,
                    GL30.GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE);
            int b = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER, GL11.GL_BACK_LEFT,
                    GL30.GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE);
            int a = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER, GL11.GL_BACK_LEFT,
                    GL30.GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE);
            int depth = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH,
                    GL30.GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE);
            int stencil = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL,
                    GL30.GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE);
            logger.info(
                    "Window Info: r=" + r + " g=" + g + " b=" + b + " a=" + a + " depth=" + depth + " stencil=" + stencil);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to dump window info", e);
        }
    }

    private void initNative(boolean crashed) throws Exception {
        String os_name = System.getProperty("os.name");
        logger.info("os.name = '" + os_name + "'");
        String os_arch = System.getProperty("os.arch");
        logger.info("os.arch = '" + os_arch + "'");
        String os_version = System.getProperty("os.version");
        logger.info("os.version = '" + os_version + "'");
        String java_version = System.getProperty("java.version");
        logger.info("java.version = '" + java_version + "'");
        String java_vendor = System.getProperty("java.vendor");
        logger.info("java.vendor = '" + java_vendor + "'");
        long total_mem = Runtime.getRuntime().maxMemory();
        logger.info("maxMemory = '" + total_mem + "'");

        AudioManager.getManager();

        try {
            int bpp = 32;
            try {
                bpp = window.getDisplayMode().getBitsPerPixel();
            } catch (Exception _) {
            } // ignore if not created

            window.setTitle("Tribal Trouble");
            // Fullscreen handled in create

            SerializableDisplayMode target_mode;
            int width = crashed ? Settings.getSettings().view_width : Settings.getSettings().new_view_width;
            int height = crashed ? Settings.getSettings().view_height : Settings.getSettings().new_view_height;
            int freq = crashed ? Settings.getSettings().view_freq : Settings.getSettings().new_view_freq;

            if (width == -1 || height == -1) {
                try {
                    SerializableDisplayMode[] modes = window.getAvailableDisplayModes();
                    if (modes.length > 0) {
                        target_mode = modes[0];
                    } else {
                        target_mode = new SerializableDisplayMode(800, 600, 32, 60);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to get available modes for default selection", e);
                    target_mode = new SerializableDisplayMode(800, 600, 32, 60);
                }
            } else {
                target_mode = new SerializableDisplayMode(width, height, bpp, freq);
            }

            // On macOS, ignore the persisted fullscreen flag — GLFW fullscreen has known issues
            // there and users get a better experience via the native green button.
            boolean fs = Settings.getSettings().fullscreen
                    && !OsPlatform.IS_MAC
                    && (!LocalEventQueue.getQueue().getDeterministic().isPlayback() || grab_frames);
            window.create(target_mode, fs);
            setModeToNearest(target_mode);

            Path iconPath = Path.of("tt/icons/DesktopIconImage.png");
            if (!Files.exists(iconPath)) {
                iconPath = Path.of("icons/DesktopIconImage.png");
            }
            logger.info("Setting icon from: " + iconPath.toAbsolutePath());
            window.setIcon(iconPath);

            int[] physSize = window.getMonitorPhysicalSize();
            logger.info("Monitor Physical Size: " + physSize[0] + "mm x " + physSize[1] + "mm");
            float[] monScale = window.getMonitorContentScale();
            logger.info("Monitor Content Scale: " + monScale[0] + "x, " + monScale[1] + "y");
            float[] winScale = window.getWindowContentScale();
            logger.info("Window Content Scale: " + winScale[0] + "x, " + winScale[1] + "y");

//if (System.currentTimeMillis() > 0)
//throw new LWJGLException("It failed because you asked it to.");
        } catch (Exception e) {
            AudioManager.getManager().close();
            failedOpenGL(e);
            throw e;
        }
        String version = GL11.glGetString(GL11.GL_VERSION);
        logger.info("GL version: '" + version + "'");
        String vendor = GL11.glGetString(GL11.GL_VENDOR);
        logger.info("GL vendor: '" + vendor + "'");
        String renderer = GL11.glGetString(GL11.GL_RENDERER);
        logger.info("GL renderer: '" + renderer + "'");

        renderContext.init();
        dumpWindowInfo();

        int num_combined_tex_units = GL11.glGetInteger(GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
        logger.info("GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS: " + num_combined_tex_units);
        if (num_combined_tex_units < 8) {
            throw new RuntimeException(
                    "Number of combined texture image units " + num_combined_tex_units + " is less than the required 8.");
        }

        resetInput();
        logger.info("vsync = " + Settings.getSettings().vsync);
        if (Settings.getSettings().vsync)
            window.setVSyncEnabled(true);
        initGL();
        initVisibleGL();
    }

    public void toggleSound() {
        Settings.getSettings().play_sfx = !Settings.getSettings().play_sfx;
        if (Settings.getSettings().play_sfx)
            AudioManager.getManager().startSources();
        else
            AudioManager.getManager().stopSources();
    }

    public void toggleMusic() {
        Settings.getSettings().play_music = !Settings.getSettings().play_music;
        if (Settings.getSettings().play_music) {
            initMusicPlayer();
        } else if (music != null) {
            music.stop(2.5f, Settings.getSettings().music_gain);
        }
    }

    private void initMusicPlayer() {
        music = AudioManager.getManager().newAudio(new AudioParameters<>(music_path));
    }

    public void setMusicPath(String music_path, float delay) {
        if (music != null && Settings.getSettings().play_music) {
            music.stop(2.5f, Settings.getSettings().music_gain);
        }
        Renderer.getRenderer().music_path = music_path;
        if (Settings.getSettings().play_music) {
            if (music_timer != null)
                music_timer.stop();
            music_timer = new TimerAnimation(new MusicTimer(), delay);
            music_timer.start();
        }
    }

    private final class MusicTimer implements Updatable {
        @Override
        public void update(@NonNull Object anim) {
            if (music_timer != null)
                music_timer.stop();
            music_timer = null;
            if (Settings.getSettings().play_music) {
                initMusicPlayer();
            }
        }
    }

    public AbstractAudioPlayer getMusicPlayer() {
        return music;
    }

    private void initVisibleGL() {
        if (window != null) window.update();
    }

    public static void initGL() {
        VBO.releaseAll();
        getRenderer().renderContext.applyDefaults();
    }

    public static void clearScreen() {
        GL11.glClearColor(0f, 0f, 0f, 0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    public boolean isCheater() {
        return cheat != null && cheat.isEnabled();
    }

    public void setCheat(@Nullable Cheat cheat) {
        this.cheat = cheat;
    }
}
