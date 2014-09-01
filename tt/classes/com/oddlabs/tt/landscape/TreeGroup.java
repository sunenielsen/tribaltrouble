package com.oddlabs.tt.landscape;

import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.util.StrictMatrix4f;

public final strictfp class TreeGroup extends AbstractTreeGroup {
	private final static int LANDSCAPE_TREES_MAX_LEVEL = 5;

	/*
	 * child2 | child3
	 * ----------------
	 * child0 | child1
	 *
	 */
	private final AbstractTreeGroup child0;
	private final AbstractTreeGroup child1;
	private final AbstractTreeGroup child2;
	private final AbstractTreeGroup child3;


	public TreeGroup(AbstractTreeGroup parent, int level) {
		super(parent);
		child0 = createChild(level);
		child1 = createChild(level);
		child2 = createChild(level);
		child3 = createChild(level);
	}

	private final AbstractTreeGroup createChild(int level) {
		if (level < LANDSCAPE_TREES_MAX_LEVEL) {
			return new TreeGroup(this, level + 1);
		} else {
			return new TreeLeaf(this);
		}
	}

	public final void visit(TreeNodeVisitor visitor) {
		visitor.visitNode(this);
	}

	public final AbstractTreeGroup getChild0() {
		return child0;
	}

	public final AbstractTreeGroup getChild1() {
		return child1;
	}

	public final AbstractTreeGroup getChild2() {
		return child2;
	}

	public final AbstractTreeGroup getChild3() {
		return child3;
	}

	public final void visitChildren(TreeNodeVisitor visitor) {
		child0.visit(visitor);
		child1.visit(visitor);
		child2.visit(visitor);
		child3.visit(visitor);
	}

	protected final boolean initBounds() {
		boolean child0_bounds = child0.initBounds();
		boolean child1_bounds = child1.initBounds();
		boolean child2_bounds = child2.initBounds();
		boolean child3_bounds = child3.initBounds();
		boolean node_bounds = false;
		node_bounds = checkBounds(child0, child0_bounds, node_bounds);
		node_bounds = checkBounds(child1, child1_bounds, node_bounds);
		node_bounds = checkBounds(child2, child2_bounds, node_bounds);
		node_bounds = checkBounds(child3, child3_bounds, node_bounds);
		super.initBounds();
		return node_bounds;
	}

	private final boolean checkBounds(AbstractTreeGroup child, boolean child_bounds, boolean node_bounds) {
		if (!child_bounds)
			return node_bounds;
		if (node_bounds)
			checkBounds(child);
		else
			setBounds(child);
		return true;
	}
}
