package com.oddlabs.tt.resource;

import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.util.GLState;

import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL11;

public final strictfp class StructureBlend extends BlendInfo {
	private final Texture structure_map;

	private final Texture createStructureMap(GLIntImage structure_image) {
		return new Texture(new GLIntImage[]{structure_image}, GL11.GL_RGB, GL11.GL_LINEAR, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT);
	}

	public StructureBlend(GLIntImage structure_image, GLByteImage alpha_image) {
		super(alpha_image, Globals.COMPRESSED_A_FORMAT);
		structure_map = createStructureMap(structure_image);
	}

	private final void bindStructure() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, structure_map.getHandle());
	}

	public final void setup() {
		GLState.activeTexture(GL13.GL_TEXTURE1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		bindAlpha();
		GLState.activeTexture(GL13.GL_TEXTURE0);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		bindStructure();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	public final void reset() {
		GLState.activeTexture(GL13.GL_TEXTURE1);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GLState.activeTexture(GL13.GL_TEXTURE0);

	}
}
