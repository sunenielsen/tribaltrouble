package com.oddlabs.tt.net;

public strictfp interface ProfileListener {
	public void success();
	public void error(int error_code);
}
