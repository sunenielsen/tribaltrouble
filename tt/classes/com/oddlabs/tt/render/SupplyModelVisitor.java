package com.oddlabs.tt.render;

import com.oddlabs.tt.model.Model;
import com.oddlabs.tt.model.SupplyModel;

abstract strictfp class SupplyModelVisitor extends WhiteModelVisitor {
	public final void markDetailPoint(ElementRenderState render_state) {
		markDetailPolygon(render_state, SpriteRenderer.LOW_POLY);
	}
}
