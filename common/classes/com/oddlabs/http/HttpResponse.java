package com.oddlabs.http;

import java.io.Serializable;

strictfp interface HttpResponse extends Serializable {
	void notify(HttpCallback callback);
}
