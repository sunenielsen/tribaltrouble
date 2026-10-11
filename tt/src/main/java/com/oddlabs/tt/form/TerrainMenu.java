package com.oddlabs.tt.form;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.matchmaking.GameMode;
import com.oddlabs.matchmaking.GameSession;
import com.oddlabs.matchmaking.MatchmakingServerInterface;
import com.oddlabs.matchmaking.Preset;
import com.oddlabs.matchmaking.RosterTemplate;
import com.oddlabs.matchmaking.StandardOptions;
import com.oddlabs.matchmaking.WorldConfig;
import com.oddlabs.net.NetworkSelector;
import com.oddlabs.registration.RegistrationKey;
import com.oddlabs.tt.delegate.Menu;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.gamemode.PresetLibrary;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.CancelButton;
import com.oddlabs.tt.gui.CheckBox;
import com.oddlabs.tt.gui.EditLine;
import com.oddlabs.tt.gui.GUIObject;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.MouseButton;
import com.oddlabs.tt.gui.OKButton;
import com.oddlabs.tt.gui.Origin;
import com.oddlabs.tt.gui.Panel;
import com.oddlabs.tt.gui.PanelGroup;
import com.oddlabs.tt.gui.PulldownButton;
import com.oddlabs.tt.gui.PulldownItem;
import com.oddlabs.tt.gui.PulldownMenu;
import com.oddlabs.tt.gui.ScrollableGroup;
import com.oddlabs.tt.gui.ScrollablePulldownMenu;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.Slider;
import com.oddlabs.tt.guievent.ItemChosenListener;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.guievent.ValueListener;
import com.oddlabs.tt.landscape.WorldParameters;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.net.GameNetwork;
import com.oddlabs.tt.net.Network;
import com.oddlabs.tt.net.PlayerSlot;
import com.oddlabs.tt.procedural.Landscape;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.ServerMessageBundler;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.util.WordsEncoding;
import com.oddlabs.tt.viewer.DefaultInGameInfo;
import com.oddlabs.tt.viewer.InGameInfo;
import com.oddlabs.tt.viewer.MultiplayerInGameInfo;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.UUID;

import static com.oddlabs.tt.gui.Placement.BOTTOM_LEFT;
import static com.oddlabs.tt.gui.Placement.BOTTOM_RIGHT;
import static com.oddlabs.tt.gui.Placement.LEFT_MID;
import static com.oddlabs.tt.gui.Placement.RIGHT_MID;
import static com.oddlabs.tt.gui.Placement.TOP_LEFT;
import static com.oddlabs.tt.gui.Placement.TOP_MID;
import static com.oddlabs.tt.gui.Placement.TOP_RIGHT;

public final class TerrainMenu extends Group {
    private static final int[] SIZES = new int[]{256, 512, 1024, 2048, 2048};
    private static final boolean[] ARCHIPELAGO = new boolean[]{false, false, false, false, true};

    private static final int SLIDER_LENGTH = 250;
    private static final int BUTTON_WIDTH = 100;
    private static final int SLIDER_MAX_VALUE = 10;

    private static final String SEED_CARDINALITY = "40000";
    private static final int SLIDER_CARDINALITY = 11;
    private static final int TERRAIN_TYPE_CARDINALITY = 4;
    private static final int TERRAIN_TYPE_CARDINALITY_LEGACY = 2;
    private static final int SIZE_CARDINALITY = 7;
    private static final int SIZE_CARDINALITY_LEGACY = 4;
    private static final int DIFFICULTY_CARDINALITY = 4;
    private static final int RACE_CARDINALITY = 2;
    private static final int TEAM_CARDINALITY = 6;
    private static final @NonNull BigInteger MAX_VALUE;
    private static final @NonNull BigInteger MAX_VALUE_LEGACY;

    private static final ResourceBundle bundle = ResourceBundle.getBundle(TerrainMenu.class.getName());

    private @NonNull String i18n(@NonNull String key, @NonNull Object @NonNull... args) {
        return Utils.getBundleString(bundle, key, args);
    }

    private final @Nullable Menu main_menu;
    private final @Nullable TerrainMenuListener owner;

    private final @NonNull PulldownMenu<Void> pulldown_size;
    private final @NonNull EditLine editline_name;
    private final @NonNull PulldownMenu<Void> pm_terrain_type;
    private final @NonNull Slider slider_hills;
    private final @NonNull Slider slider_vegetation;
    private final @NonNull Slider slider_supplies;
    private final @NonNull Label label_mapcode;
    private final @NonNull HorizButton button_ok;
    private final @NonNull HorizButton button_advanced;
    private final @NonNull PulldownMenu<Void> @NonNull [] difficulty_pulldown_menus;
    private final @NonNull PulldownMenu<Void> @NonNull [] race_pulldown_menus;
    private final @NonNull PulldownMenu<Void> @NonNull [] team_pulldown_menus;
    private final @NonNull PulldownButton<Void> @NonNull [] difficulty_pulldown_buttons;
    private final @NonNull PulldownButton<Void> @NonNull [] race_pulldown_buttons;
    private final @NonNull PulldownButton<Void> @NonNull [] team_pulldown_buttons;
    private final @NonNull Label @NonNull [] labels_players;
    private final @NonNull CheckBox cb_rated;
    private final boolean multiplayer;
    private final @NonNull PulldownMenu<Void> pm_gamespeed;
    private final @NonNull GUIRoot gui_root;
    private final @NonNull NetworkSelector network;
    private final @NonNull PresetLibrary preset_library = new PresetLibrary();
    private final @Nullable RosterPanel roster_panel;
    private final @Nullable ModeAndPresetsPanel mode_and_presets;
    private final @NonNull ScrollablePulldownMenu<Void> pulldown_menu_slots;
    private static final int DEFAULT_PLAYER_COUNT = 6;
    private int player_count = DEFAULT_PLAYER_COUNT;
    private int seed;
    private AdvancedSettingsForm.@NonNull Values advanced_settings = AdvancedSettingsForm.Values.defaults();
    private @Nullable Preset current_preset;
    private boolean modified;
    private boolean apply_in_progress;
    private boolean initialized;

