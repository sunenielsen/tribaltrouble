package com.oddlabs.tt.gui;

import com.oddlabs.tt.font.*;
import com.oddlabs.util.Quad;

public final strictfp class ColumnButton extends RadioButtonGroupElement {
	private final RowCollection rows;
	private final int arrow_offset;
	private final int column_index;

	private boolean sorted_descending;
	private boolean pressed = false;

	public ColumnButton(RadioButtonGroup group, RowCollection rows, ColumnInfo info, int column_index, boolean sorted_descending) {
		super(column_index == 0, group);
		this.rows = rows;
		this.column_index = column_index;
		this.sorted_descending = sorted_descending;
		MultiColumnComboBoxData data = Skin.getSkin().getMultiColumnComboBoxData();
		setDim(info.getWidth(), data.getButtonUnpressed().getHeight());
		Font font = data.getFont();
		Label label = new Label(info.getCaption(), font);
		addChild(label);
		label.setPos(data.getCaptionOffset(), (getHeight() - font.getHeight())/2 + 1);
		Quad arrow = Skin.getSkin().getMultiColumnComboBoxData().getDescending()[0];
		arrow_offset = info.getWidth() - arrow.getWidth();
		setCanFocus(true);
	}

	protected final void mouseReleased(int button, int x, int y) {
		pressed = false;
	}

	protected final void mousePressed(int button, int x, int y) {
		pressed = true;
	}

	protected void mouseClicked(int button, int x, int y, int clicks) {
		if (isMarked())
			sorted_descending = !sorted_descending;
		else
			sorted_descending = true;
		super.mouseClicked(button, x, y, clicks);
		rows.markChanged(column_index, sorted_descending);
	}

	public final int getColumnIndex() {
		return column_index;
	}

	protected final void renderGeometry() {
		if (isDisabled()) {
			Skin.getSkin().getMultiColumnComboBoxData().getButtonUnpressed().render(0, 0, getWidth(), Skin.DISABLED);
			if (isMarked())
				renderMark(Skin.DISABLED);
		} else if (isHovered() && pressed) {
			Skin.getSkin().getMultiColumnComboBoxData().getButtonPressed().render(0, 0, getWidth(), Skin.ACTIVE);
			if (isMarked())
				renderMark(Skin.ACTIVE);
		} else if (isActive()) {
			Skin.getSkin().getMultiColumnComboBoxData().getButtonUnpressed().render(0,0, getWidth(), Skin.ACTIVE);
			if (isMarked())
				renderMark(Skin.ACTIVE);
		} else {
			Skin.getSkin().getMultiColumnComboBoxData().getButtonUnpressed().render(0, 0, getWidth(), Skin.NORMAL);
			if (isMarked())
				renderMark(Skin.NORMAL);
		}
	}

	private final void renderMark(int mode) {
		Quad[] arrow;
		if (sorted_descending)
			arrow = Skin.getSkin().getMultiColumnComboBoxData().getDescending();
		else
			arrow = Skin.getSkin().getMultiColumnComboBoxData().getAscending();

		arrow[mode].render(arrow_offset, (getHeight() - arrow[Skin.NORMAL].getHeight())/2);
	}
}
