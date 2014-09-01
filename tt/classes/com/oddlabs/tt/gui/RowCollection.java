package com.oddlabs.tt.gui;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.guievent.MouseClickListener;

public final strictfp class RowCollection extends GUIObject {
	private final static DoubleBuffer plane_buf = BufferUtils.createDoubleBuffer(4);
	private final List rows = new ArrayList();
	private final MultiColumnComboBox multi_box;
	private Row selected_row = null;
	private int sort_index;
	private boolean sorted_descending;

	public RowCollection(MultiColumnComboBox multi_box, int sort_index, boolean sorted_descending) {
		this.multi_box = multi_box;
		this.sort_index = sort_index;
		this.sorted_descending = sorted_descending;
		setCanFocus(true);
	}

	public final void clear() {
		for (int i = 0; i < rows.size(); i++) {
			Row row = (Row)rows.get(i);
			row.remove();
		}
		rows.clear();
		selected_row = null;
		replaceRows();
	}

	public final void addRow(Row row) {
		rows.add(row);
		row.addMouseClickListener(new RowListener(row));
		row.setSortIndex(sort_index);
		addChild(row);
		Collections.sort(rows);
		replaceRows();
	}

	public final int getSize() {
		return rows.size();
	}

	public final void markChanged(int index, boolean sorted_descending) {
		sort_index = index;
		this.sorted_descending = sorted_descending;
		for (int i = 0; i < rows.size(); i++) {
			((Row)rows.get(i)).setSortIndex(sort_index);
		}
		Collections.sort(rows);
		replaceRows();
	}

	public final void replaceRows() {
		int y = getHeight() + ((MultiColumnComboBox)getParent()).getOffsetY();
		for (int i = 0; i < rows.size(); i++) {
			Row row;
			if (sorted_descending)
				row = (Row)rows.get(i);
			else
				row = (Row)rows.get(rows.size() - i - 1);
			y -= row.getHeight();
			row.setPos(0, y);
			if (i%2 == 0)
				row.setColor(Skin.getSkin().getMultiColumnComboBoxData().getColor1());
			else
				row.setColor(Skin.getSkin().getMultiColumnComboBoxData().getColor2());
		}
	}

	public final int getContentHeight() {
		int height = 0;
		for (int i = 0; i < rows.size(); i++) {
			height += ((Row)rows.get(i)).getHeight();
		}
		return height;
	}

	public final Object getSelected() {
		if (selected_row == null)
			return null;
		return selected_row.getContentObject();
	}

	public final void selectRow(Row row) {
		assert rows.contains(row); 
		if (selected_row != null)
			selected_row.mark(false);
		selected_row = row;
		selected_row.mark(true);
	}

	private final strictfp class RowListener implements MouseClickListener {
		private final Row row;

		public RowListener(Row row) {
			this.row = row;
		}

		public final void mouseClicked(int button, int x, int y, int clicks) {
			selectRow(row);
			if (button == LocalInput.RIGHT_BUTTON) {
				multi_box.clickedRow();
				multi_box.rightClickedRow((int)(row.getRootX() + x), (int)(row.getRootY() + y));
			} else if (clicks == 1) {
				multi_box.clickedRow();
			} else if (clicks == 2) {
				multi_box.doubleClickedRow();
			}
		}
	}
}
