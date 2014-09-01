package com.oddlabs.tt.gui;

import java.net.URL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.lwjgl.opengl.GL11;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.resource.TextureFile;
import com.oddlabs.util.Utils;
import com.oddlabs.util.Quad;

public strictfp class Icons {
	private static Icons icons;

	private final Texture texture;

	private final IconQuad[] harvest_icon;
	private final IconQuad[] tree_icon;
	private final IconQuad[] rock_icon;
	private final IconQuad[] iron_icon;
	private final IconQuad[] rubber_icon;
	private final IconQuad tree_status_icon;
	private final IconQuad rock_status_icon;
	private final IconQuad iron_status_icon;
	private final IconQuad rubber_status_icon;
	private final IconQuad cheat_icon;
	private final RaceIcons native_icons;
	private final RaceIcons viking_icons;
	private final IconQuad[] watch;
	private final IconQuad infinite;
	private final NotifyArrowData notify_arrow_data;

	private final Map tool_tip_icons;

	public final static void load() {
		if (icons == null)
			icons = new Icons("/gui/icons.xml");
	}

	public final static Icons getIcons() {
		return icons;
	}

	private Icons(String xml_file) {
		Node root = loadFile(xml_file, new GUIErrorHandler());
		texture = loadTexture(root);

		harvest_icon = getNamedIconQuads(root, "harvest_icon", texture);
		tree_icon = getNamedIconQuads(root, "tree_icon", texture);
		rock_icon = getNamedIconQuads(root, "rock_icon", texture);
		iron_icon = getNamedIconQuads(root, "iron_icon", texture);
		rubber_icon = getNamedIconQuads(root, "rubber_icon", texture);
		tree_status_icon = getNamedIconQuad(root, "tree_status_icon", texture);
		rock_status_icon = getNamedIconQuad(root, "rock_status_icon", texture);
		iron_status_icon = getNamedIconQuad(root, "iron_status_icon", texture);
		rubber_status_icon = getNamedIconQuad(root, "rubber_status_icon", texture);
		cheat_icon = getNamedIconQuad(root, "cheat_icon", texture);
		ResourceBundle bundle = ResourceBundle.getBundle(Icons.class.getName());
		String tt_caption = com.oddlabs.tt.util.Utils.getBundleString(bundle, "terrifying_toot", new Object[]{"S"});
		String rr_caption = com.oddlabs.tt.util.Utils.getBundleString(bundle, "ravaging_roar", new Object[]{"C"});
		String ss_caption = com.oddlabs.tt.util.Utils.getBundleString(bundle, "stinking_stew", new Object[]{"S"});
		String cc_caption = com.oddlabs.tt.util.Utils.getBundleString(bundle, "crackling_cloud", new Object[]{"C"});
		viking_icons = parseRaceIcons(root, "vikings", tt_caption, rr_caption);
		native_icons = parseRaceIcons(root, "natives", ss_caption, cc_caption);
		watch = parseWatch(root);
		infinite = getNamedIconQuad(root, "infinite", texture);
		notify_arrow_data = parseNotifyArrowData(root);
		tool_tip_icons = new HashMap();
		tool_tip_icons.put(com.oddlabs.tt.landscape.TreeSupply.class, new Quad[]{tree_status_icon});
		tool_tip_icons.put(com.oddlabs.tt.model.RockSupply.class, new Quad[]{rock_status_icon});
		tool_tip_icons.put(com.oddlabs.tt.model.IronSupply.class, new Quad[]{iron_status_icon});
		tool_tip_icons.put(com.oddlabs.tt.model.RubberSupply.class, new Quad[]{rubber_status_icon});
	}

	public Quad[] getToolTipIcon(Class key) {
		return (Quad[])(tool_tip_icons.get(key));
	}

	private final RaceIcons parseRaceIcons(Node n, String head, String magic1_desc, String magic2_desc) {
		return new RaceIcons(getNamedIconQuad(n, head + "_unit_status_icon", texture),
							 getNamedIconQuad(n, head + "_weapon_rock_status_icon", texture),
							 getNamedIconQuad(n, head + "_weapon_iron_status_icon", texture),
							 getNamedIconQuad(n, head + "_weapon_rubber_status_icon", texture),
							 getNamedIconQuads(n, head + "_build_weapons_icon", texture),
							 getNamedIconQuads(n, head + "_build_weapon_rock_icon", texture),
							 getNamedIconQuads(n, head + "_build_weapon_iron_icon", texture),
							 getNamedIconQuads(n, head + "_build_weapon_rubber_icon", texture),
							 getNamedIconQuads(n, head + "_army_icon", texture),
							 getNamedIconQuads(n, head + "_warrior_rock_icon", texture),
							 getNamedIconQuads(n, head + "_warrior_iron_icon", texture),
							 getNamedIconQuads(n, head + "_warrior_rubber_icon", texture),
							 getNamedIconQuads(n, head + "_peon_icon", texture),
							 getNamedIconQuads(n, head + "_chieftain_icon", texture),
							 getNamedIconQuads(n, head + "_transport_icon", texture),
							 getNamedIconQuads(n, head + "_attack_icon", texture),
							 getNamedIconQuads(n, head + "_move_icon", texture),
							 getNamedIconQuads(n, head + "_gather_repair_icon", texture),
							 getNamedIconQuads(n, head + "_quarters_icon", texture),
							 getNamedIconQuads(n, head + "_armory_icon", texture),
							 getNamedIconQuads(n, head + "_tower_icon", texture),
							 getNamedIconQuads(n, head + "_tower_exit_icon", texture),
							 getNamedIconQuads(n, head + "_rally_point_icon", texture),
							 getNamedIconQuads(n, head + "_magic1_icon", texture),
							 magic1_desc,
							 getNamedIconQuads(n, head + "_magic2_icon", texture),
							 magic2_desc);
	}

	public final RaceIcons getVikingIcons() {
		return viking_icons;
	}

	public final RaceIcons getNativeIcons() {
		return native_icons;
	}

	public final IconQuad[] getHarvestIcon() {
		return harvest_icon;
	}

	public final IconQuad getTreeStatusIcon() {
		return tree_status_icon;
	}

	public final IconQuad getRockStatusIcon() {
		return rock_status_icon;
	}

	public final IconQuad getIronStatusIcon() {
		return iron_status_icon;
	}

	public final IconQuad getRubberStatusIcon() {
		return rubber_status_icon;
	}
	
	public final IconQuad getCheatIcon() {
		return cheat_icon;
	}

	public final IconQuad[] getTreeIcon() {
		return tree_icon;
	}

	public final IconQuad[] getRockIcon() {
		return rock_icon;
	}

	public final IconQuad[] getIronIcon() {
		return iron_icon;
	}

	public final IconQuad[] getRubberIcon() {
		return rubber_icon;
	}

	private final IconQuad[] parseWatch(Node n) {
		ArrayList list = new ArrayList();
		Node node = getNodeByName("watch", n);
		NodeList nl = node.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeName().equals("quad")) {
				list.add(readQuadData(nl.item(i), texture));
			}
		}
		IconQuad[] result = new IconQuad[list.size()];
		list.toArray(result);
		return result;
	}

	public final IconQuad[] getWatch() {
		return watch;
	}

	public final IconQuad getInfinite() {
		return infinite;
	}

	public final NotifyArrowData getNotifyArrowData() {
		return notify_arrow_data;
	}

	public final static IconQuad readQuadData(Node n, Texture texture) {
		int left = getInt(n, "left");
		int top = getInt(n, "top");
		int right = getInt(n, "right");
		int bottom = getInt(n, "bottom");
		return new IconQuad(left/(float)texture.getWidth(),
				1f - bottom/(float)texture.getHeight(),
				right/(float)texture.getWidth(),
				1f - top/(float)texture.getHeight(),
				right - left,
				bottom - top,
				texture);
	}

	public final static Node loadFile(String xml_file, ErrorHandler error_handler) {
		URL url = Utils.makeURL(xml_file);

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(error_handler);
			Document document = builder.parse(url.openStream());
			return document.getDocumentElement();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public final static Texture loadTexture(Node n) {
		return loadTexture(n.getAttributes().getNamedItem("texture").getNodeValue());
	}

	private final static Texture loadTexture(String tex_file) {
		TextureFile file = new TextureFile(tex_file,
										   GL11.GL_RGBA,
										   GL11.GL_LINEAR,
										   GL11.GL_LINEAR,
										   GL11.GL_CLAMP,
										   GL11.GL_CLAMP);
		return (Texture)Resources.findResource(file);
	}

	public final static Node getNodeByName(String name, Node n) {
		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeName().equals(name))
				return nl.item(i);
		}
		assert false : "Missing node: " + name;
		return null;
	}

	public final static int getInt(Node n, String key) {
		String string = n.getAttributes().getNamedItem(key).getNodeValue();
		return Integer.parseInt(string);
	}

	private final NotifyArrowData parseNotifyArrowData(Node n) {
		Node node = getNodeByName("notify_arrow", n);
		IconQuad arrow = getIconQuad(node, texture);
		return new NotifyArrowData(arrow,
				getInt(node, "head_x"),
				getInt(node, "head_y"),
				getInt(node, "end_x"),
				getInt(node, "end_y"));
	}

	public final static IconQuad[] getNamedIconQuads(Node n, String name, Texture texture) {
		return getIconQuads(getNodeByName(name, n), texture);
	}

	public final static IconQuad getNamedIconQuad(Node n, String name, Texture texture) {
		return getIconQuad(getNodeByName(name, n), texture);
	}

	private final static IconQuad[] getIconQuads(Node n, Texture texture) {
		IconQuad[] result = new IconQuad[3];
		Node normal = getNodeByName("normal", n);
		result[Skin.NORMAL] = getIconQuad(normal, texture);
		Node active = getNodeByName("active", n);
		result[Skin.ACTIVE] = getIconQuad(active, texture);
		Node disabled = getNodeByName("disabled", n);
		result[Skin.DISABLED] = getIconQuad(disabled, texture);
		return result;
	}

	private final static IconQuad getIconQuad(Node n, Texture texture) {
		Node q = getNodeByName("quad", n);
		int left = getInt(q, "left");
		int top = getInt(q, "top");
		int right = getInt(q, "right");
		int bottom = getInt(q, "bottom");
		return new IconQuad(left/(float)texture.getWidth(),
						1f - bottom/(float)texture.getHeight(),
						right/(float)texture.getWidth(),
						1f - top/(float)texture.getHeight(),
						right - left,
						bottom - top,
						texture);
	}
}
