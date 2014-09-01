package com.oddlabs.tt.render;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.resource.GLIntImage;
import com.oddlabs.tt.util.Target;

public final strictfp class BuildingSiteRenderer extends ShadowRenderer {
	private final Texture green;

	public BuildingSiteRenderer() {
		GLIntImage img = new GLIntImage(16, 16, GL11.GL_RGBA);
		for (int y = 1; y < img.getHeight() - 1; y++)
			for (int x = 1; x < img.getWidth() - 1; x++)
				img.putPixel(x, y, 0xffffffff);
		green = new Texture(new GLIntImage[]{img}, GL11.GL_RGBA, GL11.GL_LINEAR, GL11.GL_LINEAR, GL11.GL_CLAMP, GL11.GL_CLAMP);
	}

	public final void renderSites(LandscapeRenderer renderer, List targets, float center_x, float center_y, float max_radius) {
		setupShadows();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, green.getHandle());
		float radius_sqr = max_radius*max_radius;
		for (int i = 0; i < targets.size(); i++) {
			Target target = (Target)targets.get(i);
			float dx = target.getPositionX() - center_x;
			float dy = target.getPositionY() - center_y;
			float a = (dx*dx + dy*dy)/radius_sqr;
			if (dx == 0f && dy == 0f)
				GL11.glColor4f(1f, 1f, 1f, 1f);
			else
				GL11.glColor4f(0f, 1f, 0f, 1 - a*a);
			renderShadow(renderer, 2f, target.getPositionX(), target.getPositionY());
		}
		resetShadows();
	}
}
