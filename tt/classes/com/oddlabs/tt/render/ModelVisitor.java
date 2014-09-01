package com.oddlabs.tt.render;

import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.model.Model;

abstract strictfp class ModelVisitor {
	public void markDetailPoint(ElementRenderState render_state) {
		Model model = render_state.model;
		render_state.getRenderer(model.getSpriteRenderer()).addToNoDetailList(render_state);
	}

	public void markDetailPolygon(ElementRenderState render_state, int index) {
		Model model = render_state.model;
		render_state.getRenderer(model.getSpriteRenderer()).addToRenderList(index, render_state, render_state.render_state.isResponding(model));
	}

	public final int getTriangleCount(ElementRenderState render_state, int index) {
		Model model = render_state.model;
		return render_state.getRenderer(model.getSpriteRenderer()).getTriangleCount(index);
	}

	public final float getEyeDistanceSquared(ElementRenderState render_state) {
		Model model = render_state.model;
		CameraState camera = render_state.render_state.getCamera();
		return RenderTools.getEyeDistanceSquared(model, camera.getCurrentX(), camera.getCurrentY(), camera.getCurrentZ());
	}

	public abstract void transform(ElementRenderState render_state);
	public abstract float[] getTeamColor(ElementRenderState render_state);
	public abstract float[] getSelectionColor(ElementRenderState render_state);
}
