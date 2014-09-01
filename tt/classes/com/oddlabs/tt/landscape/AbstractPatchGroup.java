package com.oddlabs.tt.landscape;

import java.util.Collection;

import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.util.BoundingBox;
import com.oddlabs.tt.camera.CameraState;

public abstract strictfp class AbstractPatchGroup extends BoundingBox {
	private final AbstractPatchGroup parent;
	private final float patch_radius;
	private final int colormap_x;
	private final int colormap_y;

	protected AbstractPatchGroup(HeightMap heightmap, float patch_size, int x, int y, AbstractPatchGroup parent) {
		this.parent = parent;
		patch_radius = (float)StrictMath.sqrt(2)*patch_size*heightmap.getMetersPerPatch()*0.5f;
		colormap_x = x/heightmap.getPatchesPerChunk();
		colormap_y = y/heightmap.getPatchesPerChunk();
	}

	public final int getColorMapX() {
		return colormap_x;
	}

	public final int getColorMapY() {
		return colormap_y;
	}

	final void editHeight(float height) {
		checkBoundsZ(height);
		if (parent != null)
			parent.editHeight(height); 
	}

	protected final float transformError(float error) {
		float transformed_error = error*LocalInput.getErrorConstant() + patch_radius;
		return transformed_error*transformed_error;
	}

	public abstract void visit(PatchGroupVisitor visitor);
}
