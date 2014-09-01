package com.oddlabs.tt.gui;

import java.util.ArrayList;
import java.util.List;

import com.oddlabs.tt.guievent.RowListener;

public final strictfp class MultiColumnComboBox extends GUIObject implements Scrollable {
	private final ColumnInfo[] column_infos;
	private final RadioButtonGroup group = new RadioButtonGroup();
	private final Group focus_group = new Group();
	private final RowCollection rows = new RowCollection(this, 0, true);
	private final List row_listeners = new ArrayList();
	private final ScrollBar scroll_bar;
	private final boolean use_buttons;
	private final GUIRoot gui_root;
	private int offset_y = 0;
	private PulldownMenu pulldown_menu = null;
	private Object right_clicked_row_data;


	public MultiColumnComboBox(GUIRoot gui_root, ColumnInfo[] column_infos, int height) {
		this(gui_root, column_infos, height, true);
	}

	public MultiColumnComboBox(GUIRoot gui_root, ColumnInfo[] column_infos, int height, boolean use_buttons) {
		this.column_infos = column_infos;
		this.use_buttons = use_buttons;
		this.gui_root = gui_root;
		Box box = Skin.getSkin().getMultiColumnComboBoxData().getBox();
		int width = 0;
		for (int i = 0; i < column_infos.length; i++) {
			ColumnButton column_button = new ColumnButton(group, rows, column_infos[i], i, true);
			if (use_buttons) {
				column_button.setPos(width, height - column_button.getHeight());
				focus_group.addChild(column_button);
			}
			width += column_button.getWidth();
		}
		scroll_bar = new ScrollBar(height, this);
		scroll_bar.setPos(width, 0);
		focus_group.addChild(scroll_bar);
		setDim(width + scroll_bar.getWidth(), height);
		focus_group.setDim(getWidth(), getHeight());
		focus_group.setPos(0, 0);
		addChild(focus_group);
		if (use_buttons)
			rows.setDim(width - box.getLeftOffset(), height - box.getBottomOffset() - box.getTopOffset() - group.getMarked().getHeight());
		else
			rows.setDim(width - box.getLeftOffset(), height - box.getBottomOffset() - box.getTopOffset());
		rows.setPos(box.getLeftOffset(), box.getBottomOffset());
		addChild(rows);
		setCanFocus(true);
		scroll_bar.update();
	}

	public final int getSize() {
		return rows.getSize();
	}
	
	public final void clickedRow() {
		for (int i = 0; i < row_listeners.size(); i++) {
			RowListener listener = (RowListener)row_listeners.get(i);
			if (listener != null)
				listener.rowChosen(rows.getSelected());
		}
	}

	public final void addRowListener(RowListener listener) {
		row_listeners.add(listener);
	}

	public final void doubleClickedRow() {
		for (int i = 0; i < row_listeners.size(); i++) {
			RowListener listener = (RowListener)row_listeners.get(i);
			if (listener != null)
				listener.rowDoubleClicked(rows.getSelected());
		}
	}

	public final void setPulldownMenu(PulldownMenu pulldown_menu) {
		this.pulldown_menu = pulldown_menu;
	}

	public final void rightClickedRow(int x, int y) {
		if (pulldown_menu != null) {
			int pulldown_x = StrictMath.max(0, StrictMath.min(LocalInput.getViewWidth() - pulldown_menu.getWidth(), x));
			int pulldown_y = StrictMath.max(0, StrictMath.min(LocalInput.getViewHeight() - pulldown_menu.getHeight(), y - pulldown_menu.getHeight()));
			pulldown_menu.setPos(pulldown_x, pulldown_y);
			gui_root.getDelegate().addChild(pulldown_menu);
			pulldown_menu.setFocus();
			right_clicked_row_data = getSelected();
		}
	}

	protected final void renderGeometry() {
		MultiColumnComboBoxData data = Skin.getSkin().getMultiColumnComboBoxData();
		Box box = data.getBox();
		if (use_buttons)
			box.render(0, 0, getWidth() - scroll_bar.getWidth(), getHeight() - group.getMarked().getHeight(), Skin.NORMAL);
		else
			box.render(0, 0, getWidth() - scroll_bar.getWidth(), getHeight(), Skin.NORMAL);
	}

	public final void setFocus() {
		focus_group.setGroupFocus(LocalInput.isShiftDownCurrently() ? -1 : 1);
	}

	public final void clear() {
		rows.clear();
	}

	public final void addRow(Row row) {
		row.setColumnInfos(column_infos);
		rows.addRow(row);
		scroll_bar.update();
	}

	public final Object getSelected() {
		return rows.getSelected();
	}

	public final Object getRightClickedRowData() {
		return right_clicked_row_data;
	}

	public final void selectRow(Row row) {
		rows.selectRow(row);
	}

	protected final void mouseScrolled(int amount) {
		if (amount > 0)
			setOffsetY(offset_y - 3*Skin.getSkin().getMultiColumnComboBoxData().getFont().getHeight());
		else
			setOffsetY(offset_y + 3*Skin.getSkin().getMultiColumnComboBoxData().getFont().getHeight());
	}

	public final void setOffsetY(int new_offset) {
		offset_y = new_offset;

		if (offset_y < 0)
			offset_y = 0;
		int max_offset_y = rows.getContentHeight() - rows.getHeight();
		if (max_offset_y < 0)
			max_offset_y = 0;
		if (offset_y > max_offset_y)
			offset_y = max_offset_y;
		rows.replaceRows();
		scroll_bar.update();
	}

	public final int getOffsetY() {
		return offset_y;
	}

	public final int getStepHeight() {
		return Skin.getSkin().getMultiColumnComboBoxData().getFont().getHeight();
	}

	public final void jumpPage(boolean up) {
		if (up)
			setOffsetY(offset_y - rows.getHeight());
		else
			setOffsetY(offset_y + rows.getHeight());
	}

	public final float getScrollBarRatio() {
		return rows.getHeight()/(float)StrictMath.max(rows.getContentHeight(), offset_y + rows.getHeight());
	}

	public final float getScrollBarOffset() {
		int length = StrictMath.max(rows.getContentHeight(), offset_y + rows.getHeight());
		return offset_y/(float)(length - rows.getHeight());
	}

	public final void setScrollBarOffset(float offset) {
		int length = StrictMath.max(rows.getContentHeight(), offset_y + rows.getHeight());
		setOffsetY((int)(offset*(length - rows.getHeight())));
	}
}
