package com.oddlabs.tt.guievent;

public strictfp interface MouseMotionListener extends EventListener {
	public void mouseDragged(int button, int x, int y, int rel_x, int rel_y, int abs_x, int abs_y);
	public void mouseMoved(int x, int y);
	public void mouseEntered();
	public void mouseExited();
}
