package com.oddlabs.tt.resource;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public final strictfp class FogInfo {
	private final FloatBuffer fog_color;
	private final int fog_mode;
	private final float fog_height_factor;
	private final float fog_density;
	private final float fog_start;
	private final float fog_end;

	public FogInfo(float[] fog_color_array, int fog_mode, float fog_height_factor, float fog_density, float fog_start, float fog_end) {
		this.fog_color = BufferUtils.createFloatBuffer(4);
		this.fog_color.put(fog_color_array).flip();
		this.fog_height_factor = fog_height_factor;
		this.fog_mode = fog_mode;
		this.fog_density = fog_density;
		this.fog_start = fog_start;
		this.fog_end = fog_end;
	}

	public final void enableFog(float camera_z) {
		GL11.glFog(GL11.GL_FOG_COLOR, fog_color);
		GL11.glFogf(GL11.GL_FOG_MODE, fog_mode);
		GL11.glFogf(GL11.GL_FOG_START, computeFogOffset(camera_z) + fog_start);
		GL11.glFogf(GL11.GL_FOG_END, computeFogOffset(camera_z) + fog_end);
		GL11.glFogi(GL11.GL_FOG_MODE, fog_mode);
		GL11.glFogf(GL11.GL_FOG_DENSITY, computeFogDensityFactor(camera_z)*fog_density);
		GL11.glEnable(GL11.GL_FOG);
	}

	private float computeFogOffset(float camera_z) {
		return 0;
	}

	private float computeFogDensityFactor(float camera_z) {
		return 1 - (camera_z/fog_height_factor);
	}

	public final void disableFog() {
		GL11.glDisable(GL11.GL_FOG);
	}
}
