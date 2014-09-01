package com.oddlabs.tt.render;

import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.particle.Emitter;
import com.oddlabs.tt.particle.Lightning;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.viewer.Selection;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.model.*;
import com.oddlabs.tt.util.*;

import java.util.*;

final strictfp class ElementRenderer implements ElementNodeVisitor {
	private final RenderState render_state;
	private final boolean picking;
	private CameraState camera;

	private boolean visible_override;

	ElementRenderer(Player local_player, LandscapeRenderer renderer, RenderQueues render_queues, Picker picker, boolean picking, SpriteSorter sprite_sorter, Selection selection) {
		this.picking = picking;
		this.render_state = new RenderState(local_player, renderer, sprite_sorter, render_queues, picker, selection);
	}

	final RenderState getRenderState() {
		return render_state;
	}

	final void setup(CameraState camera_state) {
		this.camera = camera_state;
		render_state.setup(picking, camera);
	}

	public final void visitNode(ElementNode node) {
		int frustum_state = RenderTools.NOT_IN_FRUSTUM;
		if (visible_override || (frustum_state = RenderTools.inFrustum(node, camera.getFrustum())) >= RenderTools.IN_FRUSTUM) {
			boolean old_override = visible_override;
			visible_override = visible_override || frustum_state == RenderTools.ALL_IN_FRUSTUM;
			node.visitChildren(this);
			node.visitElements(this);
			visible_override = old_override;
		}
	}

	public final void visitLeaf(ElementLeaf leaf) {
		int frustum_state = RenderTools.NOT_IN_FRUSTUM;
		if (visible_override || (frustum_state = RenderTools.inFrustum(leaf, camera.getFrustum())) >= RenderTools.IN_FRUSTUM) {
			boolean old_override = visible_override;
			visible_override = visible_override || frustum_state == RenderTools.ALL_IN_FRUSTUM;
			leaf.visitElements(this);
			visible_override = old_override;
		}
	}

	public final void visit(Element element) {
		int frustum_state = RenderTools.NOT_IN_FRUSTUM;
		if (visible_override || (frustum_state = RenderTools.inFrustum(element, camera.getFrustum())) >= RenderTools.IN_FRUSTUM) {
			boolean old_override = visible_override;
			visible_override = visible_override || frustum_state == RenderTools.ALL_IN_FRUSTUM;
			render_state.setVisibleOverride(visible_override);
			element.visit(render_state);
			visible_override = old_override;
		}
	}
}
