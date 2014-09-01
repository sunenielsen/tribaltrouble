package com.oddlabs.tt.render;

import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.util.BoundingBox;
import com.oddlabs.tt.model.Model;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.geometry.LowDetailModel;
import com.oddlabs.tt.resource.SpriteFile;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.landscape.*;

import java.util.List;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

strictfp class TreePicker implements TreeNodeVisitor {
	private final static int CROWN_MIPMAP_CUTOFF = Globals.NO_MIPMAP_CUTOFF;
	private final static float SELECTION_RADIUS = 1.5f;

	private final List[] render_lists;
	private final List[] respond_render_lists;
	private final List low_detail_render_list = new ArrayList();

	private final BoundingBox picking_selection_box = new BoundingBox();
	private final SpriteSorter sprite_sorter;
	private final RenderStateCache render_state_cache;
	private final Tree[] trees;
	private final LowDetailModel[] tree_low_details;
	private final RespondManager respond_manager;
	private CameraState camera;

	private boolean visible_override;

	TreePicker(SpriteSorter sprite_sorter, RespondManager respond_manager) {
		this.respond_manager = respond_manager;
		this.render_lists = new List[]{new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList()};
		this.respond_render_lists = new List[]{new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList()};
		this.trees = loadTrees();
		this.tree_low_details = LandscapeResources.loadTreeLowDetails();

		this.sprite_sorter = sprite_sorter;
		render_state_cache = new RenderStateCache(new RenderStateFactory() {
			public final Object create() {
				return new TreeRenderState(TreePicker.this);
			}
		});
	}

	private static Tree[] loadTrees() {
		SpriteList jungle_crown = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/jungle_tree_crown.binsprite", CROWN_MIPMAP_CUTOFF, false, false, true, false));
		SpriteList jungle_trunk = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/jungle_tree_trunk.binsprite", CROWN_MIPMAP_CUTOFF, true, true, true, false));
		
		SpriteList palm_crown = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/palm_crown.binsprite", CROWN_MIPMAP_CUTOFF, false, false, true, false));
		SpriteList palm_trunk = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/palm_trunk.binsprite", CROWN_MIPMAP_CUTOFF, true, true, true, false));

		SpriteList oak_crown = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/oak_tree_crown.binsprite", CROWN_MIPMAP_CUTOFF, false, false, true, false));
		SpriteList oak_trunk = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/oak_tree_trunk.binsprite", CROWN_MIPMAP_CUTOFF, true, true, true, false));

		SpriteList pine_crown = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/pine_tree_crown.binsprite", CROWN_MIPMAP_CUTOFF, false, false, true, false));
		SpriteList pine_trunk = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/pine_tree_trunk.binsprite", CROWN_MIPMAP_CUTOFF, true, true, true, false));

		Tree[] trees = new Tree[]{
			new Tree(jungle_trunk, jungle_crown),
			new Tree(palm_trunk, palm_crown),
			new Tree(oak_trunk, oak_crown),
			new Tree(pine_trunk, pine_crown)
		};
		return trees;
	}

	final Tree[] getTrees() {
		return trees;
	}

	final LowDetailModel[] getLowDetails() {
		return tree_low_details;
	}

	protected final List getLowDetailRenderList() {
		return low_detail_render_list;
	}

	protected final List[] getRenderLists() {
		return render_lists;
	}

	protected final List[] getRespondRenderLists() {
		return respond_render_lists;
	}

	public final void getAllPicks(List pick_list) {
		for (int i = 0; i < render_lists.length; i++)
			getAllPicksFromRenderList(render_lists[i], pick_list);
		for (int i = 0; i < respond_render_lists.length; i++)
			getAllPicksFromRenderList(respond_render_lists[i], pick_list);
	}

	private final void getAllPicksFromRenderList(List render_list, List pick_list) {
		for (int i = 0; i < render_list.size(); i++) {
			TreeSupply group = (TreeSupply)render_list.get(i);
			render_list.set(i, null);
			pick_list.add(group);
		}
		render_list.clear();
	}

	private void addToHighDetailList(int index, TreeSupply tree, boolean respond) {
		if (respond) {
			respond_render_lists[index].add(tree);
		} else {
			render_lists[index].add(tree);
		}
	}

	public final void markDetailPolygon(TreeSupply tree_supply, int level) {
		if (level == SpriteRenderer.HIGH_POLY || tree_supply.hasRespondingTrees()) {
			addToHighDetailList(tree_supply.getTreeTypeIndex(), tree_supply, respond_manager.isResponding(tree_supply));
		} else
			addToLowDetailRenderList(tree_supply);
	}

	public final void addToLowDetailRenderList(AbstractTreeGroup node) {
		low_detail_render_list.add(node);
	}

	public final void setup(CameraState camera_state) {
		this.camera = camera_state;
		render_state_cache.clear();
	}

	public final void visitLeaf(TreeLeaf tree_leaf) {
		int frustum_state = RenderTools.NOT_IN_FRUSTUM;
		if (tree_leaf.hasTrees() && (visible_override || (frustum_state = RenderTools.inFrustum(tree_leaf, camera.getFrustum())) >= RenderTools.IN_FRUSTUM)) {
			boolean old_override = visible_override;
			visible_override = visible_override || frustum_state == RenderTools.ALL_IN_FRUSTUM;
			if (visible_override && canRenderLowDetail(tree_leaf)) {
				addToLowDetailRenderList(tree_leaf);
			} else {
				tree_leaf.visitTrees(this);
			}
			visible_override = old_override;
		}
	}

	public final void visitNode(TreeGroup tree_group) {
		int frustum_state = RenderTools.NOT_IN_FRUSTUM;
		if (tree_group.hasTrees() && (visible_override || (frustum_state = RenderTools.inFrustum(tree_group, camera.getFrustum())) >= RenderTools.IN_FRUSTUM)) {
			boolean old_override = visible_override;
			visible_override = visible_override || frustum_state == RenderTools.ALL_IN_FRUSTUM;
			if (visible_override && canRenderLowDetail(tree_group))
				addToLowDetailRenderList(tree_group);
			else
				tree_group.visitChildren(this);
			visible_override = old_override;
		}
	}

	private float getHeightScale(int tree_type_index) {
		switch (tree_type_index) {
			case AbstractTreeGroup.TREE_INDEX:
				return .9f;
			case AbstractTreeGroup.PALMTREE_INDEX:
				return .95f;
			case AbstractTreeGroup.OAKTREE_INDEX:
				return .7f;
			case AbstractTreeGroup.PINETREE_INDEX:
				return .65f;
			default:
				throw new RuntimeException("Illegal enum: " + tree_type_index);
		}
	}

	private boolean pickingInFrustum(TreeSupply tree_supply, float[][] frustum) {
		picking_selection_box.setBounds(-SELECTION_RADIUS + tree_supply.getPositionX(), SELECTION_RADIUS + tree_supply.getPositionX(), -SELECTION_RADIUS + tree_supply.getPositionY(), SELECTION_RADIUS + tree_supply.getPositionY(), tree_supply.bmin_z, tree_supply.bmin_z + (tree_supply.bmax_z - tree_supply.bmin_z)*getHeightScale(tree_supply.getTreeTypeIndex()));
		return RenderTools.inFrustum(picking_selection_box, frustum) >= RenderTools.IN_FRUSTUM;
	}

	private void addToRenderList(TreeSupply tree, CameraState camera) {
		if (isPicking())
			markDetailPolygon(tree, SpriteRenderer.HIGH_POLY);
		else
			sprite_sorter.add(getRenderState(tree), camera, false);
	}

	private final LODObject getRenderState(TreeSupply tree_supply) {
		TreeRenderState render_state = (TreeRenderState)render_state_cache.get();
		render_state.setup(tree_supply);
		return render_state;
	}

	public final void visitTree(TreeSupply tree_supply) {
		if (tree_supply.isHidden())
			return;
		boolean in_view;
		if (isPicking())
			in_view = !tree_supply.isDead() && (visible_override || pickingInFrustum(tree_supply, camera.getFrustum()));
		else
			in_view = visible_override || RenderTools.inFrustum(tree_supply, camera.getFrustum()) >= RenderTools.IN_FRUSTUM;
		if (in_view) {
			if (canRenderLowDetail(tree_supply)) {
				addToLowDetailRenderList(tree_supply);
			} else {
				addToRenderList(tree_supply, camera);
			}
		}
	}

	private boolean canRenderLowDetail(AbstractTreeGroup tree_group) {
		return !isPicking() && !tree_group.hasRespondingTrees() && isLowDetailDistance(tree_group);
	}

	private boolean isLowDetailDistance(AbstractTreeGroup tree_group) {
		float eye_dist = RenderTools.getEyeDistanceSquared(tree_group, camera.getCurrentX(), camera.getCurrentY(), camera.getCurrentZ());
		return eye_dist >= tree_group.getGroupMinSquared();
	}

	boolean isPicking() {
		return true;
	}

	final CameraState getCamera() {
		return camera;
	}
}
