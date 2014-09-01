package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Profile;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.OKButton;
import com.oddlabs.tt.gui.OKListener;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.util.Utils;

public final strictfp class InfoForm extends Form {
	private final HorizButton ok_button;
	
	public InfoForm(Profile profile) {
		ResourceBundle bundle = ResourceBundle.getBundle(InfoForm.class.getName());
		String profile_str = Utils.getBundleString(bundle, "profile");
		Label label_headline = new Label(profile_str, Skin.getSkin().getHeadlineFont());
		addChild(label_headline);

		Group types = new Group();
		Group values = new Group();

		Label label_name = new Label(Utils.getBundleString(bundle, "name"), Skin.getSkin().getEditFont());
		Label label_name_value = new Label(profile.getNick(), Skin.getSkin().getEditFont());
		types.addChild(label_name);
		values.addChild(label_name_value);
		
		Label label_rating = new Label(Utils.getBundleString(bundle, "rating"), Skin.getSkin().getEditFont());
		Label label_rating_value = new Label(""+profile.getRating(), Skin.getSkin().getEditFont());
		types.addChild(label_rating);
		values.addChild(label_rating_value);
		
		Label label_wins = new Label(Utils.getBundleString(bundle, "wins"), Skin.getSkin().getEditFont());
		Label label_wins_value = new Label(""+profile.getWins(), Skin.getSkin().getEditFont());
		types.addChild(label_wins);
		values.addChild(label_wins_value);
		
		Label label_losses = new Label(Utils.getBundleString(bundle, "losses"), Skin.getSkin().getEditFont());
		Label label_losses_value = new Label(""+profile.getLosses(), Skin.getSkin().getEditFont());
		types.addChild(label_losses);
		values.addChild(label_losses_value);
		
		Label label_invalid = new Label(Utils.getBundleString(bundle, "invalid"), Skin.getSkin().getEditFont());
		Label label_invalid_value = new Label(""+profile.getInvalid(), Skin.getSkin().getEditFont());
		types.addChild(label_invalid);
		values.addChild(label_invalid_value);
		
		label_name.place();
		label_rating.place(label_name, BOTTOM_LEFT);
		label_wins.place(label_rating, BOTTOM_LEFT);
		label_losses.place(label_wins, BOTTOM_LEFT);
		label_invalid.place(label_losses, BOTTOM_LEFT);
		types.compileCanvas();
		addChild(types);
		
		label_name_value.place();
		label_rating_value.place(label_name_value, BOTTOM_LEFT);
		label_wins_value.place(label_rating_value, BOTTOM_LEFT);
		label_losses_value.place(label_wins_value, BOTTOM_LEFT);
		label_invalid_value.place(label_losses_value, BOTTOM_LEFT);
		values.compileCanvas();
		addChild(values);
		
		ok_button = new OKButton(100);
		addChild(ok_button);
		ok_button.addMouseClickListener(new OKListener(this));

		label_headline.place();
		types.place(label_headline, BOTTOM_LEFT);
		values.place(types, RIGHT_TOP);
			
		ok_button.place(ORIGIN_BOTTOM_RIGHT);
		compileCanvas();
		centerPos();
	}
	
	public final void setFocus() {
		ok_button.setFocus();
	}
}
