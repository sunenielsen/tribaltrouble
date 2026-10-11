package com.oddlabs.tt.delegate;

import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.camera.MapCamera;
import com.oddlabs.tt.form.InGameChatForm;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.gui.ActionButtonPanel;
import com.oddlabs.tt.gui.CursorType;
import com.oddlabs.tt.font.Font;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.MouseButton;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.SpectatorCursor;
import com.oddlabs.tt.gui.SpectatorPanel;
import com.oddlabs.tt.gui.SpectatorSelectionBox;
import com.oddlabs.tt.input.GameAction;
import com.oddlabs.tt.input.InputManager;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.viewer.SpectatorView;
import com.oddlabs.tt.input.InputEvent;
import com.oddlabs.tt.input.InputPhase;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Action;
import com.oddlabs.tt.model.Army;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.LandBuilding;
import com.oddlabs.tt.model.BuildingTemplate;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Ship;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.UnitTemplate;
import com.oddlabs.tt.model.behaviour.IdleController;
import com.oddlabs.tt.render.CompassRenderer;
import com.oddlabs.tt.render.GUIRenderer;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.viewer.Notification;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.util.Color;
import org.joml.Vector4fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.ResourceBundle;

public final class SelectionDelegate extends ControllableCameraDelegate {
    private static final ResourceBundle bundle = ResourceBundle.getBundle(SelectionDelegate.class.getName());
    private static final Vector4fc SELECTION_COLOR = Color.argb4v(0xFF_4C_FF_00);
    private static final GameAction[] ARMY_CREATES = new GameAction[]{GameAction.ARMY_CREATE_0, GameAction.ARMY_CREATE_1, GameAction.ARMY_CREATE_2, GameAction.ARMY_CREATE_3, GameAction.ARMY_CREATE_4, GameAction.ARMY_CREATE_5, GameAction.ARMY_CREATE_6, GameAction.ARMY_CREATE_7, GameAction.ARMY_CREATE_8, GameAction.ARMY_CREATE_9,
    };
    private static final GameAction[] ARMY_SELECTS = new GameAction[]{GameAction.ARMY_SELECT_0, GameAction.ARMY_SELECT_1, GameAction.ARMY_SELECT_2, GameAction.ARMY_SELECT_3, GameAction.ARMY_SELECT_4, GameAction.ARMY_SELECT_5, GameAction.ARMY_SELECT_6, GameAction.ARMY_SELECT_7, GameAction.ARMY_SELECT_8, GameAction.ARMY_SELECT_9
    };
    private final @NonNull InGameChatForm chat_form;
    private static final int SPECTATOR_MARGIN = 10;
    private final List<Label> spectator_labels = new ArrayList<>();
    private final @NonNull GameCamera game_camera;

    private boolean close_chat_override = false;
    private boolean chat_visible;
    private boolean selection = false;
    private int selection_x1;
    private int selection_y1;
    private int selection_x2;
    private int selection_y2;
    private boolean pick_done = false;
    private boolean map_mode = false;
    private boolean observer = false;
    private int last_idle_peon_name = -1;

    public SelectionDelegate(@NonNull WorldViewer viewer, @NonNull GameCamera camera) {
        super(viewer, camera);
        this.game_camera = (GameCamera) getCamera();
        displayChangedNotify(getGUIRoot().getWidth(), getGUIRoot().getHeight());
        addChild(getViewer().getPanel());
        chat_form = new InGameChatForm(getViewer().getGUIRoot().getInfoPrinter(), getViewer());
        chat_form.addCloseListener(() -> {
            if (Renderer.getLocalInput().getInputManager().isActive(GameAction.GLOBAL_CHAT)) {
                close_chat_override = true;
            }
            chat_visible = false;
        });
        chat_visible = false;
        ((GameCamera) getCamera()).setOwner(this);
    }

    public @NonNull InGameChatForm getChatForm() {
        return chat_form;
    }

    private @NonNull ActionButtonPanel getActionButtonPanel() {
        return getViewer().getPanel();
    }

    public void setObserverMode() {
        if (observer)
            return;
        observer = true;
        getViewer().getSelection().clearSelection();
        SpectatorView view = getViewer().getSpectatorView();
        if (view != null) {
            getActionButtonPanel().remove();
            getViewer().getRenderer().setSelectedBuilding(null);
            view.setListener(this::refreshSpectator);
            refreshSpectator();
            BooleanSupplier hud_shown = () -> !map_mode && Globals.draw_hud;
            addChild(new SpectatorCursor(getViewer(), getGUIRoot(), hud_shown));
            addChild(new SpectatorSelectionBox(getViewer(), getGUIRoot(), hud_shown));
            addChild(new SpectatorPanel(getViewer(), getGUIRoot(), hud_shown));
        }
    }

