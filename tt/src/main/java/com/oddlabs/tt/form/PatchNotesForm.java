package com.oddlabs.tt.form;

import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.OKButton;
import com.oddlabs.tt.gui.Origin;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.TextBox;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.util.BuildInfo;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.oddlabs.tt.gui.Placement.BOTTOM_LEFT;

public final class PatchNotesForm extends Form {
    private static final ResourceBundle bundle = ResourceBundle.getBundle(PatchNotesForm.class.getName());
    private static final String CHANGELOG_RESOURCE = "CHANGELOG.md";
    private static final String UNRELEASED_HEADING = "## [Unreleased]";
    private static final String VERSION_HEADING_PREFIX = "## ";
    private static final String SECTION_HEADING_PREFIX = "### ";
    private static final int BOX_WIDTH = 500;
    private static final int BOX_HEIGHT = 350;
    private static final int MAX_CHARS = 200000;
    private static final int BUTTON_WIDTH = 100;

    private static @NonNull String i18n(@NonNull String key, @NonNull Object @NonNull... args) {
        return Utils.getBundleString(bundle, key, args);
    }

    public PatchNotesForm() {
        Label head_label = new Label(i18n("patch_notes"), Skin.getSkin().getHeadlineFont());
        addChild(head_label);
        head_label.place();

        TextBox notes_box = new TextBox(BOX_WIDTH, BOX_HEIGHT, Skin.getSkin().getEditFont(), MAX_CHARS);
        addChild(notes_box);
        notes_box.append(loadNotes());
        notes_box.place(head_label, BOTTOM_LEFT);

        HorizButton ok_button = new OKButton(BUTTON_WIDTH);
        addChild(ok_button);
        ok_button.addMouseClickListener((_, _, _, _) -> this.cancel());
        ok_button.place(Origin.AT_END);
        compileCanvas();
        centerPos();
    }

    private static @NonNull String loadNotes() {
        try (InputStream stream = PatchNotesForm.class.getResourceAsStream(CHANGELOG_RESOURCE)) {
            if (stream == null) {
                return i18n("unavailable");
            }
            return toPlainText(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return i18n("unavailable");
        }
    }

    // Keeps the version sections of a Keep a Changelog file and drops the markdown markers the game font cannot render.
    static @NonNull String toPlainText(@NonNull String markdown) {
        StringBuilder text = new StringBuilder();
        boolean in_versions = false;
        for (String raw_line : markdown.split("\n")) {
            String line = raw_line.stripTrailing();
            if (line.startsWith(VERSION_HEADING_PREFIX)) {
                in_versions = true;
            }
            if (!in_versions || isLinkReference(line)) {
                continue;
            }
            if (line.startsWith(UNRELEASED_HEADING)) {
                line = VERSION_HEADING_PREFIX + BuildInfo.FULL_VERSION;
            }
            if (line.startsWith(SECTION_HEADING_PREFIX)) {
                text.append(line.substring(SECTION_HEADING_PREFIX.length()).toUpperCase(Locale.ROOT));
            } else if (line.startsWith(VERSION_HEADING_PREFIX)) {
                text.append(line.substring(VERSION_HEADING_PREFIX.length()).replace("[", "").replace("]", ""));
            } else {
                text.append(line);
            }
            text.append('\n');
        }
        return text.toString().strip();
    }

    private static boolean isLinkReference(@NonNull String line) {
        return line.startsWith("[") && line.contains("]: ");
    }
}
