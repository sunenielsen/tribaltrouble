package com.oddlabs.tt.gui;

import java.util.ResourceBundle;
import com.oddlabs.tt.util.Utils;

public final strictfp class CancelButton extends HorizButton {
	public CancelButton(int width) {
		super(Utils.getBundleString(ResourceBundle.getBundle(CancelButton.class.getName()), "cancel"), width);
	}
}
