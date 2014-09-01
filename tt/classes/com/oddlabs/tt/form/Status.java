package com.oddlabs.tt.form;

import com.oddlabs.tt.animation.AnimationManager;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.font.TextLineRenderer;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.TextField;
import com.oddlabs.tt.model.Element;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.pathfinder.UnitGrid;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.resource.NativeResource;

public final strictfp class Status {
	private final TextLineRenderer text_renderer = new TextLineRenderer(Skin.getSkin().getEditFont());
	private final StringBuffer buf = new StringBuffer();
	private final GUIRoot gui_root;

	public Status(GUIRoot gui_root) {
		this.gui_root = gui_root;
	}

	public final void render(float clip_left, float clip_right, float clip_bottom, float clip_top) {
		long free_mem = Runtime.getRuntime().freeMemory();
		buf.delete(0, buf.length());
		if (Settings.getSettings().inDeveloperMode()) {
			buf.append("TPF ");
			TextField.appendNumberToStringBuffer(Renderer.getTrianglesRendered(), buf);
			buf.append(" JHeap ");
			TextField.appendNumberToStringBuffer(free_mem, buf);
			buf.append("(");
			int total_jheap = (int)(Runtime.getRuntime().totalMemory()/(1024*1024));
			TextField.appendNumberToStringBuffer(total_jheap, buf);
			buf.append("M) globj ");
			TextField.appendNumberToStringBuffer(NativeResource.getCount(), buf);
/*			float x = gui_root.getLandscapeLocationX();
			float y = gui_root.getLandscapeLocationY();
			if (UnitGrid.getGrid() != null) {
				int grid_x = UnitGrid.getGrid().toGridCoordinate(x);
				int grid_y = UnitGrid.getGrid().toGridCoordinate(y);
				buf.append(" X ");
				TextField.appendNumberToStringBuffer(grid_x, buf);
				buf.append(" Y ");
				TextField.appendNumberToStringBuffer(grid_y, buf);
			}*/
		}
		buf.append(" FPS ");
		TextField.appendNumberToStringBuffer(StrictMath.round(1000f/Renderer.getFPS()), buf);
		buf.append(" (");
		TextField.appendNumberToStringBuffer(StrictMath.round(Renderer.getFPS()), buf);
		buf.append(" ms/frame)");

		text_renderer.renderCropped(0, 0, clip_left, clip_right, clip_bottom, clip_top, buf);
	}
}
