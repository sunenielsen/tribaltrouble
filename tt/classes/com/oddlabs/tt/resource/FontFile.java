package com.oddlabs.tt.resource;

import com.oddlabs.tt.font.Font;
import com.oddlabs.util.FontInfo;

public final strictfp class FontFile extends File {
	public FontFile(String file_name) {
		super(file_name);
	}

	public final Object newInstance() {
		FontInfo font_info = FontInfo.loadFromFile(getURL());
		return new Font(font_info);
	}

	public final boolean equals(Object o) {
		if (!(o instanceof FontFile))
			return false;
		return super.equals(o);
	}
}
