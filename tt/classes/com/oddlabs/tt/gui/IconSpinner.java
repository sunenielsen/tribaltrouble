package com.oddlabs.tt.gui;

import java.util.ResourceBundle;

import com.oddlabs.tt.form.DemoForm;
import com.oddlabs.tt.form.InGameDemoForm;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.tt.guievent.MouseButtonListener;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.ToolTip;
import com.oddlabs.util.Quad;
import com.oddlabs.tt.util.Utils;

public abstract strictfp class IconSpinner extends GUIObject implements ToolTip {
	private final IconQuad[] icon_quad;
	private final String tool_tip;
	private final Quad[] tool_tip_icons;
	private final TextField label;
	private final GUIObject button_plus;
	private final GUIObject button_minus;
	private final WorldViewer viewer;
	private IconDisabler icon_disabler = null;

	private String demo = null;

	private int text_count = 0;

	public IconSpinner(WorldViewer viewer, IconQuad[] icon_quad, String tool_tip, Quad[] tool_tip_icons, String shortcut_key) {
		this.icon_quad = icon_quad;
		this.tool_tip = tool_tip;
		this.tool_tip_icons = tool_tip_icons;
		this.viewer = viewer;
		ResourceBundle bundle = ResourceBundle.getBundle(IconSpinner.class.getName());
		setCanFocus(true);
		setDim(icon_quad[0].getWidth(), icon_quad[0].getHeight());

		String inc_str = Utils.getBundleString(bundle, "increase", new Object[]{shortcut_key});
		button_plus = new IconSpinnerButton(Skin.getSkin().getPlusButton(), inc_str, this);
		addChild(button_plus);
		button_plus.setPos(0, 0);
		button_plus.addMouseButtonListener(new IncreaseListener());

		String dec_str = Utils.getBundleString(bundle, "decrease", new Object[]{shortcut_key});
		button_minus = new IconSpinnerButton(Skin.getSkin().getMinusButton(), dec_str, this);
		addChild(button_minus);
		button_minus.setPos(button_plus.getWidth(), 0);
		button_minus.addMouseButtonListener(new DecreaseListener());

		label = new Label("", Skin.getSkin().getHeadlineFont(), icon_quad[0].getWidth(), Label.ALIGN_CENTER);
		addChild(label);
		label.setPos(0, (getHeight() - label.getHeight())/2);
	}

	public final void setFocus() {
		viewer.getGUIRoot().getDelegate().setFocus();
	}

	public final void setIconDisabler(IconDisabler icon_disabler) {
		this.icon_disabler = icon_disabler;
	}
	
	public final void setNag(String text) {
		demo = text;
	}

	private final boolean showNag() {
		return demo != null && !Renderer.isRegistered();
	}

	private final void addDemoForm() {
		ResourceBundle db = ResourceBundle.getBundle(DemoForm.class.getName());
		Form demo_form = new InGameDemoForm(viewer, Utils.getBundleString(db, "chicken_weapons_unavailable_header"), new GUIImage(512, 256, 0f, 0f, 1f, 1f, "/textures/gui/demo_chickenweapons"), Utils.getBundleString(db, "chicken_weapons_unavailable"));
		viewer.getGUIRoot().addModalForm(demo_form);
	}

	public final void doUpdate() {
		setCount();
		if (icon_disabler != null) {
			setDisabled(computeCount() == 0 && getOrderSize() == 0 && icon_disabler.isDisabled());
		}
	}

	public abstract int computeCount();
	protected abstract void increase(int amount);
	protected abstract void decrease(int amount);
	protected abstract void release();
	protected abstract int getOrderSize();
	protected abstract boolean renderInfinite();
	protected abstract float getProgress();

	private final void setCount() {
		int count = computeCount();
		if (count != text_count) {
			text_count = count;
			label.clear();
			if (text_count != 0 && !renderInfinite()) {
				label.append(text_count);
			}
		}			
	}

	public void appendToolTip(ToolTipBox tool_tip_box) {
		tool_tip_box.append(tool_tip);
		tool_tip_box.append(tool_tip_icons);
	}

	public final void shortcutPressed(boolean shift_down, boolean ctrl_down) {
		if (!isDisabled()) {
			if (showNag()) {
				addDemoForm();
			} else {
				int mouse_button;
				if (ctrl_down)
					mouse_button = LocalInput.RIGHT_BUTTON;
				else
					mouse_button = LocalInput.LEFT_BUTTON;

				if (shift_down)
					button_minus.mousePressedAll(mouse_button, 0, 0);
				else
					button_plus.mousePressedAll(mouse_button, 0, 0);
			}
		}
	}

	public final void shortcutReleased(boolean shift_down, boolean ctrl_down) {
		if (!isDisabled()) {
			if (showNag()) {
				addDemoForm();
			} else {
				release();
			}
		}
	}

	protected final void renderGeometry() {
		int x = (getWidth() - icon_quad[Skin.NORMAL].getWidth())/2;
		int y = (getHeight() - icon_quad[Skin.NORMAL].getHeight())/2;
		if (isDisabled())
			icon_quad[Skin.DISABLED].render(x, y);
		else if (isHovered())
			icon_quad[Skin.ACTIVE].render(x, y);
		else
			icon_quad[Skin.NORMAL].render(x, y);

		if (text_count > 0) {
			IconQuad[] watch = Icons.getIcons().getWatch();
			int index = (int)(getProgress()*(watch.length - 1));
			watch[index].render(getWidth() - watch[index].getWidth(),  getHeight() - watch[index].getHeight());
		}
	}

	protected final void mouseReleased(int button, int x, int y) {
	}

	protected final void mousePressed(int button, int x, int y) {
	}

	protected final void mouseClicked(int button, int x, int y, int clicks) {
	}

	protected final void mouseHeld(int button, int x, int y) {
	}

	private final strictfp class IncreaseListener implements MouseButtonListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
		}

		public final void mouseHeld(int button, int x, int y) {
			mousePressed(button, x, y);
		}

		public final void mousePressed(int button, int x, int y) {
			if (showNag()) {
				addDemoForm();
			} else {
				if (button == LocalInput.RIGHT_BUTTON)
					increase(10);
				else
					increase(1);
			}
		}

		public final void mouseReleased(int button, int x, int y) {
			release();
		}
	}

	private final strictfp class DecreaseListener implements MouseButtonListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
		}

		public final void mouseHeld(int button, int x, int y) {
			mousePressed(button, x, y);
		}

		public final void mousePressed(int button, int x, int y) {
			if (showNag()) {
				addDemoForm();
			} else {
				if (button == LocalInput.RIGHT_BUTTON)
					decrease(10);
				else
					decrease(1);
			}
		}

		public final void mouseReleased(int button, int x, int y) {
			release();
		}
	}
}
