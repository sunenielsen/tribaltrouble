package com.oddlabs.converter2;

import org.xml.sax.*;

public final strictfp class GeometryErrorHandler implements ErrorHandler {
	public final void fatalError(SAXParseException exception) {
		// ignore fatal errors (an exception is guaranteed)
	}

	// treat validation errors as fatal
	public void error(SAXParseException e) throws SAXParseException {
		throw e;
	}

	// dump warnings too
	public final void warning(SAXParseException err) {
		System.out.println ("** Warning, line " + err.getLineNumber () + ", uri " + err.getSystemId ());
		System.out.println("   " + err.getMessage ());
	}
}

