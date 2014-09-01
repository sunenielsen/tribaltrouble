package com.oddlabs.tt.animation;

import com.oddlabs.tt.util.*;

public strictfp interface Animated {
	void animate(float t);
	void updateChecksum(StateChecksum checksum);
}
