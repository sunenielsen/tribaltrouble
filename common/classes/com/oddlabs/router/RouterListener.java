package com.oddlabs.router;

import java.io.IOException;

public strictfp interface RouterListener {
	void routerFailed(IOException e);
}
