package com.oddlabs.tt.render;

import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.util.BoundingBox;
import com.oddlabs.tt.model.Model;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.viewer.Cheat;
import com.oddlabs.tt.landscape.*;

import java.util.List;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

public final strictfp class TreeRenderer extends TreePicker {
	private final TreeLowDetail tree_low_detail;
	private final WaveAnimation wave_animation = new WaveAnimation();
	private final Cheat cheat;

	TreeRenderer(World world, Cheat cheat, int terrain_type, List tree_positions, List palm_tree_positions, SpriteSorter sprite_sorter, RespondManager respond_manager) {
		super(sprite_sorter, respond_manager);
		this.cheat = cheat;
		this.tree_low_detail = new TreeLowDetail(world, getTrees(), getLowDetails(), tree_positions, palm_tree_positions, terrain_type);
		tree_low_detail.build(world.getTreeRoot());
	}

	public final TreeLowDetail getLowDetail() {
		return tree_low_detail;
	}

	private void renderLowDetail(AbstractTreeGroup group) {
		tree_low_detail.renderLowDetail(group.getLowDetailStart(), group.getLowDetailCount());
	}

	final void renderAll() {
		wave_animation.setTime(LocalEventQueue.getQueue().getTime());
		List low_detail_render_list = getLowDetailRenderList();
		tree_low_detail.setupTrees();
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, .3f);
		GL11.glDisable(GL11.GL_CULL_FACE);
		for (int i = 0; i < low_detail_render_list.size(); i++) {
			AbstractTreeGroup group = (AbstractTreeGroup)low_detail_render_list.get(i);
			low_detail_render_list.set(i, null);
			if (Globals.draw_trees && cheat.draw_trees)
				renderLowDetail(group);
		}
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		low_detail_render_list.clear();
		List[] render_lists = getRenderLists();
		List[] respond_render_lists = getRespondRenderLists();
		for (int i = 0; i < render_lists.length; i++)
			renderList(tree_low_detail.getTrees()[i], render_lists[i], false);
		for (int i = 0; i < respond_render_lists.length; i++)
			if (respond_render_lists[i].size() > 0)
				renderList(tree_low_detail.getTrees()[i], respond_render_lists[i], true);
	}

	private void loadMatrix(TreeSupply tree) {
		tree_low_detail.loadMatrix(tree.getMatrix());
		if (tree.isEmpty()) {
			float time = tree.getTreeFallProgress();
			GL11.glTranslatef(0f, 0f, -13f*(time*time*time*time*time*time));
			GL11.glRotatef(90f*time*time, 1f, 0f, 0f);
		} else {
			float scale = tree.getScale();
			GL11.glScalef(scale, scale, scale);
			wave_animation.mulRotation();
		}
	}

	private final void renderList(Tree tree, List render_list, boolean respond) {
		tree.getCrown().getSprite(0).setup(0, respond);
		for (int i = 0; i < render_list.size(); i++) {
			TreeSupply group = (TreeSupply)render_list.get(i);
			if (Globals.isBoundsEnabled(Globals.BOUNDING_PLAYERS))
				RenderTools.draw(group);
			if (Globals.draw_trees && cheat.draw_trees) {
				GL11.glPushMatrix();
				loadMatrix(group);
				tree.getCrown().render(0, 0, 0);
				GL11.glPopMatrix();
			}
		}
		tree.getCrown().reset(0, respond, false);
		tree.getTrunk().getSprite(0).setup(0, respond);
		for (int i = 0; i < render_list.size(); i++) {
			TreeSupply group = (TreeSupply)render_list.get(i);
			render_list.set(i, null);
			if (Globals.draw_trees && cheat.draw_trees) {
				GL11.glPushMatrix();
				loadMatrix(group);
				tree.getTrunk().render(0, 0, 0);
				GL11.glPopMatrix();
			}
		}
		tree.getTrunk().reset(0, respond, false);
		render_list.clear();
	}

	final boolean isPicking() {
		return false;
	}
}
