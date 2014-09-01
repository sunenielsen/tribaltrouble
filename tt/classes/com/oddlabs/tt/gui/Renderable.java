package com.oddlabs.tt.gui;

import com.oddlabs.util.*;

import org.lwjgl.opengl.*;

public abstract strictfp class Renderable extends ListElementImpl {
	private int x = 0;
	private int y = 0;
	private int width = 0;
	private int height = 0;
	private float scale_x = 1f;
	private float scale_y = 1f;

	private final LinkedList children = new LinkedList();

	private Renderable parent = null;

	public void setDim(int w, int h) {
		width = w;
		height = h;
	}

	public final int getWidth() {
		return width;
	}

	public final int getHeight() {
		return height;
	}

	public void setPos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public final int getX() {
		return x;
	}

	public final int getY() {
		return y;
	}

	public final int getNumChildren() {
		return children.size();
	}

	public final Renderable getParent() {
		return parent;
	}

	public final void putLast(Renderable child) {
		children.putLast(child);
	}

	public final void putFirst(Renderable child) {
		children.putFirst(child);
	}

	public void removeChild(Renderable child) {
		child.parent = null;
		children.remove(child);
	}

	public final void clearChildren() {
		while (children.size() > 0) {
			((Renderable)getLastChild()).remove();
		}
	}
/*
	protected final void disableTree() {
		ListElement current = children.getFirst();
		while (current != null) {
			((Renderable)current).disableTree();
			current = current.getNext();
		}
	}

	protected final void enableTree() {
		ListElement current = children.getFirst();
		while (current != null) {
			((Renderable)current).enableTree();
			current = current.getNext();
		}
	}
*/
	protected void doAdd() {
//		enableTree();
	}

	protected final GUIRoot getParentGUIRoot() {
		Renderable current = this;
		while (current != null && !(current instanceof GUIRoot)) {
			current = (Renderable)current.getParent();
		}
		return (GUIRoot)current;
	}

	public void addChild(Renderable child) {
		child.remove();
		children.addFirst(child);
		child.parent = this;
		child.addTree();
	}

	public final Renderable getLastChild() {
		return (Renderable)children.getLast();
	}

	public final Renderable getFirstChild() {
		return (Renderable)children.getFirst();
	}

	public final void displayChanged(int width, int height) {
		displayChangedNotify(width, height);
		ListElement current = children.getFirst();
		while (current != null) {
			((Renderable)current).displayChanged(width, height);
			current = current.getNext();
		}
	}

	protected void displayChangedNotify(int width, int height) {
	}

	public final void setScale(float scale_x, float scale_y) {
		this.scale_x = scale_x;
		this.scale_y = scale_y;
	}

	public final float getScaleX() {
		return scale_x;
	}

	public final float getScaleY() {
		return scale_y;
	}

	public final void render() {
		render(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
	}

	private void render(float clip_left, float clip_right, float clip_bottom, float clip_top) {
		clip_left = transformX(clip_left);
		clip_right = transformX(clip_right);
		clip_bottom = transformY(clip_bottom);
		clip_top = transformY(clip_top);
		clip_left = StrictMath.max(clip_left, 0);
		clip_right = StrictMath.min(clip_right, width);
		clip_bottom = StrictMath.max(clip_bottom, 0);
		clip_top = StrictMath.min(clip_top, height);
		if (clip_left >= width || clip_right <= 0 || clip_bottom >= height || clip_top <= 0) {
			return;
		}
		ListElement current = children.getLast();
		if (!(this instanceof GUIRoot)) {
			GL11.glEnd();
			GL11.glPushMatrix();
			if (scale_x != 1f || scale_y != 1f) {
				GL11.glScalef(scale_x, scale_y, 1f);
			}
			GL11.glTranslatef(getX(), getY(), 0);
			GL11.glBegin(GL11.GL_QUADS);
		}
		renderGeometry(clip_left, clip_right, clip_bottom, clip_top);
		if (current != null) {
			while (current != null) {
				((Renderable)current).render(clip_left, clip_right, clip_bottom, clip_top);
				current = current.getPrior();
			}
		}
		postRender();
		if (!(this instanceof GUIRoot)) {
			GL11.glEnd();
			GL11.glPopMatrix();
			GL11.glBegin(GL11.GL_QUADS);
		}
	}

	protected void postRender() {
	}

	protected void renderGeometry(float clip_left, float clip_right, float clip_bottom, float clip_top) {
		renderGeometry();
	}

	protected void renderGeometry() {
	}

	protected abstract boolean isFocusable();

	public final float getRootX() {
		if (parent == null)
			return 0;
		return (parent.getRootX() + getX())*scale_x;
	}

	public final float getRootY() {
		if (parent == null)
			return 0;
		return (parent.getRootY() + getY())*scale_y;
	}

	private float transformX(float x) {
		return x/scale_x - getX();
	}

	private float transformY(float y) {
		return y/scale_y - getY();
	}

	protected final Renderable pick(float x, float y) {
		float trans_x = transformX(x);
		float trans_y = transformY(y);
		if (isFocusable() && trans_x >= 0 && trans_y >= 0 && trans_x < getWidth() && trans_y < getHeight()) {
			ListElement current = children.getFirst();
			while (current != null) {
				GUIObject gui = (GUIObject)current;
				Renderable picked = gui.pick(trans_x, trans_y);
				if (picked != null)
					return picked;
				current = current.getNext();
			}
			return this;
		}
		return null;
	}

	private void addTree() {
		if (getParentGUIRoot() != null) {
			doAdd();
			ListElement current = children.getFirst();
			while (current != null) {
				((Renderable)current).addTree();
				current = current.getNext();
			}
		}
	}

	final void removeTree() {
		doRemove();
		ListElement current = children.getFirst();
		while (current != null) {
			((Renderable)current).removeTree();
			current = current.getNext();
		}
	}

	protected void doRemove() {
	}

	public void remove() {
		boolean notify_remove = getParentGUIRoot() != null;
		if (parent != null) {
			parent.removeChild(this);
			if (notify_remove)
				removeTree();
		}
	}
}
