package com.oddlabs.tt.form;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.gui.CancelButton;
import com.oddlabs.tt.gui.CheckBox;
import com.oddlabs.tt.gui.FocusDirection;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIObject;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.OKButton;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.Slider;
import com.oddlabs.tt.util.Utils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ResourceBundle;

import static com.oddlabs.tt.gui.Placement.BOTTOM_LEFT;
import static com.oddlabs.tt.gui.Placement.BOTTOM_RIGHT;
import static com.oddlabs.tt.gui.Placement.LEFT_MID;
import static com.oddlabs.tt.gui.Placement.RIGHT_MID;

/**
 * Modal for the gameplay limits and ship availability of a new game. Values are handed back through
 * {@link Listener} on OK; the form never touches the create-game dialog directly.
 */
public final class AdvancedSettingsForm extends Form {
    public static final int MIN_MAX_UNITS = 20;
    public static final int MAX_MAX_UNITS = 1000;
    public static final int MAX_UNITS_STEP = 10;
    public static final int MIN_STARTING_UNITS = 1;
    public static final int MIN_MAX_BUILDINGS = 1;
    public static final int MAX_MAX_BUILDINGS = 100;

    private static final int BUTTON_WIDTH = 100;
    private static final int RESET_BUTTON_WIDTH = 160;
    private static final int LABEL_WIDTH = 150;
    private static final int SLIDER_LENGTH = 250;
    private static final int VALUE_WIDTH = 60;
    private static final int WARNING_WIDTH = 470;
    private static final ResourceBundle bundle = ResourceBundle.getBundle(AdvancedSettingsForm.class.getName());

    public record Values(int maxUnits, int startingUnits, int maxBuildings, boolean ships) {
        public static @NonNull Values defaults() {
            return new Values(Game.DEFAULT_MAX_UNIT_COUNT, Game.DEFAULT_INITIAL_UNIT_COUNT,
                    Game.DEFAULT_MAX_BUILDING_COUNT, false);
        }

        public boolean isOverDefaults() {
            return maxUnits > Game.DEFAULT_MAX_UNIT_COUNT || startingUnits > Game.DEFAULT_INITIAL_UNIT_COUNT
                    || maxBuildings > Game.DEFAULT_MAX_BUILDING_COUNT;
        }
    }

    @FunctionalInterface
    public interface Listener {
        void applied(@NonNull Values values);
    }

    private final @NonNull Listener listener;
    private final @NonNull Slider slider_max_units;
    private final @NonNull Slider slider_starting_units;
    private final @NonNull Slider slider_max_buildings;
    private final @NonNull Label value_max_units;
    private final @NonNull Label value_starting_units;
    private final @NonNull Label value_max_buildings;
    private final @Nullable CheckBox cb_ships;
    private final @NonNull Label label_warning;
    private final boolean force_ships;
    private boolean ships_choice;

