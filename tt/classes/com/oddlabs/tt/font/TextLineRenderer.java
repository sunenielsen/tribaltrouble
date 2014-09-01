package com.oddlabs.tt.font;

import com.oddlabs.util.Quad;

public final strictfp class TextLineRenderer {
	private final Font font;
	private final int dot_limit;

	private int render_x;
	private int render_y;
	private int index_render_x;
	private int target_render_x;
	private int best_dx;
	private int new_index;

	public TextLineRenderer(Font font) {
		this.font = font;
		dot_limit = font.getWidth("...");
	}

	public final int getIndexRenderX(int x, int y, int offset_x, CharSequence text, int index) {
		render(x, y, offset_x, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, text, index, false);
		return index_render_x;
	}

	public final int jumpDirect(int x, int y, int new_x, CharSequence text, int index) {
		target_render_x = new_x;
		best_dx = Integer.MAX_VALUE;
		render(x, y, 0, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, text, index, false);
		return new_index;
	}

	public final void renderCropped(int x, int y, float clip_left, float clip_right, float clip_bottom, float clip_top, CharSequence text) {
		render(x, y, 0, clip_left, clip_right, clip_bottom, clip_top, text, -1, true, true);
	}

	public final void render(int x, int y, float clip_left, float clip_right, float clip_bottom, float clip_top, CharSequence text) {
		render(x, y, 0, clip_left, clip_right, clip_bottom, clip_top, text, -1);
	}

	public final void render(int x, int y, int offset_x, float clip_left, float clip_right, float clip_bottom, float clip_top, CharSequence text, int index) {
		render(x, y, offset_x, clip_left, clip_right, clip_bottom, clip_top, text, index, true, false);
	}

	private final void render(int x, int y, int offset_x, float clip_left, float clip_right, float clip_bottom, float clip_top, CharSequence text, int index, boolean render) {
		render(x, y, offset_x, clip_left, clip_right, clip_bottom, clip_top, text, index, render, false);
	}

	private final void render(int x, int y, int offset_x, float clip_left, float clip_right, float clip_bottom, float clip_top, CharSequence text, int index, boolean render, boolean render_dots) {
		if (render) {
			font.setup();
		}
		int render_pos = 0;
		render_x = x + offset_x;
		render_y = y;
		renderIndex(index, render_pos, render);
		while (render_pos < text.length()) {
			if (render_dots && nearEnd(text, render_pos, (int)clip_right - render_x)) {
				renderDots(render_x, render_y, clip_left, clip_right, clip_bottom, clip_top, render);
				break;
			}
			Quad quad = font.getQuad(text.charAt(render_pos));
			if (render && quad != null)
				quad.renderClipped(render_x, render_y, clip_left, clip_right, clip_bottom, clip_top);
			render_x += getQuadWidth(quad);
			render_pos++;
			renderIndex(index, render_pos, render);
		}
		if (render) {
			font.reset();
		}
	}

	private final void renderDots(int render_x, int render_y, float clip_left, float clip_right, float clip_bottom, float clip_top, boolean render) {
		if (render) {
			Quad quad = font.getQuad('.');
			int dx = getQuadWidth(quad);
			for (int i = 0; i < 3; i++) {
				quad.renderClipped(render_x, render_y, clip_left, clip_right, clip_bottom, clip_top);
				render_x += dx;
			}
		}
	}

	private final int getQuadWidth(Quad quad) {
		if (quad != null) {
			return quad.getWidth() - font.getXBorder();
		} else {
			return 0;
		}
	}

	private final boolean nearEnd(CharSequence text, int render_pos, int available) {
		int length = getQuadWidth(font.getQuad(text.charAt(render_pos)));

		if (length + dot_limit < available) {
			return false;
		}

		render_pos++;
		while (render_pos < text.length()) {
			length += getQuadWidth(font.getQuad(text.charAt(render_pos)));

			if (length > available) {
				return true;
			}
			render_pos++;
		}
		return false;
	}

	private final void renderIndex(int index, int render_pos, boolean render) {
		if (!render) {
			int dx = StrictMath.abs(render_x + font.getXBorder()/2 - target_render_x);
			if (dx < best_dx) {
				best_dx = dx;
				new_index = render_pos;
			}
		}

		if (render_pos == index) {
			if (render) {
				Index.renderIndex(render_x + font.getXBorder()/2, render_y, font);
			} else {
				index_render_x = render_x + font.getXBorder()/2;
			}
		}
	}

	public final Font getFont() {
		return font;
	}
}
