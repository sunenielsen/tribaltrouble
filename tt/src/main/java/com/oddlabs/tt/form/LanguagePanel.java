package com.oddlabs.tt.form;

import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.ColumnInfo;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.gui.IconLabel;
import com.oddlabs.tt.gui.IconQuad;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.Languages;
import com.oddlabs.tt.gui.MultiColumnComboBox;
import com.oddlabs.tt.gui.Panel;
import com.oddlabs.tt.gui.Row;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.guievent.RowListener;
import com.oddlabs.tt.render.Renderer;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Locale;

import static com.oddlabs.tt.gui.Placement.BOTTOM_LEFT;

public class LanguagePanel extends Panel {
    public LanguagePanel(@NonNull GUIRoot gui_root) {
        super(AbstractOptionsMenu.i18n("language_caption"));

        // language
        Group language_group = new Group();
        addChild(language_group);
        Label language_label = new Label(AbstractOptionsMenu.i18n("language_label"), Skin.getSkin().getEditFont());
        language_group.addChild(language_label);

        ColumnInfo[] language_infos = new ColumnInfo[]{new ColumnInfo("", 300)};
        var language_list_box = new MultiColumnComboBox<@NonNull Locale>(gui_root, language_infos, 200, false);

        // Check language logic
        boolean languageFound = false;
        if (!Settings.getSettings().language.equals("default")) {
            for (String[] lang : Languages.getLanguages()) {
                if (Settings.getSettings().language.equals(lang[0])) {
                    languageFound = true;
                    break;
                }
            }
            if (!languageFound) {
                Settings.getSettings().language = "default";
            }
        }

        Row<Locale, IconLabel> selectedLanguage = null;
        String[][] languages = Languages.getLanguages();
        IconQuad[] flags = Languages.getFlags();
        for (int i = 0; i < languages.length; i++) {
            IconLabel label = new IconLabel(flags[i], new Label(languages[i][1],
                    Skin.getSkin().getMultiColumnComboBoxData().font()));
            Row<Locale, IconLabel> row = new Row<>(new IconLabel[]{label}, Locale.of(languages[i][0]));
            language_list_box.addRow(row);
            if (languages[i][0].equals(Settings.getSettings().language))
                selectedLanguage = row;
        }
        IconLabel label = new IconLabel(Skin.getSkin().getFlagDefault(), new Label(AbstractOptionsMenu.i18n(
                "system_default"), Skin.getSkin().getMultiColumnComboBoxData().font()));
        Row<Locale, IconLabel> row = new Row<>(List.of(label), Renderer.getRenderer().getDefaultLocale());
        language_list_box.addRow(row);
        if (Settings.getSettings().language.equals("default"))
            selectedLanguage = row;

        language_list_box.selectRow(selectedLanguage);
        language_list_box.addRowListener(new RowListener<>() {
            @Override
            public void rowDoubleClicked(@NonNull Locale locale) {
                Settings.getSettings().language = locale.getVariant().equals(
                        "default") ? "default" : locale.getLanguage();
                gui_root.addModalForm(new MessageForm(AbstractOptionsMenu.i18n("language_change_next_run")));
            }
        });

        language_group.addChild(language_list_box);
        language_label.place();
        language_list_box.place(language_label, BOTTOM_LEFT);
        language_group.compileCanvas();

        // Placement
        language_group.place();
        compileCanvas();
    }
}
