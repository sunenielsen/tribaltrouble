package com.oddlabs.tt.gui;

import com.oddlabs.util.Quad;

public final strictfp class ScrollBarData {
	private final Vertical scroll_bar;
	private final Quad[] scroll_down_button_pressed;
	private final Quad[] scroll_down_button_unpressed;
	private final Quad[] scroll_down_arrow;
	private final Quad[] scroll_up_button_pressed;
	private final Quad[] scroll_up_button_unpressed;
	private final Quad[] scroll_up_arrow;
	private final Vertical scroll_button;
	private final int left_offset;
	private final int bottom_offset;
	private final int top_offset;

	public ScrollBarData(Vertical scroll_bar,
						 Quad[] scroll_down_button_pressed,
						 Quad[] scroll_down_button_unpressed,
						 Quad[] scroll_down_arrow,
						 Quad[] scroll_up_button_pressed,
						 Quad[] scroll_up_button_unpressed,
						 Quad[] scroll_up_arrow,
						 Vertical scroll_button,
						 int left_offset,
						 int bottom_offset,
						 int top_offset) {
		 this.scroll_bar = scroll_bar;
		 this.scroll_down_button_pressed = scroll_down_button_pressed;
		 this.scroll_down_button_unpressed = scroll_down_button_unpressed;
		 this.scroll_down_arrow = scroll_down_arrow;
		 this.scroll_up_button_pressed = scroll_up_button_pressed;
		 this.scroll_up_button_unpressed = scroll_up_button_unpressed;
		 this.scroll_up_arrow = scroll_up_arrow;
		 this.scroll_button = scroll_button;
		 this.left_offset = left_offset;
		 this.bottom_offset = bottom_offset;
		 this.top_offset = top_offset;
	}

	public final Vertical getScrollBar() {
		return scroll_bar;
	}

	public final Quad[] getScrollDownButtonPressed() {
		return scroll_down_button_pressed;
	}

	public final Quad[] getScrollDownButtonUnpressed() {
		return scroll_down_button_unpressed;
	}

	public final Quad[] getScrollDownArrow() {
		return scroll_down_arrow;
	}

	public final Quad[] getScrollUpButtonPressed() {
		return scroll_up_button_pressed;
	}

	public final Quad[] getScrollUpButtonUnpressed() {
		return scroll_up_button_unpressed;
	}

	public final Quad[] getScrollUpArrow() {
		return scroll_up_arrow;
	}

	public final Vertical getScrollButton() {
		return scroll_button;
	}

	public final int getLeftOffset() {
		return left_offset;
	}

	public final int getBottomOffset() {
		return bottom_offset;
	}

	public final int getTopOffset() {
		return top_offset;
	}
}
