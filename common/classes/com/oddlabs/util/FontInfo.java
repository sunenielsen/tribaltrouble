package com.oddlabs.util;

import com.oddlabs.util.Utils;
import java.io.*;
import java.net.*;

public final strictfp class FontInfo implements Serializable {
	private final static long serialVersionUID = 1;

	private final String texture_name;
	private final Quad[] key_map;
	private final int x_border;
	private final int y_border;
	private final int font_height;

	public FontInfo(String texture_name, Quad[] key_map, int x_border, int y_border, int font_height) {
		this.texture_name = texture_name;
		this.key_map = key_map;
		this.x_border = x_border;
		this.y_border = y_border;
		this.font_height = font_height;
	}

	public final String getTextureName() {
		return texture_name;
	}

	public final Quad[] getKeyMap() {
		return key_map;
	}

	public final int getBorderX() {
		return x_border;
	}

	public final int getBorderY() {
		return y_border;
	}

	public final int getHeight() {
		return font_height;
	}

	public final void saveToFile(String file_name) {
		try {
			ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file_name)));
			os.writeObject(this);
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public final static FontInfo loadFromFile(URL url) {
		return (FontInfo)Utils.loadObject(url);
	}
}
