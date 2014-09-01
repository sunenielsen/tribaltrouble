package com.oddlabs.http;

import java.io.InputStream;
import java.io.IOException;

public strictfp interface HttpResponseParser {
	Object parse(InputStream in) throws IOException;
}