    private static @NonNull String spectatorText(@NonNull String key, @NonNull Object @NonNull... args) {
        return Utils.getBundleString(ResourceBundle.getBundle(SelectionDelegate.class.getName()), key, args);
    }

    // Who is being observed, centered at the top with the name in the player's color; the keys stacked in the top
    // left corner, one per line.
    private void refreshSpectator() {
        SpectatorView view = getViewer().getSpectatorView();
        if (view == null)
            return;
        for (Label label : spectator_labels)
            label.remove();
        spectator_labels.clear();
        if (map_mode || !Globals.draw_hud)
            return;
        Player followed = view.getFollowedPlayer();
        Font headline = Skin.getSkin().getHeadlineFont();
        int width = getGUIRoot().getWidth();
        int top = getGUIRoot().getHeight() - SPECTATOR_MARGIN;
        Label title = new Label(spectatorText(followed == null ? "spectator_free" : "spectator_following"), headline);
        Label name = followed == null ? null : new Label(followed.getPlayerInfo().getName(), headline).setColor(
                followed.getColor());
        int x = (width - title.getWidth() - (name == null ? 0 : name.getWidth())) / 2;
        int y = top - title.getHeight();
        showSpectatorLabel(title, x, y);
        if (name != null)
            showSpectatorLabel(name, x + title.getWidth(), y);
        InputManager input = Renderer.getLocalInput().getInputManager();
        String[] lines = {spectatorText("spectator_next", input.getBindingString(
                GameAction.SPECTATOR_NEXT_PLAYER)), spectatorText("spectator_previous", input.getBindingString(
                        GameAction.SPECTATOR_PREV_PLAYER)), spectatorText("spectator_free_cam", input.getBindingString(
                                GameAction.SPECTATOR_FREE_CAMERA)), spectatorText("spectator_exit")};
        y = top;
        for (String text : lines) {
            Label line = new Label(text, Skin.getSkin().getEditFont());
            y -= line.getHeight();
            showSpectatorLabel(line, SPECTATOR_MARGIN, y);
        }
    }

    private void showSpectatorLabel(@NonNull Label label, int x, int y) {
        label.setPos(x, y);
        addChild(label);
        spectator_labels.add(label);
    }

