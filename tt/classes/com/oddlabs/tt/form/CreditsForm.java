package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.registration.RegistrationInfo;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.gui.OKButton;
import com.oddlabs.tt.gui.Panel;
import com.oddlabs.tt.gui.PanelGroup;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.TextBox;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.Utils;

// unicode codes: ae: 00E6 - oe: 00F8 - aa: 00E5 - AE: 00C6 - OE: 00D8 - AA: 00C5 - (C): 00A9

public final strictfp class CreditsForm extends Form {
	public CreditsForm() {
		ResourceBundle bundle = ResourceBundle.getBundle(CreditsForm.class.getName());
		Label head_label = new Label(Utils.getBundleString(bundle, "about"), Skin.getSkin().getHeadlineFont());
		addChild(head_label);
		head_label.place();
		
		Panel about = new Panel(Utils.getBundleString(bundle, "about"));
		Panel credits = new Panel(Utils.getBundleString(bundle, "credits"));
		Panel thanks = new Panel(Utils.getBundleString(bundle, "thanks_to"));
		
		// about tab
		TextBox about_box = new TextBox(400, 300, Skin.getSkin().getEditFont(), 100000);
		about.addChild(about_box);
		String about_text = Utils.getBundleString(bundle, "about_text", new Object[]{
								Integer.toString(LocalInput.getRevision())});
		about_box.append(about_text);
		if (Renderer.isRegistered()) {
			RegistrationInfo info = Renderer.getRegistrationClient().getRegistrationInfo();
			if (info != null) {
				about_box.append(Utils.getBundleString(bundle, "registered_to") + "\n");

				about_box.append(info.getName() + "\n");
				about_box.append(info.getAddress1() + "\n");
				if (info.getAddress2() != null && !info.getAddress2().trim().equals(""))
					about_box.append(info.getAddress2() + "\n");
				about_box.append(info.getZip() + "\n");
				about_box.append(info.getCity() + "\n");
				if (info.getState() != null && !info.getState().trim().equals(""))
					about_box.append(info.getState() + "\n");
				about_box.append(info.getCountry() + "\n");
				about_box.append("\n");
			}
			if (!Settings.getSettings().hide_regkey) {
				about_box.append(Utils.getBundleString(bundle, "registration_key") + "\n");
				if (info != null) {
					about_box.append(info.getRegKey());
				} else {
					about_box.append(Settings.getSettings().reg_key);
				}
			}
		} else {
			about_box.append(Utils.getBundleString(bundle, "unregistered"));
		}

		about_box.place();
		about.compileCanvas();
		
		// credits tab
		TextBox credits_box = new TextBox(400, 300, Skin.getSkin().getEditFont(), 100000);
		credits.addChild(credits_box);
		credits_box.append(Utils.getBundleString(bundle, "game_design_and_programming") + "\n");
		credits_box.append("Elias Naur\n");
		credits_box.append("Mikkel Jensen\n");
		credits_box.append("Sune Nielsen\n");
		credits_box.append("Jacob Olsen\n");
		credits_box.append("\n");
		credits_box.append(Utils.getBundleString(bundle, "3d_artwork_and_animation") + "\n");
		credits_box.append("Chaz Willets\n");
		credits_box.append("\n");
		credits_box.append(Utils.getBundleString(bundle, "audio") + "\n");
		credits_box.append("Michael Huang\n");
		credits_box.append("Nicklas Schmidt\n");
		credits_box.append("Herman Witkam");
		credits_box.place();
		credits.compileCanvas();
		
		// thanks tab
		TextBox thanks_box = new TextBox(400, 300, Skin.getSkin().getEditFont(), 100000);
		thanks.addChild(thanks_box);
		thanks_box.append(Utils.getBundleString(bundle, "thanks") + "\n");
		thanks_box.append(Utils.getBundleString(bundle, "oddlabs_thanks") + "\n");
		thanks_box.append("\n");
		thanks_box.append("Caspian Rychlik-Prince\n");
		thanks_box.append("Brian Matzon\n");
		thanks_box.append("The LWJGL team\n");
		thanks_box.append("Johannes Sebastian J\u00F8rgen Erik M\u00F8gelvang\n");
		thanks_box.append("Martin B. K. Nielsen\n");
		thanks_box.append("Martin Vestergaard Madsen\n");
		thanks_box.append("Christian Mosb\u00E6k\n");
		thanks_box.append("Camilla Dahle\n");
		thanks_box.append("\n");
		thanks_box.append(Utils.getBundleString(bundle, "beta_thank") + "\n");
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
		thanks_box.append("S\u00F8ren Vesti Lassen\n");
		thanks_box.append("Martyn Lewis\n");
		thanks_box.append("Benjamin Dirks\n");
		thanks_box.append("Tonny L. Rasmussen\n");
		thanks_box.append("Bj\u00F8rn Andersen\n");
		thanks_box.append("\n");
		thanks_box.append(Utils.getBundleString(bundle, "additional_thanks") + "\n");
		thanks_box.append("\n");
		thanks_box.append(Utils.getBundleString(bundle, "special_thanks") + "\n");
		thanks_box.append(Utils.getBundleString(bundle, "special_thanks_text") + "\n");
		thanks_box.append("\n");
		thanks_box.append("Niels Haulrich AKA \"DJ Fnillerz\"\n");
		thanks_box.append("Morten M. Hansen AKA \"blue\"\n");
		thanks_box.append("S\u00F8ren Garb\u00F8l AKA \"Mythor\"\n");
		thanks_box.append("Andreas Beier AKA \"AB\"\n");
		thanks_box.append("Jesper S. Pedersen AKA \"fronk\"\n");
		thanks_box.append("Kxre Schou Gjaldbxk AKA \"hardkxre\"");
		thanks_box.place();
		thanks.compileCanvas();
		
		PanelGroup panel_group = new PanelGroup(new Panel[]{about, credits, thanks}, 0);
		addChild(panel_group);
		panel_group.place(head_label, BOTTOM_LEFT);
		HorizButton ok_button = new OKButton(100);
		addChild(ok_button);
		ok_button.addMouseClickListener(new CancelListener(this));
		ok_button.place(ORIGIN_BOTTOM_RIGHT);
		compileCanvas();
		centerPos();
	}
}
