package com.oddlabs.tt.net;

import com.oddlabs.tt.viewer.WorldViewer;

public strictfp interface WorldInitAction {
	void run(WorldViewer viewer);
}
