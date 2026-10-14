package com.oddlabs.tt.form;

import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.Origin;
import com.oddlabs.tt.gui.PanelGroup;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.util.Utils;
import org.jspecify.annotations.NonNull;

import java.util.ResourceBundle;

import static com.oddlabs.tt.gui.Placement.BOTTOM_LEFT;
import static com.oddlabs.tt.gui.Placement.LEFT_MID;

public abstract class AbstractOptionsMenu extends Form {
    private static final int BUTTON_WIDTH = 100;
    public static final ResourceBundle bundle = ResourceBundle.getBundle(OptionsMenu.class.getName());

    static @NonNull String i18n(@NonNull String key, @NonNull Object @NonNull... args) {
        return Utils.getBundleString(bundle, key, args);
    }

    private final @NonNull GeneralPanel generalPanel;
    private final @NonNull GraphicsPanel graphicsPanel;

    AbstractOptionsMenu(@NonNull GUIRoot gui_root) {
        Label label_headline = new Label(i18n("options_caption"), Skin.getSkin().getHeadlineFont());
        addChild(label_headline);

        generalPanel = new GeneralPanel(gui_root, this::changeGamespeed);
        graphicsPanel = new GraphicsPanel(gui_root, this);

        PanelGroup panel_group = new PanelGroup(
                generalPanel,
                graphicsPanel,
                new CameraPanel(),
                new KeyBindingPanel(gui_root),
                new AccessibilityPanel(gui_root),
                new SoundPanel(gui_root),
                new LanguagePanel(gui_root)
        );
        addChild(panel_group);

        // Buttons
        HorizButton button_close = new HorizButton(i18n("close"), BUTTON_WIDTH);
        button_close.addMouseClickListener(new CancelListener(this));
        addChild(button_close);

        HorizButton button_about = new HorizButton(i18n("about"), BUTTON_WIDTH);
        button_about.addMouseClickListener((_, _, _, _) -> gui_root.addModalForm(new CreditsForm()));
        addChild(button_about);

        HorizButton button_patch_notes = new HorizButton(i18n("patch_notes"), BUTTON_WIDTH);
        button_patch_notes.addMouseClickListener((_, _, _, _) -> gui_root.addModalForm(new PatchNotesForm()));
        addChild(button_patch_notes);

        addCloseListener(() -> Settings.getSettings().save());

        // Place objects
        label_headline.place();
        panel_group.place(label_headline, BOTTOM_LEFT);
        button_close.place(Origin.AT_END);
        button_about.place(button_close, LEFT_MID);
        button_patch_notes.place(button_about, LEFT_MID);
        compileCanvas();
    }

    @Override
    protected void displayChangedNotify(int width, int height) {
        super.displayChangedNotify(width, height);
        centerPos();
        graphicsPanel.updateScaleLabel();
    }

    protected final void chooseGamespeed(int speed) {
        generalPanel.chooseGamespeed(speed);
    }

    protected void changeGamespeed(int index) {
        com.oddlabs.tt.global.Globals.gamespeed = index;
        Settings.getSettings().gamespeed = index;
    }
}
