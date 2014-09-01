package com.oddlabs.texturegenerator;

import java.io.File;

import org.lwjgl.opengl.*;
import org.lwjgl.*;

import com.oddlabs.geometry.LowDetailModel;
import com.oddlabs.tt.render.SpriteList;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.render.BillboardPainter;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.resource.SpriteFile;
import com.oddlabs.tt.util.OffscreenRenderer;
import com.oddlabs.tt.util.OffscreenRendererFactory;
import com.oddlabs.tt.util.GLStateStack;
import com.oddlabs.util.Utils;

public final strictfp class TextureGenerator {
	private final static int LOW_DETAIL_TEX_SIZE = 256;
	private final static int LOWDETAIL_MIPMAP_CUTOFF = Globals.NO_MIPMAP_CUTOFF;
	private final static int CROWN_MIPMAP_CUTOFF = Globals.NO_MIPMAP_CUTOFF;
	
	private SpriteList[] crowns;
	private SpriteList[] trunks;
	
	public static void main(String[] args) throws LWJGLException {
		assert args.length == 1;
		new TextureGenerator(args[0]);
	}

	public TextureGenerator(String dest) throws LWJGLException {
		Settings.setSettings(new Settings());
		File path = new File(dest);
		path.mkdirs();

		GLStateStack display_state_stack = new GLStateStack();
		GLStateStack.setCurrent(display_state_stack);

		Display.setDisplayMode(new DisplayMode(LOW_DETAIL_TEX_SIZE, LOW_DETAIL_TEX_SIZE));
		Display.create(new PixelFormat(Globals.VIEW_BIT_DEPTH, 1, 16, 0, 0));
		OffscreenRendererFactory factory = new OffscreenRendererFactory();
		OffscreenRenderer buffer = factory.createRenderer(LOW_DETAIL_TEX_SIZE, LOW_DETAIL_TEX_SIZE, new PixelFormat(Globals.VIEW_BIT_DEPTH, 1, 16, 0, 0), false, false, true);
		SpriteList jungle_crown = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/jungle_tree_crown.binsprite", CROWN_MIPMAP_CUTOFF, false, false, true, false));
		SpriteList jungle_trunk = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/jungle_tree_trunk.binsprite", CROWN_MIPMAP_CUTOFF, true, true, false, false));
		SpriteList palm_crown = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/palm_crown.binsprite", CROWN_MIPMAP_CUTOFF, false, false, true, false));
		SpriteList palm_trunk = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/palm_trunk.binsprite", CROWN_MIPMAP_CUTOFF, true, true, false, false));
		crowns = new SpriteList[]{jungle_crown, palm_crown};
		trunks = new SpriteList[]{jungle_trunk, palm_trunk};
		LowDetailModel jungle_lowdetail = (LowDetailModel)Utils.loadObject(Utils.makeURL("/geometry/misc/tree_low.binlowdetail"));
		LowDetailModel palm_lowdetail = (LowDetailModel)Utils.loadObject(Utils.makeURL("/geometry/misc/palm_low.binlowdetail"));
		generateLowDetailTexture(buffer, new LowDetailModel[]{jungle_lowdetail, palm_lowdetail}, dest + "/lowdetail_tree");
		
		OffscreenRenderer viking_buffer = factory.createRenderer(LOW_DETAIL_TEX_SIZE, LOW_DETAIL_TEX_SIZE, new PixelFormat(Globals.VIEW_BIT_DEPTH, 1, 16, 0, 0), false, false, true);
		SpriteList oak_crown = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/oak_tree_crown.binsprite", CROWN_MIPMAP_CUTOFF, false, false, true, false));
		SpriteList oak_trunk = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/oak_tree_trunk.binsprite", CROWN_MIPMAP_CUTOFF, true, true, false, false));
		SpriteList pine_crown = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/pine_tree_crown.binsprite", CROWN_MIPMAP_CUTOFF, false, false, true, false));
		SpriteList pine_trunk = (SpriteList)Resources.findResource(new SpriteFile("/geometry/misc/pine_tree_trunk.binsprite", CROWN_MIPMAP_CUTOFF, true, true, false, false));
		crowns = new SpriteList[]{oak_crown, pine_crown};
		trunks = new SpriteList[]{oak_trunk, pine_trunk};
		LowDetailModel oak_lowdetail = (LowDetailModel)Utils.loadObject(Utils.makeURL("/geometry/misc/oak_tree_low.binlowdetail"));
		LowDetailModel pine_lowdetail = (LowDetailModel)Utils.loadObject(Utils.makeURL("/geometry/misc/pine_tree_low.binlowdetail"));
		generateLowDetailTexture(viking_buffer, new LowDetailModel[]{oak_lowdetail, pine_lowdetail}, dest + "/viking_lowdetail_tree");
		Display.destroy();
	}


	private final void generateLowDetailTexture(OffscreenRenderer buffer, LowDetailModel[] models, String dest) {
		int[] indices = new int[models.length];
		for (int i = 0; i < models.length; i++) {
			indices[i] = i;
		}
		do {
			drawBillboardsToBuffer(models, this, indices, buffer, Globals.COMPRESSED_RGBA_FORMAT, LOWDETAIL_MIPMAP_CUTOFF, 0);
			buffer.dumpToFile(dest);
		} while (buffer.isLost());
	}

	private final static void generateBillboardMip(LowDetailModel lowdetail, TextureGenerator renderer, int mode, float ortho_size, int tex_index) {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0f, ortho_size, 0.0f, ortho_size, -50.0f, 50.0f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		BillboardPainter.init();
		for (int i = 0; i < lowdetail.getIndices().length/3; i++) {
			BillboardPainter.loadFaceMatrixAndClipPlanes(i, lowdetail.getIndices(), lowdetail.getVertices(), lowdetail.getTexCoords());
			renderer.renderModel(mode, tex_index);
//buffer.dumpToFile("test_bill" + i + ".image");
//GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		}
		BillboardPainter.finish();
	}

	private final static void drawBillboardsToBuffer(LowDetailModel[] lowdetails, TextureGenerator renderer, int[] modes, OffscreenRenderer buffer, int format, int mipmap_cutoff, int tex_index) {
		int ortho_size = 1;
		float[] clear_color = renderer.getModelClearColor();
		GL11.glClearColor(clear_color[0], clear_color[1], clear_color[2], 0f);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		for (int i = 0; i < lowdetails.length; i++)
			generateBillboardMip(lowdetails[i], renderer, modes[i], ortho_size, tex_index);
	}

	public final float[] getModelClearColor() {
		return trunks[0].getClearColor();
	}

	public final void renderModel(int mode, int tex_index) {
		trunks[mode].renderModel(tex_index);
		crowns[mode].renderModel(tex_index);
	}

}
