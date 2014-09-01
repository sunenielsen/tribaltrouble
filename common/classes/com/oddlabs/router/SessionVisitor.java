package com.oddlabs.router;

strictfp interface SessionVisitor {
	void visit(RouterClient client);
}