    @Override
    public void handleInput(@NonNull InputEvent event) {
        // Prevent base GUIObject from handling UI_ACTIVATE (Space/Return as Click)
        // because we handle Space for Map Mode and Return for Chat.
        event.consumeAction(GameAction.UI_ACTIVATE);

        // Intercept Esc for armory submenu navigation before super reaches InGameDelegate
        if ((event.getPhase() == InputPhase.PRESSED || event.getPhase() == InputPhase.REPEAT)
                && !map_mode && !observer
                && (event.hasAction(GameAction.GLOBAL_MENU) || event.hasAction(GameAction.UI_CANCEL))) {
            if (getActionButtonPanel().tryCloseSubmenu(event)) {
                return;
            }
        }

        super.handleInput(event);
        if (event.isConsumed()) return;

        if (event.getPhase() == InputPhase.PRESSED) {
            SpectatorView view = observer ? getViewer().getSpectatorView() : null;
            if (view != null) {
                if (event.consumeAction(GameAction.SPECTATOR_NEXT_PLAYER)) {
                    view.next();
                    event.consume();
                    return;
                }
                if (event.consumeAction(GameAction.SPECTATOR_PREV_PLAYER)) {
                    view.previous();
                    event.consume();
                    return;
                }
                if (event.consumeAction(GameAction.SPECTATOR_FREE_CAMERA)) {
                    view.toggleFreeCamera();
                    event.consume();
                    return;
                }
            }
            if (event.hasActions()) {
                if (event.consumeAction(GameAction.CAMERA_MAP_MODE)) {
                    enterMapMode();
                    event.consume();
                    return;
                }

                if (event.consumeAction(GameAction.NOTIFICATION_JUMP)) {
                    if (!observer) {
                        Notification n = getViewer().getNotificationManager().getLatestNotification();
                        if (n != null) {
                            if (getCamera() instanceof GameCamera)
                                getGUIRoot().pushDelegate(new JumpDelegate(getViewer(), (GameCamera) getCamera(),
                                        n.getX(), n.getY()));
                            else if (getCamera() instanceof MapCamera)
                                ((MapCamera) getCamera()).mapGoto(n.getX(), n.getY(), true);
                        }
                    }
                    event.consume();
                    return;
                }

                // Army Shortcuts
                for (int i = 0; i <= 9; i++) {
                    if (event.consumeAction(ARMY_SELECTS[i])) {
                        if (!map_mode && !observer) {
                            boolean selected = getViewer().getSelection().enableShortcutArmy(i);
                            if (selected && event.getClicks() > 1) {
                                var set = getViewer().getSelection().getCurrentSelection().getSet();
                                if (!set.isEmpty()) {
                                    var s = set.iterator().next();
                                    getGUIRoot().pushDelegate(new JumpDelegate(getViewer(), (GameCamera) getCamera(),
                                            s.getPositionX(), s.getPositionY()));
                                }
                            }
                        }
                        event.consume();
                        return;
                    }
                    if (event.consumeAction(ARMY_CREATES[i])) {
                        if (!map_mode && !observer) {
                            getViewer().getSelection().setShortcutArmy(i);
                        }
                        event.consume();
                        return;
                    }
                }

                if (event.consumeAction(GameAction.GLOBAL_CHAT)) {
                    if (!chat_visible)
                        chat_form.setReceivers(true);
                    event.consume();
                    return;
                }
                if (event.consumeAction(GameAction.GLOBAL_CHAT_TEAM)) {
                    if (!chat_visible)
                        chat_form.setReceivers(false);
                    event.consume();
                    return;
                }
                if (event.consumeAction(GameAction.UNIT_BEACON)) {
                    if (!map_mode && !observer) {
                        getGUIRoot().pushDelegate(new BeaconDelegate(getViewer(), (GameCamera) getCamera()));
                    }
                    event.consume();
                    return;
                }

                if (event.consumeAction(GameAction.UNIT_NEXT_IDLE)) {
                    nextIdlePeon();
                    event.consume();
                    return;
                }

                if (event.consumeAction(GameAction.GAME_SPEED_UP)) {
                    changeGamespeed(1);
                    event.consume();
                    return;
                }

                if (event.consumeAction(GameAction.GAME_SPEED_DOWN)) {
                    changeGamespeed(-1);
                    event.consume();
                    return;
                }

                if (event.consumeAction(GameAction.GLOBAL_TOGGLE_HUD)) {
                    setHUDVisible(!Globals.draw_hud);
                    event.consume();
                    return;
                }

                if (event.consumeAction(GameAction.CAMERA_CINEMATIC)) {
                    Globals.cinematic_camera = !Globals.cinematic_camera;
                    getGUIRoot().getInfoPrinter().print(Utils.getBundleString(bundle,
                            Globals.cinematic_camera ? "cinematic_on" : "cinematic_off"));
                    event.consume();
                    return;
                }

                if (event.consumeAction(GameAction.CAMERA_ORBIT_LEFT)) {
                    if (!map_mode)
                        game_camera.toggleOrbit(1);
                    event.consume();
                    return;
                }

                if (event.consumeAction(GameAction.CAMERA_ORBIT_RIGHT)) {
                    if (!map_mode)
                        game_camera.toggleOrbit(-1);
                    event.consume();
                    return;
                }

                if (event.consumeAction(GameAction.CAMERA_AUTO_PAN_FORWARD)) {
                    if (!map_mode)
                        game_camera.toggleAutoPan(1);
                    event.consume();
                    return;
                }

                if (event.consumeAction(GameAction.CAMERA_AUTO_PAN_BACKWARD)) {
                    if (!map_mode)
                        game_camera.toggleAutoPan(-1);
                    event.consume();
                    return;
                }

                if (event.hasAction(GameAction.CAMERA_FIRST_PERSON) || event.hasAction(GameAction.CAMERA_ZOOM_MODE)) {
                    if (map_mode) {
                        event.consume(); // Consume in map mode
                        return;
                    }
                    // Otherwise bubble (to super)
                }
            }

            if (map_mode || observer) {
                // Bubble
            } else {
                getActionButtonPanel().handleInput(event);
                if (event.isConsumed()) {
                    return;
                }
            }
        } else if (event.getPhase() == InputPhase.REPEAT) {
            if (event.hasActions()) {
                if (event.consumeAction(GameAction.GAME_SPEED_UP)) {
                    changeGamespeed(1);
                    event.consume();
                    return;
                }
                if (event.consumeAction(GameAction.GAME_SPEED_DOWN)) {
                    changeGamespeed(-1);
                    event.consume();
                    return;
                }
            }

            if (!map_mode && !observer) {
                getActionButtonPanel().handleInput(event);
                if (event.isConsumed()) {
                    return;
                }
            }
        } else if (event.getPhase() == InputPhase.RELEASED) {
            if (event.consumeAction(GameAction.GLOBAL_CHAT) || event.consumeAction(GameAction.GLOBAL_CHAT_TEAM)) {
                if (!close_chat_override) {
                    if (!chat_visible) {
                        addChild(chat_form);
                        chat_form.setPos(GameCamera.SCROLL_BUFFER, GameCamera.SCROLL_BUFFER);
                        chat_form.setFocus();
                        chat_visible = true;
                    }
                } else {
                    close_chat_override = false;
                }
                event.consume();
                return;
            }

            if (!map_mode && !observer) {
                getActionButtonPanel().handleInput(event);
                if (event.isConsumed()) {
                    return;
                }
            }
        }
        super.handleInput(event);
    }

