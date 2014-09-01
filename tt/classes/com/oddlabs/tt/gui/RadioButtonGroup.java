package com.oddlabs.tt.gui;

import java.util.*;

public final strictfp class RadioButtonGroup {
	private List buttons;

	public RadioButtonGroup() {
		buttons = new ArrayList();
	}

	public final void mark(RadioButtonGroupElement button) {
		RadioButtonGroupElement marked = getMarked();
		if (marked != null)
			marked.setMarked(false);
		button.setMarked(true);
	}

	public final void add(RadioButtonGroupElement button) {
		buttons.add(button);
//		button.setMarked(false);
	}

	public final RadioButtonGroupElement getMarked() {
		for (int i = 0; i < buttons.size(); i++) {
			if (((RadioButtonGroupElement)buttons.get(i)).isMarked())
				return (RadioButtonGroupElement)buttons.get(i);
		}
		return null;
	}
}
