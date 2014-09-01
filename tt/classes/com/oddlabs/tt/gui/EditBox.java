package com.oddlabs.tt.gui;

import org.lwjgl.input.Keyboard;

import com.oddlabs.tt.font.TextBoxRenderer;

public final strictfp class EditBox extends TextBox {
	private int index;

	public EditBox(int width, int height, int max_chars) {
		super(width, height, Skin.getSkin().getEditFont(), max_chars);
		index = 0;
	}

	protected final void renderGeometry() {
		Box edit_box = Skin.getSkin().getEditBox();
		TextBoxRenderer text_renderer = getTextRenderer();
		if (isDisabled())
			renderBox(Skin.DISABLED);
		else
			renderBox(Skin.NORMAL);
		if (isActive())
			text_renderer.render(edit_box.getLeftOffset(), edit_box.getBottomOffset(), getOffsetY(), getText(), index);
		else
			text_renderer.render(edit_box.getLeftOffset(), edit_box.getBottomOffset(), getOffsetY(), getText());
	}

	protected final void keyRepeat(KeyboardEvent event) {
		//char ch = event.getKeyChar();
		Box edit_box = Skin.getSkin().getEditBox();
		switch (event.getKeyCode()) {
			case Keyboard.KEY_RETURN:
				if (insert(index, '\n')) {
					index++;
				}
				break;
			case Keyboard.KEY_BACK:
				if (index > 0)
					delete(--index);
				break;
			case Keyboard.KEY_DELETE:
				if (index < getText().length())
					delete(index);
				break;
			case Keyboard.KEY_LEFT:
				if (index > 0)
					index--;
				break;
			case Keyboard.KEY_RIGHT:
				if (index < getText().length())
					index++;
				break;
			case Keyboard.KEY_UP:
				index = getTextRenderer().jump(edit_box.getLeftOffset(),
											   edit_box.getBottomOffset(),
											   0,
											   getFont().getHeight(),
											   getText(),
											   index);
				break;
			case Keyboard.KEY_DOWN:
				index = getTextRenderer().jump(edit_box.getLeftOffset(),
											   edit_box.getBottomOffset(),
											   0,
											   -getFont().getHeight(),
											   getText(),
											   index);
				break;
			case Keyboard.KEY_HOME:
				index = getTextRenderer().jump(edit_box.getLeftOffset(),
											   edit_box.getBottomOffset(),
											   -getWidth(),
											   0,
											   getText(),
											   index);
				break;
			case Keyboard.KEY_END:
				index = getTextRenderer().jump(edit_box.getLeftOffset(),
											   edit_box.getBottomOffset(),
											   getWidth(),
											   0,
											   getText(),
											   index);
				break;
			case Keyboard.KEY_TAB:
			case Keyboard.KEY_ESCAPE:
				super.keyRepeat(event);
				break;
			default:
				char key = event.getKeyChar();
				if (getFont().getQuad(key) != null) {
					if (insert(index, key))
						index++;
				} else {
					super.keyRepeat(event);
				}
				
				break;
		}
		correctOffsetY();
	}

	private final void correctOffsetY() {
		Box edit_box = Skin.getSkin().getEditBox();
		int offset_y = getOffsetY();
		int index_render_y = getTextRenderer().getIndexRenderY(edit_box.getLeftOffset(),
										   edit_box.getBottomOffset(),
										   offset_y,
										   getText(),
										   index);
		int max_y = getY() + getHeight() - edit_box.getTopOffset() - getFont().getHeight();
		int min_y = getY() + edit_box.getBottomOffset();
		if (index_render_y > max_y) {
			offset_y -= index_render_y - max_y;
		} else if (index_render_y < min_y) {
			offset_y += min_y - index_render_y;
		}
		setOffsetY(offset_y);
	}

	protected final int getCursorIndex() {
		return GUIRoot.CURSOR_TEXT;
	}

	protected final void mouseClicked(int button, int x, int y, int clicks) {
		if (button == LocalInput.LEFT_BUTTON) {
			Box edit_box = Skin.getSkin().getEditBox();
			index = getTextRenderer().jumpDirect(edit_box.getLeftOffset(),
												 edit_box.getBottomOffset() + getFont().getHeight()/2 + getOffsetY(),
												 x,
												 y,
												 getText(),
												 index);
		}
	}
}