    private void changeGamespeed(int delta) {
        getViewer().getPeerHub().getPlayerInterface().changePreferredGamespeed(delta);
    }

    private void nextIdlePeon() {
        var set = getViewer().getLocalPlayer().getUnits().getSet();

        boolean has_idle_peon = false;
        int lowest_name = Integer.MAX_VALUE;
        Selectable<?> lowest_peon = null;

        boolean has_greater_name = false;
        int lowest_greater_name = Integer.MAX_VALUE;
        Selectable<?> lowest_greater_peon = null;
        for (var s : set) {
            if (s.getOwner() != getViewer().getLocalPlayer())
                continue;
            Abilities abilities = s.getAbilities();
            if ((abilities.hasAbilities(Abilities.BUILD)) && (s.getPrimaryController() instanceof IdleController)) {
                int name = getViewer().getDistributableTable().getName(s);
                if (name < lowest_name) {
                    has_idle_peon = true;
                    lowest_name = name;
                    lowest_peon = s;
                }
                if (name > last_idle_peon_name && name < lowest_greater_name) {
                    has_greater_name = true;
                    lowest_greater_name = name;
                    lowest_greater_peon = s;
                }
            }
        }

        Selectable<?> target = null;
        if (has_greater_name) {
            last_idle_peon_name = lowest_greater_name;
            target = lowest_greater_peon;
        } else if (has_idle_peon) {
            last_idle_peon_name = lowest_name;
            target = lowest_peon;
        }

        if (target != null && getCamera() instanceof GameCamera) {
            getViewer().getSelection().clearSelection();
            getViewer().getSelection().getCurrentSelection().add(target);
            getGUIRoot().pushDelegate(new JumpDelegate(getViewer(), (GameCamera) getCamera(), target.getPositionX(),
                    target.getPositionY()));
        }
    }

    @Override
    protected @NonNull CursorType getCursorType() {
        if (!Globals.draw_hud)
            return CursorType.HIDDEN;
        return map_mode ? CursorType.TARGET : CursorType.NORMAL;
    }

    @Override
    public boolean renderCursor() {
        return Globals.draw_hud;
    }

    public void enterMapMode() {
        if (map_mode)
            return;
        selection = false;
        getViewer().getPicker().pickRotate((GameCamera) getCamera());
        map_mode = true;
        if (observer)
            refreshSpectator();
        else
            getActionButtonPanel().remove();
        getCamera().disable();
        setCamera(new MapCamera(this, game_camera));
        getCamera().enable();
    }

    public void exitMapMode() {
        map_mode = false;
        getCamera().disable();
        // Snap GameCamera's current position to its target so there's no
        // interpolation lag when switching back from MapCamera
        game_camera.getState().snapToTarget();
        setCamera(game_camera);
        getCamera().enable();
        if (observer)
            refreshSpectator();
        else
            addChild(getActionButtonPanel());

        if (chat_visible) {
            chat_form.remove();
            addChild(chat_form);
            chat_visible = true;
        }
    }

