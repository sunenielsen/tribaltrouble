package com.oddlabs.tt.render;

import com.oddlabs.tt.model.Model;

final strictfp class ElementRenderState implements ModelState {
	final RenderState render_state;
	ModelVisitor visitor;
	Model model;
	float f;

	ElementRenderState(RenderState render_state) {
		this.render_state = render_state;
	}

	public final Model getModel() {
		return model;
	}

	public final void transform() {
		visitor.transform(this);
	}

	public final float[] getTeamColor() {
		return visitor.getTeamColor(this);
	}

	public final float[] getSelectionColor() {
		return visitor.getSelectionColor(this);
	}

	final void setup(ModelVisitor visitor, Model model, float f) {
		this.visitor = visitor;
		this.model = model;
		this.f = f;
	}

	final void setup(ModelVisitor visitor, Model model) {
		this.visitor = visitor;
		this.model = model;
	}

	public void markDetailPoint() {
		visitor.markDetailPoint(this);
	}

	public void markDetailPolygon(int index) {
		visitor.markDetailPolygon(this, index);
	}

	public final int getTriangleCount(int index) {
		return visitor.getTriangleCount(this, index);
	}

	public final float getEyeDistanceSquared() {
		return visitor.getEyeDistanceSquared(this);
	}

	final SpriteRenderer getRenderer(SpriteKey key) {
		return render_state.getRenderQueues().getRenderer(key);
	}
}
