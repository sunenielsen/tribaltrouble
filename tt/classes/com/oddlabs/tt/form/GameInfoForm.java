package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.OKButton;
import com.oddlabs.tt.gui.OKListener;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.util.ServerMessageBundler;
import com.oddlabs.tt.util.Utils;

public final strictfp class GameInfoForm extends Form {
	private final HorizButton ok_button;
	
	public GameInfoForm(Game game) {
		ResourceBundle bundle = ResourceBundle.getBundle(GameInfoForm.class.getName());
		Label label_headline = new Label(Utils.getBundleString(bundle, "game_info"), Skin.getSkin().getHeadlineFont());
		addChild(label_headline);

		Group types = new Group();
		Group values = new Group();

		Label label_name = new Label(Utils.getBundleString(bundle, "name"), Skin.getSkin().getEditFont());
		Label label_name_value = new Label(game.getName(), Skin.getSkin().getEditFont());
		types.addChild(label_name);
		values.addChild(label_name_value);
		
		Label label_size = new Label(Utils.getBundleString(bundle, "size"), Skin.getSkin().getEditFont());
		Label label_size_value = new Label(ServerMessageBundler.getSizeString(game.getSize()), Skin.getSkin().getEditFont());
		types.addChild(label_size);
		values.addChild(label_size_value);
		
		Label label_terrain_type = new Label(Utils.getBundleString(bundle, "terrain_type"), Skin.getSkin().getEditFont());
		Label label_terrain_type_value = new Label(ServerMessageBundler.getTerrainTypeString(game.getTerrainType()), Skin.getSkin().getEditFont());
		types.addChild(label_terrain_type);
		values.addChild(label_terrain_type_value);
		
		Label label_hills = new Label(Utils.getBundleString(bundle, "hills"), Skin.getSkin().getEditFont());
		Label label_hills_value = new Label(ServerMessageBundler.getHillsString(game.getHills()), Skin.getSkin().getEditFont());
		types.addChild(label_hills);
		values.addChild(label_hills_value);
		
		Label label_trees = new Label(Utils.getBundleString(bundle, "trees"), Skin.getSkin().getEditFont());
		Label label_trees_value = new Label(ServerMessageBundler.getTreesString(game.getTrees()), Skin.getSkin().getEditFont());
		types.addChild(label_trees);
		values.addChild(label_trees_value);
		
		Label label_supplies = new Label(Utils.getBundleString(bundle, "supplies"), Skin.getSkin().getEditFont());
		Label label_supplies_value = new Label(ServerMessageBundler.getSuppliesString(game.getSupplies()), Skin.getSkin().getEditFont());
		types.addChild(label_supplies);
		values.addChild(label_supplies_value);
		
		Label label_rated = new Label(Utils.getBundleString(bundle, "rated"), Skin.getSkin().getEditFont());
		Label label_rated_value = new Label(ServerMessageBundler.getRatedString(game.isRated()), Skin.getSkin().getEditFont());
		types.addChild(label_rated);
		values.addChild(label_rated_value);
		
		Label label_gamespeed = new Label(Utils.getBundleString(bundle, "gamespeed"), Skin.getSkin().getEditFont());
		Label label_gamespeed_value = new Label(ServerMessageBundler.getGamespeedString(game.getGamespeed()), Skin.getSkin().getEditFont());
		types.addChild(label_gamespeed);
		values.addChild(label_gamespeed_value);
		
		Label label_mapcode = new Label(Utils.getBundleString(bundle, "mapcode"), Skin.getSkin().getEditFont());
		Label label_mapcode_value = new Label(game.getMapcode(), Skin.getSkin().getEditFont());
		types.addChild(label_mapcode);
		values.addChild(label_mapcode_value);
		
		label_name.place();
		label_rated.place(label_name, BOTTOM_LEFT);
		label_gamespeed.place(label_rated, BOTTOM_LEFT);
		label_terrain_type.place(label_gamespeed, BOTTOM_LEFT);
		label_size.place(label_terrain_type, BOTTOM_LEFT);
		label_hills.place(label_size, BOTTOM_LEFT);
		label_trees.place(label_hills, BOTTOM_LEFT);
		label_supplies.place(label_trees, BOTTOM_LEFT);
		label_mapcode.place(label_supplies, BOTTOM_LEFT);
		types.compileCanvas();
		addChild(types);
		
		label_name_value.place();
		label_rated_value.place(label_name_value, BOTTOM_LEFT);
		label_gamespeed_value.place(label_rated_value, BOTTOM_LEFT);
		label_terrain_type_value.place(label_gamespeed_value, BOTTOM_LEFT);
		label_size_value.place(label_terrain_type_value, BOTTOM_LEFT);
		label_hills_value.place(label_size_value, BOTTOM_LEFT);
		label_trees_value.place(label_hills_value, BOTTOM_LEFT);
		label_supplies_value.place(label_trees_value, BOTTOM_LEFT);
		label_mapcode_value.place(label_supplies_value, BOTTOM_LEFT);
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