    private void updateSelection(@NonNull List<@NonNull Selectable<UnitTemplate>> friendly_units,
            @NonNull List<@NonNull Ship> friendly_ships, Selectable<BuildingTemplate> friendly_building,
            Selectable<?> enemy) {
        Army current_selection = getViewer().getSelection().getCurrentSelection();
        Selectable<?> first = current_selection.getSet().iterator().next();
        if (first instanceof Building || first.getOwner() != getViewer().getLocalPlayer()) {
            if (first == friendly_building || first == enemy) {
                current_selection.clear();
            }
            return;
        }
        if (first instanceof Ship) {
            toggleSelection(current_selection, friendly_ships);
            return;
        }
        toggleSelection(current_selection, friendly_units);
    }

    private void toggleSelection(@NonNull Army current_selection,
            @NonNull List<? extends @NonNull Selectable<?>> picked) {
        boolean add = false;
        for (Selectable<?> selectable : picked) {
            if (!current_selection.contains(selectable)) {
                add = true;
                break;
            }
        }
        for (Selectable<?> selectable : picked) {
            if (add) {
                if (!current_selection.contains(selectable))
                    current_selection.add(selectable);
            } else {
                current_selection.remove(selectable);
            }
        }
    }

    private void replaceSelection(@NonNull List<Selectable<UnitTemplate>> friendly_units,
            @NonNull List<Ship> friendly_ships, @Nullable Selectable<BuildingTemplate> friendly_building,
            @Nullable Selectable<?> enemy) {
        Army current_selection = getViewer().getSelection().getCurrentSelection();
        current_selection.clear();
        if (!friendly_units.isEmpty()) {
            for (Selectable<?> friendlyUnit : friendly_units) {
                current_selection.add(friendlyUnit);
            }
        } else if (!friendly_ships.isEmpty()) {
            for (Ship ship : friendly_ships) {
                current_selection.add(ship);
            }
        } else if (friendly_building != null) {
            current_selection.add(friendly_building);
        } else if (enemy != null) {
            current_selection.add(enemy);
        }
    }

    @Override
    public void mouseClicked(@NonNull MouseButton button, int x, int y, int clicks) {
        if (button == MouseButton.LEFT && !map_mode && !observer) {
            if (selection) {
                selection = false;
                Selectable<?>[] picked = getViewer().getPicker().pickBoxed(
                        getViewer().getGUIRoot().getDelegate().getCamera().getState(), selection_x1, selection_y1,
                        selection_x2, selection_y2, clicks);
                List<Selectable<UnitTemplate>> friendly_units = new ArrayList<>();
                List<Ship> friendly_ships = new ArrayList<>();
                Selectable<BuildingTemplate> friendly_building = null;
                Selectable<?> enemy = null;
                for (Selectable<?> selectable : picked) {
                    if (selectable != null) {
                        if (selectable.getOwner() == getViewer().getLocalPlayer()) {
                            if (selectable instanceof Ship ship) {
                                friendly_ships.add(ship);
                            } else if (selectable instanceof LandBuilding building) {
                                friendly_building = building;
                            } else if (selectable instanceof Unit unit) {
                                friendly_units.add(unit);
                            } else {
                                throw new RuntimeException();
                            }
                        } else {
                            enemy = selectable;
                        }
                    }
                }
                if (Renderer.getLocalInput().isShiftDownCurrently()
                        && getViewer().getSelection().getCurrentSelection().size() > 0)
                    updateSelection(friendly_units, friendly_ships, friendly_building, enemy);
                else
                    replaceSelection(friendly_units, friendly_ships, friendly_building, enemy);
                pick_done = true;
            }
        }
    }

    @Override
    public void mouseReleased(@NonNull MouseButton button, int x, int y) {
        if (map_mode) {
            if (button == MouseButton.LEFT) {
                getViewer().getPicker().pickMapGoto(x, y, (MapCamera) getCamera());
            }
        } else if (!observer) {
            if (!pick_done)
                mouseClicked(button, x, y, 1);
            pick_done = false;
            super.mouseReleased(button, x, y);
        } else {
            super.mouseReleased(button, x, y);
        }
    }

    @Override
    public boolean canHoverBehind() {
        return true;
    }

    @Override
    public void mouseDragged(@NonNull MouseButton button, int x, int y, int relative_x, int relative_y, int absolute_x,
            int absolute_y) {
        if (!map_mode) {
            if (!observer) {
                if (button == MouseButton.LEFT) {
                    selection_x2 += relative_x;
                    selection_y2 += relative_y;
                } else {
                    super.mouseDragged(button, x, y, relative_x, relative_y, absolute_x, absolute_y);
                }
            } else {
                super.mouseDragged(button, x, y, relative_x, relative_y, absolute_x, absolute_y);
            }
        }
    }

