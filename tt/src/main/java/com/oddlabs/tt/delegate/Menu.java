package com.oddlabs.tt.delegate;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.matchmaking.MatchmakingServerInterface;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.camera.Camera;
import com.oddlabs.tt.form.ConnectingForm;
import com.oddlabs.tt.form.OptionsMenu;
import com.oddlabs.tt.form.QuitForm;
import com.oddlabs.tt.form.SelectGameMenu;
import com.oddlabs.tt.gui.FocusDirection;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUI;
import com.oddlabs.tt.gui.GUIImage;
import com.oddlabs.tt.gui.GUIObject;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.MenuButton;
import com.oddlabs.tt.input.GameAction;
import com.oddlabs.tt.input.InputEvent;
import com.oddlabs.tt.input.InputPhase;
import com.oddlabs.tt.landscape.WorldParameters;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.GameNetwork;
import com.oddlabs.tt.net.Server;
import com.oddlabs.tt.net.WorldInitAction;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.resource.IslandGenerator;
import com.oddlabs.tt.resource.WorldGenerator;
import com.oddlabs.tt.trigger.GameOverTrigger;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.viewer.InGameInfo;
import com.oddlabs.tt.viewer.MultiplayerInGameInfo;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.util.Color;
import org.joml.Vector4fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.Desktop;
import java.net.InetAddress;
import java.net.URI;
import java.util.ResourceBundle;

public abstract class Menu extends CameraDelegate<Camera> {
    static final Vector4fc COLOR_NORMAL = Color.WHITE;
    static final Vector4fc COLOR_ACTIVE = Color.argb4v(0xFF_FF_CC_9F);
    private static final int MENU_X = 160;
    private static final int overlay_texture_width = 1024;
    private static final int overlay_texture_height = 1024;
    private static final int overlay_image_width = 800;
    private static final int overlay_image_height = 600;
    private static final String overlay_texture_name = "/textures/gui/mainmenu";
    private static final String discord_texture_name = "/textures/gui/discord";
    private static final String github_texture_name = "/textures/gui/github";

    private static final ResourceBundle bundle = ResourceBundle.getBundle(MainMenu.class.getName());

    public static @NonNull String i18n(@NonNull String key, @NonNull Object @NonNull... args) {
        return Utils.getBundleString(bundle, key, args);
    }

    private final @NonNull NetworkSelector network;

    private @Nullable Form current_menu;
    private boolean current_menu_centered;

    private @Nullable GUIImage overlay;
    private @Nullable GUIImage logo;
    private @Nullable GUIImage discord;
    private @Nullable GUIImage github;

    protected Menu(@NonNull NetworkSelector network, @NonNull GUIRoot gui_root, @NonNull Camera camera) {
        super(gui_root, camera);
        this.network = network;
        setCanFocus(true);
        setFocusCycle(true);
    }

    final @NonNull NetworkSelector getNetwork() {
        return network;
    }

    private void init() {
        clearChildren();
        int screen_width = getGUIRoot().getWidth();
        int screen_height = getGUIRoot().getHeight();
        overlay = new GUIImage(screen_width, screen_height, 0f, 0f, (float) overlay_image_width / overlay_texture_width,
                (float) overlay_image_height / overlay_texture_height, overlay_texture_name);
        overlay.setPos(0, 0);
        addChild(overlay);

        String logo_file = i18n("logo_file");

        float heightScale = screen_height / 600f;
        int logoHeight = (int) (206f * heightScale);
        int logoWidth = (int) (347f * heightScale);

        logo = new GUIImage(logoWidth, logoHeight, 0f, 0f, 347f / 512f, 206f / 256f, logo_file);
        logo.setPos(0, screen_height - logoHeight);
        addChild(logo);

        // Discord and GitHub buttons in bottom-right corner
        github = new GUIImage(76, 76, 0f, 0f, 1f, 1f, github_texture_name, true);
        github.setPos(screen_width - github.getWidth() - 20, github.getHeight() / 2 + 4);
        github.addMouseClickListener((_, _, _, _) -> openURL(Settings.GITHUB_URL));
        addChild(github);

        discord = new GUIImage(80, 80, 0f, 0f, 1f, 1f, discord_texture_name, true);
        discord.setPos(screen_width - discord.getWidth() - 20 - github.getWidth() - 20, discord.getHeight() / 2);
        discord.addMouseClickListener((_, _, _, _) -> openURL(Settings.DISCORD_URL));
        addChild(discord);
    }

