package com.oddlabs.tt.guievent;

public strictfp interface MouseClickListener extends EventListener {
	public void mouseClicked(int button, int x, int y, int clicks);
}
