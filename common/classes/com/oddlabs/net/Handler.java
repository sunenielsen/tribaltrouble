package com.oddlabs.net;

import java.io.IOException;

strictfp interface Handler {
	public void handle() throws IOException;
	public void handleError(IOException e) throws IOException;
}
