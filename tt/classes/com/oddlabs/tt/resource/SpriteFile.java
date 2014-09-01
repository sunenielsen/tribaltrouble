package com.oddlabs.tt.resource;

import com.oddlabs.tt.render.SpriteList;

public final strictfp class SpriteFile extends File {
	private final boolean lighting;
	private final boolean cullface;
	private final boolean alpha;
	private final boolean modulate_color;
	private final boolean max_alpha;
	private final int mipmap_cutoff;

	public SpriteFile(String location, int mipmap_cutoff, boolean lighting, boolean cullface, boolean alpha, boolean modulate_color) {
		this(location, mipmap_cutoff, lighting, cullface, alpha, modulate_color, false);
	}
	
	public SpriteFile(String location, int mipmap_cutoff, boolean lighting, boolean cullface, boolean alpha, boolean modulate_color, boolean max_alpha) {
		super(location);
		this.lighting = lighting;
		this.cullface = cullface;
		this.alpha = alpha;
		this.modulate_color = modulate_color;
		this.mipmap_cutoff = mipmap_cutoff;
		this.max_alpha = max_alpha;
	}

	public final Object newInstance() {
		return new SpriteList(this);
	}

	public final boolean equals(Object o) {
		if (!(o instanceof SpriteFile))
			return false;
		SpriteFile other = (SpriteFile)o;
		if (mipmap_cutoff != other.mipmap_cutoff || lighting != other.lighting || cullface != other.cullface || alpha != other.alpha || modulate_color != other.modulate_color)
			return false;
		return super.equals(o);
	}

	public final int getMipmapCutoff() {
		return mipmap_cutoff;
	}

	public final boolean isLighted() {
		return lighting;
	}

	public final boolean isCulled() {
		return cullface;
	}

	public final boolean hasAlpha() {
		return alpha;
	}

	public final boolean hasModulateColor() {
		return modulate_color;
	}
	
	public final boolean hasMaxAlpha() {
		return max_alpha;
	}
}