    public AdvancedSettingsForm(@NonNull Values current, boolean show_ships, boolean force_ships,
            @NonNull Listener listener) {
        super(i18n("caption"));
        this.listener = listener;
        this.force_ships = force_ships;

        Label label_max_units = new Label(i18n("max_units"), Skin.getSkin().getEditFont(), LABEL_WIDTH);
        slider_max_units = new Slider(SLIDER_LENGTH, MIN_MAX_UNITS / MAX_UNITS_STEP, MAX_MAX_UNITS / MAX_UNITS_STEP,
                Game.DEFAULT_MAX_UNIT_COUNT / MAX_UNITS_STEP);
        value_max_units = new Label("", Skin.getSkin().getEditFont(), VALUE_WIDTH);
        Label label_starting_units = new Label(i18n("starting_units"), Skin.getSkin().getEditFont(), LABEL_WIDTH);
        slider_starting_units = new Slider(SLIDER_LENGTH, MIN_STARTING_UNITS, MAX_MAX_UNITS,
                Game.DEFAULT_INITIAL_UNIT_COUNT);
        value_starting_units = new Label("", Skin.getSkin().getEditFont(), VALUE_WIDTH);
        Label label_max_buildings = new Label(i18n("max_buildings"), Skin.getSkin().getEditFont(), LABEL_WIDTH);
        slider_max_buildings = new Slider(SLIDER_LENGTH, MIN_MAX_BUILDINGS, MAX_MAX_BUILDINGS,
                Game.DEFAULT_MAX_BUILDING_COUNT);
        value_max_buildings = new Label("", Skin.getSkin().getEditFont(), VALUE_WIDTH);
        cb_ships = show_ships ? new CheckBox(current.ships(), i18n("ships"), i18n("ships_tip")) : null;
        if (cb_ships != null && force_ships) {
            cb_ships.setDisabled(true);
        }
        label_warning = new Label("", Skin.getSkin().getEditFont(), WARNING_WIDTH);

        slider_max_units.addValueListener(_ -> update());
        slider_starting_units.addValueListener(_ -> update());
        slider_max_buildings.addValueListener(_ -> update());

        HorizButton button_reset = new HorizButton(i18n("reset_defaults"), RESET_BUTTON_WIDTH);
        button_reset.addMouseClickListener((_, _, _, _) -> setValues(Values.defaults()));
        HorizButton button_ok = new OKButton(BUTTON_WIDTH);
        button_ok.addMouseClickListener((_, _, _, _) -> submit());
        HorizButton button_cancel = new CancelButton(BUTTON_WIDTH);
        button_cancel.addMouseClickListener((_, _, _, _) -> this.cancel());

        addChild(label_max_units);
        addChild(slider_max_units);
        addChild(value_max_units);
        addChild(label_starting_units);
        addChild(slider_starting_units);
        addChild(value_starting_units);
        addChild(label_max_buildings);
        addChild(slider_max_buildings);
        addChild(value_max_buildings);
        if (cb_ships != null) {
            addChild(cb_ships);
        }
        addChild(label_warning);
        addChild(button_reset);
        addChild(button_ok);
        addChild(button_cancel);

        label_max_units.place();
        slider_max_units.place(label_max_units, RIGHT_MID);
        value_max_units.place(slider_max_units, RIGHT_MID);
        label_starting_units.place(label_max_units, BOTTOM_LEFT);
        slider_starting_units.place(label_starting_units, RIGHT_MID);
        value_starting_units.place(slider_starting_units, RIGHT_MID);
        label_max_buildings.place(label_starting_units, BOTTOM_LEFT);
        slider_max_buildings.place(label_max_buildings, RIGHT_MID);
        value_max_buildings.place(slider_max_buildings, RIGHT_MID);
        GUIObject last = label_max_buildings;
        if (cb_ships != null) {
            cb_ships.place(label_max_buildings, BOTTOM_LEFT);
            last = cb_ships;
        }
        label_warning.place(last, BOTTOM_LEFT, Skin.getSkin().getFormData().sectionSpacing());
        button_cancel.place(label_warning, BOTTOM_RIGHT);
        button_ok.place(button_cancel, LEFT_MID);
        button_reset.place(button_ok, LEFT_MID);

        setValues(current);
        compileCanvas();
        centerPos();
    }

    private static @NonNull String i18n(@NonNull String key, @NonNull Object @NonNull... args) {
        return Utils.getBundleString(bundle, key, args);
    }

    @Override
    public void setFocus(@NonNull FocusDirection direction) {
        if (direction == FocusDirection.BACKWARD) {
            super.setFocus(direction);
        } else {
            slider_max_units.setFocus(direction);
        }
    }

    private @NonNull Values currentValues() {
        // While the map forces boats the box is only showing that, so the player's own setting is kept instead.
        boolean ships = force_ships ? ships_choice : cb_ships != null && cb_ships.isMarked();
        return new Values(slider_max_units.getValue() * MAX_UNITS_STEP, slider_starting_units.getValue(),
                slider_max_buildings.getValue(), ships);
    }

    private void setValues(@NonNull Values values) {
        slider_max_units.setValue(values.maxUnits() / MAX_UNITS_STEP);
        slider_starting_units.setValue(values.startingUnits());
        slider_max_buildings.setValue(values.maxBuildings());
        ships_choice = values.ships();
        if (cb_ships != null) {
            cb_ships.setMarked(force_ships || values.ships());
        }
        update();
    }

    private void update() {
        // A colony cannot start with more units than it may ever hold, so starting units ranges up to max units.
        slider_starting_units.setMax(slider_max_units.getValue() * MAX_UNITS_STEP);
        Values values = currentValues();
        value_max_units.set(Integer.toString(values.maxUnits()));
        value_starting_units.set(Integer.toString(values.startingUnits()));
        value_max_buildings.set(Integer.toString(values.maxBuildings()));
        label_warning.set(values.isOverDefaults() ? i18n("performance_warning") : "");
    }

    private void submit() {
        remove();
        listener.applied(currentValues());
    }
}
