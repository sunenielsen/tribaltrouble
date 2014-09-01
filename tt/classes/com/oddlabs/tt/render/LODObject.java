package com.oddlabs.tt.render;

strictfp interface LODObject {
	public void markDetailPoint();
	public void markDetailPolygon(int level);
	public int getTriangleCount(int level);
	public float getEyeDistanceSquared();
}
