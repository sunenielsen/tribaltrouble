package com.oddlabs.tt.guievent;

public strictfp interface MouseButtonListener extends MouseClickListener {
	public void mousePressed(int button, int x, int y);
	public void mouseReleased(int button, int x, int y);
	public void mouseHeld(int button, int x, int y);
}
