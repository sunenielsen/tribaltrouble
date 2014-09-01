package com.oddlabs.http;

strictfp final class OkResponse implements HttpResponse {
	private final Object result;

	OkResponse(Object result) {
		this.result = result;
	}

	public final void notify(HttpCallback callback) {
		callback.success(result);
	}
}
