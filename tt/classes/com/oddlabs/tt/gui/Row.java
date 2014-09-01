package com.oddlabs.tt.gui;

import org.lwjgl.opengl.*;

public final strictfp class Row extends GUIObject implements Comparable {
	private final Object[] columns;
	private final Object content_object;
	private int sort_index;
	private Color color = null;
	private boolean marked = false;

	public Row(GUIObject[] columns, Object content_object) {
		this.columns = columns;
		this.content_object = content_object;
		setDim(0, columns[0].getHeight());
		setCanFocus(true);
	}

	public final Object getColumn(int index) {
		return columns[index];
	}

	public final void setColumnInfos(ColumnInfo[] column_infos) {
		int x = 0;
		for (int i = 0; i < column_infos.length; i++) {
			GUIObject gui_object = (GUIObject)getColumn(i);
			gui_object.setPos(x, 0);
			addChild(gui_object);
			x += column_infos[i].getWidth();

			// if left most column, correct for the radio button starting without left_offset
			if (i == 0)
				x -= Skin.getSkin().getMultiColumnComboBoxData().getBox().getLeftOffset(); 
			// if right most column, correct for the radio button extending over right_offset
			if (i == column_infos.length - 1)
				x -= Skin.getSkin().getMultiColumnComboBoxData().getBox().getRightOffset(); 
		}
		setDim(x, getHeight());
	}

	public final void setSortIndex(int sort_index) {
		this.sort_index = sort_index;
	}

	public final int compareTo(Object o) {
		Comparable local_object = (Comparable)getColumn(sort_index);
		Comparable compared_object = (Comparable)((Row)o).getColumn(sort_index);
		return local_object.compareTo(compared_object);
	}

	public final void setColor(Color color) {
		this.color = color;
	}

	protected final void renderGeometry(float clip_left, float clip_right, float clip_bottom ,float clip_top) {
		GL11.glEnd();
		if (marked) {
			Color color = Skin.getSkin().getMultiColumnComboBoxData().getColorMarked();
			GL11.glColor4f(color.getR(), color.getG(), color.getB(), color.getA());
		} else {
			GL11.glColor4f(color.getR(), color.getG(), color.getB(), color.getA());
		}
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex3f(clip_left, clip_bottom, 0);
		GL11.glVertex3f(clip_right, clip_bottom, 0);
		GL11.glVertex3f(clip_right, clip_top, 0);
		GL11.glVertex3f(clip_left, clip_top, 0);
		GL11.glEnd();
		GL11.glColor4f(1f, 1f, 1f, 1f);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBegin(GL11.GL_QUADS);
	}

	public final Object getContentObject() {
		return content_object;
	}

	public final void mark(boolean marked) {
		this.marked = marked;
	}
}
