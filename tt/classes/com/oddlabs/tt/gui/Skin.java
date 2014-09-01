package com.oddlabs.tt.gui;

import java.net.URL;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.lwjgl.opengl.GL11;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.oddlabs.tt.font.Font;
import com.oddlabs.tt.resource.FontFile;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.resource.TextureFile;
import com.oddlabs.util.Utils;
import com.oddlabs.util.Quad;

public final strictfp class Skin {
	public final static int NORMAL = 0;
	public final static int ACTIVE = 1;
	public final static int DISABLED = 2;

	private static Skin skin;

	private final Texture texture;
	private final Font edit_font;
	private final Font button_font;
	private final Font headline_font;

	private final Quad[] plus_button;
	private final Quad[] minus_button;
	private final Quad[] accept_button;
	private final Quad[] cancel_button;
	private final Quad[] back_button;
	private final Horizontal horiz_button_pressed;
	private final Horizontal horiz_button_unpressed;
	private final FormData form_data;
	private final Box edit_box;
	private final Box background_box;
	private final Quad[] check_box_marked;
	private final Quad[] check_box_unmarked;
	private final Quad[] radio_button_marked;
	private final Quad[] radio_button_unmarked;
	private final GroupData group_data;
	private final ScrollBarData scroll_bar_data;
	private final SliderData slider_data;
	private final PulldownData pulldown_data;
	private final ProgressBarData progress_bar_data;
	private final MultiColumnComboBoxData multi_columnCombo_box_data;
	private final ToolTipBoxInfo tool_tip;
	private final Quad[] diode;
	private final PanelData panel_data;
	private final Quad flag_default;
	private final Quad flag_da;
	private final Quad flag_en;
	private final Quad flag_de;
	private final Quad flag_es;
	private final Quad flag_it;

	public final static void load() {
		if (skin == null)
			skin = new Skin("/gui/gui_skin.xml");
	}

	public final static Skin getSkin() {
		return skin;
	}

	private Skin(String xml_file) {
		Node root = loadFile(xml_file, new GUIErrorHandler());
		texture = loadTexture(root);
		edit_font = parseEditFont(root);
		button_font = parseButtonFont(root);
		headline_font = parseHeadlineFont(root);

		plus_button = getNamedQuads(root, "plus_button");
		minus_button = getNamedQuads(root, "minus_button");
		accept_button = getNamedQuads(root, "accept_button");
		cancel_button = getNamedQuads(root, "cancel_button");
		back_button = getNamedQuads(root, "back_button");
		check_box_marked = parseCheckBoxMarked(root);
		check_box_unmarked = parseCheckBoxUnmarked(root);
		radio_button_marked = parseRadioButtonMarked(root);
		radio_button_unmarked = parseRadioButtonUnmarked(root);
		horiz_button_pressed = parseHorizButtonPressed(root);
		horiz_button_unpressed = parseHorizButtonUnpressed(root);
		form_data = parseFormData(root);
		edit_box = parseBox(root, "editbox");
		background_box = parseBox(root, "backgroundbox");
		group_data = parseGroupData(root);
		scroll_bar_data = parseScrollBarData(root);
		slider_data = parseSliderData(root);
		pulldown_data = parsePulldownData(root);
		progress_bar_data = parseProgressBarData(root);
		multi_columnCombo_box_data = parseMultiColumnComboBoxData(root);
		tool_tip = parseToolTipInfo(root);
		diode = getNamedQuads(root, "diode");
		panel_data = parsePanelData(root);
		flag_default = getNamedQuad(root, "flag_default");
		flag_da = getNamedQuad(root, "flag_da");
		flag_en = getNamedQuad(root, "flag_en");
		flag_de = getNamedQuad(root, "flag_de");
		flag_es = getNamedQuad(root, "flag_es");
		flag_it = getNamedQuad(root, "flag_it");
	}

	private final static Node loadFile(String xml_file, ErrorHandler error_handler) {
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

	private final static Texture loadTexture(Node n) {
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

	private final static Node getNodeByName(String name, Node n) {
		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeName().equals(name))
				return nl.item(i);
		}
		assert false : "Missing node: " + name;
		return null;
	}

	private final static int getInt(Node n, String key) {
		String string = n.getAttributes().getNamedItem(key).getNodeValue();
		return Integer.parseInt(string);
	}

	private final Color getColor(Node n) {
		Node q = getNodeByName("color", n);
		int r = getInt(q, "r");
		int g = getInt(q, "g");
		int b = getInt(q, "b");
		int a = getInt(q, "a");
		return new Color(r/255f,
						 g/255f,
						 b/255f,
						 a/255f);
	}

	private final Quad[] getNamedQuads(Node n, String name) {
		return getQuads(getNodeByName(name, n), texture);
	}

	private final Quad getNamedQuad(Node n, String name) {
		return getQuad(getNodeByName(name, n), texture);
	}

	private final Quad[] getQuads(Node n, Texture texture) {
		Quad[] result = new Quad[3];
		Node normal = getNodeByName("normal", n);
		result[NORMAL] = getQuad(normal, texture);
		Node active = getNodeByName("active", n);
		result[ACTIVE] = getQuad(active, texture);
		Node disabled = getNodeByName("disabled", n);
		result[DISABLED] = getQuad(disabled, texture);
		return result;
	}

	private final Quad getQuad(Node n, Texture texture) {
		Node q = getNodeByName("quad", n);
		return readQuadData(q, texture);
	}

	private final Quad readQuadData(Node n, Texture texture) {
		int left = getInt(n, "left");
		int top = getInt(n, "top");
		int right = getInt(n, "right");
		int bottom = getInt(n, "bottom");
		return new Quad(left/(float)texture.getWidth(),
						1f - bottom/(float)texture.getHeight(),
						right/(float)texture.getWidth(),
						1f - top/(float)texture.getHeight(),
						right - left,
						bottom - top);
	}

	private final Horizontal getHorizontal(Node n) {
		Node horizontal_node = getNodeByName("horizontal", n);
		Node left_node = getNodeByName("left", horizontal_node);
		Quad[] left = getQuads(left_node, texture);
		Node center_node = getNodeByName("center", horizontal_node);
		Quad[] center = getQuads(center_node, texture);
		Node right_node = getNodeByName("right", horizontal_node);
		Quad[] right = getQuads(right_node, texture);
		return new Horizontal(left, center, right);
	}

	private final Vertical getVertical(Node n) {
		Node vertical_node = getNodeByName("vertical", n);
		Node bottom_node = getNodeByName("bottom", vertical_node);
		Quad[] bottom = getQuads(bottom_node, texture);
		Node center_node = getNodeByName("center", vertical_node);
		Quad[] center = getQuads(center_node, texture);
		Node top_node = getNodeByName("top", vertical_node);
		Quad[] top = getQuads(top_node, texture);
		return new Vertical(bottom, center, top);
	}

	private final Box getBox(Node n) {
		Node box_node = getNodeByName("box", n);
		Node left_bottom_node = getNodeByName("left_bottom", box_node);
		Quad[] left_bottom = getQuads(left_bottom_node, texture);
		Node bottom_node = getNodeByName("bottom", box_node);
		Quad[] bottom = getQuads(bottom_node, texture);
		Node right_bottom_node = getNodeByName("right_bottom", box_node);
		Quad[] right_bottom = getQuads(right_bottom_node, texture);
		Node right_node = getNodeByName("right", box_node);
		Quad[] right = getQuads(right_node, texture);
		Node right_top_node = getNodeByName("right_top", box_node);
		Quad[] right_top = getQuads(right_top_node, texture);
		Node top_node = getNodeByName("top", box_node);
		Quad[] top = getQuads(top_node, texture);
		Node left_top_node = getNodeByName("left_top", box_node);
		Quad[] left_top = getQuads(left_top_node, texture);
		Node left_node = getNodeByName("left", box_node);
		Quad[] left = getQuads(left_node, texture);
		Node center_node = getNodeByName("center", box_node);
		Quad[] center = getQuads(center_node, texture);
		int left_offset = getInt(box_node, "left_offset");
		int bottom_offset = getInt(box_node, "bottom_offset");
		int right_offset = getInt(box_node, "right_offset");
		int top_offset = getInt(box_node, "top_offset");
		return new Box(left_bottom,
					   bottom,
					   right_bottom,
					   right,
					   right_top,
					   top,
					   left_top,
					   left, center,
					   left_offset,
					   bottom_offset,
					   right_offset,
					   top_offset);
	}

	private final Font getFont(Node n) {
		String path = n.getFirstChild().getNodeValue();
		FontFile font_file = new FontFile(path);
		return (Font)Resources.findResource(font_file);
	}

	public final void bindTexture() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getHandle());
	}

	private final Font parseEditFont(Node n) {
		Node node = getNodeByName("editfont", n);
		return getFont(node);
	}

	public final Font getEditFont() {
		return edit_font;
	}

	private final Font parseButtonFont(Node n) {
		Node node = getNodeByName("buttonfont", n);
		return getFont(node);
	}

	public final Font getButtonFont() {
		return button_font;
	}

	private final Font parseHeadlineFont(Node n) {
		Node node = getNodeByName("headlinefont", n);
		return getFont(node);
	}

	public final Font getHeadlineFont() {
		return headline_font;
	}

	private final Quad[] parseCheckBoxMarked(Node n) {
		Node node = getNodeByName("checkbox", n);
		node = getNodeByName("marked", node);
		return getQuads(node, texture);
	}

	public final Quad[] getCheckBoxMarked() {
		return check_box_marked;
	}

	private final Quad[] parseCheckBoxUnmarked(Node n) {
		Node node = getNodeByName("checkbox", n);
		node = getNodeByName("unmarked", node);
		return getQuads(node, texture);
	}

	public final Quad[] getCheckBoxUnmarked() {
		return check_box_unmarked;
	}

	private final Quad[] parseRadioButtonMarked(Node n) {
		Node node = getNodeByName("radiobutton", n);
		node = getNodeByName("marked", node);
		return getQuads(node, texture);
	}

	public final Quad[] getRadioButtonMarked() {
		return radio_button_marked;
	}

	private final Quad[] parseRadioButtonUnmarked(Node n) {
		Node node = getNodeByName("radiobutton", n);
		node = getNodeByName("unmarked", node);
		return getQuads(node, texture);
	}

	public final Quad[] getRadioButtonUnmarked() {
		return radio_button_unmarked;
	}

	private final Horizontal parseHorizButtonPressed(Node n) {
		Node node = getNodeByName("horiz_button", n);
		node = getNodeByName("horiz_pressed", node);
		return getHorizontal(node);
	}

	public final Horizontal getHorizButtonPressed() {
		return horiz_button_pressed;
	}

	private final Horizontal parseHorizButtonUnpressed(Node n) {
		Node node = getNodeByName("horiz_button", n);
		node = getNodeByName("horiz_unpressed", node);
		return getHorizontal(node);
	}

	public final Horizontal getHorizButtonUnpressed() {
		return horiz_button_unpressed;
	}

	private final ScrollBarData parseScrollBarData(Node n) {
		Node node = getNodeByName("vert_scroll", n);
		Vertical scroll_bar = getVertical(node);

		Node temp;
		temp = getNodeByName("less", node);
		temp = getNodeByName("pushbutton", temp);
		temp = getNodeByName("pressed", temp);
		Quad[] scroll_down_button_pressed = getQuads(temp, texture);

		temp = getNodeByName("less", node);
		temp = getNodeByName("pushbutton", temp);
		temp = getNodeByName("unpressed", temp);
		Quad[] scroll_down_button_unpressed = getQuads(temp, texture);

		temp = getNodeByName("less", node);
		Quad[] scroll_down_arrow = getQuads(temp, texture);

		temp = getNodeByName("more", node);
		temp = getNodeByName("pushbutton", temp);
		temp = getNodeByName("pressed", temp);
		Quad[] scroll_up_button_pressed = getQuads(temp, texture);

		temp = getNodeByName("more", node);
		temp = getNodeByName("pushbutton", temp);
		temp = getNodeByName("unpressed", temp);
		Quad[] scroll_up_button_unpressed = getQuads(temp, texture);

		temp = getNodeByName("more", node);
		Quad[] scroll_up_arrow = getQuads(temp, texture);

		temp = getNodeByName("vert_scroll_button", n);
		Vertical scroll_button = getVertical(temp);

		return new ScrollBarData(scroll_bar,
								 scroll_down_button_pressed,
								 scroll_down_button_unpressed,
								 scroll_down_arrow,
								 scroll_up_button_pressed,
								 scroll_up_button_unpressed,
								 scroll_up_arrow,
								 scroll_button,
								 getInt(node, "left_offset"),
								 getInt(node, "bottom_offset"),
								 getInt(node, "top_offset"));
	}

	public final ScrollBarData getScrollBarData() {
		return scroll_bar_data;
	}

	private final SliderData parseSliderData(Node n) {
		Node node = getNodeByName("slider", n);
		Horizontal slider = getHorizontal(node);

		Quad[] button = getQuads(node, texture);

		return new SliderData(slider,
							  button,
							  getInt(node, "left_offset"),
							  getInt(node, "right_offset"));
	}

	public final SliderData getSliderData() {
		return slider_data;
	}

	private final PulldownData parsePulldownData(Node n) {
		Node node = getNodeByName("pulldown_menu", n);

		Node temp;
		temp = getNodeByName("pulldown_top", node);
		Horizontal pulldown_top = getHorizontal(temp);

		temp = getNodeByName("pulldown_bottom", node);
		Horizontal pulldown_bottom = getHorizontal(temp);

		Node item_node = getNodeByName("pulldown_item", n);
		Box pulldown_item = getBox(item_node);

		Node button_node = getNodeByName("pulldown_button", n);
		Horizontal pulldown_button = getHorizontal(button_node);

		Quad[] arrow = getQuads(button_node, texture);

		return new PulldownData(pulldown_top,
								pulldown_bottom,
								pulldown_item,
								pulldown_button,
								arrow,
								getInt(button_node, "arrow_offset_right"),
								getInt(button_node, "text_offset_left"),
								getFont(getNodeByName("pulldownfont", n)));
	}

	public final PulldownData getPulldownData() {
		return pulldown_data;
	}

	private final ProgressBarData parseProgressBarData(Node n) {
		Node node = getNodeByName("progressbar", n);
		Horizontal progressbar = getHorizontal(node);

		Node temp;
		temp = getNodeByName("left", node);
		Quad[] left = getQuads(temp, texture);

		temp = getNodeByName("center", node);
		Quad[] center = getQuads(temp, texture);

		temp = getNodeByName("right", node);
		Quad[] right = getQuads(temp, texture);

		return new ProgressBarData(progressbar,
								   left,
								   center,
								   right,
								   getFont(getNodeByName("progressfont", n)));
	}

	public final ProgressBarData getProgressBarData() {
		return progress_bar_data;
	}

	private final FormData parseFormData(Node n) {
		Node node = getNodeByName("slim_form", n);
		Box slim_form = getBox(node);

		node = getNodeByName("form", n);
		return new FormData(getBox(node),
							slim_form,
							getQuads(node, texture),
							getInt(node, "spacing"),
							getInt(node, "section_spacing"),
							getInt(node, "caption_left"),
							getInt(node, "caption_y"),
							getInt(node, "close_right"),
							getInt(node, "close_top"),
							getFont(getNodeByName("formfont", n)));
	}

	public final FormData getFormData() {
		return form_data;
	}

	public final Quad[] getPlusButton() {
		return plus_button;
	}

	public final Quad[] getMinusButton() {
		return minus_button;
	}

	public final Quad[] getAcceptButton() {
		return accept_button;
	}

	public final Quad[] getCancelButton() {
		return cancel_button;
	}
	
	public final Quad[] getBackButton() {
		return back_button;
	}

	public final Quad[] getDiode() {
		return diode;
	}

	private final Box parseBox(Node n, String name) {
		Node node = getNodeByName(name, n);
		return getBox(node);
	}

	public final Box getEditBox() {
		return edit_box;
	}

	public final Box getBackgroundBox() {
		return background_box;
	}

	private final GroupData parseGroupData(Node n) {
		Node node = getNodeByName("group", n);
		return new GroupData(getBox(node),
							 getInt(node, "caption_left"),
							 getInt(node, "caption_y"),
							 getInt(node, "caption_offset"),
							 getFont(getNodeByName("groupfont", n)));
	}

	public final GroupData getGroupData() {
		return group_data;
	}

	private final MultiColumnComboBoxData parseMultiColumnComboBoxData(Node n) {
		Node node = getNodeByName("multi_column_combo", n);
		Node desc = getNodeByName("descending", node);
		Node asc = getNodeByName("ascending", node);
		Node color1 = getNodeByName("color1", node);
		Node color2 = getNodeByName("color2", node);
		Node color_marked = getNodeByName("color_marked", node);
		return new MultiColumnComboBoxData(getBox(node),
										   parseHorizButtonPressed(node),
										   parseHorizButtonUnpressed(node),
										   getQuads(desc, texture),
										   getQuads(asc, texture),
										   getColor(color1),
										   getColor(color2),
										   getColor(color_marked),
										   getFont(getNodeByName("combofont", n)),
										   getInt(node, "caption_offset"));
	}

	public final MultiColumnComboBoxData getMultiColumnComboBoxData() {
		return multi_columnCombo_box_data;
	}

	private final ToolTipBoxInfo parseToolTipInfo(Node n) {
		Node node = getNodeByName("tool_tip", n);
		return new ToolTipBoxInfo(getHorizontal(node),
							  getInt(node, "left_offset"),
							  getInt(node, "bottom_offset"),
							  getInt(node, "right_offset"),
							  getInt(node, "top_offset"));
	}

	public final ToolTipBoxInfo getToolTipInfo() {
		return tool_tip;
	}

	private final PanelData parsePanelData(Node n) {
		Node node = getNodeByName("panel", n);
		return new PanelData(getBox(node),
							 getHorizontal(node),
							 getInt(node, "left_caption_offset"),
							 getInt(node, "right_caption_offset"),
							 getInt(node, "bottom_caption_offset"),
							 getInt(node, "left_tab_offset"),
							 getInt(node, "bottom_tab_offset"));
	}

	public final PanelData getPanelData() {
		return panel_data;
	}

	public final Quad getFlagDefault() {
		return flag_default;
	}

	public final Quad getFlagDa() {
		return flag_da;
	}

	public final Quad getFlagEn() {
		return flag_en;
	}

	public final Quad getFlagDe() {
		return flag_de;
	}

	public final Quad getFlagEs() {
		return flag_es;
	}

	public final Quad getFlagIt() {
		return flag_it;
	}
}
