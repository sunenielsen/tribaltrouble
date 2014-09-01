package com.oddlabs.tt.gui;

import com.oddlabs.tt.font.TextLineRenderer;
import com.oddlabs.util.Quad;

public final strictfp class ToolTipBox extends TextField {
	public final static float MAX_DELAY_SECONDS = 1.5f;
		
	private final TextLineRenderer tool_tip_renderer;
	private Quad[] icons;
	
	public ToolTipBox() {
		super(Skin.getSkin().getEditFont(), 1000);
		tool_tip_renderer = new TextLineRenderer(getFont());
	}
	
	protected void renderGeometry() {
		throw new RuntimeException();
	}

	public final void append(Quad[] icons) {
		this.icons = icons;
	}

	public void clear() {
		super.clear();
		icons = null;
	}
	
	public final void render(int center_x, int top_y, int clip_left, int clip_right, int clip_bottom, int clip_top) {
		if (getText().length() == 0)
			return;
		ToolTipBoxInfo box = Skin.getSkin().getToolTipInfo();
		int text_width = getFont().getWidth(getText());
		int box_width = text_width + box.getLeftOffset() + box.getRightOffset();
		int box_height = box.getBox().getHeight();
		if (icons != null) {
			int i;
			for (i = 0; i < icons.length; i++) {
				box_width += icons[i].getWidth()/3;
			}
			box_width += icons[i - 1].getWidth()*2/3;
		}
		int x = center_x - box_width/2;
		int y = top_y - box_height;
		if (x < 0)
			x = 0;
		if (x > LocalInput.getViewWidth() - box_width)
			x = LocalInput.getViewWidth() - box_width;
		if (y < 0)
			y = 0;
		if (y > LocalInput.getViewWidth() - box_height)
			y = LocalInput.getViewWidth() - box_height;
		

		box.getBox().render(x, y, box_width, Skin.NORMAL);

		clip_left = StrictMath.max(clip_left, x + box.getLeftOffset());
		clip_right = StrictMath.min(clip_right, x + box.getLeftOffset() + text_width);
		tool_tip_renderer.render(x + box.getLeftOffset(), y + box.getBottomOffset(), clip_left, clip_right, clip_bottom, clip_top, getText());
		if (icons != null) {
			int render_x = box_width - box.getRightOffset() - icons[icons.length - 1].getWidth();
			for (int i = 0; i < icons.length; i++) {
				Quad icon = icons[i];
				icon.render(x + render_x, y + (box_height - icon.getHeight())/2);
				render_x -= icon.getWidth()/3;
			}
		}
	}
}
