package com.oddlabs.tt.gui;

import java.util.ResourceBundle;
import com.oddlabs.tt.util.Utils;

public final strictfp class OKButton extends HorizButton {
	public OKButton(int width) {
		super(Utils.getBundleString(ResourceBundle.getBundle(OKButton.class.getName()), "ok"), width);
	}
}
