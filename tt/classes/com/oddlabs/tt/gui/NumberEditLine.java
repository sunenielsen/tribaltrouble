package com.oddlabs.tt.gui;

import com.oddlabs.tt.guievent.ValueListener;

public strictfp class NumberEditLine extends EditLine {
	private final java.util.List value_listeners = new java.util.ArrayList();
		
	private final int min_value;
	private final int max_value;

	private int value;

	public NumberEditLine(int width, int max_chars, int max_value) {
		this(width, max_chars, 0, max_value, 0);
	}

	public NumberEditLine(int width, int max_chars, int min_value, int max_value, int init_value) {
		super(width, max_chars, "0123456789", RIGHT_ALIGNED);
		this.min_value = min_value;
		this.max_value = max_value;
		setValue(init_value);
	}

	public final void addValueListener(ValueListener listener) {
		value_listeners.add(listener);
	}

	public final void removeValueListener(ValueListener listener) {
		value_listeners.remove(listener);
	}

	protected final void enterPressed(CharSequence text) {
		validate();
	}

/*	private final void setOffset() {
		int text_width = getTextLineRenderer().getFont().getWidth(getText());
		setOffsetX(getTextLineRenderer().getWidth() - text_width);
	}
*/
	private final void validate() {
		int value;
		String str = getText().toString();

		try {
			value = crop(Integer.parseInt(str));
		} catch (Exception e) {
			// ignore exception, assume minimum value
			value = max_value;
		};
		setValue(value);
	}

	private final int crop(int value) {
		if (value > max_value) {
			return max_value;
		} else if (value < min_value) {
			return min_value;
		} else {
			return value;
		}
	}

	public final void setValue(int value) {
		clear();
		value = crop(value);
		append(value);

		if (value != this.value) {
			this.value = value;
			for (int i = 0; i < value_listeners.size(); i++) {
				ValueListener listener = (ValueListener)value_listeners.get(i);
				if (listener != null)
					listener.valueSet(this.value);
			}
		}
	}

	public final int getValue() {
		validate();
		return value;
	}
	
	protected void focusNotify(boolean focus) {
		validate();
		super.focusNotify(focus);
	}
}
