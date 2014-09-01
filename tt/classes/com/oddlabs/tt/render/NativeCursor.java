package com.oddlabs.tt.render;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.opengl.GL11;

import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.input.PointerInput;
import com.oddlabs.tt.resource.GLIntImage;
import com.oddlabs.tt.resource.NativeResource;
import com.oddlabs.util.Image;
import com.oddlabs.util.Utils;

public final strictfp class NativeCursor extends NativeResource {
	private final org.lwjgl.input.Cursor cursor;
	
	public NativeCursor(GLIntImage image_16_1, int offset_x_16_1, int offset_y_16_1,
						GLIntImage image_32_1, int offset_x_32_1, int offset_y_32_1,
						GLIntImage image_32_8, int offset_x_32_8, int offset_y_32_8) {
		org.lwjgl.input.Cursor native_cursor = null;
		int caps = Cursor.getCapabilities();
		
		int alpha_bits = 0;
		if ((caps & Cursor.CURSOR_8_BIT_ALPHA) != 0)
			alpha_bits = 8;
		else if ((caps & Cursor.CURSOR_ONE_BIT_TRANSPARENCY) != 0)
			alpha_bits = 1;

		int max_size = Cursor.getMaxCursorSize();
			
		try {
			if (max_size < 32 && max_size >= 16 && alpha_bits >= 1)
				native_cursor = new org.lwjgl.input.Cursor(image_16_1.getWidth(), image_16_1.getHeight(), offset_x_16_1, offset_y_16_1, 1, image_16_1.createCursorPixels(), null);
			else if (max_size >= 32) {
				if (alpha_bits == 8)
					native_cursor = new org.lwjgl.input.Cursor(image_32_8.getWidth(), image_32_8.getHeight(), offset_x_32_8, offset_y_32_8, 1, image_32_8.createCursorPixels(), null);
				else if (alpha_bits == 1)
					native_cursor = new org.lwjgl.input.Cursor(image_32_1.getWidth(), image_32_1.getHeight(), offset_x_32_1, offset_y_32_1, 1, image_32_1.createCursorPixels(), null);
			}
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		cursor = native_cursor;
	}

	public final org.lwjgl.input.Cursor getCursor() {
		return cursor;
	}

	public final boolean setActive() {
		if (Settings.getSettings().use_native_cursor && cursor != null) {
			PointerInput.setActiveCursor(cursor);
			return true;
		} else {
			PointerInput.setActiveCursor(null);
			return false;
		}
	}

	public final void doDelete() {
		if (cursor != null) {
			PointerInput.deletingCursor(cursor);
			cursor.destroy();
		}
	}
}
