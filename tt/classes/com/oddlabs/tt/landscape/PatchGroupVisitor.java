package com.oddlabs.tt.landscape;

public strictfp interface PatchGroupVisitor {
	void visitGroup(PatchGroup group);
	void visitLeaf(LandscapeLeaf leaf);
}
