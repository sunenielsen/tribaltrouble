package com.oddlabs.tt.gui;

import com.oddlabs.tt.guievent.*;

public final strictfp class CancelListener implements MouseClickListener {
	private final Form form;
	
	public CancelListener(Form form) {
		this.form = form;
	}
	
	public final void mouseClicked(int button, int x, int y, int clicks) {
		form.cancel();
	}
}
