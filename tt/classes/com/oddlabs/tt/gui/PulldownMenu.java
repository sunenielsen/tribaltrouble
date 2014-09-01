package com.oddlabs.tt.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.oddlabs.tt.guievent.ItemChosenListener;
import com.oddlabs.tt.guievent.MouseClickListener;

public final strictfp class PulldownMenu extends Group {// GUIObject {
	private final java.util.List chosen_listeners = new java.util.ArrayList();

	private final List items = new ArrayList();
	private int chosen_item_index = -1;
	
	public PulldownMenu() {
		setCanFocus(true);
		setFocusCycle(true);
	}

	public final PulldownItem getItem(int index) {
		return (PulldownItem)items.get(index);
	}

	public final int getSize() {
		return items.size();
	}

	protected final void renderGeometry() {
		// Render bottom edge
		Horizontal bot = Skin.getSkin().getPulldownData().getPulldownBottom();
		bot.render(0, 0, getWidth(), Skin.NORMAL);

		// Render top edge
		Horizontal top = Skin.getSkin().getPulldownData().getPulldownTop();
		top.render(0, getHeight() - top.getHeight(), getWidth(), Skin.NORMAL);
	}

	public final void addItem(PulldownItem item) {
		items.add(item);
		addChild(item);
		item.addMouseClickListener(new ItemListener(items.size() - 1));
		setDim(getWidth(), getHeight());
	}

	public final void setDim(int width, int height) {
		int min_width = 0;
		Box item_box = Skin.getSkin().getPulldownData().getPulldownItem();
		// Adjust all items
		for (int i = 0; i < items.size(); i++) {
			PulldownItem item = (PulldownItem)items.get(i);
			if (item.getTextWidth() > min_width)
				min_width = item.getTextWidth();
		}
		int item_pos_count = Skin.getSkin().getPulldownData().getPulldownBottom().getHeight();
		min_width = StrictMath.max(width, item_box.getLeftOffset() + min_width + item_box.getRightOffset());
		for (int i = 0; i < items.size(); i++) {
			PulldownItem item = (PulldownItem)items.get(items.size() - 1 - i);
			int item_height = item_box.getBottomOffset() + item.getTextHeight() + item_box.getTopOffset();
			item.setDim(min_width, item_height);
			item.setPos(0, item_pos_count);
			item_pos_count += item_height;
		}
		int min_height = StrictMath.max(height, item_pos_count + Skin.getSkin().getPulldownData().getPulldownTop().getHeight());
		super.setDim(min_width, min_height);
	}

	public final int getChosenItemIndex() {
		return chosen_item_index;
	}

	public final void chooseItem(int index) {
		chosen_item_index = index;
		itemChosenAll();
	}

	protected final void focusNotify(boolean focus) {
		if (!focus) {
			remove();
		}
	}

	protected final void keyRepeat(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_UP:
				focusPrior();
				break;
			case Keyboard.KEY_DOWN:
				focusNext();
				break;
			default:
				super.keyRepeat(event);
				break;
		}
	}

	// Sending click on to appropiate item when PulldownButton has been pressed and released on an item
	protected final void clickItem(int button, int x, int y, int clicks) {
		for (int i = 0; i < items.size(); i++) {
			PulldownItem item = getItem(i);
			if (item.isHovered())
				item.mouseClickedAll(button, x, y, clicks);
		}
	}

	public final void itemChosenAll() {
		for (int i = 0; i < chosen_listeners.size(); i++) {
			ItemChosenListener listener = (ItemChosenListener)chosen_listeners.get(i);
			if (listener != null)
				listener.itemChosen(this, chosen_item_index);
		}
	}

	public final void addItemChosenListener(ItemChosenListener listener) {
		chosen_listeners.add(listener);
	}

	public final void removeItemChosenListener(ItemChosenListener listener) {
		chosen_listeners.remove(listener);
	}

	public final strictfp class ItemListener implements MouseClickListener {
		private final int index;

		public ItemListener(int index) {
			this.index = index;
		}

		public final void mouseClicked(int button, int x, int y, int clicks) {
			chooseItem(index);
		}
	}
}
