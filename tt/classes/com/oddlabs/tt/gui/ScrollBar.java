package com.oddlabs.tt.gui;

import org.lwjgl.input.Keyboard;

import com.oddlabs.tt.guievent.KeyListener;
import com.oddlabs.tt.guievent.MouseButtonListener;
import com.oddlabs.tt.guievent.MouseMotionListener;

public final strictfp class ScrollBar extends GUIObject {
	private final Group focus_group;
	private final ArrowButton less_button;
	private final ArrowButton more_button;
	private final ScrollButton scroll_button;
	private final Scrollable owner;

	public ScrollBar(int height, Scrollable owner) {
		this.owner = owner;
		focus_group = new Group();
		less_button = new ArrowButton(Skin.getSkin().getScrollBarData().getScrollDownButtonPressed(),
									  Skin.getSkin().getScrollBarData().getScrollDownButtonUnpressed(),
									  Skin.getSkin().getScrollBarData().getScrollDownArrow());
		more_button = new ArrowButton(Skin.getSkin().getScrollBarData().getScrollUpButtonPressed(),
									  Skin.getSkin().getScrollBarData().getScrollUpButtonUnpressed(),
									  Skin.getSkin().getScrollBarData().getScrollUpArrow());
		less_button.setPos(0, 0);
		more_button.setPos(0, height - more_button.getHeight());
		focus_group.addChild(more_button);

		less_button.addMouseButtonListener(new LessListener());
		more_button.addMouseButtonListener(new MoreListener());

		setDim(less_button.getWidth(), height);
		setCanFocus(true);

		scroll_button = new ScrollButton();
		focus_group.addChild(scroll_button);
		focus_group.addChild(less_button); // add here to secure proper tabbing order
		DragListener drag_listener = new DragListener();
		scroll_button.addMouseMotionListener(drag_listener);
		scroll_button.addMouseButtonListener(drag_listener);
		scroll_button.addKeyListener(new ButtonKeyListener());

		focus_group.setDim(getWidth(), getHeight());
		focus_group.setPos(0, 0);
		addChild(focus_group);
	}

	public final void update() {
		if (scroll_button != null)
			scroll_button.setupPos(this);
	}

	public final void setPos(int x, int y) {
		super.setPos(x, y);
		if (scroll_button != null)
			scroll_button.setupPos(this);
	}

	public final void setDim(int width, int height) {
		super.setDim(width, height);
		if (scroll_button != null)
			scroll_button.setupPos(this);
	}

	public final void setFocus() {
		focus_group.setGroupFocus(LocalInput.isShiftDownCurrently() ? -1 : 1);
	}

	protected final void renderGeometry() {
		ScrollBarData data = Skin.getSkin().getScrollBarData();
		Vertical scroll_bar = data.getScrollBar();
		scroll_bar.render(0, less_button.getHeight(), getHeight() - less_button.getHeight() - more_button.getHeight(), Skin.NORMAL);
	}

	public final int getButtonX() {
		return Skin.getSkin().getScrollBarData().getLeftOffset();
	}

	public final int getButtonY() {
		ScrollBarData data = Skin.getSkin().getScrollBarData();
		int max_height = getHeight() - less_button.getHeight() - more_button.getHeight() - data.getBottomOffset() - data.getTopOffset();
		int size = getButtonHeight();
		int offset = max_height - size - (int)((max_height - size) * owner.getScrollBarOffset());
		return less_button.getHeight() + data.getBottomOffset() + offset;
	}

	public final int getButtonHeight() {
		ScrollBarData data = Skin.getSkin().getScrollBarData();
		int max_height = getHeight() - less_button.getHeight() - more_button.getHeight() - data.getBottomOffset() - data.getTopOffset();
		float ratio = owner.getScrollBarRatio();
		int size = (int)(ratio*max_height);
		if (size < data.getScrollButton().getMinHeight())
			size = data.getScrollButton().getMinHeight();
		return size;
	}

	protected final void mouseClicked(int button, int x, int y, int clicks) {
		int button_y = getButtonY();
		owner.jumpPage(y > button_y);
		scroll_button.setupPos(this);
	}

	private final strictfp class LessListener implements MouseButtonListener {
		public final void mousePressed(int button, int x, int y) {
			owner.setOffsetY(owner.getOffsetY() + owner.getStepHeight());
			scroll_button.setupPos(ScrollBar.this);
		}

		public final void mouseReleased(int button, int x, int y) {}
		public final void mouseHeld(int button, int x, int y) {}
		public final void mouseClicked(int button, int x, int y, int clicks) {}
	}

	private final strictfp class MoreListener implements MouseButtonListener {
		public final void mousePressed(int button, int x, int y) {
			owner.setOffsetY(owner.getOffsetY() - owner.getStepHeight());
			scroll_button.setupPos(ScrollBar.this);
		}

		public final void mouseReleased(int button, int x, int y) {}
		public final void mouseHeld(int button, int x, int y) {}
		public final void mouseClicked(int button, int x, int y, int clicks) {}
	}

	private final strictfp class DragListener implements MouseMotionListener, MouseButtonListener {
		ScrollBarData data = Skin.getSkin().getScrollBarData();
		float start_offset;

		public final void mousePressed(int button, int x, int y) {
			start_offset = owner.getScrollBarOffset();
		}

		public final void mouseDragged(int button, int x, int y, int rel_x, int rel_y, int abs_x, int abs_y) {
			int max_height = getHeight() - less_button.getHeight() - more_button.getHeight() - data.getBottomOffset() - data.getTopOffset();
			float ratio = owner.getScrollBarRatio();
			int size = (int)(ratio*max_height);
			int scroll_button_space = max_height - size;
			owner.setScrollBarOffset(start_offset - abs_y/(float)scroll_button_space);
			scroll_button.setupPos(ScrollBar.this);
		}

		public final void mouseMoved(int x, int y) {}
		public final void mouseEntered() {}
		public final void mouseExited() {}
		public final void mouseReleased(int button, int x, int y) {}
		public final void mouseHeld(int button, int x, int y) {}
		public final void mouseClicked(int button, int x, int y, int clicks) {}
	}

	private final strictfp class ButtonKeyListener implements KeyListener {
		public final void keyRepeat(KeyboardEvent event) {
			switch (event.getKeyCode()) {
				case Keyboard.KEY_UP:
					owner.setOffsetY(owner.getOffsetY() - owner.getStepHeight());
					scroll_button.setupPos(ScrollBar.this);
					break;
				case Keyboard.KEY_DOWN:
					owner.setOffsetY(owner.getOffsetY() + owner.getStepHeight());
					scroll_button.setupPos(ScrollBar.this);
					break;
				default:
					break;
			}
		}

		public final void keyPressed(KeyboardEvent event) {}
		public final void keyReleased(KeyboardEvent event) {}
	}
}
