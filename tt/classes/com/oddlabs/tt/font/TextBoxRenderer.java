package com.oddlabs.tt.font;

import com.oddlabs.util.Quad;

public final strictfp class TextBoxRenderer {
	private final Font font;

	private int width;
	private int height;
	private int render_x;
	private int render_y;
	private int text_height;
	private boolean in_view;

	// Variables for moving index positions over lines
	private int index_render_x;
	private int index_render_y;
	private int target_render_x;
	private int target_render_y;
	private int best_dx;
	private int best_dy;
	private int new_index;

	public TextBoxRenderer(Font font, int width, int height) {
		this.font = font;
		setDim(width, height);
	}

	public final void setDim(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public final int jump(int x, int y, int dx, int dy, CharSequence text, int index) {
		render(x, y, 0, text, index, false);
		return jumpDirect(x, y, index_render_x + dx, index_render_y + dy, text, index);
	}

	public final int jumpDirect(int x, int y, int new_x, int new_y, CharSequence text, int index) {
		target_render_x = new_x;
		target_render_y = new_y;
		best_dx = Integer.MAX_VALUE;
		best_dy = Integer.MAX_VALUE;
		render(x, y, 0, text, index, false);
		return new_index;
	}

	public final int getIndexRenderY(int x, int y, int offset_y, CharSequence text, int index) {
		render(x, y, offset_y, text, index, false);
		return index_render_y;
	}

	public final int getTextHeight(CharSequence text) {
		render(0, -height, 0, text, text.length(), false);
		return -index_render_y;
	}

	public final int getTotalTextHeight(CharSequence text) {
		render(0, -height, 0, text, text.length(), false);
		return text_height;
	}

	public final void render(int x, int y, CharSequence text) {
		render(x, y, 0, text, -1);
	}

	public final void render(int x, int y, int offset_y, CharSequence text) {
		render(x, y, offset_y, text, -1);
	}

	public final void render(int x, int y, int offset_y, CharSequence text, int index) {
		render(x, y, offset_y, text, index, true);
	}

	private final void render(int x, int y, int offset_y, CharSequence text, int index, boolean render) {
		int clip_left = x;
		int clip_bottom = y;
		int clip_right = x + width;
		int clip_top = y + height;

		text_height = 0;
		if (render) {
			font.setup();
		}
		int render_pos = 0;
		render_y = clip_top + offset_y;
		render_x = clip_left - font.getXBorder()/2;
		render_y -= font.getHeight();
		in_view = render_y < clip_top && render_y > clip_bottom - font.getHeight();
		text_height += font.getHeight() - font.getYBorder();
		
		renderIndex(index, render_pos, render);
		while (render_pos < text.length()) {
			int word_width = getWordWidth(render_pos, text);
			render_pos = renderWord(render_pos, word_width, clip_left, clip_bottom, clip_right, clip_top, text, index, render);
			render_pos = renderSpace(render_pos, clip_left, clip_bottom, clip_right, clip_top, text, index, render);
		}
		if (render) {
			font.reset();
		}
	}

	private final void newLine(int clip_left, int clip_bottom, int clip_top) {
		render_x = clip_left - font.getXBorder()/2;
		render_y -= font.getHeight() - font.getYBorder();
		in_view = render_y < clip_top && render_y > clip_bottom - font.getHeight();
		text_height += font.getHeight() - font.getYBorder();
	}

	private final int getWordWidth(int render_pos, CharSequence text) {
		int width = 0;
		while (render_pos < text.length()) {
			char key = text.charAt(render_pos);
			if (key == ' ' || key == '\n')
				break;
			Quad quad = font.getQuad(key);
			if (quad != null)
				width += quad.getWidth() - font.getXBorder();
			render_pos++;
		}
		return width;
	}

	private int renderWord(int render_pos,
						   int word_width,
						   int clip_left,
						   int clip_bottom,
						   int clip_right,
						   int clip_top,
						   CharSequence text,
						   int index,
						   boolean render) {
		while (word_width > 0) {
			char key = text.charAt(render_pos);
			Quad quad = font.getQuad(key);
			int dx = 0;
			if (quad != null) {
				int width = quad.getWidth();
				if (render_x - font.getXBorder()/2 + width >= clip_right)
					newLine(clip_left, clip_bottom, clip_top);
				if (render && in_view)
					quad.renderClipped(render_x, render_y, clip_left, clip_right, clip_bottom, clip_top);
				dx = width - font.getXBorder();
			}
			render_x += dx;
			word_width -= dx;
			render_pos++;
			renderIndex(index, render_pos, render);
		}
		return render_pos;
	}

	private final int renderSpace(int render_pos, int clip_left, int clip_bottom, int clip_right, int clip_top, CharSequence text, int index, boolean render) {
		if (render_pos < text.length()) {
			char key = text.charAt(render_pos++);

			if (key == '\n') {
				newLine(clip_left, clip_bottom, clip_top);
			} else {
				Quad quad = font.getQuad(key);
				if (quad != null) {
					int width = quad.getWidth();
					int next_word_width = getWordWidth(render_pos, text);
					if (render_x - font.getXBorder()/2 + width + next_word_width < clip_right) {
						if (render)
							quad.render(render_x, render_y);
						render_x += width - font.getXBorder();
					} else {
						newLine(clip_left, clip_bottom, clip_top);
					}
				}
			}
			renderIndex(index, render_pos, render);
		}
		return render_pos;
	}

	private final void renderIndex(int index, int render_pos, boolean render) {
		if (!render) {
			int dx = StrictMath.abs(render_x + font.getXBorder()/2 - target_render_x);
			int dy = StrictMath.abs(render_y - target_render_y);
			if (dy < best_dy || dy == best_dy && dx < best_dx) {
				best_dx = dx;
				best_dy = dy;
				new_index = render_pos;
			}
		}

		if (render_pos == index) {
			if (render) {
				Index.renderIndex(render_x + font.getXBorder()/2, render_y, font);
			} else {
				index_render_x = render_x + font.getXBorder()/2;
				index_render_y = render_y;
			}
		}
	}

	public final int getWidth() {
		return width;
	}
}
