package com.oddlabs.tt.gui;

import com.oddlabs.tt.guievent.MouseButtonListener;
import com.oddlabs.tt.guievent.MouseMotionListener;
import com.oddlabs.tt.guievent.ValueListener;

public final strictfp class Slider extends GUIObject {
	private final java.util.List value_listeners = new java.util.ArrayList();

	private final SliderButton button;
	private final int left_offset;
	private final int right_offset;
	private final int cardinality;
	private final float step;
	private final int min;
	private int value;

	public Slider(int width, int min, int max, int init_value) {
		cardinality = max - min + 1;
		assert cardinality > 0 && max >= init_value && init_value >= min: "Invalid values. cardinality = " + cardinality + " | max = " + max + " | min = " + min + " | init_value = " + init_value;
		this.min = min;
		left_offset = Skin.getSkin().getSliderData().getLeftOffset();
		right_offset = Skin.getSkin().getSliderData().getRightOffset();
		setDim(width, Skin.getSkin().getSliderData().getSlider().getHeight());
		setCanFocus(true);

		button = new SliderButton(this, Skin.getSkin().getSliderData().getButton());
		step = (getWidth() - left_offset - right_offset - button.getWidth())/(float)(cardinality - 1);
		setValue(init_value);
		addChild(button);

		DragListener drag_listener = new DragListener();
		button.addMouseMotionListener(drag_listener);
		button.addMouseButtonListener(drag_listener);
	}

	protected final void renderGeometry() {
		Horizontal slider = Skin.getSkin().getSliderData().getSlider();
		if (isDisabled())
			slider.render(0, 0, getWidth(), Skin.DISABLED);
		else
			slider.render(0, 0, getWidth(), Skin.NORMAL);
	}

	public final int getValue() {
		return min + value;
	}

	private final int valueToOffset(int value) {
		return (int)(value*step) + left_offset;
	}

	private final int offsetToValue(int offset) {
		return (int)(offset/step + .5f);
	}
	
	public final void mouseHeld(int button, int x, int y) {
		mousePressed(button, x, y);
	}

	public void mouseScrolled(int amount) {
		if (!isDisabled()) {
			if (amount < 0)
				setValue(value - 1 + min);
			else
				setValue(value + 1 + min);
			button.setFocus();
		}
	}

	public final void mousePressed(int button, int x, int y) {
		if (!isDisabled()) {
			int dx = x - this.button.getX();
			if (dx < -step/2)
				setValue(value - 1 + min);
			else if (dx > step/2)
				setValue(value + 1 + min);
			this.button.setFocus();
		}
	}

	public final void setValue(int value) {
		int start_value = this.value;
		this.value = value - min;
		cropValue();
		button.setPos(valueToOffset(this.value), 0);
		if (start_value != this.value)
			valueSetAll(getValue());
	}

	private final void cropValue() {
		if (value < 0) {
			value = 0;
		} else if (value > cardinality - 1) {
			value = cardinality - 1;
		}
	}

	public final void valueSetAll(int value) {
		valueSet(value);
		for (int i = 0; i < value_listeners.size(); i++) {
			ValueListener listener = (ValueListener)value_listeners.get(i);
			if (listener != null)
				listener.valueSet(value);
		}
	}

	protected void valueSet(int value) {
/*		
		GUIObject parent = (GUIObject)getParent();
		if (parent != null)
			parent.valueSetAll(value);
*/
	}

	public final void addValueListener(ValueListener listener) {
		value_listeners.add(listener);
	}

	public final void removeValueListener(ValueListener listener) {
		value_listeners.remove(listener);
	}

	private final strictfp class DragListener implements MouseMotionListener, MouseButtonListener {
		private int start_offset;

		public final void mousePressed(int button, int x, int y) {
			start_offset = (int)valueToOffset(value);
		}

		public final void mouseDragged(int button, int x, int y, int rel_x, int rel_y, int abs_x, int abs_y) {
			if (!isDisabled()) {
				setValue(offsetToValue(start_offset + abs_x) + min);
			}
		}

		public final void mouseMoved(int x, int y) {}
		public final void mouseEntered() {}
		public final void mouseExited() {}
		public final void mouseReleased(int button, int x, int y) {}
		public final void mouseHeld(int button, int x, int y) {}
		public final void mouseClicked(int button, int x, int y, int clicks) {}
	}
}
