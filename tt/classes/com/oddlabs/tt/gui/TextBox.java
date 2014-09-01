package com.oddlabs.tt.gui;

import com.oddlabs.tt.font.Font;
import com.oddlabs.tt.font.TextBoxRenderer;

public strictfp class TextBox extends TextField implements Scrollable {
	private final TextBoxRenderer text_renderer;
	private final ScrollBar scroll_bar;

	private int offset_y;

	public TextBox(int width, int height, Font font, int max_chars) {
		super(font, max_chars);
		setDim(width, height);
		setCanFocus(true);
		offset_y = 0;

		Box edit_box = Skin.getSkin().getEditBox();
		scroll_bar = new ScrollBar(height, this);

		text_renderer = new TextBoxRenderer(font,
											width - edit_box.getLeftOffset() - edit_box.getRightOffset() - scroll_bar.getWidth(),
											height - edit_box.getBottomOffset() - edit_box.getTopOffset());
		scroll_bar.setPos(width - scroll_bar.getWidth(), 0);
		addChild(scroll_bar);
	}

	public final void append(String str) {
		super.append(str);
		scroll_bar.update();
	}

	public final void append(StringBuffer str) {
		super.append(str);
		scroll_bar.update();
	}

	public final void append(CharSequence str) {
		super.append(str);
		scroll_bar.update();
	}

	protected final TextBoxRenderer getTextRenderer() {
		return text_renderer;
	}

	protected final void renderBox(int mode) {
		Box edit_box = Skin.getSkin().getEditBox();
		edit_box.render(0, 0, getWidth() - scroll_bar.getWidth(), getHeight(), mode);
	}

	protected void renderGeometry() {
		Box edit_box = Skin.getSkin().getEditBox();
		renderBox(Skin.NORMAL);
		text_renderer.render(edit_box.getLeftOffset(), edit_box.getBottomOffset(), offset_y, getText());
	}

	protected final void mouseScrolled(int amount) {
		if (amount > 0)
			setOffsetY(offset_y - 3*getFont().getHeight());
		else
			setOffsetY(offset_y + 3*getFont().getHeight());
	}

	public final void setOffsetY(int new_offset) {
		offset_y = new_offset;

		if (offset_y < 0)
			offset_y = 0;
		Box edit_box = Skin.getSkin().getEditBox();
		int max_offset_y = text_renderer.getTextHeight(getText()) - (getHeight() - edit_box.getBottomOffset() - edit_box.getTopOffset());
		if (max_offset_y < 0)
			max_offset_y = 0;
		if (offset_y > max_offset_y)
			offset_y = max_offset_y;
		scroll_bar.update();
	}

	public final int getOffsetY() {
		return offset_y;
	}

	public final int getStepHeight() {
		return getFont().getHeight();
	}

	public final void jumpPage(boolean up) {
		Box edit_box = Skin.getSkin().getEditBox();
		int inner_height = getHeight() - edit_box.getBottomOffset() - edit_box.getTopOffset();
		if (up)
			setOffsetY(offset_y - inner_height);
		else
			setOffsetY(offset_y + inner_height);
	}

	public final float getScrollBarRatio() {
		int text_height = text_renderer.getTotalTextHeight(getText());
		Box edit_box = Skin.getSkin().getEditBox();
		int inner_height = getHeight() - edit_box.getBottomOffset() - edit_box.getTopOffset();
		int offset_height = offset_y + inner_height;
		return inner_height/(float)StrictMath.max(text_height, offset_height);
	}

	public final float getScrollBarOffset() {
		int text_height = text_renderer.getTotalTextHeight(getText());
		Box edit_box = Skin.getSkin().getEditBox();
		int inner_height = getHeight() - edit_box.getBottomOffset() - edit_box.getTopOffset();
		int offset_height = offset_y + inner_height;
		int length = StrictMath.max(text_height, offset_height);
		return offset_y/(float)(length - inner_height);
	}

	public final void setScrollBarOffset(float offset) {
		int text_height = text_renderer.getTotalTextHeight(getText());
		Box edit_box = Skin.getSkin().getEditBox();
		int inner_height = getHeight() - edit_box.getBottomOffset() - edit_box.getTopOffset();
		int offset_height = offset_y + inner_height;
		int length = StrictMath.max(text_height, offset_height);
		offset_y = (int)(offset*(length - inner_height));
		if (offset_y < 0)
			offset_y = 0;
		else if (offset_y > length - inner_height)
			offset_y = length - inner_height;
	}
}
