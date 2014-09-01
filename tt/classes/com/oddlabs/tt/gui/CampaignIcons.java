package com.oddlabs.tt.gui;

public strictfp interface CampaignIcons {
//	public CampaignIcons getIcons();

	public GUIIcon[] getHiddenRoutes();
	public IconQuad[] getFaces();
	public IconQuad getMap();
	public int getNumIslands();
//	public int getOffsetX();
//	public int getOffsetY();
//	public int getInternalWidth();
//	public int getInternalHeight();
	public MapIslandData getMapIslandData(int i);
}
