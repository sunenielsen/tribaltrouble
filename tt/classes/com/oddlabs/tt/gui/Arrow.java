package com.oddlabs.tt.gui;

import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.landscape.HeightMap;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.StrictVector4f;
import com.oddlabs.tt.util.StrictMatrix4f;

public final strictfp class Arrow extends GUIObject {
	private final static float SECONDS_PER_FLASH = .5f;
	private final static float COLOR_DELTA = .5f;
	
	private final float target_x;
	private final float target_y;
	private final float target_z;
	private final float r;
	private final float g;
	private final float b;
	private final boolean show_always;
	private final GUIRoot gui_root;

	public Arrow(HeightMap heightmap, GUIRoot gui_root, float target_x, float target_y, float r, float g, float b, boolean show_always) {
		this.gui_root = gui_root;
		this.target_x = target_x;
		this.target_y = target_y;
		this.target_z = heightmap.getNearestHeight(target_x, target_y);
		this.r = r;
		this.g = g;
		this.b = b;
		this.show_always = show_always;
		displayChangedNotify(LocalInput.getViewWidth(), LocalInput.getViewHeight());
	}

	protected final void displayChangedNotify(int width, int height) {
		setDim(width, height);
	}

	private final static StrictVector4f point = new StrictVector4f();
	private StrictVector4f project3DTo2D(float x, float y, float z) {
		point.set(x,y,z,1);
		StrictMatrix4f.transform(gui_root.getDelegate().getCamera().getState().getProjectionModelView(), point, point);
		if (point.w < .1f)
			point.w = .1f;
		float inv_w = 1/point.w;
		point.set((point.x*inv_w + 1)*.5f*LocalInput.getViewWidth(), (point.y*inv_w + 1)*.5f*LocalInput.getViewHeight());
		return point;
	}

	protected final void renderGeometry() {
		StrictVector4f result = project3DTo2D(target_x, target_y, target_z);
		float x = result.x;
		float y = result.y;
		float dx = x - LocalInput.getViewWidth()/2f;
		float dy = y - LocalInput.getViewHeight()/2f;
		float dist_sqr = dx*dx + dy*dy;
		float inv_dist = 0;
		if (dist_sqr < 1f) {
			dx = 1f;
			dy = 0f;
		} else {
			inv_dist = 1f/(float)Math.sqrt(dist_sqr);
			dx *= inv_dist;
			dy *= inv_dist;
		}

		float angle = (float)StrictMath.toDegrees(StrictMath.acos(dx));
		if (dy < 0f)
			angle = 360f - angle;
		float real_t = (x - LocalInput.getViewWidth()/2f)/dx;
		float t = real_t;
		float t_min_x = (-LocalInput.getViewWidth()/2f)/dx;
		float t_max_x = (LocalInput.getViewWidth()/2f)/dx;
		float t_x = StrictMath.max(t_min_x, t_max_x);
		t = StrictMath.min(t, t_x);
		float t_min_y = (-LocalInput.getViewHeight()/2f)/dy;
		float t_max_y = (LocalInput.getViewHeight()/2f)/dy;
		float t_y = StrictMath.max(t_min_y, t_max_y);
		t = StrictMath.min(t, t_y);
		if (show_always || gui_root.getDelegate().getCamera().getState().inNoDetailMode() || t < real_t) {
			NotifyArrowData data = Icons.getIcons().getNotifyArrowData();
			float head_x = data.getHeadX();
			float head_y = data.getHeadY();
			GL11.glEnd();
			GL11.glPushMatrix();
			GL11.glTranslatef(LocalInput.getViewWidth()/2f + dx*t, LocalInput.getViewHeight()/2f + dy*t, 0f);
			GL11.glRotatef(angle, 0f, 0f, 1f);
			float val = (LocalEventQueue.getQueue().getTime()%SECONDS_PER_FLASH)/(SECONDS_PER_FLASH*.5f);
			if (val > 1f)
				val = 2f - val;
			val = COLOR_DELTA*val;
			GL11.glColor4f(r, g, b, 1f - val);
			GL11.glBegin(GL11.GL_QUADS);
			data.getArrow().render(-head_x, -head_y, data.getArrow().getWidth(), data.getArrow().getHeight());
			GL11.glEnd();
			GL11.glPopMatrix();
			GL11.glColor4f(1f, 1f, 1f, 1f);
			GL11.glBegin(GL11.GL_QUADS);
		}
	}
}

