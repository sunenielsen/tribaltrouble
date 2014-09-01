package com.oddlabs.tt.gui;

import org.w3c.dom.Node;

import com.oddlabs.tt.render.Texture;

public final strictfp class VikingCampaignIcons implements CampaignIcons {
	private final static int NUM_ISLANDS = 15;

	private static CampaignIcons icons;

	private final Texture texture;

	private final IconQuad map;
	private final MapIslandData[] islands = new MapIslandData[NUM_ISLANDS];
	private final IconQuad[] flags = new IconQuad[5];
	private final IconQuad[] boats = new IconQuad[5];
	private final GUIIcon[] hidden = new GUIIcon[2];
	private final IconQuad[] faces = new IconQuad[9];
	private final int offset_x;
	private final int offset_y;
	private final int width;
	private final int height;

	public final static void load() {
		if (icons == null)
			icons = new VikingCampaignIcons("/gui/viking_campaign.xml");
	}

	public final static CampaignIcons getIcons() {
		return icons;
	}

	private VikingCampaignIcons(String xml_file) {
		Node root = Icons.loadFile(xml_file, new GUIErrorHandler());
		texture = Icons.loadTexture(root);

		flags[0] = Icons.getNamedIconQuad(root, "flag0", texture);
		flags[1] = Icons.getNamedIconQuad(root, "flag1", texture);
		flags[2] = Icons.getNamedIconQuad(root, "flag2", texture);
		flags[3] = Icons.getNamedIconQuad(root, "flag3", texture);
		flags[4] = Icons.getNamedIconQuad(root, "flag4", texture);
		boats[0] = Icons.getNamedIconQuad(root, "boat0", texture);
		boats[1] = Icons.getNamedIconQuad(root, "boat1", texture);
		boats[2] = Icons.getNamedIconQuad(root, "boat2", texture);
		boats[3] = Icons.getNamedIconQuad(root, "boat3", texture);
		boats[4] = Icons.getNamedIconQuad(root, "boat4", texture);
		hidden[0] = getNamedGUIIcon(root, "hidden0", texture);
		hidden[1] = getNamedGUIIcon(root, "hidden1", texture);
		faces[0] = Icons.getNamedIconQuad(root, "face0", texture);
		faces[1] = Icons.getNamedIconQuad(root, "face1", texture);
		faces[2] = Icons.getNamedIconQuad(root, "face2", texture);
		faces[3] = Icons.getNamedIconQuad(root, "face3", texture);
		faces[4] = Icons.getNamedIconQuad(root, "face4", texture);
		faces[5] = Icons.getNamedIconQuad(root, "face5", texture);
		faces[6] = Icons.getNamedIconQuad(root, "face6", texture);
		faces[7] = Icons.getNamedIconQuad(root, "face7", texture);
		faces[8] = Icons.getNamedIconQuad(root, "face8", texture);

		map = Icons.getNamedIconQuad(root, "map", texture);
		for (int i = 0; i < NUM_ISLANDS; i++) {
			islands[i] = loadMapIslandData(root, "island" + i, texture);
		}

		Node map_node = Icons.getNodeByName("map", root);
		offset_x = Icons.getInt(map_node, "offset_x");
		offset_y = Icons.getInt(map_node, "offset_y");
		width = Icons.getInt(map_node, "width");
		height = Icons.getInt(map_node, "height");
	}

	private final MapIslandData loadMapIslandData(Node root, String name, Texture texture) {
		Node node = Icons.getNodeByName(name, root);
		IconQuad[] quads = Icons.getNamedIconQuads(node, "island", texture);
		Node n = Icons.getNodeByName("island", node);
		int x = Icons.getInt(n, "x");
		int y = texture.getHeight() - Icons.getInt(n, "y");
		int pin_index = Icons.getInt(n, "pin_index");
		int pin_x = Icons.getInt(n, "pin_x");
		int pin_y = texture.getHeight() - Icons.getInt(n, "pin_y");
		return new MapIslandData(quads, x, y, flags[pin_index], boats[pin_index], pin_x, pin_y);
	}

	private final GUIIcon getNamedGUIIcon(Node root, String name, Texture texture) {
		IconQuad temp = Icons.getNamedIconQuad(root, name, texture);
		Node n = Icons.getNodeByName(name, root);
		int x = Icons.getInt(n, "x");
		int y = texture.getHeight() - Icons.getInt(n, "y");
		GUIIcon gui_icon = new GUIIcon(temp);
		gui_icon.setPos(x, y);
		return gui_icon;
	}

	public final GUIIcon[] getHiddenRoutes() {
		return hidden;
	}

	public final IconQuad[] getFaces() {
		return faces;
	}

	public final IconQuad getMap() {
		return map;
	}

	public final int getNumIslands() {
		return islands.length;
	}

	public final int getOffsetX() {
		return offset_x;
	}

	public final int getOffsetY() {
		return offset_y;
	}

	public final int getInternalWidth() {
		return width;
	}

	public final int getInternalHeight() {
		return height;
	}

	public final MapIslandData getMapIslandData(int i) {
		return islands[i];
	}
}
