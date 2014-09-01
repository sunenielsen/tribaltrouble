package com.oddlabs.tt.render;

import com.oddlabs.geometry.LowDetailModel;

final strictfp class Tree {
	private final SpriteList crown;
	private final SpriteList trunk;

	public Tree(SpriteList trunk, SpriteList crown) {
		this.trunk = trunk;
		this.crown = crown;
	}

	public final SpriteList getTrunk() {
		return trunk;
	}

	public final SpriteList getCrown() {
		return crown;
	}

	public final boolean equals(Object other) {
		if (!(other instanceof Tree))
			return false;
		Tree other_tree = (Tree)other;
		return crown == other_tree.crown && trunk == other_tree.trunk;
	}
}
