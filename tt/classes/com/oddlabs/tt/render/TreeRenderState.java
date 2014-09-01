package com.oddlabs.tt.render;

import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.landscape.LandscapeResources;
import com.oddlabs.tt.camera.CameraState;

final strictfp class TreeRenderState implements LODObject {
	private final TreePicker tree_renderer;
	private TreeSupply tree_supply;

	TreeRenderState(TreePicker tree_renderer) {
		this.tree_renderer = tree_renderer;
	}

	final void setup(TreeSupply tree_supply) {
		this.tree_supply = tree_supply;
	}

	public final void markDetailPoint() {
		tree_renderer.addToLowDetailRenderList(tree_supply);
	}

	public final void markDetailPolygon(int level) {
		tree_renderer.markDetailPolygon(tree_supply, level);
	}

	public final int getTriangleCount(int index) {
		Tree tree = tree_renderer.getTrees()[tree_supply.getTreeTypeIndex()];
		if (index == SpriteRenderer.HIGH_POLY) 
			return tree.getTrunk().getSprite(0).getTriangleCount() + tree.getCrown().getSprite(0).getTriangleCount();
		else if (index == SpriteRenderer.LOW_POLY)
			return tree_renderer.getLowDetails()[tree_supply.getTreeTypeIndex()].getPolyCount();
		else
			throw new RuntimeException();
	}

	public final float getEyeDistanceSquared() {
		CameraState camera = tree_renderer.getCamera();
		return RenderTools.getEyeDistanceSquared(tree_supply, camera.getCurrentX(), camera.getCurrentY(), camera.getCurrentZ());
	}
}
