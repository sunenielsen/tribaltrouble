package com.oddlabs.tt.render;

import com.oddlabs.tt.model.Model;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.util.BoundingBox;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.net.PeerHub;

strictfp class SelectableVisitor extends ModelVisitor {
	private final static float[] COLOR_RED = {1f, 0f, 0f};
	private final static float[] COLOR_RED_HOVER = {.7f, 0f, 0f};
	private final static float[] COLOR_GREEN = {0f, 1f, 0f};
	private final static float[] COLOR_GREEN_HOVER = {0f, .7f, 0f};
	private final static float[] COLOR_BLUE = {0f, 0f, 1f};
	private final static float[] COLOR_BLUE_HOVER = {0f, 0f, .7f};

	public final void transform(ElementRenderState render_state) {
		Model model = render_state.model;
		RenderTools.translateAndRotate(model.getPositionX(), model.getPositionY(), render_state.f, model.getDirectionX(), model.getDirectionY());
	}

	static float[] getTeamColor(Selectable model) {
		return model.getOwner().getColor();
	}

	public final float[] getTeamColor(ElementRenderState render_state) {
		return getTeamColor((Selectable)render_state.getModel());
	}

	public final float[] getSelectionColor(ElementRenderState render_state) {
		Player local_player = render_state.render_state.getLocalPlayer();
		Selectable model = (Selectable)render_state.getModel();
		if (render_state.render_state.isSelected(model)) {
			if (model.getOwner() == local_player)
				return COLOR_GREEN;
			else if (local_player.isEnemy(model.getOwner()))
				return COLOR_RED;
			else
				return COLOR_BLUE;
		} else if (render_state.render_state.isHovered(model)) {
			if (model.getOwner() == local_player)
				return COLOR_GREEN_HOVER;
			else if (local_player.isEnemy(model.getOwner()))
				return COLOR_RED_HOVER;
			else
				return COLOR_BLUE_HOVER;
		} else
			return model.getOwner().getColor();
	}

	public final void markDetailPoint(ElementRenderState render_state) {
		Selectable selectable = (Selectable)render_state.model;
		if (!selectable.isDead())
			super.markDetailPoint(render_state);
	}
}
