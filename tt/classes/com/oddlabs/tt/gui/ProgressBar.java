package com.oddlabs.tt.gui;

import java.util.ResourceBundle;

import org.lwjgl.opengl.Display;

import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.font.TextLineRenderer;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.util.Quad;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.util.Utils;

public final strictfp class ProgressBar extends GUIObject {
	private final ProgressBarInfo[] info;
	private final boolean text_only;
	
	private final TextLineRenderer text_renderer = new TextLineRenderer(Skin.getSkin().getHeadlineFont());
	private final GUI gui;
	private final NetworkSelector network;
	private int left_margin;
	private int right_margin;

	private int index;
	private float step;

	public ProgressBar(NetworkSelector network, GUI gui, int width, ProgressBarInfo[] info, boolean text_only) {
		this.info = info;
		this.network = network;
		this.text_only = text_only;
		this.gui = gui;
		if (text_only) {
			setDim(width, Skin.getSkin().getHeadlineFont().getHeight());
		} else {
			ProgressBarData data = Skin.getSkin().getProgressBarData();
			left_margin = data.getLeftFill()[Skin.NORMAL].getWidth();
			right_margin = data.getRightFill()[Skin.NORMAL].getWidth();
			assert width > left_margin + right_margin : "Progress bar too small.";
			setDim(width, data.getProgressBar().getHeight());
		}
		pixelize(info, width);
		setCanFocus(false);
	}

	public final void progress() {
		assert index < info.length: "Too much progress";
		index++;
		step = 0;
//		Renderable prior_label = (Renderable)getLastChild();
//		if (prior_label != null)
//			removeChild(prior_label);
//		if (index < info.length) {
//			Label label = info[index].getLabel();
//			addChild(label);
//			label.setPos(0, 0);
//		}
		update();
	}

	public final void progress(float fraction) {
		int current = 0;
		if (index > 0)
			current = info[index - 1].getWaypoint();

		step += fraction*(info[index].getWaypoint() - current);
		if (step > info[index].getWaypoint() - current) {
			step = info[index].getWaypoint() - current;
		}

//		assert step <= info[index].getWaypoint() - current : "Too many steps.";
		update();
	}

	private final void renderText(float clip_left, float clip_right, float clip_bottom, float clip_top) {
		int offset = 0;
		if (index > 0)
			offset = info[index - 1].getWaypoint();
		float done = (offset + step)/getWidth();
		ResourceBundle bundle = ResourceBundle.getBundle(ProgressBar.class.getName());
		int percentage = ((int)(done*100));
		String string = Utils.getBundleString(bundle, "loading", new Object[]{new Integer(percentage)});
		text_renderer.renderCropped(0, 0, clip_left, clip_right, clip_bottom, clip_top, new StringBuffer(string));
	}

	protected final void renderGeometry(float clip_left, float clip_right, float clip_bottom, float clip_top) {
		if (text_only)
			renderText(clip_left, clip_right, clip_bottom, clip_top);
		else {
			ProgressBarData data = Skin.getSkin().getProgressBarData();
			Horizontal progress_bar = data.getProgressBar();
			progress_bar.render(0, 0, getWidth(), Skin.NORMAL);
			renderFill(0);
		}
	}

	private final void pixelize(ProgressBarInfo[] info, int width) {
		float sum = 0;
		for (int i = 0; i < info.length; i++) {
			sum += info[i].getWeight();
		}

		info[0].setWaypoint((int)((info[0].getWeight()/sum)*width));
		for (int i = 1; i < info.length - 1; i++) {
			info[i].setWaypoint(info[i - 1].getWaypoint() + (int)((info[i].getWeight()/sum)*width));
		}
		info[info.length - 1].setWaypoint(width);

		for (int i = 0; i < info.length; i++) {
			if (info[i].getWaypoint() > width - right_margin) {
				info[i].setWaypoint(width - right_margin);
			} else if (info[i].getWaypoint() < left_margin) {
				info[i].setWaypoint(left_margin);
			}
		}
	}

	private final void renderFill(int y) {
		if (index == 0 && step < left_margin)
			return;
		ProgressBarData data = Skin.getSkin().getProgressBarData();
		Quad[] left = data.getLeftFill();
		Quad[] center = data.getCenterFill();
		Quad[] right = data.getRightFill();

		left[Skin.NORMAL].render(0, y);
		int offset = 0;
		if (index > 0)
			offset = info[index - 1].getWaypoint();
		center[Skin.NORMAL].render(left_margin, y, offset - left_margin + (int)step, center[Skin.NORMAL].getHeight());
		if (index == info.length) {
			right[Skin.NORMAL].render(info[index - 1].getWaypoint(), y);
		}
	}

	private final void update() {
		Renderer.clearScreen();
		gui.renderGUI();
		network.tick();
		Display.update();
	}
}