    private static void openURL(@NonNull String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.err.println("Failed to open URL: " + e.getMessage());
        }
    }

    final void addDefaultOptionsButton() {
        addOptionsButton(() -> new OptionsMenu(getGUIRoot()));
    }

    final void addOptionsButton(@NonNull FormFactory<?> factory) {
        MenuButton options = new MenuButton(i18n("options"), COLOR_NORMAL, COLOR_ACTIVE);
        options.addMouseClickListener((_, _, _, _) -> setMenuCentered(factory.create()));
        addChild(options);
    }

    final void addExitButton() {
        MenuButton exit = new MenuButton(i18n("quit"), COLOR_NORMAL, COLOR_ACTIVE);
        exit.addMouseClickListener((_, _, _, _) -> setMenuCentered(new QuitForm(getGUIRoot())));
        addChild(exit);
    }

    protected abstract void addButtons();

    final void reload() {
        init();
        addButtons();

        displayChangedNotify(getGUIRoot().getWidth(), getGUIRoot().getHeight());
    }

    @Override
    public void handleInput(@NonNull InputEvent event) {
        if ((event.getPhase() == InputPhase.PRESSED || event.getPhase() == InputPhase.REPEAT) && event.hasActions()) {
            if (event.consumeAction(GameAction.UI_CANCEL)) {
                event.consume(); // Menu usually swallows escape
                return;
            }
            if (event.consumeAction(GameAction.UI_FOCUS_NEXT)) {
                switchFocus(FocusDirection.FORWARD);
                event.consume();
                return;
            }
            if (event.consumeAction(GameAction.UI_FOCUS_PREV)) {
                switchFocus(FocusDirection.BACKWARD);
                event.consume();
                return;
            }
            if (event.consumeAction(GameAction.UI_NAV_UP)) {
                focusPrior();
                event.consume();
                return;
            }
            if (event.consumeAction(GameAction.UI_NAV_DOWN)) {
                focusNext();
                event.consume();
                return;
            }
        }

        super.handleInput(event);
    }

    @Override
    public void displayChangedNotify(int width, int height) {
        setDim(width, height);

        int y = height - (int) (190f * height / overlay_image_height);
        int x = 15;

        overlay.setDim(width, height);

        // Maintain aspect ratio based on height
        float heightScale = height / 600f;
        int logoHeight = (int) (206f * heightScale);
        int logoWidth = (int) (347f * heightScale);

        logo.setDim(logoWidth, logoHeight);
        logo.setPos(0, height - logoHeight);

        // Reposition Discord/GitHub buttons on resize
        if (github != null) {
            github.setPos(width - github.getWidth() - 20, github.getHeight() / 2 + 4);
        }
        if (discord != null) {
            discord.setPos(width - discord.getWidth() - 20 - (github != null ? github.getWidth() + 20 : 0),
                    discord.getHeight() / 2);
        }

        GUIObject child = getLastChild();
        while (child != null) {
            if (child instanceof MenuButton) {
                child.setPos(x, y - child.getHeight());
                y -= (int) (child.getHeight() * .875);
            }
            child = child.getPrior();
        }
        if (current_menu != null) {
            if (current_menu_centered) {
                current_menu.centerPos();
            } else {
                positionMenu();
            }
        }
    }

    private void disableButtons(boolean disabled) {
        GUIObject child = getLastChild();
        while (child != null) {
            if (child instanceof MenuButton button) {
                button.setDisabled(disabled);
            }
            child = child.getPrior();
        }
    }

    @Override
    public final void setFocus() {
        if (current_menu != null) {
            current_menu.setFocus();
        } else {
            GUIObject child = getLastChild();
            while (child != null) {
                if (child instanceof MenuButton button) {
                    button.setFocus();
                    break;
                }
                child = child.getPrior();
            }
            super.setFocus();
            focusNext();
        }
    }

    @Override
    public void mouseScrolled(int amount) {
    }

    public final void setMenuCentered(@NonNull Form menu) {
        setMenu(menu);
        menu.centerPos();
        current_menu_centered = true;
    }

    public final void setMenu(@NonNull Form menu) {
        if (current_menu != null)
            current_menu.remove();
        disableButtons(true);
        menu.addCloseListener(() -> {
            disableButtons(false);
            current_menu = null;
        });
        current_menu = menu;
        addChild(current_menu);
        current_menu.setFocus();
        positionMenu();
        current_menu_centered = false;
    }

    private void positionMenu() {
        current_menu.setPos(MENU_X, (getGUIRoot().getHeight() - current_menu.getHeight()) * 2 / 3);
    }

    protected final void addResumeButton() {
        MenuButton resume = new MenuButton(i18n("resume"), COLOR_NORMAL, COLOR_ACTIVE);
        addChild(resume);
        resume.addMouseClickListener((_, _, _, _) -> pop());
    }

    public static void completeGameSetupHack(@NonNull WorldViewer world_viewer) {
        world_viewer.getGUIRoot().pushDelegate(world_viewer.getDelegate());
        Renderer.getRenderer().setMusicPath(world_viewer.getLocalPlayer().getRace().getMusicPath(), 10f);
    }

    public static final class DefaultWorldInitAction implements WorldInitAction {
        @Override
        public void run(@NonNull WorldViewer viewer) {
            new GameOverTrigger(viewer);
            if (viewer.isMultiplayer()) {
                viewer.getGUIRoot().pushDelegate(new CountdownDelegate(viewer, viewer.getCamera()));
            } else {
                completeGameSetupHack(viewer);
            }
        }
    }

    public final @NonNull GameNetwork joinGame(@NonNull NetworkSelector network, GUI gui, int host_id,
            @NonNull Game game, SelectGameMenu owner) {
        GUIRoot gui_root = getGUIRoot();
        // spotless:off
        WorldParameters world_params = WorldParameters.builder()
                .initialGameSpeed(game.getGamespeed())
                .mapcode(game.getMapcode())
                .initialUnitCount(game.getInitialUnitCount())
                .maxUnitCount(game.getMaxUnitCount())
                .mapSize(game.getSize())
                .maxBuildingCount(game.getMaxBuildingCount())
                .ships(game.isShips())
                .build();
        // spotless:on
        Client client = new Client(null, network, gui, host_id, world_params,
                new MultiplayerInGameInfo(game.getRandomStartPos(), game.isRated()),
                new DefaultWorldInitAction());
        GameNetwork game_network = new GameNetwork(null, client);
        ConnectingForm connecting_form = new ConnectingForm(game_network, getGUIRoot(), owner, true);
        client.setConfigurationListener(connecting_form);
        gui_root.addModalForm(connecting_form);
        return game_network;
    }

    public static @NonNull GameNetwork startNewGame(@NonNull NetworkSelector network, @NonNull GUIRoot gui_root,
            SelectGameMenu owner, WorldParameters world_params, @NonNull InGameInfo ingame_info,
            WorldInitAction init_action, Game game, int meters_per_world, Landscape.@NonNull TerrainType terrain,
            float hills, float vegetation_amount, float supplies_amount, int seed, boolean archipelago,
            String[] ai_names) {
        return startNewGame(network, gui_root, owner, world_params, ingame_info, init_action, game, meters_per_world,
                terrain, hills, vegetation_amount, supplies_amount, seed, archipelago, ai_names,
                MatchmakingServerInterface.MAX_PLAYERS);
    }

    public static @NonNull GameNetwork startNewGame(@NonNull NetworkSelector network, @NonNull GUIRoot gui_root,
            SelectGameMenu owner, WorldParameters world_params, @NonNull InGameInfo ingame_info,
            WorldInitAction init_action, Game game, int meters_per_world, Landscape.@NonNull TerrainType terrain,
            float hills, float vegetation_amount, float supplies_amount, int seed, boolean archipelago,
            String[] ai_names,
            int player_count) {
        boolean multiplayer = ingame_info.isMultiplayer();
        WorldGenerator generator = new IslandGenerator(meters_per_world, terrain, hills, vegetation_amount,
                supplies_amount, seed, archipelago);
        InetAddress address = multiplayer ? null : com.oddlabs.util.Utils.getLoopbackAddress();
        final Server server = new Server(network, game, address, generator, multiplayer, ai_names, player_count);
        Client client = new Client(server::close, network, gui_root.getGUI(), -1, world_params, ingame_info,
                init_action);
        GameNetwork game_network = new GameNetwork(server, client);
        ConnectingForm connecting_form = new ConnectingForm(game_network, gui_root, owner, multiplayer);
        client.setConfigurationListener(connecting_form);
        gui_root.addModalForm(connecting_form);
        return game_network;
    }
}
