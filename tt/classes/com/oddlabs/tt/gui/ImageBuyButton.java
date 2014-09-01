package com.oddlabs.tt.gui;

import java.util.ResourceBundle;
import com.oddlabs.tt.util.Utils;

public final strictfp class ImageBuyButton extends ImageButton {
	private final static ResourceBundle bundle = ResourceBundle.getBundle(ImageBuyButton.class.getName());
	public ImageBuyButton(GUIRoot gui_root) {
		this(gui_root, Utils.getBundleString(bundle, "image_path"));
	}
	
	private ImageBuyButton(GUIRoot gui_root, String path) {
		super(new GUIImage(256, 64, 0f, .5f, 1f, 1f, path), new GUIImage(256, 64, 0f, 0f, 1f, .5f, path), new GUIImage(256, 64, 0f, 0f, 1f, .5f, path));
		BuyButton.addListener(gui_root, this);
	}
}
