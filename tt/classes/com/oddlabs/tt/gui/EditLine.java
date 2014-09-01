package com.oddlabs.tt.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.oddlabs.tt.font.Index;
import com.oddlabs.tt.font.TextLineRenderer;
import com.oddlabs.tt.guievent.EnterListener;

public strictfp class EditLine extends TextField {
	public final static int RIGHT_ALIGNED = 1;
	public final static int LEFT_ALIGNED = 2;
	
	private final List enter_listeners = new ArrayList();
	private final int alignment;
	private final String allowed_chars;
	private final int max_text_width;

	private TextLineRenderer text_renderer;
	private int offset_x;
	private int index;

	public EditLine(int width, int max_chars) {
		this(width, max_chars, LEFT_ALIGNED);
	}

	public EditLine(int width, int max_chars, int alignment) {
		this(width, max_chars, null, alignment);
	}

	public EditLine(int width, int max_chars, String allowed_chars, int alignment) {
		super(Skin.getSkin().getEditFont(), max_chars);
		this.allowed_chars = allowed_chars;
		this.alignment = alignment;
		Box edit_box = Skin.getSkin().getEditBox();
		setDim(width, getFont().getHeight() + edit_box.getBottomOffset() + edit_box.getTopOffset());
		setCanFocus(true);
		text_renderer = new TextLineRenderer(getFont()); 
		this.max_text_width = width - edit_box.getLeftOffset() - edit_box.getRightOffset();
		clear();
	}

	protected final int getCursorIndex() {
		return GUIRoot.CURSOR_TEXT;
	}

	protected void renderGeometry(float clip_left, float clip_right, float clip_bottom, float clip_top) {
		Box edit_box = Skin.getSkin().getEditBox();
		if (isDisabled())
			edit_box.render(0, 0, getWidth(), getHeight(), Skin.DISABLED);
		else
			edit_box.render(0, 0, getWidth(), getHeight(), Skin.NORMAL);
		int render_index = isActive() ? index : -1;
		renderText(text_renderer, edit_box.getLeftOffset(), edit_box.getBottomOffset(), offset_x, clip_left, clip_right, clip_bottom, clip_top, render_index);
	}

	protected void renderText(TextLineRenderer text_renderer, int x, int y, int offset_x, float clip_left, float clip_right, float clip_bottom, float clip_top, int render_index) {
		clip_left = StrictMath.max(clip_left, x);
		clip_right = StrictMath.min(clip_right, x + max_text_width);
		text_renderer.render(x, y, offset_x, clip_left, clip_right, clip_bottom, clip_top, getText(), render_index);
	}

	protected void keyReleased(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_RETURN:
				enterPressedAll();
				break;
			default:
				super.keyReleased(event);
				break;
		}
	}

	protected void keyRepeat(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_BACK:
				if (index > 0) {
					index--;
					if (alignment == RIGHT_ALIGNED) {
						char key = getText().charAt(index);
						offset_x += (getFont().getQuad(key).getWidth() - getFont().getXBorder());
					}
					delete(index);
				}
				break;
			case Keyboard.KEY_DELETE:
				if (index < getText().length()) {
					if (alignment == RIGHT_ALIGNED) {
						char key = getText().charAt(index);
						offset_x += (getFont().getQuad(key).getWidth() - getFont().getXBorder());
					}
					delete(index);
				}
				break;
			case Keyboard.KEY_LEFT:
				if (index > 0)
					index--;
				break;
			case Keyboard.KEY_RIGHT:
				if (index < getText().length())
					index++;
				break;
			case Keyboard.KEY_HOME:
				index = 0;
				break;
			case Keyboard.KEY_END:
				index = getText().length();
				break;
			case Keyboard.KEY_TAB:
			case Keyboard.KEY_RETURN:
				super.keyRepeat(event);
				break;
			default:
				char key = event.getKeyChar();
				if (isAllowed(key)) {
					boolean result = insert(index, key);
					assert result;
					index++;
					if (alignment == RIGHT_ALIGNED)
						offset_x -= (getFont().getQuad(key).getWidth() - getFont().getXBorder());
				} else {
					super.keyRepeat(event);
				}
				break;
		}
		correctOffsetX();
	}

	public final boolean isAllowed(char ch) {
		return super.isAllowed(ch) && getFont().getQuad(ch) != null && (allowed_chars == null || allowed_chars.indexOf(ch) != -1);
	}
	
	private final void correctOffsetX() {
		Box edit_box = Skin.getSkin().getEditBox();
		int index_render_x = text_renderer.getIndexRenderX(edit_box.getLeftOffset(),
										   edit_box.getBottomOffset(),
										   offset_x,
										   getText(),
										   index);

		int max_x = computeMaxX();
		if (index_render_x > max_x) {
			offset_x -= index_render_x - max_x;
		} else if (index_render_x < edit_box.getLeftOffset()) {
			offset_x += edit_box.getLeftOffset() - index_render_x;
		}
	}

	private final int computeMaxX() {
		Box edit_box = Skin.getSkin().getEditBox();
		return getWidth() - edit_box.getRightOffset() - Index.INDEX_WIDTH/* - getFont().getXBorder()/2*/;
	}

	public final int getIndex() {
		return index;
	}

	public final void setIndex(int index) {
		this.index = index;
	}

	public final void clear() {
		super.clear();
		index = 0;
		if (alignment == LEFT_ALIGNED)
			offset_x = -getFont().getXBorder()/2;
		else {
			int text_width = Skin.getSkin().getEditFont().getWidth(getText());
			offset_x = max_text_width - text_width - Index.INDEX_WIDTH - getFont().getXBorder()/2;
		}
	}

	protected final void appendNotify(CharSequence str) {
		if (alignment == RIGHT_ALIGNED) {
			for (int i = 0; i < str.length(); i++) {
				char key = str.charAt(i);
				offset_x -= (getFont().getQuad(key).getWidth() - getFont().getXBorder());
			}
		}
	}

	protected void focusNotify(boolean focus) {
		if (focus)
			index = getText().length();
	}

	protected final void mouseEntered() {
	}

	protected final void mouseExited() {
	}

	protected final void mousePressed(int button, int x, int y) {
		if (button == LocalInput.LEFT_BUTTON) {
			Box edit_box = Skin.getSkin().getEditBox();
			index = text_renderer.jumpDirect(edit_box.getLeftOffset() + offset_x,
											 edit_box.getBottomOffset(),
											 x,
											 getText(),
											 index);
		}
	}

	public final void enterPressedAll() {
		CharSequence text = getText();
		enterPressed(text);
		for (int i = 0; i < enter_listeners.size(); i++) {
			EnterListener listener = (EnterListener)enter_listeners.get(i);
			if (listener != null)
				listener.enterPressed(text);
		}
	}

	protected void enterPressed(CharSequence text) {
	}

	public final void addEnterListener(EnterListener listener) {
		enter_listeners.add(listener);
	}
}
