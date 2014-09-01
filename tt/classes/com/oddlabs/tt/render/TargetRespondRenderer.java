package com.oddlabs.tt.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.landscape.LandscapeTargetRespond;
import com.oddlabs.tt.procedural.GeneratorRing;
import com.oddlabs.tt.resource.ResourceDescriptor;
import com.oddlabs.tt.resource.Resources;

public final strictfp class TargetRespondRenderer extends ShadowListRenderer {
	private final static float SHADOW_SIZE = 1.6f;
	private final Texture ring;

	private final List target_list = new ArrayList();

	public TargetRespondRenderer(ResourceDescriptor desc) {
		ring = ((Texture[])Resources.findResource(desc))[0];
	}

	public final void addToTargetList(LandscapeTargetRespond target) {
		if (Globals.process_shadows)
			target_list.add(target);
	}

	public final void renderShadows(LandscapeRenderer renderer) {
		setupShadows();
		GL11.glColor3f(0f, 1f, 0f);
		for (int i = 0; i < target_list.size(); i++) {
			LandscapeTargetRespond target = (LandscapeTargetRespond)target_list.get(i);
			target_list.set(i, null);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, ring.getHandle());
			renderShadow(renderer, SHADOW_SIZE*target.getProgress(), target.getPositionX(), target.getPositionY());
		}
		resetShadows();
		target_list.clear();
	}
}