    static {
        // Legacy mapcode encoding (per-player settings, 6 players)
        BigInteger max = BigInteger.ONE;
        max = max.multiply(new BigInteger(SEED_CARDINALITY));
        max = max.multiply(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        max = max.multiply(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        max = max.multiply(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        max = max.multiply(new BigInteger(new byte[]{TERRAIN_TYPE_CARDINALITY_LEGACY}));
        max = max.multiply(new BigInteger(new byte[]{SIZE_CARDINALITY_LEGACY}));
        max = max.multiply(new BigInteger(new byte[]{RACE_CARDINALITY}));
        max = max.multiply(new BigInteger(new byte[]{TEAM_CARDINALITY}));
        for (int i = 1; i < DEFAULT_PLAYER_COUNT; i++) {
            max = max.multiply(new BigInteger(new byte[]{DIFFICULTY_CARDINALITY}));
            max = max.multiply(new BigInteger(new byte[]{RACE_CARDINALITY}));
            max = max.multiply(new BigInteger(new byte[]{TEAM_CARDINALITY}));
        }
        MAX_VALUE_LEGACY = max;

        // New word-based mapcode encoding (terrain-only, supports more sizes/types)
        max = BigInteger.ONE;
        max = max.multiply(new BigInteger(SEED_CARDINALITY));
        max = max.multiply(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        max = max.multiply(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        max = max.multiply(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        max = max.multiply(new BigInteger(new byte[]{TERRAIN_TYPE_CARDINALITY}));
        max = max.multiply(new BigInteger(new byte[]{SIZE_CARDINALITY}));
        MAX_VALUE = max;
    }

    @SuppressWarnings("unchecked")
    public TerrainMenu(@NonNull NetworkSelector network, @NonNull GUIRoot gui_root, @Nullable Menu main_menu,
            boolean multiplayer, @Nullable TerrainMenuListener owner) {
        this.network = network;
        this.main_menu = main_menu;
        this.multiplayer = multiplayer;
        this.owner = owner;
        this.gui_root = gui_root;

        // headline
        Label label_headline = new Label(i18n(multiplayer ? "new_game" : "skirmish"), Skin.getSkin().getHeadlineFont());
        addChild(label_headline);
        if (multiplayer) {
            preset_library.load(Renderer.getLocalInput().getGameDir().resolve(Globals.getPresetsFileName()));
        }
        mode_and_presets = multiplayer ? new ModeAndPresetsPanel(gui_root, preset_library, new PresetsHandler()) : null;
        Panel standard = new Panel(i18n("standard_options"));
        Panel advanced = new Panel(i18n("advanced_options"));
        roster_panel = multiplayer ? new RosterPanel() : null;
        Group group_map_options = new Group();

        // game name
        Label label_name = new Label(i18n("game_name"), Skin.getSkin().getEditFont());
        Label label_default_name = null;
        editline_name = new EditLine(180, Game.MAX_LENGTH);
        if (multiplayer) {
            standard.addChild(label_name);
            String default_name = i18n("default_name", Network.getMatchmakingClient().getProfile().getNick());
            label_default_name = new Label(default_name, Skin.getSkin().getEditFont());
            editline_name.append(default_name);
            if (Renderer.isRegistered())
                standard.addChild(editline_name);
            else
                standard.addChild(label_default_name);
        }
        String rated_tip = i18n("rated_game_tip", GameSession.MIN_WINS_FOR_RANKING);
        cb_rated = new CheckBox(false, i18n("rated_game"), rated_tip);
        if (multiplayer) {
            standard.addChild(cb_rated);
            cb_rated.setDisabled(Network.getMatchmakingClient().getProfile() == null
                    || Network.getMatchmakingClient().getProfile().getWins() < GameSession.MIN_WINS_FOR_RANKING);
        }

        // gamespeed
        Group group_gamespeed = new Group();
        Label label_gamespeed = new Label(i18n("gamespeed"), Skin.getSkin().getEditFont());
        group_gamespeed.addChild(label_gamespeed);
        pm_gamespeed = new PulldownMenu<>();
        pm_gamespeed.addItem(new PulldownItem<>(ServerMessageBundler.getGamespeedString(Game.GAMESPEED_SLOW)));
        pm_gamespeed.addItem(new PulldownItem<>(ServerMessageBundler.getGamespeedString(Game.GAMESPEED_NORMAL)));
        pm_gamespeed.addItem(new PulldownItem<>(ServerMessageBundler.getGamespeedString(Game.GAMESPEED_FAST)));
        pm_gamespeed.addItem(new PulldownItem<>(ServerMessageBundler.getGamespeedString(Game.GAMESPEED_LUDICROUS)));
        // Default the pulldown to the player's game speed setting from the options menu.
        int gamespeed_index = Math.clamp(Globals.gamespeed - Game.GAMESPEED_SLOW, 0,
                Game.GAMESPEED_LUDICROUS - Game.GAMESPEED_SLOW);
        var pb_gamespeed = new PulldownButton<>(gui_root, pm_gamespeed, gamespeed_index, 150);
        pm_gamespeed.addItemChosenListener((_, _) -> markModified());
        group_gamespeed.addChild(pb_gamespeed);
        label_gamespeed.place();
        pb_gamespeed.place(label_gamespeed, RIGHT_MID);
        group_gamespeed.compileCanvas();

        if (multiplayer) {
            group_map_options.addChild(group_gamespeed);
        }
        // size
        Group group_size = new Group();

        Label label_size = new Label(i18n("island_size"), Skin.getSkin().getEditFont());
        group_size.addChild(label_size);

        pulldown_size = new PulldownMenu<>();
        pulldown_size.addItem(new PulldownItem<>(ServerMessageBundler.getSizeString(Game.SIZE_SMALL)));
        pulldown_size.addItem(new PulldownItem<>(ServerMessageBundler.getSizeString(Game.SIZE_MEDIUM)));
        pulldown_size.addItem(new PulldownItem<>(ServerMessageBundler.getSizeString(Game.SIZE_LARGE)));
        pulldown_size.addItem(new PulldownItem<>(ServerMessageBundler.getSizeString(Game.SIZE_ENORMOUS)));
        if (Globals.SHIPS_ENABLED) {
            pulldown_size.addItem(new PulldownItem<>(ServerMessageBundler.getSizeString(Game.SIZE_ARCHIPELAGO)));
        }

        var pb_size = new PulldownButton<>(gui_root, pulldown_size, 1, 150);
        group_size.addChild(pb_size);
        label_size.place();
        pb_size.place(label_size, RIGHT_MID);
        group_size.compileCanvas();
        group_map_options.addChild(group_size);
        pulldown_size.addItemChosenListener(new PulldownUpdateMapcodeListener());

        // seed
        Label label_seed = new Label(i18n("map_code"), Skin.getSkin().getEditFont());
        label_mapcode = new Label("", Skin.getSkin().getHeadlineFont(), 500);

        Group group_seed = new Group();
        group_seed.addChild(label_seed);
        group_seed.addChild(label_mapcode);
        label_seed.place();
        label_mapcode.place(label_seed, RIGHT_MID);
        group_seed.compileCanvas();
        advanced.addChild(group_seed);

        // terrain_type
        Group group_terrain_type = new Group();
        Label label_terrain_type = new Label(i18n("terrain_type"), Skin.getSkin().getEditFont());
        group_terrain_type.addChild(label_terrain_type);
        pm_terrain_type = new PulldownMenu<>();
        pm_terrain_type.addItem(new PulldownItem<>(ServerMessageBundler.getTerrainTypeString(
                Game.TERRAIN_TYPE_NATIVE)));
        pm_terrain_type.addItem(new PulldownItem<>(ServerMessageBundler.getTerrainTypeString(
                Game.TERRAIN_TYPE_VIKING)));
        var pb_terrain_type = new PulldownButton<>(gui_root, pm_terrain_type, 0, 150);
        group_terrain_type.addChild(pb_terrain_type);
        label_terrain_type.place();
        pb_terrain_type.place(label_terrain_type, RIGHT_MID);
        group_terrain_type.compileCanvas();
        pm_terrain_type.addItemChosenListener(new PulldownUpdateMapcodeListener());
        group_map_options.addChild(group_terrain_type);

        Group group_sliders = new Group();
        // hills
        Label label_hills_low = new Label(i18n("min"), Skin.getSkin().getEditFont());
        group_sliders.addChild(label_hills_low);
        Label label_hills_high = new Label(i18n("max"), Skin.getSkin().getEditFont());
        group_sliders.addChild(label_hills_high);
        Label label_hills = new Label(i18n("hills"), Skin.getSkin().getEditFont());
        group_sliders.addChild(label_hills);
        slider_hills = new Slider(SLIDER_LENGTH, 0, SLIDER_MAX_VALUE, SLIDER_MAX_VALUE / 2);
        slider_hills.addValueListener(new SliderUpdateMapcodeListener());
        group_sliders.addChild(slider_hills);

        // vegetation
        Label label_vegetation_low = new Label(i18n("min"), Skin.getSkin().getEditFont());
        group_sliders.addChild(label_vegetation_low);
        Label label_vegetation_high = new Label(i18n("max"), Skin.getSkin().getEditFont());
        group_sliders.addChild(label_vegetation_high);
        Label label_vegetation = new Label(i18n("trees"), Skin.getSkin().getEditFont());
        group_sliders.addChild(label_vegetation);
        slider_vegetation = new Slider(SLIDER_LENGTH, 0, SLIDER_MAX_VALUE, SLIDER_MAX_VALUE / 2);
        slider_vegetation.addValueListener(new SliderUpdateMapcodeListener());
        group_sliders.addChild(slider_vegetation);

        // supplies
        Label label_supplies_low = new Label(i18n("min"), Skin.getSkin().getEditFont());
        group_sliders.addChild(label_supplies_low);
        Label label_supplies_high = new Label(i18n("max"), Skin.getSkin().getEditFont());
        group_sliders.addChild(label_supplies_high);
        Label label_supplies = new Label(i18n("resources"), Skin.getSkin().getEditFont());
        group_sliders.addChild(label_supplies);
        slider_supplies = new Slider(SLIDER_LENGTH, 0, SLIDER_MAX_VALUE, SLIDER_MAX_VALUE / 2);
        slider_supplies.addValueListener(new SliderUpdateMapcodeListener());
        group_sliders.addChild(slider_supplies);

        // supplies
        label_supplies.place();
        label_supplies_low.place(label_supplies, RIGHT_MID);
        slider_supplies.place(label_supplies_low, RIGHT_MID);
        label_supplies_high.place(slider_supplies, RIGHT_MID);
        // vegetation
        label_vegetation.place(label_supplies, TOP_LEFT, Skin.getSkin().getFormData().sectionSpacing());
        slider_vegetation.place(slider_supplies, TOP_MID, Skin.getSkin().getFormData().sectionSpacing());
        label_vegetation_low.place(slider_vegetation, LEFT_MID);
        label_vegetation_high.place(slider_vegetation, RIGHT_MID);
        // hills
        label_hills.place(label_vegetation, TOP_LEFT, Skin.getSkin().getFormData().sectionSpacing());
        slider_hills.place(slider_vegetation, TOP_MID, Skin.getSkin().getFormData().sectionSpacing());
        label_hills_low.place(slider_hills, LEFT_MID);
        label_hills_high.place(slider_hills, RIGHT_MID);
        // sliders
        group_sliders.compileCanvas();
        advanced.addChild(group_sliders);

        // player count dropdown (in advanced panel)
        Group group_num_players = new Group();
        Label label_player_slots = new Label(i18n("players"), Skin.getSkin().getEditFont());
        group_num_players.addChild(label_player_slots);
        pulldown_menu_slots = new ScrollablePulldownMenu<>(DEFAULT_PLAYER_COUNT);
        for (int i = DEFAULT_PLAYER_COUNT; i <= MatchmakingServerInterface.MAX_PLAYERS; i++) {
            pulldown_menu_slots.addItem(new PulldownItem<>(Integer.toString(i)));
        }
        var pulldown_player_slots = new PulldownButton<>(gui_root, pulldown_menu_slots, 0, 150);
        group_num_players.addChild(pulldown_player_slots);
        label_player_slots.place();
        pulldown_player_slots.place(label_player_slots, RIGHT_MID);
        group_num_players.compileCanvas();
        advanced.addChild(group_num_players);

        // races and teams
        labels_players = new Label[MatchmakingServerInterface.MAX_PLAYERS];
        difficulty_pulldown_menus = new PulldownMenu[MatchmakingServerInterface.MAX_PLAYERS];
        race_pulldown_menus = new PulldownMenu[MatchmakingServerInterface.MAX_PLAYERS];
        team_pulldown_menus = new PulldownMenu[MatchmakingServerInterface.MAX_PLAYERS];
        difficulty_pulldown_buttons = new PulldownButton[MatchmakingServerInterface.MAX_PLAYERS];
        race_pulldown_buttons = new PulldownButton[MatchmakingServerInterface.MAX_PLAYERS];
        team_pulldown_buttons = new PulldownButton[MatchmakingServerInterface.MAX_PLAYERS];
        ScrollableGroup group_race_team = buildPlayerSlots(player_count);
        if (multiplayer) {
            roster_panel.setRoster(group_race_team);
        } else {
            standard.addChild(group_race_team);
        }

        // buttons
        Group group_buttons = new Group();

        button_ok = new OKButton(BUTTON_WIDTH);
        button_ok.addMouseClickListener(new OKListener());
        HorizButton button_cancel = new CancelButton(BUTTON_WIDTH);
        button_cancel.addMouseClickListener(new CancelButtonListener());
        HorizButton button_mapcode = new HorizButton(i18n("enter_map_code"), 170);
        button_mapcode.addMouseClickListener(new MapcodeListener());
        button_advanced = new HorizButton(i18n("advanced"), 130);
        button_advanced.addMouseClickListener((_, _, _, _) -> gui_root.addModalForm(new AdvancedSettingsForm(
                advanced_settings, Globals.SHIPS_ENABLED,
                Globals.SHIPS_ENABLED && ARCHIPELAGO[pulldown_size.getChosenItemIndex()],
                this::applyAdvancedSettings)));

        addChild(button_advanced);

        group_buttons.addChild(button_mapcode);
        group_buttons.addChild(button_ok);
        group_buttons.addChild(button_cancel);

        button_cancel.place();
        button_ok.place(button_cancel, LEFT_MID);
        button_mapcode.place(button_ok, LEFT_MID);

        group_buttons.compileCanvas();
        addChild(group_buttons);

        // map options
        if (multiplayer) {
            group_gamespeed.place();
            group_size.place(group_gamespeed, BOTTOM_RIGHT);
        } else {
            group_size.place();
        }
        group_terrain_type.place(group_size, BOTTOM_RIGHT);
        group_map_options.compileCanvas();
        standard.addChild(group_map_options);

        // standard
        if (multiplayer) {
            label_name.place();
            if (Renderer.isRegistered())
                editline_name.place(label_name, RIGHT_MID);
            else
                label_default_name.place(label_name, RIGHT_MID);
            cb_rated.place(label_name, BOTTOM_LEFT, Skin.getSkin().getFormData().sectionSpacing());
            group_map_options.place(cb_rated, BOTTOM_LEFT);
        } else {
            group_map_options.place();
            group_race_team.place(group_map_options, BOTTOM_LEFT, Skin.getSkin().getFormData().sectionSpacing());
        }
        standard.compileCanvas();

        // advanced
        group_sliders.place();
        group_num_players.place(group_sliders, BOTTOM_LEFT, Skin.getSkin().getFormData().sectionSpacing());
        group_seed.place(group_num_players, BOTTOM_LEFT, Skin.getSkin().getFormData().sectionSpacing());
        advanced.compileCanvas();

        PanelGroup panel_group = multiplayer ? new PanelGroup(1, mode_and_presets, standard, advanced,
                roster_panel) : new PanelGroup(standard, advanced);
        addChild(panel_group);
        var playersChangedListener = new PulldownUpdatePlayersChangedListener(standard);
        playersChangedListener.setCurrentGroup(group_race_team);
        pulldown_menu_slots.addItemChosenListener(playersChangedListener);

        // Place objects
        label_headline.place();
        panel_group.place(label_headline, BOTTOM_LEFT);
        button_advanced.place(panel_group, TOP_RIGHT);

        // buttons
        group_buttons.place(Origin.AT_END);

        compileCanvas();
        randomize();

        // set standard game
        pulldown_size.addItemChosenListener(new PulldownUpdateSizeListener());
        pm_terrain_type.addItemChosenListener(new PulldownUpdateTerrainListener());
        for (int i = 0; i < player_count; i++) {
            difficulty_pulldown_menus[i].addItemChosenListener(new PulldownUpdateHardListener());
            team_pulldown_menus[i].chooseItem(defaultTeam(i));
            if (!multiplayer && i == 1) {
                difficulty_pulldown_menus[i].chooseItem(PlayerSlot.AI_EASY);
                race_pulldown_menus[i].chooseItem((race_pulldown_menus[0].getChosenItemIndex() + 1) % 2);
            } else {
                // MP non-host slots default to Open (index 0); SP non-host slots default to Closed (index 0).
                difficulty_pulldown_menus[i].chooseItem(0);
            }
        }
        pulldown_size.chooseItem(1);
        if (!Renderer.isRegistered())
            pm_terrain_type.chooseItem(0);

        cb_rated.addCheckBoxListener(marked -> {
            // Rated games play with the default limits so ratings stay comparable.
            button_advanced.setDisabled(marked);
            markModified();
        });
        initialized = true;
    }

    private void applyAdvancedSettings(AdvancedSettingsForm.@NonNull Values values) {
        if (!values.equals(advanced_settings)) {
            advanced_settings = values;
            markModified();
        }
    }

    private void markModified() {
        if (!initialized || apply_in_progress) {
            return;
        }
        if (!modified) {
            modified = true;
            updateBanner();
        }
    }

    private void updateBanner() {
        if (mode_and_presets != null) {
            mode_and_presets.setPresetState(current_preset, modified);
        }
    }

    private void setMapcode() {
        BigInteger max_val = BigInteger.ONE;
        BigInteger result = BigInteger.ZERO;
        result = result.add((new BigInteger("" + seed)).multiply(max_val));
        max_val = max_val.multiply(new BigInteger(SEED_CARDINALITY));
        int hills = slider_hills.getValue();
        result = result.add((new BigInteger(new byte[]{(byte) hills})).multiply(max_val));
        max_val = max_val.multiply(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        int vegetation_amount = slider_vegetation.getValue();
        result = result.add((new BigInteger(new byte[]{(byte) vegetation_amount})).multiply(max_val));
        max_val = max_val.multiply(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        int supplies_amount = slider_supplies.getValue();
        result = result.add((new BigInteger(new byte[]{(byte) supplies_amount})).multiply(max_val));
        max_val = max_val.multiply(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        int terrain_type = pm_terrain_type.getChosenItemIndex();
        result = result.add((new BigInteger(new byte[]{(byte) terrain_type})).multiply(max_val));
        max_val = max_val.multiply(new BigInteger(new byte[]{TERRAIN_TYPE_CARDINALITY}));
        int size = pulldown_size.getChosenItemIndex();
        result = result.add((new BigInteger(new byte[]{(byte) size})).multiply(max_val));

        String code = WordsEncoding.encode(result);
        label_mapcode.clear();
        label_mapcode.append(code);
        markModified();
    }

    private void setMapcodeLegacy() {
        BigInteger max_val = BigInteger.ONE;
        BigInteger result = BigInteger.ZERO;
        result = result.add((new BigInteger("" + seed)).multiply(max_val));
        max_val = max_val.multiply(new BigInteger(SEED_CARDINALITY));
        int hills = slider_hills.getValue();
        result = result.add((new BigInteger(new byte[]{(byte) hills})).multiply(max_val));
        max_val = max_val.multiply(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        int vegetation_amount = slider_vegetation.getValue();
        result = result.add((new BigInteger(new byte[]{(byte) vegetation_amount})).multiply(max_val));
        max_val = max_val.multiply(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        int supplies_amount = slider_supplies.getValue();
        result = result.add((new BigInteger(new byte[]{(byte) supplies_amount})).multiply(max_val));
        max_val = max_val.multiply(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        int terrain_type = pm_terrain_type.getChosenItemIndex();
        result = result.add((new BigInteger(new byte[]{(byte) terrain_type})).multiply(max_val));
        max_val = max_val.multiply(new BigInteger(new byte[]{TERRAIN_TYPE_CARDINALITY_LEGACY}));
        int size = pulldown_size.getChosenItemIndex();
        result = result.add((new BigInteger(new byte[]{(byte) size})).multiply(max_val));
        max_val = max_val.multiply(new BigInteger(new byte[]{SIZE_CARDINALITY_LEGACY}));
        int player_race = race_pulldown_menus[0].getChosenItemIndex();
        result = result.add((new BigInteger(new byte[]{(byte) player_race})).multiply(max_val));
        max_val = max_val.multiply(new BigInteger(new byte[]{RACE_CARDINALITY}));
        int player_team = team_pulldown_menus[0].getChosenItemIndex();
        result = result.add((new BigInteger(new byte[]{(byte) player_team})).multiply(max_val));
        max_val = max_val.multiply(new BigInteger(new byte[]{TEAM_CARDINALITY}));
        for (int i = 1; i < DEFAULT_PLAYER_COUNT; i++) {
            int difficulty = difficulty_pulldown_menus[i].getChosenItemIndex();
            result = result.add((new BigInteger(new byte[]{(byte) difficulty})).multiply(max_val));
            max_val = max_val.multiply(new BigInteger(new byte[]{DIFFICULTY_CARDINALITY}));
            int race = race_pulldown_menus[i].getChosenItemIndex();
            result = result.add((new BigInteger(new byte[]{(byte) race})).multiply(max_val));
            max_val = max_val.multiply(new BigInteger(new byte[]{RACE_CARDINALITY}));
            int team = team_pulldown_menus[i].getChosenItemIndex();
            result = result.add((new BigInteger(new byte[]{(byte) team})).multiply(max_val));
            max_val = max_val.multiply(new BigInteger(new byte[]{TEAM_CARDINALITY}));
        }

        String code = RegistrationKey.createString(result);
        label_mapcode.clear();
        label_mapcode.append(code);
        markModified();
    }

    public void parseMapcode(@NonNull String text) {
        if (text.indexOf(' ') == -1) {
            // Legacy letter-based notation
            String code = text.toUpperCase();
            BigInteger result = RegistrationKey.parseBits(code);
            parseBigIntegerLegacy(result);
            label_mapcode.clear();
            label_mapcode.append(code);
        } else {
            // New word-based notation
            try {
                BigInteger result = WordsEncoding.decode(text);
                parseBigInteger(result);
                label_mapcode.clear();
                label_mapcode.append(text);
            } catch (IllegalArgumentException _) {
                // Invalid word code — ignore
            }
        }
    }

    private void parseBigInteger(BigInteger result) {
        BigInteger max_val = MAX_VALUE;
        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(new byte[]{SIZE_CARDINALITY}));
        int size = result.divide(max_val).intValue();
        if (pulldown_size.getSize() > size) {
            pulldown_size.chooseItem(size);
        }
        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(new byte[]{TERRAIN_TYPE_CARDINALITY}));
        int terrain_type = result.divide(max_val).intValue();
        if (pm_terrain_type.getSize() > terrain_type) {
            pm_terrain_type.chooseItem(terrain_type);
        }
        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        int supplies_amount = result.divide(max_val).intValue();
        slider_supplies.setValue(supplies_amount);
        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        int vegetation_amount = result.divide(max_val).intValue();
        slider_vegetation.setValue(vegetation_amount);
        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        int hills = result.divide(max_val).intValue();
        slider_hills.setValue(hills);
        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(SEED_CARDINALITY));
        seed = result.divide(max_val).intValue();
    }

    private void parseBigIntegerLegacy(BigInteger result) {
        BigInteger max_val = MAX_VALUE_LEGACY;
        for (int i = DEFAULT_PLAYER_COUNT - 1; i >= 1; i--) {
            result = result.mod(max_val);
            max_val = max_val.divide(new BigInteger(new byte[]{TEAM_CARDINALITY}));
            int team = result.divide(max_val).intValue();
            team_pulldown_menus[i].chooseItem(team);
            result = result.mod(max_val);
            max_val = max_val.divide(new BigInteger(new byte[]{RACE_CARDINALITY}));
            int race = result.divide(max_val).intValue();
            race_pulldown_menus[i].chooseItem(race);
            result = result.mod(max_val);
            max_val = max_val.divide(new BigInteger(new byte[]{DIFFICULTY_CARDINALITY}));
            int difficulty = result.divide(max_val).intValue();
            difficulty_pulldown_menus[i].chooseItem(difficulty);
            if (difficulty == 0) {
                labels_players[i].setDisabled(true);
                race_pulldown_buttons[i].setDisabled(true);
                team_pulldown_buttons[i].setDisabled(true);
            } else {
                labels_players[i].setDisabled(false);
                race_pulldown_buttons[i].setDisabled(false);
                team_pulldown_buttons[i].setDisabled(false);
            }
        }
        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(new byte[]{TEAM_CARDINALITY}));
        int player_team = result.divide(max_val).intValue();
        team_pulldown_menus[0].chooseItem(player_team);
        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(new byte[]{RACE_CARDINALITY}));
        int player_race = result.divide(max_val).intValue();
        race_pulldown_menus[0].chooseItem(player_race);
        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(new byte[]{SIZE_CARDINALITY_LEGACY}));
        int size = result.divide(max_val).intValue();
        pulldown_size.chooseItem(size);
        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(new byte[]{TERRAIN_TYPE_CARDINALITY_LEGACY}));
        int terrain_type = result.divide(max_val).intValue();
        pm_terrain_type.chooseItem(terrain_type);
        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        int supplies_amount = result.divide(max_val).intValue();
        slider_supplies.setValue(supplies_amount);
        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        int vegetation_amount = result.divide(max_val).intValue();
        slider_vegetation.setValue(vegetation_amount);

        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(new byte[]{SLIDER_CARDINALITY}));
        int hills = result.divide(max_val).intValue();
        slider_hills.setValue(hills);

        result = result.mod(max_val);
        max_val = max_val.divide(new BigInteger(SEED_CARDINALITY));
        seed = result.divide(max_val).intValue();
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public @NonNull GUIObject getButtonOK() {
        return button_ok;
    }

    private final class CancelButtonListener implements MouseClickListener {
        @Override
        public void mouseClicked(@NonNull MouseButton button, int x, int y, int clicks) {
            owner.terrainMenuCancel();
        }
    }

    @SuppressWarnings("unchecked")
    private ScrollableGroup buildPlayerSlots(int count) {
        ScrollableGroup inner = new ScrollableGroup(200, 64);
        Random random = new Random(
                LocalEventQueue.getQueue().getHighPrecisionManager().getTick() * (long) LocalEventQueue.getQueue().getHighPrecisionManager().getTick());
        random.nextFloat();
        for (int i = 0; i < count; i++) {
            difficulty_pulldown_menus[i] = new PulldownMenu<>();
            race_pulldown_menus[i] = new PulldownMenu<>();
            team_pulldown_menus[i] = new ScrollablePulldownMenu<>(DEFAULT_PLAYER_COUNT);

            if (i == 0) {
                difficulty_pulldown_menus[i].addItem(new PulldownItem<>(i18n("human")));
            } else {
                // MP slots can wait for a human joiner; SP has no joiners so it omits Open. Adding Open shifts the MP
                // slot indices (Open 0, Closed 1, AI 2-4). See fillToDifficultyIndex / difficultyIndexToFill.
                if (multiplayer) {
                    difficulty_pulldown_menus[i].addItem(new PulldownItem<>(i18n("open")));
                }
                difficulty_pulldown_menus[i].addItem(new PulldownItem<>(i18n("closed")));
                difficulty_pulldown_menus[i].addItem(new PulldownItem<>(i18n("easy_ai")));
                difficulty_pulldown_menus[i].addItem(new PulldownItem<>(i18n("normal_ai")));
                difficulty_pulldown_menus[i].addItem(new PulldownItem<>(i18n("hard_ai")));
            }

            difficulty_pulldown_buttons[i] = new PulldownButton<>(gui_root, difficulty_pulldown_menus[i], 0, 115);
            inner.addChild(difficulty_pulldown_buttons[i]);

            for (int j = 0; j < RacesResources.getNumRaces(); j++) {
                race_pulldown_menus[i].addItem(new PulldownItem<>(RacesResources.getRaceName(j)));
            }

            race_pulldown_buttons[i] = new PulldownButton<>(gui_root, race_pulldown_menus[i], 0, 115);
            inner.addChild(race_pulldown_buttons[i]);
            for (int j = 0; j < count; j++) {
                team_pulldown_menus[i].addItem(new PulldownItem<>(i18n("team", Integer.toString(j + 1))));
            }
            team_pulldown_buttons[i] = new PulldownButton<>(gui_root, team_pulldown_menus[i], Math.min(i, count - 1),
                    115);
            inner.addChild(team_pulldown_buttons[i]);
            if (i == 0) {
                labels_players[0] = new Label(i18n("player", Integer.toString(1)),
                        Skin.getSkin().getEditFont()).setColor(Settings.getSettings().team_colours[0]);
                inner.addChild(labels_players[0]);
                labels_players[0].place();
                difficulty_pulldown_buttons[0].place(labels_players[0], RIGHT_MID);
                race_pulldown_buttons[0].place(difficulty_pulldown_buttons[0], RIGHT_MID);
                team_pulldown_buttons[0].place(race_pulldown_buttons[0], RIGHT_MID);
            } else {
                labels_players[i] = new Label(i18n("player", Integer.toString(i + 1)),
                        Skin.getSkin().getEditFont()).setColor(Settings.getSettings().team_colours[i]);
                inner.addChild(labels_players[i]);
                labels_players[i].place(labels_players[i - 1], BOTTOM_RIGHT);
                difficulty_pulldown_buttons[i].place(labels_players[i], RIGHT_MID);
                race_pulldown_buttons[i].place(difficulty_pulldown_buttons[i], RIGHT_MID);
                team_pulldown_buttons[i].place(race_pulldown_buttons[i], RIGHT_MID);
                difficulty_pulldown_menus[i].addItemChosenListener(new DisableListener(i));
            }
            difficulty_pulldown_menus[i].addItemChosenListener(new PulldownUpdateMapcodeListener());
            race_pulldown_menus[i].addItemChosenListener(new PulldownUpdateMapcodeListener());
            team_pulldown_menus[i].addItemChosenListener(new PulldownUpdateMapcodeListener());
        }
        inner.compileCanvas();
        return inner;
    }

    private void randomize() {
        Random random = new Random(
                LocalEventQueue.getQueue().getHighPrecisionManager().getTick() * (long) LocalEventQueue.getQueue().getHighPrecisionManager().getTick());
        random.nextInt();
        BigInteger rand_int = new BigInteger(100, random);
        parseBigIntegerLegacy(rand_int);
        setMapcode();
    }

    void doCancel() {
        if (multiplayer)
            new SelectGameMenu(network, gui_root, main_menu);
    }

    /**
     * Default team for slot {@code i}. MP puts each slot on its own team (Team 1, 2, 3, ...), matching the server's
     * slot-based assignment. Single-player keeps the host on team 1 and the AIs on team 2 (you vs the AIs).
     */
    private int defaultTeam(int i) {
        if (multiplayer) {
            return i;
        }
        return i == 0 ? 0 : 1;
    }

    private boolean isChosen(@NonNull PulldownMenu<Void> menu) {
        return menu.getChosenItemIndex() != 0;
    }

    public boolean startGame() {
        int hills = slider_hills.getValue();
        int vegetation_amount = slider_vegetation.getValue();
        int supplies_amount = slider_supplies.getValue();
        Landscape.TerrainType terrain_type = Landscape.TerrainType.values()[pm_terrain_type.getChosenItemIndex()];
        Game game;
        boolean rated = cb_rated.isMarked();
        if (rated)
            team_pulldown_menus[0].chooseItem(team_pulldown_menus[0].getChosenItemIndex() % 2);
        AdvancedSettingsForm.Values settings = rated ? AdvancedSettingsForm.Values.defaults() : advanced_settings;
        int size = pulldown_size.getChosenItemIndex();
        // Archipelago islands are only reachable by ship, so it always plays with boats.
        boolean ships = Globals.SHIPS_ENABLED && (settings.ships() || ARCHIPELAGO[size]);
        if (multiplayer) {
            String game_name = editline_name.getContents();
            if (game_name.length() < Game.MIN_LENGTH) {
                String min_name = i18n("min_name_length", Game.MIN_LENGTH);
                gui_root.addModalForm(new MessageForm(min_name));
                return false;
            }
            float random_start_pos = LocalEventQueue.getQueue().getTime() % 1f;
            // spotless:off
            game = Game.builder()
                    .name(game_name)
                    .size((byte) size)
                    .terrain((byte) terrain_type.ordinal())
                    .hills((byte) hills)
                    .trees((byte) vegetation_amount)
                    .supplies((byte) supplies_amount)
                    .rated(rated)
                    .gamespeed((byte) (pm_gamespeed.getChosenItemIndex() + 1))
                    .mapcode(label_mapcode.getContents())
                    .randomStartPos(random_start_pos)
                    .maxUnitCount(settings.maxUnits())
                    .initialUnitCount(settings.startingUnits())
                    .maxBuildingCount(settings.maxBuildings())
                    .ships(ships)
                    .build();
            // spotless:on
        } else {
            boolean has_enemy = false;
            for (int i = 1; i < player_count; i++) {
                if (isChosen(difficulty_pulldown_menus[i])
                        && team_pulldown_menus[i].getChosenItemIndex() != team_pulldown_menus[0].getChosenItemIndex()) {
                    has_enemy = true;
                    break;
                }
            }
            if (!has_enemy) {
                String min_name = i18n("min_num_teams", 2);
                gui_root.addModalForm(new MessageForm(min_name));
                return false;
            }
            game = null;
        }
        if (owner != null)
            owner.terrainMenuOK();
        SelectGameMenu menu = null;
        if (multiplayer)
            menu = (SelectGameMenu) owner;
        int gametype;
        IO.println(
                "hills = " + hills / (float) SLIDER_MAX_VALUE + " | vegetation_amount = " + vegetation_amount / (float) SLIDER_MAX_VALUE + " | supplies_amount = " + supplies_amount / (float) SLIDER_MAX_VALUE + " | seed = " + seed * seed);
        String ai_string = i18n("ai");
        String[] ai_names = new String[MatchmakingServerInterface.MAX_PLAYERS];
        for (int i = 0; i < ai_names.length; i++) {
            ai_names[i] = ai_string + i;
        }
        InGameInfo ingame_info = multiplayer ? new MultiplayerInGameInfo(game.getRandomStartPos(),
                game.isRated()) : new DefaultInGameInfo(snapshotRoster());
        // spotless:off
        WorldParameters world_params = WorldParameters.builder()
                .initialGameSpeed(multiplayer ? game.getGamespeed() : Globals.gamespeed)
                .mapcode(label_mapcode.getContents())
                .initialUnitCount(settings.startingUnits())
                .maxUnitCount(settings.maxUnits())
                .mapSize(size)
                .maxBuildingCount(settings.maxBuildings())
                .ships(ships)
                .build();
        // spotless:on
        GameNetwork game_network = Menu.startNewGame(network, gui_root,
                menu,
                world_params,
                ingame_info,
                new Menu.DefaultWorldInitAction(),
                game,
                SIZES[size],
                terrain_type,
                hills / (float) SLIDER_MAX_VALUE,
                vegetation_amount / (float) SLIDER_MAX_VALUE,
                supplies_amount / (float) SLIDER_MAX_VALUE,
                seed * seed,
                ARCHIPELAGO[size] && Globals.SHIPS_ENABLED,
                ai_names,
                player_count);
        game_network.getClient().getServerInterface().setPlayerSlot(0, PlayerSlot.HUMAN,
                race_pulldown_menus[0].getChosenItemIndex(), team_pulldown_menus[0].getChosenItemIndex(), !multiplayer,
                PlayerSlot.AI_NONE);
        if (multiplayer) {
            // Carry the host's roster into the lobby; GameMenu applies it once on open (host only).
            game_network.setInitialRoster(snapshotRoster());
        }
        if (!multiplayer) {
            for (int i = 1; i < player_count; i++) {
                if (isChosen(difficulty_pulldown_menus[i]))
                    game_network.getClient().getServerInterface().setPlayerSlot(i, PlayerSlot.AI,
                            race_pulldown_menus[i].getChosenItemIndex(), team_pulldown_menus[i].getChosenItemIndex(),
                            true, difficulty_pulldown_menus[i].getChosenItemIndex());
            }
            game_network.getClient().getServerInterface().startServer();
            IO.println("Start server");
        }
        IO.println("Map code: " + label_mapcode.getContents());
        return true;
    }

    private final class MapcodeListener implements MouseClickListener {
        @Override
        public void mouseClicked(@NonNull MouseButton button, int x, int y, int clicks) {
            gui_root.addModalForm(new MapcodeForm(TerrainMenu.this));
        }
    }

    private final class OKListener implements MouseClickListener {
        @Override
        public void mouseClicked(@NonNull MouseButton button, int x, int y, int clicks) {
            boolean started = startGame();
            if (started)
                button_ok.setDisabled(true);
        }
    }

    private final class DisableListener implements ItemChosenListener<Void> {
        final int i;

        public DisableListener(int i) {
            this.i = i;
        }

        @Override
        public void itemChosen(@NonNull PulldownMenu<Void> menu, int item_index) {
            // Closed slots grey out race/team. Open (MP index 0) keeps them enabled; they define what a joiner
            // inherits. Closed is index 0 in SP, index 1 in MP (Open occupies 0).
            int closed_index = multiplayer ? 1 : 0;
            boolean closed = item_index == closed_index;
            labels_players[i].setDisabled(closed);
            race_pulldown_buttons[i].setDisabled(closed);
            team_pulldown_buttons[i].setDisabled(closed);
        }
    }

    private final class PulldownUpdateMapcodeListener implements ItemChosenListener<Void> {
        @Override
        public void itemChosen(@NonNull PulldownMenu<Void> menu, int item_index) {
            setMapcode();
        }
    }

    private static final class PulldownUpdateSizeListener implements ItemChosenListener<Void> {
        @Override
        public void itemChosen(@NonNull PulldownMenu<Void> menu, int item_index) {
        }
    }

    private static final class PulldownUpdateHardListener implements ItemChosenListener<Void> {
        @Override
        public void itemChosen(@NonNull PulldownMenu<Void> menu, int item_index) {
        }
    }

    private static final class PulldownUpdateTerrainListener implements ItemChosenListener<Void> {
        @Override
        public void itemChosen(@NonNull PulldownMenu<Void> menu, int item_index) {
        }
    }

    private final class SliderUpdateMapcodeListener implements ValueListener {
        @Override
        public void valueSet(long value) {
            setMapcode();
        }
    }

    private final class PresetsHandler implements ModeAndPresetsHandler {
        @Override
        public void modeChosen(@NonNull GameMode mode) {
        }

        @Override
        public void presetChosen(@NonNull Preset preset) {
            if (modified) {
                // The card already marked itself on click. If the host declines, rebuild the grid so the highlight
                // reverts to the currently-applied preset instead of the un-applied one.
                gui_root.addModalForm(new QuestionForm(i18n("confirm_discard_changes"),
                        (_, _, _, _) -> applySelectedPreset(preset), this::revertSelection));
            } else {
                applySelectedPreset(preset);
            }
        }

        private void applySelectedPreset(@NonNull Preset preset) {
            current_preset = preset;
            apply_in_progress = true;
            applyPreset(preset);
            apply_in_progress = false;
            modified = false;
            updateBanner();
        }

        private void revertSelection() {
            if (mode_and_presets != null) {
                mode_and_presets.refreshPresets();
            }
        }

        @Override
        public void presetDeleted(@NonNull Preset preset) {
            gui_root.addModalForm(new QuestionForm(i18n("confirm_delete_preset", preset.getName()),
                    (_, _, _, _) -> confirmDeletePreset(preset)));
        }

        private void confirmDeletePreset(@NonNull Preset preset) {
            preset_library.remove(preset);
            preset_library.save(Renderer.getLocalInput().getGameDir().resolve(Globals.getPresetsFileName()));
            if (current_preset != null && current_preset.getId().equals(preset.getId())) {
                current_preset = null;
                modified = false;
                updateBanner();
            }
            if (mode_and_presets != null) {
                mode_and_presets.refreshPresets();
            }
        }

        @Override
        public void saveClicked() {
            gui_root.addModalForm(new SavePresetDialog(preset_library::hasName, this::saveNewPreset));
        }

        @Override
        public void resetClicked() {
            if (current_preset == null) {
                return;
            }
            apply_in_progress = true;
            applyPreset(current_preset);
            apply_in_progress = false;
            modified = false;
            updateBanner();
        }

        @Override
        public void updateClicked() {
            if (current_preset == null) {
                return;
            }
            Preset updated = new Preset(current_preset.getId(), current_preset.getName(), snapshotWorldConfig(),
                    snapshotModeOptions(), snapshotRoster(), false);
            preset_library.remove(current_preset);
            preset_library.add(updated);
            preset_library.save(Renderer.getLocalInput().getGameDir().resolve(Globals.getPresetsFileName()));
            current_preset = updated;
            modified = false;
            updateBanner();
            if (mode_and_presets != null) {
                mode_and_presets.refreshPresets();
            }
        }

        private void saveNewPreset(@NonNull String name) {
            Preset preset = new Preset(UUID.randomUUID().toString(), name, snapshotWorldConfig(),
                    snapshotModeOptions(), snapshotRoster(), false);
            preset_library.add(preset);
            preset_library.save(Renderer.getLocalInput().getGameDir().resolve(Globals.getPresetsFileName()));
            // Auto-select the new preset: the saved state matches the form, so it is selected and not modified.
            current_preset = preset;
            modified = false;
            updateBanner();
            if (mode_and_presets != null) {
                mode_and_presets.refreshPresets();
            }
        }
    }

    private @NonNull StandardOptions snapshotModeOptions() {
        return StandardOptions.builder().rated(cb_rated.isMarked()).build();
    }

    private @NonNull WorldConfig snapshotWorldConfig() {
        // spotless:off
        return WorldConfig.builder()
                .gamespeed(pm_gamespeed.getChosenItemIndex())
                .islandSize(pulldown_size.getChosenItemIndex())
                .terrainType(pm_terrain_type.getChosenItemIndex())
                .hills(slider_hills.getValue())
                .vegetation(slider_vegetation.getValue())
                .supplies(slider_supplies.getValue())
                .maxUnits(advanced_settings.maxUnits())
                .startingUnits(advanced_settings.startingUnits())
                .maxBuildings(advanced_settings.maxBuildings())
                .ships(advanced_settings.ships())
                .build();
        // spotless:on
    }

    private void applyWorldConfig(@NonNull WorldConfig world) {
        // Stored values are pulldown indices. Within a build the item counts are fixed, but a preset file from a build
        // with different terrain/size lists could hold a stale index; clamp so it applies the nearest valid option
        // instead of silently leaving the prior value.
        pm_gamespeed.chooseItem(clampIndex(world.getGamespeed(), pm_gamespeed.getSize()));
        pulldown_size.chooseItem(clampIndex(world.getIslandSize(), pulldown_size.getSize()));
        pm_terrain_type.chooseItem(clampIndex(world.getTerrainType(), pm_terrain_type.getSize()));
        slider_hills.setValue(world.getHills());
        slider_vegetation.setValue(world.getVegetation());
        slider_supplies.setValue(world.getSupplies());
        advanced_settings = new AdvancedSettingsForm.Values(world.getMaxUnits(), world.getStartingUnits(),
                world.getMaxBuildings(), world.isShips());
        setMapcode();
    }

    private static int clampIndex(int index, int size) {
        if (index < 0) {
            return 0;
        }
        return Math.min(index, size - 1);
    }

    private @NonNull RosterTemplate snapshotRoster() {
        RosterTemplate.Slot[] slots = new RosterTemplate.Slot[player_count];
        for (int i = 0; i < player_count; i++) {
            int race = race_pulldown_menus[i].getChosenItemIndex();
            int team = team_pulldown_menus[i].getChosenItemIndex();
            slots[i] = new RosterTemplate.Slot(difficultyIndexToFill(i,
                    difficulty_pulldown_menus[i].getChosenItemIndex()),
                    race, team);
        }
        return new RosterTemplate(slots);
    }

    // MP slot menu order: Open 0, Closed 1, Easy 2, Normal 3, Hard 4. SP omits Open, so its indices are one lower.
    private RosterTemplate.@NonNull Fill difficultyIndexToFill(int slot_index, int difficulty_index) {
        if (slot_index == 0) {
            return RosterTemplate.Fill.HOST;
        }
        return switch (multiplayer ? difficulty_index : difficulty_index + 1) {
            case 1 -> RosterTemplate.Fill.CLOSED;
            case 2 -> RosterTemplate.Fill.EASY_AI;
            case 3 -> RosterTemplate.Fill.NORMAL_AI;
            case 4 -> RosterTemplate.Fill.HARD_AI;
            default -> RosterTemplate.Fill.OPEN;
        };
    }

    private void applyPreset(@NonNull Preset preset) {
        applyWorldConfig(preset.getWorld());
        applyRoster(preset.getRoster());
        if (preset.getModeOptions() instanceof StandardOptions opts) {
            cb_rated.setMarked(opts.isRated());
        }
    }

    public void applyRoster(@NonNull RosterTemplate roster) {
        RosterTemplate.Slot[] slots = roster.getSlots();
        int target_count = Math.min(slots.length, MatchmakingServerInterface.MAX_PLAYERS);
        if (target_count >= DEFAULT_PLAYER_COUNT && target_count != player_count) {
            pulldown_menu_slots.chooseItem(target_count - DEFAULT_PLAYER_COUNT);
        }

        for (int i = 0; i < Math.min(slots.length, player_count); i++) {
            RosterTemplate.Slot slot = slots[i];
            difficulty_pulldown_menus[i].chooseItem(fillToDifficultyIndex(slot.getFill(), i));
            if (slot.getRace() != null && slot.getRace() < race_pulldown_menus[i].getSize()) {
                race_pulldown_menus[i].chooseItem(slot.getRace());
            }
            if (slot.getTeam() != null && slot.getTeam() < team_pulldown_menus[i].getSize()) {
                team_pulldown_menus[i].chooseItem(slot.getTeam());
            }
        }
    }

    private int fillToDifficultyIndex(RosterTemplate.@NonNull Fill fill, int slot_index) {
        if (slot_index == 0) {
            return 0;
        }
        int index = switch (fill) {
            case HOST, OPEN -> 0;
            case CLOSED -> 1;
            case EASY_AI -> 2;
            case NORMAL_AI -> 3;
            case HARD_AI -> 4;
        };
        return multiplayer ? index : Math.max(0, index - 1);
    }

    private final class PulldownUpdatePlayersChangedListener implements ItemChosenListener<Void> {
        private final Panel standard;
        private ScrollableGroup current_race_team;

        PulldownUpdatePlayersChangedListener(Panel standard) {
            this.standard = standard;
        }

        void setCurrentGroup(ScrollableGroup group) {
            this.current_race_team = group;
        }

        @Override
        public void itemChosen(@NonNull PulldownMenu<Void> menu, int item_index) {
            int previous_count = player_count;
            player_count = item_index + DEFAULT_PLAYER_COUNT;

            // Rebuilding the grid throws away the old menus, so snapshot the host's current choices first and
            // carry them over to the matching slots. Only genuinely new slots fall back to defaults; changing
            // the count no longer wipes already-configured slots.
            int[] prev_difficulty = new int[previous_count];
            int[] prev_race = new int[previous_count];
            int[] prev_team = new int[previous_count];
            for (int i = 0; i < previous_count; i++) {
                prev_difficulty[i] = difficulty_pulldown_menus[i].getChosenItemIndex();
                prev_race[i] = race_pulldown_menus[i].getChosenItemIndex();
                prev_team[i] = team_pulldown_menus[i].getChosenItemIndex();
            }

            ScrollableGroup new_group = buildPlayerSlots(player_count);
            if (multiplayer) {
                roster_panel.setRoster(new_group);
            } else {
                if (current_race_team != null) {
                    standard.removeChild(current_race_team);
                }
                new_group.place();
                standard.addChild(new_group);
            }
            current_race_team = new_group;

            for (int i = 0; i < player_count; i++) {
                if (i < previous_count) {
                    difficulty_pulldown_menus[i].chooseItem(prev_difficulty[i]);
                    race_pulldown_menus[i].chooseItem(prev_race[i]);
                    // Team options run 1..player_count, so clamp the carried-over choice when the count shrinks.
                    team_pulldown_menus[i].chooseItem(Math.min(prev_team[i], player_count - 1));
                } else {
                    team_pulldown_menus[i].chooseItem(defaultTeam(i));
                    if (!multiplayer && i == 1) {
                        difficulty_pulldown_menus[i].chooseItem(PlayerSlot.AI_EASY);
                        race_pulldown_menus[i].chooseItem((race_pulldown_menus[0].getChosenItemIndex() + 1) % 2);
                    } else if (i != 0) {
                        // MP non-host slots default to Open (index 0); SP non-host slots default to Closed (index 0).
                        difficulty_pulldown_menus[i].chooseItem(0);
                    }
                }
            }
            markModified();
        }
    }
}
