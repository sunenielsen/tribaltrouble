package com.oddlabs.tt.gui;

import org.lwjgl.input.Keyboard;

import com.oddlabs.tt.guievent.FocusListener;
import com.oddlabs.tt.guievent.KeyListener;
import com.oddlabs.tt.guievent.MouseButtonListener;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.guievent.MouseMotionListener;
import com.oddlabs.tt.guievent.MouseWheelListener;
import com.oddlabs.tt.delegate.ModalDelegate;

public abstract strictfp class GUIObject extends Renderable {
	public final static int TOP_LEFT	 =  1;
	public final static int TOP_MID	  =  2;
	public final static int TOP_RIGHT	=  3;
	public final static int BOTTOM_LEFT  =  4;
	public final static int BOTTOM_MID   =  5;
	public final static int BOTTOM_RIGHT =  6;
	public final static int LEFT_TOP	 =  7;
	public final static int LEFT_MID	 =  8;
	public final static int LEFT_BOTTOM  =  9;
	public final static int RIGHT_TOP	= 10;
	public final static int RIGHT_MID	= 11;
	public final static int RIGHT_BOTTOM = 12;

	public final static int ORIGIN_TOP_LEFT	 = 0;
	public final static int ORIGIN_BOTTOM_RIGHT = 1;

	private boolean disabled;
	private boolean hovered;
	private boolean active;
	private boolean focus_cycle;
	private boolean can_focus;
	private int current_taborder = 0;
	private int tab_order;

	private GUIObject focused_child = null;
	private GUIObject next_hover = null;

	private final java.util.List mouse_click_listeners = new java.util.ArrayList();
	private final java.util.List mouse_button_listeners = new java.util.ArrayList();
	private final java.util.List mouse_motion_listeners = new java.util.ArrayList();
	private final java.util.List mouse_wheel_listeners = new java.util.ArrayList();
	private final java.util.List key_listeners = new java.util.ArrayList();
	private final java.util.List focus_listeners = new java.util.ArrayList();

	private boolean placed = false;
	private int origin;

	public GUIObject() {
		focus_cycle = false;
		can_focus = false;
		disabled = false;
	}

	public int translateXToLocal(int x) {
		GUIObject parent = (GUIObject)getParent();
		if (parent == null)
			return x - getX();
		else
			return parent.translateXToLocal(x) - getX();
	}

	public int translateYToLocal(int y) {
		GUIObject parent = (GUIObject)getParent();
		if (parent == null)
			return y - getY();
		else
			return parent.translateYToLocal(y) - getY();
	}

	public void correctPos(int dx, int dy) {
		setPos(getX() + dx, getY() + dy);
	}

	public final void place() {
		place(ORIGIN_TOP_LEFT);
	}

	public final void place(int origin_index) {
		assert !placed : "Object already placed";
		origin = origin_index;
		setPos(0, 0);
		placed = true;
	}

	public final void place(GUIObject neighbor, int direction) {
		place(neighbor, direction, Skin.getSkin().getFormData().getObjectSpacing());
	}

	public final void place(GUIObject neighbor, int direction, int spacing) {
		assert !placed : "Object already placed";
		int new_x = getXFromDirection(direction, spacing, neighbor.getX(), neighbor.getWidth());
		int new_y = getYFromDirection(direction, spacing, neighbor.getY(), neighbor.getHeight());

		origin = neighbor.origin;
		setPos(new_x, new_y);
		placed = true;
	}

	private final int getXFromDirection(int direction, int spacing, int neightbour_x, int neighbour_width) {
		switch (direction) {
			case BOTTOM_LEFT:
			case TOP_LEFT:
				return neightbour_x;
			case BOTTOM_MID:
			case TOP_MID:
				return neightbour_x + (neighbour_width - getWidth())/2;
			case BOTTOM_RIGHT:
			case TOP_RIGHT:
				return neightbour_x + neighbour_width - getWidth();
			case RIGHT_BOTTOM:
			case RIGHT_MID:
			case RIGHT_TOP:
				return neightbour_x + neighbour_width + spacing;
			case LEFT_BOTTOM:
			case LEFT_MID:
			case LEFT_TOP:
				return neightbour_x - getWidth() - spacing;
			default:
				throw new RuntimeException("Invalid direction");
		}
	}

	private final int getYFromDirection(int direction, int spacing, int neighbour_y, int neighbour_height) {
		switch (direction) {
			case BOTTOM_LEFT:
			case BOTTOM_MID:
			case BOTTOM_RIGHT:
				return neighbour_y - getHeight() - spacing;
			case TOP_LEFT:
			case TOP_RIGHT:
			case TOP_MID:
				return neighbour_y + neighbour_height + spacing;
			case RIGHT_BOTTOM:
			case LEFT_BOTTOM:
				return neighbour_y;
			case RIGHT_TOP:
			case LEFT_TOP:
				return neighbour_y + neighbour_height - getHeight();
			case LEFT_MID:
			case RIGHT_MID:
				return neighbour_y + (neighbour_height - getHeight())/2;
			default:
				throw new RuntimeException("Invalid direction");
		}
	}

	public final int getOrigin() {
		assert placed : "Object " + this + " compiled before being placed";
		return origin;
	}

	protected final void setFocusCycle(boolean cycle) {
		focus_cycle = cycle;
	}

	protected final void setCanFocus(boolean can_focus) {
		this.can_focus = can_focus;
	}

	public void setDisabled(boolean disabled) {
		if (this.disabled == disabled)
			return;

		this.disabled = disabled;
		GUIObject gui_child = (GUIObject)getFirstChild();
		while (gui_child != null) {
			gui_child.setDisabled(disabled);
			gui_child = (GUIObject)gui_child.getNext();
		}
		if (isFocused())
			focusNext();
	}

	public final boolean isActive() {
		return active;
	}

	private void setGlobalFocus() {
		GUIRoot gui_root = getParentGUIRoot();
		if (gui_root != null)
			gui_root.setGlobalFocus(this);
	}

	private GUIObject getGlobalFocus() {
		GUIRoot gui_root = getParentGUIRoot();
		if (gui_root != null)
			return gui_root.getGlobalFocus();
		else
			return null;
	}

	public final boolean isFocused() {
		return this == getGlobalFocus();
	}

	public final boolean isFocusable() {
		return can_focus;
	}

	public final boolean canFocus() {
		return can_focus && !disabled;
	}

	public final boolean isDisabled() {
		GUIObject parent = (GUIObject)getParent();
		if (parent != null)
			return disabled || parent.isDisabled();
		else
			return disabled;
	}

	public final boolean isHovered() {
		return hovered;
	}

	public final GUIObject getNextHover() {
		return next_hover;
	}

	protected final void setNextHover(GUIObject next_hover) {
		this.next_hover = next_hover;
	}

	public final GUIObject getFocusedChild() {
		return focused_child;
	}

	protected final void setGlobalFocused(GUIObject gui_object) {
		if (gui_object != null && gui_object != focused_child) {
			putFirst(gui_object);
		}
		focused_child = gui_object;
	}

	public void addChild(Renderable child) {
		super.addChild(child);
		GUIObject current;
		current = (GUIObject)child;
		current.setTabOrder(current_taborder);
		current_taborder++;
	}

	protected final void setTabOrder(int tab_order) {
		this.tab_order = tab_order;
	}

	protected final int getTabOrder() {
		return tab_order;
	}

	public final void removeChild(Renderable child) {
		GUIObject current;
		if (child == focused_child) {
			focused_child = null;
			// If child is in the current focused path, select new current focus
			current = (GUIObject)child;
			while (current.getFocusedChild() != null)
				current = current.getFocusedChild();
			if (current.isFocused())
				setGlobalFocus();
		}
		super.removeChild(child);
	}

	private final void switchFocusToFirstChild(int dir) {
		GUIObject gui_child = (GUIObject)getFirstChild();
		GUIObject min_obj = null;
		while (gui_child != null) {
			if (gui_child.canFocus() && (min_obj == null || dir*gui_child.getTabOrder() < dir*min_obj.getTabOrder()))
				min_obj = gui_child;
			gui_child = (GUIObject)gui_child.getNext();
		}
		if (min_obj != null)
			switchFocusToObject(min_obj, dir);
		else if (!focus_cycle)
			((GUIObject)getParent()).switchFocusToNextChild(dir);
	}

	private final void switchFocusToNextChild(int dir) {
		int tab_order = focused_child.getTabOrder();
		GUIObject gui_child = (GUIObject)getFirstChild();
		GUIObject greater_obj = null;
		GUIObject min_obj = null;
		while (gui_child != null) {
			if (gui_child.canFocus()) {
				if (min_obj == null || dir*gui_child.getTabOrder() < dir*min_obj.getTabOrder())
					min_obj = gui_child;
				if (dir*gui_child.getTabOrder() > dir*tab_order && (greater_obj == null || dir*gui_child.getTabOrder() < dir*greater_obj.getTabOrder()))
					greater_obj = gui_child;
			}
			gui_child = (GUIObject)gui_child.getNext();
		}
		if (greater_obj != null) {
			switchFocusToObject(greater_obj, dir);
		} else if (focus_cycle) {
			if (min_obj != null) {
				switchFocusToObject(min_obj, dir);
			}
		} else if (canFocus()) {
			switchFocusToObject(this, dir);
		} else {
			((GUIObject)getParent()).switchFocusToNextChild(dir);
		}
	}

	private final void switchFocusToObject(GUIObject obj, int dir) {
		if (obj instanceof Group) {
			((Group)obj).setGroupFocus(dir);
		} else {
			obj.setFocus();
		}
	}

	public final void switchFocus(int direction) {
		// find any GUIObject to focus
		if (focused_child == null) {
			switchFocusToFirstChild(direction);
			return;
		}
		// Find next GUIObject in tab_order
		switchFocusToNextChild(direction);
	}

	public final void focusNext() {
		switchFocus(1);
	}

	public final void focusPrior() {
		switchFocus(-1);
	}

	public final void defocusBranch() {
		if (getFocusedChild() != null)
			getFocusedChild().defocusBranch();
		focusNotifyAll(false);
		focused_child = null;
	}

	private final void refocusTree(GUIObject caller_child) {
		GUIObject parent = (GUIObject)getParent();
		if (parent != null) {
			parent.refocusTree(this);
		}
		if (getFocusedChild() != null && getFocusedChild() != caller_child) {
			getFocusedChild().defocusBranch();
		}

		if (!active)
			focusNotifyAll(true);
		focused_child = caller_child;;
	}

	public void setFocus() {
		GUIRoot gui_root = getParentGUIRoot();
		// Make sure we are linked to the root
		if (gui_root == null)
			return;
		if (isFocused() || !canFocus() || modalBlocked(gui_root)) {
			if (modalBlocked(gui_root)) {
				gui_root.swapFocusBackup(this);
			}
			return;
		}
		refocusTree(null);
		setGlobalFocus();
	}

	protected final void focusNotifyAll(boolean focus) {
		active = focus;
		focusNotify(focus);
		for (int i = 0; i < focus_listeners.size(); i++) {
			FocusListener listener = (FocusListener)focus_listeners.get(i);
			if (listener != null)
				listener.activated(focus);
		}
	}

	protected void focusNotify(boolean focus) {
	}

	private final boolean modalBlocked(GUIRoot gui_root) {
		ModalDelegate modal_delegate = gui_root.getModalDelegate();
		if (modal_delegate != null && !modalRelative(modal_delegate)) {
			return true;
		}
		return false;
	}

	private final boolean modalRelative(ModalDelegate modal_delegate) {
		if (this == modal_delegate) {
			return true;
		} else if (getParent() == null) {
			return false;
		} else if (getParent() == modal_delegate) {
			return true;
		} else {
			return ((GUIObject)getParent()).modalRelative(modal_delegate);
		}
	}

	protected int getCursorIndex() {
		return GUIRoot.CURSOR_NORMAL;
	}

	public boolean canHoverBehind() {
		return false;
	}

	public final void mouseScrolledAll(int amount) {
		if (disabled)
			return;
		mouseScrolled(amount);
		for (int i = 0; i < mouse_wheel_listeners.size(); i++) {
			MouseWheelListener listener = (MouseWheelListener)mouse_wheel_listeners.get(i);
			if (listener != null)
				listener.mouseScrolled(amount);
		}
	}

	protected void mouseScrolled(int amount) {
		GUIObject parent = (GUIObject)getParent();
		if (parent != null)
			parent.mouseScrolledAll(amount);
	}

	public final void mouseDraggedAll(int button, int x, int y, int relative_x, int relative_y, int absolute_x, int absolute_y) {
		if (disabled)
			return;
		mouseDragged(button, x, y, relative_x, relative_y, absolute_x, absolute_y);
		for (int i = 0; i < mouse_motion_listeners.size(); i++) {
			MouseMotionListener listener = (MouseMotionListener)mouse_motion_listeners.get(i);
			if (listener != null)
				listener.mouseDragged(button, x, y, relative_x, relative_y, absolute_x, absolute_y);
		}
	}

	protected void mouseDragged(int button, int x, int y, int relative_x, int relative_y, int absolute_x, int absolute_y) {
		// do not send this to parents, because it would move the form if unstopped
	}

	public final void mouseMovedAll(int x, int y) {
		if (disabled)
			return;
		mouseMoved(x, y);
		for (int i = 0; i < mouse_motion_listeners.size(); i++) {
			MouseMotionListener listener = (MouseMotionListener)mouse_motion_listeners.get(i);
			if (listener != null)
				listener.mouseMoved(x, y);
		}
	}

	protected void mouseMoved(int x, int y) {
		GUIObject parent = (GUIObject)getParent();
		if (parent != null)
			parent.mouseMovedAll(x, y);
	}

	public final void mouseExitedAll() {
		hovered = false;
		mouseExited();
		for (int i = 0; i < mouse_motion_listeners.size(); i++) {
			MouseMotionListener listener = (MouseMotionListener)mouse_motion_listeners.get(i);
			if (listener != null)
				listener.mouseExited();
		}
	}

	protected void mouseExited() {
		GUIObject parent = (GUIObject)getParent();
		if (parent != null)
			parent.mouseExitedAll();
	}

	public final void mouseEnteredAll() {
		hovered = true;
		mouseEntered();
		for (int i = 0; i < mouse_motion_listeners.size(); i++) {
			MouseMotionListener listener = (MouseMotionListener)mouse_motion_listeners.get(i);
			if (listener != null)
				listener.mouseEntered();
		}
	}

	protected void mouseEntered() {
		GUIObject parent = (GUIObject)getParent();
		if (parent != null)
			parent.mouseEnteredAll();
	}

	public final void mouseClickedAll(int button, int x, int y, int clicks) {
		if (disabled)
			return;
		mouseClicked(button, x, y, clicks);
		for (int i = 0; i < mouse_click_listeners.size(); i++) {
			MouseClickListener listener = (MouseClickListener)mouse_click_listeners.get(i);
			if (listener != null)
				listener.mouseClicked(button, x, y, clicks);
		}
	}

	protected void mouseClicked(int button, int x, int y, int clicks) {
		GUIObject parent = (GUIObject)getParent();
		if (parent != null)
			parent.mouseClickedAll(button, x, y, clicks);
	}

	public final void mouseReleasedAll(int button, int x, int y) {
		if (disabled)
			return;
		mouseReleased(button, x, y);
		for (int i = 0; i < mouse_button_listeners.size(); i++) {
			MouseButtonListener listener = (MouseButtonListener)mouse_button_listeners.get(i);
			if (listener  != null)
				listener.mouseReleased(button, x, y);
		}
	}

	protected void mouseReleased(int button, int x, int y) {
		GUIObject parent = (GUIObject)getParent();
		if (parent != null)
			parent.mouseReleasedAll(button, x, y);
	}

	public final void mousePressedAll(int button, int x, int y) {
		if (disabled)
			return;
		mousePressed(button, x, y);
		for (int i = 0; i < mouse_button_listeners.size(); i++) {
			MouseButtonListener listener = (MouseButtonListener)mouse_button_listeners.get(i);
			if (listener  != null)
				listener.mousePressed(button, x, y);
		}
	}

	protected void mousePressed(int button, int x, int y) {
		GUIObject parent = (GUIObject)getParent();
		if (parent != null)
			parent.mousePressedAll(button, x, y);
	}

	public final void mouseHeldAll(int button, int x, int y) {
		if (disabled)
			return;
		mouseHeld(button, x, y);
		for (int i = 0; i < mouse_button_listeners.size(); i++) {
			MouseButtonListener listener = (MouseButtonListener)mouse_button_listeners.get(i);
			if (listener  != null)
				listener.mouseHeld(button, x, y);
		}
	}

	protected void mouseHeld(int button, int x, int y) {
		GUIObject parent = (GUIObject)getParent();
		if (parent != null)
			parent.mouseHeldAll(button, x, y);
	}

	public final void keyPressedAll(KeyboardEvent event) {
		keyPressed(event);
		for (int i = 0; i < key_listeners.size(); i++) {
			KeyListener listener = (KeyListener)key_listeners.get(i);
			if (listener != null)
				listener.keyPressed(event);
		}
	}

	protected void keyPressed(KeyboardEvent event) {
		if (event.getKeyCode() == Keyboard.KEY_SPACE || event.getKeyCode() == Keyboard.KEY_RETURN) {
			mousePressedAll(LocalInput.LEFT_BUTTON, 0, 0);
		} else {
			GUIObject parent = (GUIObject)getParent();
			if (parent != null)
				parent.keyPressedAll(event);
		}
	}

	public final void keyReleasedAll(KeyboardEvent event) {
		keyReleased(event);
		for (int i = 0; i < key_listeners.size(); i++) {
			KeyListener listener = (KeyListener)key_listeners.get(i);
			if (listener != null)
				listener.keyReleased(event);
		}
	}

	protected void keyReleased(KeyboardEvent event) {
		if (event.getKeyCode() == Keyboard.KEY_SPACE || event.getKeyCode() == Keyboard.KEY_RETURN) {
			mouseReleasedAll(LocalInput.LEFT_BUTTON, 0, 0);
			mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
		} else {
			GUIObject parent = (GUIObject)getParent();
			if (parent != null)
				parent.keyReleasedAll(event);
		}
	}

	public final void keyRepeatAll(KeyboardEvent event) {
		keyRepeat(event);
		for (int i = 0; i < key_listeners.size(); i++) {
			KeyListener listener = (KeyListener)key_listeners.get(i);
			if (listener != null)
				listener.keyRepeat(event);
		}
	}

	protected void keyRepeat(KeyboardEvent event) {
		GUIObject parent = (GUIObject)getParent();
		if (parent != null)
			parent.keyRepeatAll(event);
	}

	public final void addMouseClickListener(MouseClickListener listener) {
		mouse_click_listeners.add(listener);
	}

	public final void addMouseButtonListener(MouseButtonListener listener) {
		mouse_button_listeners.add(listener);
	}

	public final void addMouseMotionListener(MouseMotionListener listener) {
		mouse_motion_listeners.add(listener);
	}

	public final void addMouseWheelListener(MouseWheelListener listener) {
		mouse_wheel_listeners.add(listener);
	}

	public final void addKeyListener(KeyListener listener) {
		key_listeners.add(listener);
	}

	public final void addFocusListener(FocusListener listener) {
		focus_listeners.add(listener);
	}
}
