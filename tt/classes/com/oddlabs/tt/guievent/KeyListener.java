package com.oddlabs.tt.guievent;

import com.oddlabs.tt.gui.KeyboardEvent;

public strictfp interface KeyListener extends EventListener {
	public void keyPressed(KeyboardEvent event);
	public void keyReleased(KeyboardEvent event);
	public void keyRepeat(KeyboardEvent event);
}
