package com.oddlabs.tt.form;

import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.OKButton;
import com.oddlabs.tt.gui.Origin;
import com.oddlabs.tt.gui.Panel;
import com.oddlabs.tt.gui.PanelGroup;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.TextBox;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.util.BuildInfo;
import org.jspecify.annotations.NonNull;

import java.util.ResourceBundle;

import static com.oddlabs.tt.gui.Placement.BOTTOM_LEFT;

public final class CreditsForm extends Form {
    private static final ResourceBundle bundle = ResourceBundle.getBundle(CreditsForm.class.getName());

    private static @NonNull String i18n(@NonNull String key, @NonNull Object @NonNull... args) {
        return Utils.getBundleString(bundle, key, args);
    }

    public CreditsForm() {
        Label head_label = new Label(i18n("about"), Skin.getSkin().getHeadlineFont());
        addChild(head_label);
        head_label.place();

        PanelGroup panel_group = new PanelGroup(createAboutPanel(bundle), createCreditsPanel(bundle),
                createContributorsPanel(bundle), createThanksPanel(bundle));
        addChild(panel_group);
        panel_group.place(head_label, BOTTOM_LEFT);

        HorizButton ok_button = new OKButton(100);
        addChild(ok_button);
        ok_button.addMouseClickListener((_, _, _, _) -> this.cancel());
        ok_button.place(Origin.AT_END);
        compileCanvas();
        centerPos();
    }

    private static @NonNull Panel createAboutPanel(@NonNull ResourceBundle bundle) {
        Panel about = new Panel(i18n("about"));
        TextBox about_box = new TextBox(400, 300, Skin.getSkin().getEditFont(), 100000);
        about.addChild(about_box);
        about_box.append(BuildInfo.FULL_VERSION + "\n\n");
        String about_text = i18n("about_notice", Integer.toString(Renderer.getLocalInput().getRevision()));
        about_box.append(about_text);

        about_box.place();
        about.compileCanvas();
        return about;
    }

    private static @NonNull Panel createCreditsPanel(@NonNull ResourceBundle bundle) {
        Panel credits = new Panel(i18n("credits"));
        TextBox credits_box = new TextBox(400, 300, Skin.getSkin().getEditFont(), 100000);
        credits.addChild(credits_box);
        credits_box.append(i18n("game_design_and_programming") + "\n");
        credits_box.append("Elias Naur\n");
        credits_box.append("Mikkel Jensen\n");
        credits_box.append("Sune Nielsen\n");
        credits_box.append("Jacob Olsen\n");
        credits_box.append("\n");
        credits_box.append(i18n("3d_artwork_and_animation") + "\n");
        credits_box.append("Chaz Willets\n");
        credits_box.append("\n");
        credits_box.append(i18n("audio") + "\n");
        credits_box.append("Michael Huang\n");
        credits_box.append("Nicklas Schmidt\n");
        credits_box.append("Herman Witkam");
        credits_box.place();
        credits.compileCanvas();
        return credits;
    }

