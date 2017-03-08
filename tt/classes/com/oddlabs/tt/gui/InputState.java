package com.oddlabs.tt.gui;

/* Team Penguin */
import java.util.concurrent.atomic.AtomicInteger;

import com.oddlabs.tt.animation.Updatable;
import com.oddlabs.tt.animation.TimerAnimation;
import com.oddlabs.tt.font.Index;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.gui.KeyboardEvent;
import com.oddlabs.tt.input.KeyboardInput;
import com.oddlabs.tt.input.PointerInput;

public final strictfp class InputState {
	private final static float MOUSE_REPEAT_DELAY = .5f;
	private final static float MOUSE_REPEAT_RATE = .05f;

	private final static float DOUBLE_CLICK_TIMEOUT = .4f;
	private final static int DOUBLE_CLICK_THRESHOLD = 2;

	private final TimerAnimation double_click_timer;
	private final TimerAnimation double_key_timer;
	private final GUIRoot gui_root;
	private TimerAnimation mouse_timer;

	private int drag_x;
	private int drag_y;
	private int absolute_drag_x;
	private int absolute_drag_y;
	private GUIObject drag_obj;
	private int drag_button;
	private GUIObject press_obj;
	private GUIObject clicked_obj;
	private int held_button;
	private int click_counter = 0;
	private int clicked_x;
	private int clicked_y;
	private KeyboardEvent key_event;
	private AtomicInteger key_counter = new AtomicInteger(0);

	// Keyboard handling
	private KeyboardEvent held_event;

	public InputState(GUIRoot gui_root) {
		this.gui_root = gui_root;
		double_click_timer = new TimerAnimation(new DoubleClickTimer(), 0);
		double_key_timer = new TimerAnimation(new DoubleKeyTimer(), 0);
		press_obj = gui_root;
	}

	private final GUIObject pick() {
		gui_root.mousePick();
		return gui_root.getCurrentGUIObject();
	}

	public final void mouseMoved(short x, short y) {
		GUIObject gui_hit = pick();
		gui_hit.mouseMovedAll(x, y);
	}

	private final void resetKeyTimer() {
		Index.resetBlinking();
	}

	public final void mouseScrolled(int dz) {
		GUIObject gui_hit = pick();
		int scroll_amount = StrictMath.round(dz*Globals.WHEEL_SCALE);
		gui_hit.setFocus();
		gui_hit.mouseScrolledAll(scroll_amount);
	}

	public final void mousePressed(int button) {
		GUIObject gui_hit = pick();
		int local_x = gui_hit.translateXToLocal(LocalInput.getMouseX());
		int local_y = gui_hit.translateYToLocal(LocalInput.getMouseY());
		Index.resetBlinking();
		drag_x = LocalInput.getMouseX();
		drag_y = LocalInput.getMouseY();
		absolute_drag_x = drag_x;
		absolute_drag_y = drag_y;
		if (drag_obj == null) {
			drag_obj = gui_hit;
			drag_button = button;
		}
		held_button = button;
		gui_hit.setFocus();
		press_obj = gui_hit;
		press_obj.mousePressedAll(button, local_x, local_y);
		if (mouse_timer != null)
			mouse_timer.stop();
		mouse_timer = new TimerAnimation(new Updatable() {
			public final void update(Object anim) {
				TimerAnimation timer = (TimerAnimation)anim;
				timer.setTimerInterval(MOUSE_REPEAT_RATE);
				timer.resetTime();
				if (press_obj == gui_root.getCurrentGUIObject()) {
					int local_x = press_obj.translateXToLocal(LocalInput.getMouseX());
					int local_y = press_obj.translateYToLocal(LocalInput.getMouseY());
					press_obj.mouseHeldAll(held_button, local_x, local_y);
				}
			}
		}, MOUSE_REPEAT_DELAY);

		mouse_timer.start();
	}

	public final void mouseReleased(int button) {
		GUIObject gui_hit = pick();
		int local_x = gui_hit.translateXToLocal(LocalInput.getMouseX());
		int local_y = gui_hit.translateYToLocal(LocalInput.getMouseY());
		Index.resetBlinking();
		if (button == drag_button)
			drag_obj = null;
		if (press_obj == null)
			return;
		if (gui_hit == press_obj) {
			if (gui_hit != clicked_obj || !clickedSameArea()) {
				if (double_click_timer.isRunning()) {
					stopDoubleClickTimer();
				}
				double_click_timer.setTimerInterval(DOUBLE_CLICK_TIMEOUT);
				double_click_timer.start();
			}
			click_counter++;
			clicked_obj = gui_hit;
			clicked_x = LocalInput.getMouseX();
			clicked_y = LocalInput.getMouseY();
			press_obj.mouseClickedAll(button, local_x, local_y, click_counter);
		}
		press_obj.mouseReleasedAll(button, local_x, local_y);
		if (mouse_timer != null)
			mouse_timer.stop();
	}

	public final void mouseDragged(int button, short x, short y) {
		if (drag_obj != null)
			drag_obj.mouseDraggedAll(button, x, y, x - drag_x, y - drag_y, x - absolute_drag_x, y - absolute_drag_y);
		drag_x = x;
		drag_y = y;
	}

	private final boolean clickedSameArea() {
		return StrictMath.abs(LocalInput.getMouseX()- clicked_x) < DOUBLE_CLICK_THRESHOLD && StrictMath.abs(LocalInput.getMouseY() - clicked_y) < DOUBLE_CLICK_THRESHOLD;
	}

	private final void stopDoubleClickTimer() {
		double_click_timer.stop();
		double_click_timer.resetTime();
		clicked_obj = null;
		click_counter = 0;
	}

	private final void stopDoubleKeyTimer() {
		double_key_timer.stop();
		double_key_timer.resetTime();
		key_event = null;
		key_counter.set(0);
	}

	public final void keyTyped( int key_code, char key_char) {
		GUIObject focused = gui_root.getGlobalFocus();
		KeyboardEvent event = new KeyboardEvent(key_code, key_char, LocalInput.isShiftDownCurrently(), LocalInput.isControlDownCurrently());
		focused.keyRepeatAll(event);
	}

	public final void keyPressed(int key_code, char key_char, boolean shift_down, boolean control_down, boolean menu_down, boolean repeat) {
		GUIObject focused = gui_root.getGlobalFocus();
		resetKeyTimer();
		if (!repeat && (key_event == null 
				|| key_event.getKeyCode() != key_code
				|| key_event.getKeyChar() != key_char
				|| key_event.isShiftDown() != shift_down
				|| key_event.isControlDown() != control_down)) {
			if (double_key_timer.isRunning()) {
				stopDoubleKeyTimer();
			}
			double_key_timer.setTimerInterval(DOUBLE_CLICK_TIMEOUT);
			double_key_timer.start();
		}
		if (!repeat)
			key_counter.getAndIncrement();
/* End Penguin */
		KeyboardEvent event = new KeyboardEvent(key_code, key_char, shift_down, control_down, key_counter);
		key_event = event;

		if (!repeat)
			focused.keyPressedAll(event);
		focused.keyRepeatAll(event);
	}

	public final void keyReleased(int key_code, char key_char, boolean shift_down, boolean control_down, boolean menu_down) {
		GUIObject focused = gui_root.getGlobalFocus();
		resetKeyTimer();
		KeyboardEvent event = new KeyboardEvent(key_code, key_char, shift_down, control_down);
		focused.keyReleasedAll(event);
	}

	private final strictfp class DoubleClickTimer implements Updatable {
		public final void update(Object anim) {
			stopDoubleClickTimer();
		}
	}

	private final strictfp class DoubleKeyTimer implements Updatable {
		public final void update(Object anim) {
			stopDoubleKeyTimer();
		}
	}

}