    @Override
    public void mousePressed(@NonNull MouseButton button, int x, int y) {
        if (!map_mode) {
            if (!observer) {
                var inputManager = Renderer.getLocalInput().getInputManager();
                switch (button) {
                    case LEFT:
                        if (!inputManager.isActive(GameAction.CAMERA_MAP_MODE)) {
                            selection = true;
                        }
                        selection_x1 = x;
                        selection_y1 = y;
                        selection_x2 = x;
                        selection_y2 = y;
                        break;
                    case RIGHT: {
                        Army selection = getViewer().getSelection().getCurrentSelection();
                        if (selection.size() > 0) {
                            if (selection.containsAbility(Abilities.SAIL)) {
                                getViewer().getPicker().pickSailingTarget(selection,
                                        getViewer().getGUIRoot().getDelegate().getCamera().getState(),
                                        getViewer().getPeerHub().getPlayerInterface(), x, y);
                            } else if (selection.containsAbility(Abilities.TARGET)) {
                                getViewer().getPicker().pickTarget(selection,
                                        getViewer().getGUIRoot().getDelegate().getCamera().getState(),
                                        getViewer().getPeerHub().getPlayerInterface(), x, y, Action.DEFAULT);
                            }
                        }
                        break;
                    }
                    default:
                        super.mousePressed(button, x, y);
                        break;
                }
            } else {
                super.mousePressed(button, x, y);
            }
        }

    }

    public boolean isInMapMode() {
        return map_mode;
    }

    /** On the overview map and not on the way back down. */
    public boolean isOnMap() {
        return map_mode && getCamera() instanceof MapCamera map_camera && !map_camera.isLeaving();
    }

    /** The drag rectangle in GUI coordinates while the player is box selecting. */
    public boolean getSelectionBox(int @NonNull [] dest) {
        dest[0] = selection_x1;
        dest[1] = selection_y1;
        dest[2] = selection_x2;
        dest[3] = selection_y2;
        return selection;
    }

    public boolean isSelecting() {
        return selection;
    }

    private void setHUDVisible(boolean visible) {
        Globals.draw_hud = visible;
        Renderer.getLocalInput().getPointerInput().setActiveCursor(getCursorType());
        if (map_mode)
            return;
        if (observer) {
            refreshSpectator();
            return;
        }
        if (visible)
            addChild(getActionButtonPanel());
        else
            getActionButtonPanel().remove();
    }

    @Override
    public boolean keyboardBlocked() {
        return chat_visible && chat_form.isActive();
    }

    @Override
    public void render2D(@NonNull GUIRenderer renderer) {
        if (!Globals.draw_hud)
            return;
        if (com.oddlabs.tt.global.Settings.getSettings().show_compass && getCamera() != null) {
            float horizAngle = getCamera().getState().getHorizAngle();
            CompassRenderer.render(renderer, Skin.getSkin().getEditFont(),
                    horizAngle, getGUIRoot().getWidth(), getGUIRoot().getHeight());
        }

        if (selection)
            drawSelectionBox(renderer, selection_x1, selection_y1, selection_x2, selection_y2);
    }

    public static void drawSelectionBox(@NonNull GUIRenderer renderer, float x1, float y1, float x2, float y2) {
        float minX = Math.min(x1, x2);
        float minY = Math.min(y1, y2);
        float maxX = Math.max(x1, x2);
        float maxY = Math.max(y1, y2);
        float w = maxX - minX;
        float h = maxY - minY;

        float thickness = com.oddlabs.tt.global.Settings.getSettings().high_contrast ? 3.0f : 1.0f;

        // Ensure thickness doesn't exceed half dimensions
        if (thickness > w / 2) thickness = w / 2;
        if (thickness > h / 2) thickness = h / 2;

        renderer.drawColoredQuad(minX, minY, w, thickness, SELECTION_COLOR);
        renderer.drawColoredQuad(minX, maxY - thickness, w, thickness, SELECTION_COLOR);
        renderer.drawColoredQuad(minX, minY + thickness, thickness, h - 2 * thickness, SELECTION_COLOR);
        renderer.drawColoredQuad(maxX - thickness, minY + thickness, thickness, h - 2 * thickness, SELECTION_COLOR);
    }

    @Override
    public void displayChangedNotify(int width, int height) {
        super.displayChangedNotify(width, height);
        if (observer)
            refreshSpectator();
    }
}
