package com.oddlabs.tt.gui;

import org.lwjgl.input.Keyboard;

import com.oddlabs.tt.font.Font;

public abstract strictfp class TextField extends GUIObject implements CharSequence {
	private final static StringBuffer digit_buf = new StringBuffer();
	private final static String[] digits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

	private final StringBuffer text;
	private final Font font;
	private final int max_chars;

	public TextField(Font font, int max_chars) {
		this("", font, max_chars);
	}

	public TextField(CharSequence text, Font font, int max_chars) {
		this.font = font;
		this.text = new StringBuffer(text.toString());
		this.max_chars = max_chars;
	}

	public final Font getFont() {
		return font;
	}

	public final String getContents() {
		return text.toString();
	}

	protected final StringBuffer getText() {
		return text;
	}

	public final char charAt(int i) {
		return text.charAt(i);
	}

	public final int length() {
		return text.length();
	}

	public final int getTextWidth() {
		return font.getWidth(text);
	}

	public final CharSequence subSequence(int start, int end) {
		return text.subSequence(start, end);
	}

	public final String toString() {
		return text.toString();
	}

	public final void set(CharSequence str) {
		clear();
		append(str.toString());
	}

	public void clear() {
		text.delete(0, text.length());
	}

	public void append(String str) {
		text.append(str);
		appendNotify(str);
	}

	public void append(StringBuffer str) {
		text.append(str);
		appendNotify(str);
	}

	public void append(CharSequence str) {
		text.append(str);
		appendNotify(str);
	}

	public final void append(long i) {
		fillDigitBuffer(i);
		append(digit_buf);
	}

	private final static void fillDigitBuffer(long i) {
		digit_buf.delete(0, digit_buf.length());
		boolean sign = i < 0;
		if (sign)
			i = -i;
		do {
			byte digit = (byte)(i % 10);
			digit_buf.insert(0, digits[digit]);
			i /= 10;
		} while (i > 0);
		if (sign)
			digit_buf.insert(0, '-');
	}

	public final static void appendNumberToStringBuffer(long i, StringBuffer buffer) {
		fillDigitBuffer(i);
		buffer.append(digit_buf);
	}

	protected boolean insert(int index, char key) {
		if (isAllowed(key)) {
			text.insert(index, key);
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean isAllowed(char key) {
		return max_chars == -1 || text.length() < max_chars;
	}
	
	protected void delete(int index) {
		text.deleteCharAt(index);
	}

	protected void appendNotify(CharSequence str) {
	}

	protected final void keyPressed(KeyboardEvent event) {
		if (event.getKeyCode() != Keyboard.KEY_SPACE && event.getKeyCode() != Keyboard.KEY_RETURN)
			super.keyPressed(event);
	}

	protected void keyReleased(KeyboardEvent event) {
		if (event.getKeyCode() != Keyboard.KEY_SPACE && event.getKeyCode() != Keyboard.KEY_RETURN)
			super.keyReleased(event);
	}
}