    private static @NonNull Panel createContributorsPanel(@NonNull ResourceBundle bundle) {
        Panel contributors = new Panel(i18n("contributors"));
        TextBox contributors_box = new TextBox(400, 300, Skin.getSkin().getEditFont(), 100000);
        contributors.addChild(contributors_box);
        contributors_box.append(i18n("contributors_text") + "\n");
        contributors_box.append("\n");
        contributors_box.append(i18n("programming") + "\n");
        contributors_box.append("Ryan Linehan (RcubDev)\n");
        contributors_box.append("bondolo\n");
        contributors_box.append("Omar Mokhtar\n");
        contributors_box.append("Merikrotti\n");
        contributors_box.append("Ian Andrew Moore (Dapianokid)\n");
        contributors_box.append("Maxime Michon (Maxenor)\n");
        contributors_box.append("Pyprohly\n");
        contributors_box.append("TimmyTonkaBuilder\n");
        contributors_box.append("Xzf\n");
        contributors_box.append("Rafal Okuniewski\n");
        contributors_box.append("\n");
        contributors_box.append(i18n("3d_artwork_and_animation") + "\n");
        contributors_box.append("Chaz Willets\n");
        contributors_box.append("Camersteph\n");
        contributors_box.append("\n");
        contributors_box.append(i18n("translations") + "\n");
        contributors_box.append("Daniel Araújo Nacarini (Piratax10)\n");
        contributors_box.append("Rafal Okuniewski\n");
        contributors_box.append("\n");
        contributors_box.append(i18n("community") + "\n");
        contributors_box.append("Ian Andrew Moore (Dapianokid)\n");
        contributors_box.append("Ryan Linehan (RcubDev)");
        contributors_box.place();
        contributors.compileCanvas();
        return contributors;
    }

    private static @NonNull Panel createThanksPanel(@NonNull ResourceBundle bundle) {
        Panel thanks = new Panel(i18n("thanks_to"));
        TextBox thanks_box = new TextBox(400, 300, Skin.getSkin().getEditFont(), 100000);
        thanks.addChild(thanks_box);
        thanks_box.append(i18n("thanks") + "\n");
        thanks_box.append(i18n("original_thanks") + "\n");
        thanks_box.append("\n");
        thanks_box.append("Caspian Rychlik-Prince\n");
        thanks_box.append("Brian Matzon\n");
        thanks_box.append("The LWJGL team\n");
        thanks_box.append("Johannes Sebastian Jørgen Erik Møgelvang\n");
        thanks_box.append("Martin B. K. Nielsen\n");
        thanks_box.append("Martin Vestergaard Madsen\n");
        thanks_box.append("Christian Mosbæk\n");
        thanks_box.append("Camilla Dahle\n");
        thanks_box.append("\n");
        thanks_box.append(i18n("beta_thank") + "\n");
        thanks_box.append("\n");
        thanks_box.append("Patric Schenke\n");
        thanks_box.append("Oliver Hutt\n");
        thanks_box.append("Mathew Foscarini\n");
        thanks_box.append("Scott Call\n");
        thanks_box.append("Gheorghe Costin\n");
        thanks_box.append("Eric Spierings\n");
        thanks_box.append("Tomasz Malecki\n");
        thanks_box.append("Benjamin Dirks\n");
        thanks_box.append("Paulo Augusto\n");
        thanks_box.append("Carlos Rayon\n");
        thanks_box.append("Robert Speelpenning\n");
        thanks_box.append("Matt Chelen\n");
        thanks_box.append("Paul van Schayck\n");
        thanks_box.append("Binks\n");
        thanks_box.append("Søren Vesti Lassen\n");
        thanks_box.append("Martyn Lewis\n");
        thanks_box.append("Benjamin Dirks\n");
        thanks_box.append("Tonny L. Rasmussen\n");
        thanks_box.append("Bjørn Andersen\n");
        thanks_box.append("\n");
        thanks_box.append(i18n("additional_thanks") + "\n");
        thanks_box.append("\n");
        thanks_box.append(i18n("special_thanks") + "\n");
        thanks_box.append(i18n("special_thanks_text") + "\n");
        thanks_box.append("\n");
        thanks_box.append("Niels Haulrich AKA \"DJ Fnillerz\"\n");
        thanks_box.append("Morten M. Hansen AKA \"blue\"\n");
        thanks_box.append("Søren Garbøl AKA \"Mythor\"\n");
        thanks_box.append("Andreas Beier AKA \"AB\"\n");
        thanks_box.append("Jesper S. Pedersen AKA \"fronk\"\n");
        thanks_box.append("Kxre Schou Gjaldbxk AKA \"hardkxre\"");
        thanks_box.place();
        thanks.compileCanvas();
        return thanks;
    }
}
