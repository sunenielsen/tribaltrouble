package com.oddlabs.http;

import java.io.IOException;

public strictfp interface HttpCallback {
	void success(Object result);
	void error(int error_code, String error_message);
	void error(IOException e);
}
