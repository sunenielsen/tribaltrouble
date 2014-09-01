package com.oddlabs.tt.landscape;

import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.util.StrictMatrix4f;

public final strictfp class TreeLeaf extends AbstractTreeGroup {
	private TreeSupply[] infos = new TreeSupply[0];

	public TreeLeaf(AbstractTreeGroup parent) {
		super(parent);
	}

	final void insertTree(TreeSupply tree) {
		TreeSupply[] new_infos = new TreeSupply[infos.length + 1];
		System.arraycopy(infos, 0, new_infos, 0, infos.length);
		new_infos[new_infos.length - 1] = tree;
		infos = new_infos;
	}

	protected final boolean initBounds() {
		if (infos.length != 0) {
			TreeSupply info = infos[0];
			info.initBounds();
			setBounds(info);
			for (int i = 1; i < infos.length; i++) {
				info = infos[i];
				info.initBounds();
				checkBounds(info);
			}
			super.initBounds();
			return true;
		}
		return false;
	}

	public final void visit(TreeNodeVisitor visitor) {
		visitor.visitLeaf(this);
	}

	public final void visitTrees(TreeNodeVisitor visitor) {
		for (int i = 0; i < infos.length; i++) {
			TreeSupply info = infos[i];
			visitor.visitTree(info);
		}
	}
}
