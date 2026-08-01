package com.oddlabs.tt.gui;

import com.oddlabs.tt.font.Font;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.resource.FontFile;
import com.oddlabs.tt.resource.Resources;
import org.jspecify.annotations.NonNull;
import org.w3c.dom.Node;

import static com.oddlabs.tt.gui.Icons.getIconQuads;
import static com.oddlabs.tt.gui.Icons.getInt;
import static com.oddlabs.tt.gui.Icons.getNamedColor;
import static com.oddlabs.tt.gui.Icons.getNamedIconQuad;
import static com.oddlabs.tt.gui.Icons.getNamedIconQuads;
import static com.oddlabs.tt.gui.Icons.getNodeByName;

/**
 * Skin for GUI
 */
public final class Skin {

    private static final Skin SKIN = new Skin("/gui/gui_skin.xml");

    private final @NonNull Font edit_font;
    private final @NonNull Font button_font;
    private final @NonNull Font headline_font;

    private final @NonNull ModeIconQuads plus_button;
    private final @NonNull ModeIconQuads minus_button;
    private final @NonNull ModeIconQuads accept_button;
    private final @NonNull ModeIconQuads cancel_button;
    private final @NonNull ModeIconQuads back_button;
    private final @NonNull Horizontal horiz_button_pressed;
    private final @NonNull Horizontal horiz_button_unpressed;
    private final @NonNull FormData form_data;
    private final @NonNull Box edit_box;
    private final @NonNull Box background_box;
    private final @NonNull ModeIconQuads check_box_marked;
    private final @NonNull ModeIconQuads check_box_unmarked;
    private final @NonNull ModeIconQuads radio_button_marked;
    private final @NonNull ModeIconQuads radio_button_unmarked;
    private final @NonNull GroupData group_data;
    private final @NonNull ScrollBarData scroll_bar_data;
    private final @NonNull SliderData slider_data;
    private final @NonNull PulldownData pulldown_data;
    private final @NonNull ProgressBarData progress_bar_data;
    private final @NonNull MultiColumnComboBoxData multi_columnCombo_box_data;
    private final @NonNull ToolTipBoxInfo tool_tip;
    private final @NonNull ModeIconQuads diode;
    private final @NonNull PanelData panel_data;
    private final @NonNull IconQuad flag_default;
    private final @NonNull IconQuad flag_da;
    private final @NonNull IconQuad flag_en;
    private final @NonNull IconQuad flag_de;
    private final @NonNull IconQuad flag_es;
    private final @NonNull IconQuad flag_it;
    private final @NonNull IconQuad flag_pt;

    public static @NonNull Skin getSkin() {
        return SKIN;
    }

    private Skin(@NonNull String xml_file) {
        Node root = Icons.loadFile(xml_file, new GUIErrorHandler());
        Texture texture = Icons.loadTexture(root);
        edit_font = parseEditFont(root);
        button_font = parseButtonFont(root);
        headline_font = parseHeadlineFont(root);

        plus_button = getNamedIconQuads(root, "plus_button", texture);
        minus_button = getNamedIconQuads(root, "minus_button", texture);
        accept_button = getNamedIconQuads(root, "accept_button", texture);
        cancel_button = getNamedIconQuads(root, "cancel_button", texture);
        back_button = getNamedIconQuads(root, "back_button", texture);
        check_box_marked = parseCheckBoxMarked(root, texture);
        check_box_unmarked = parseCheckBoxUnmarked(root, texture);
        radio_button_marked = parseRadioButtonMarked(root, texture);
        radio_button_unmarked = parseRadioButtonUnmarked(root, texture);
        horiz_button_pressed = parseHorizButtonPressed(root, texture);
        horiz_button_unpressed = parseHorizButtonUnpressed(root, texture);
        form_data = parseFormData(root, texture);
        edit_box = parseBox(root, "editbox", texture);
        background_box = parseBox(root, "backgroundbox", texture);
        group_data = parseGroupData(root, texture);
        scroll_bar_data = parseScrollBarData(root, texture);
        slider_data = parseSliderData(root, texture);
        pulldown_data = parsePulldownData(root, texture);
        progress_bar_data = parseProgressBarData(root, texture);
        multi_columnCombo_box_data = parseMultiColumnComboBoxData(root, texture);
        tool_tip = parseToolTipInfo(root, texture);
        diode = getNamedIconQuads(root, "diode", texture);
        panel_data = parsePanelData(root, texture);
        flag_default = getNamedIconQuad(root, "flag_default", texture);
        flag_da = getNamedIconQuad(root, "flag_da", texture);
        flag_en = getNamedIconQuad(root, "flag_en", texture);
        flag_de = getNamedIconQuad(root, "flag_de", texture);
        flag_es = getNamedIconQuad(root, "flag_es", texture);
        flag_it = getNamedIconQuad(root, "flag_it", texture);
        flag_pt = getNamedIconQuad(root, "flag_pt", texture);
    }

    private @NonNull Horizontal getHorizontal(@NonNull Node n, @NonNull Texture texture) {
        Node horizontal_node = getNodeByName("horizontal", n);
        return new Horizontal(
                getIconQuads(getNodeByName("left", horizontal_node), texture),
                getIconQuads(getNodeByName("center", horizontal_node), texture),
                getIconQuads(getNodeByName("right", horizontal_node), texture)
        );
    }

    private @NonNull Vertical getVertical(@NonNull Node n, @NonNull Texture texture) {
        Node vertical_node = getNodeByName("vertical", n);
        return new Vertical(
                getIconQuads(getNodeByName("bottom", vertical_node), texture),
                getIconQuads(getNodeByName("center", vertical_node), texture),
                getIconQuads(getNodeByName("top", vertical_node), texture)
        );
    }

    private @NonNull Box getBox(@NonNull Node n, @NonNull Texture texture) {
        Node box_node = getNodeByName("box", n);
        ModeIconQuads left_bottom = getIconQuads(getNodeByName("left_bottom", box_node), texture);
        ModeIconQuads bottom = getIconQuads(getNodeByName("bottom", box_node), texture);
        ModeIconQuads right_bottom = getIconQuads(getNodeByName("right_bottom", box_node), texture);
        ModeIconQuads right = getIconQuads(getNodeByName("right", box_node), texture);
        ModeIconQuads right_top = getIconQuads(getNodeByName("right_top", box_node), texture);
        ModeIconQuads top = getIconQuads(getNodeByName("top", box_node), texture);
        ModeIconQuads left_top = getIconQuads(getNodeByName("left_top", box_node), texture);
        ModeIconQuads left = getIconQuads(getNodeByName("left", box_node), texture);
        ModeIconQuads center = getIconQuads(getNodeByName("center", box_node), texture);
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

    private static final String TAHOMA_FONT = "/font/tahoma_13.font";
    private static final String IMPACT_FONT = "/font/impact_24.font";
    private static final String FALLBACK_EDIT_FONT = "/font/inter-light_13.font";
    private static final String FALLBACK_HEADLINE_FONT = "/font/intertight-black_28.font";

    private @NonNull Font getFont(@NonNull Node n) {
        String path = n.getFirstChild().getNodeValue();
        if (getClass().getResource(path) == null) {
            if (path.equals(IMPACT_FONT)) {
                path = FALLBACK_HEADLINE_FONT;
            } else if (path.equals(TAHOMA_FONT)) {
                path = FALLBACK_EDIT_FONT;
            }
        }
        FontFile font_file = new FontFile(path);
        return Resources.findResource(font_file);
    }

    private @NonNull Font parseEditFont(@NonNull Node n) {
        Node node = getNodeByName("editfont", n);
        return getFont(node);
    }

    public @NonNull Font getEditFont() {
        return edit_font;
    }

    private @NonNull Font parseButtonFont(@NonNull Node n) {
        Node node = getNodeByName("buttonfont", n);
        return getFont(node);
    }

    public @NonNull Font getButtonFont() {
        return button_font;
    }

    private @NonNull Font parseHeadlineFont(@NonNull Node n) {
        Node node = getNodeByName("headlinefont", n);
        return getFont(node);
    }

    public @NonNull Font getHeadlineFont() {
        return headline_font;
    }

    private @NonNull ModeIconQuads parseCheckBoxMarked(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("checkbox", n);
        node = getNodeByName("marked", node);
        return getIconQuads(node, texture);
    }

    public @NonNull ModeIconQuads getCheckBoxMarked() {
        return check_box_marked;
    }

    private @NonNull ModeIconQuads parseCheckBoxUnmarked(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("checkbox", n);
        node = getNodeByName("unmarked", node);
        return getIconQuads(node, texture);
    }

    public @NonNull ModeIconQuads getCheckBoxUnmarked() {
        return check_box_unmarked;
    }

    private @NonNull ModeIconQuads parseRadioButtonMarked(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("radiobutton", n);
        node = getNodeByName("marked", node);
        return getIconQuads(node, texture);
    }

    public @NonNull ModeIconQuads getRadioButtonMarked() {
        return radio_button_marked;
    }

    private @NonNull ModeIconQuads parseRadioButtonUnmarked(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("radiobutton", n);
        node = getNodeByName("unmarked", node);
        return getIconQuads(node, texture);
    }

    public @NonNull ModeIconQuads getRadioButtonUnmarked() {
        return radio_button_unmarked;
    }

    private @NonNull Horizontal parseHorizButtonPressed(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("horiz_button", n);
        node = getNodeByName("horiz_pressed", node);
        return getHorizontal(node, texture);
    }

    public @NonNull Horizontal getHorizButtonPressed() {
        return horiz_button_pressed;
    }

    private @NonNull Horizontal parseHorizButtonUnpressed(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("horiz_button", n);
        node = getNodeByName("horiz_unpressed", node);
        return getHorizontal(node, texture);
    }

    public @NonNull Horizontal getHorizButtonUnpressed() {
        return horiz_button_unpressed;
    }

    private @NonNull ScrollBarData parseScrollBarData(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("vert_scroll", n);
        Vertical scroll_bar = getVertical(node, texture);

        Node temp;
        temp = getNodeByName("less", node);
        temp = getNodeByName("pushbutton", temp);
        temp = getNodeByName("pressed", temp);
        ModeIconQuads scroll_down_button_pressed = getIconQuads(temp, texture);

        temp = getNodeByName("less", node);
        temp = getNodeByName("pushbutton", temp);
        temp = getNodeByName("unpressed", temp);
        ModeIconQuads scroll_down_button_unpressed = getIconQuads(temp, texture);

        temp = getNodeByName("less", node);
        ModeIconQuads scroll_down_arrow = getIconQuads(temp, texture);

        temp = getNodeByName("more", node);
        temp = getNodeByName("pushbutton", temp);
        temp = getNodeByName("pressed", temp);
        ModeIconQuads scroll_up_button_pressed = getIconQuads(temp, texture);

        temp = getNodeByName("more", node);
        temp = getNodeByName("pushbutton", temp);
        temp = getNodeByName("unpressed", temp);
        ModeIconQuads scroll_up_button_unpressed = getIconQuads(temp, texture);

        temp = getNodeByName("more", node);
        ModeIconQuads scroll_up_arrow = getIconQuads(temp, texture);

        temp = getNodeByName("vert_scroll_button", n);
        Vertical scroll_button = getVertical(temp, texture);

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

    public @NonNull ScrollBarData getScrollBarData() {
        return scroll_bar_data;
    }

    private @NonNull SliderData parseSliderData(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("slider", n);
        Horizontal slider = getHorizontal(node, texture);

        ModeIconQuads button = getIconQuads(node, texture);

        return new SliderData(slider,
                button,
                getInt(node, "left_offset"),
                getInt(node, "right_offset"));
    }

    public @NonNull SliderData getSliderData() {
        return slider_data;
    }

    private @NonNull PulldownData parsePulldownData(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("pulldown_menu", n);

        Node temp;
        temp = getNodeByName("pulldown_top", node);
        Horizontal pulldownTop = getHorizontal(temp, texture);

        temp = getNodeByName("pulldown_bottom", node);
        Horizontal pulldownBottom = getHorizontal(temp, texture);

        Node item_node = getNodeByName("pulldown_item", n);
        Box pulldownItem = getBox(item_node, texture);

        Node button_node = getNodeByName("pulldown_button", n);
        Horizontal pulldown_button = getHorizontal(button_node, texture);

        ModeIconQuads arrow = getIconQuads(button_node, texture);

        return new PulldownData(pulldownTop,
                pulldownBottom,
                pulldownItem,
                pulldown_button,
                arrow,
                getInt(button_node, "arrow_offset_right"),
                getInt(button_node, "text_offset_left"),
                getFont(getNodeByName("pulldownfont", n)));
    }

    public @NonNull PulldownData getPulldownData() {
        return pulldown_data;
    }

    private @NonNull ProgressBarData parseProgressBarData(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("progressbar", n);
        Horizontal progressbar = getHorizontal(node, texture);

        Node temp;
        temp = getNodeByName("left", node);
        ModeIconQuads left = getIconQuads(temp, texture);

        temp = getNodeByName("center", node);
        ModeIconQuads center = getIconQuads(temp, texture);

        temp = getNodeByName("right", node);
        ModeIconQuads right = getIconQuads(temp, texture);

        return new ProgressBarData(progressbar,
                left,
                center,
                right,
                getFont(getNodeByName("progressfont", n)));
    }

    public @NonNull ProgressBarData getProgressBarData() {
        return progress_bar_data;
    }

    private @NonNull FormData parseFormData(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("slim_form", n);
        Box slim_form = getBox(node, texture);

        node = getNodeByName("form", n);
        return new FormData(getBox(node, texture),
                slim_form,
                getIconQuads(node, texture),
                getInt(node, "spacing"),
                getInt(node, "section_spacing"),
                getInt(node, "caption_left"),
                getInt(node, "caption_y"),
                getInt(node, "close_right"),
                getInt(node, "close_top"),
                getFont(getNodeByName("formfont", n)));
    }

    public @NonNull FormData getFormData() {
        return form_data;
    }

    public @NonNull ModeIconQuads getPlusButton() {
        return plus_button;
    }

    public @NonNull ModeIconQuads getMinusButton() {
        return minus_button;
    }

    public @NonNull ModeIconQuads getAcceptButton() {
        return accept_button;
    }

    public @NonNull ModeIconQuads getCancelButton() {
        return cancel_button;
    }

    public @NonNull ModeIconQuads getBackButton() {
        return back_button;
    }

    public @NonNull ModeIconQuads getDiode() {
        return diode;
    }

    private @NonNull Box parseBox(@NonNull Node n, @NonNull String name, @NonNull Texture texture) {
        Node node = getNodeByName(name, n);
        return getBox(node, texture);
    }

    public @NonNull Box getEditBox() {
        return edit_box;
    }

    public @NonNull Box getBackgroundBox() {
        return background_box;
    }

    private @NonNull GroupData parseGroupData(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("group", n);
        return new GroupData(getBox(node, texture),
                getInt(node, "caption_left"),
                getInt(node, "caption_y"),
                getInt(node, "caption_offset"),
                getFont(getNodeByName("groupfont", n)));
    }

    public @NonNull GroupData getGroupData() {
        return group_data;
    }

    private @NonNull MultiColumnComboBoxData parseMultiColumnComboBoxData(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("multi_column_combo", n);
        Node desc = getNodeByName("descending", node);
        Node asc = getNodeByName("ascending", node);
        return new MultiColumnComboBoxData(getBox(node, texture),
                parseHorizButtonPressed(node, texture),
                parseHorizButtonUnpressed(node, texture),
                getIconQuads(desc, texture),
                getIconQuads(asc, texture),
                getNamedColor(node, "color1"),
                getNamedColor(node, "color2"),
                getNamedColor(node, "color_marked"),
                getFont(getNodeByName("combofont", n)),
                getInt(node, "caption_offset"));
    }

    public @NonNull MultiColumnComboBoxData getMultiColumnComboBoxData() {
        return multi_columnCombo_box_data;
    }

    private @NonNull ToolTipBoxInfo parseToolTipInfo(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("tool_tip", n);
        return new ToolTipBoxInfo(getHorizontal(node, texture),
                getInt(node, "left_offset"),
                getInt(node, "bottom_offset"),
                getInt(node, "right_offset"),
                getInt(node, "top_offset"));
    }

    public @NonNull ToolTipBoxInfo getToolTipInfo() {
        return tool_tip;
    }

    private @NonNull PanelData parsePanelData(@NonNull Node n, @NonNull Texture texture) {
        Node node = getNodeByName("panel", n);
        return new PanelData(getBox(node, texture),
                getHorizontal(node, texture),
                getInt(node, "left_caption_offset"),
                getInt(node, "right_caption_offset"),
                getInt(node, "bottom_caption_offset"),
                getInt(node, "left_tab_offset"),
                getInt(node, "bottom_tab_offset"));
    }

    public @NonNull PanelData getPanelData() {
        return panel_data;
    }

    public @NonNull IconQuad getFlagDefault() {
        return flag_default;
    }

    public @NonNull IconQuad getFlagDa() {
        return flag_da;
    }

    public @NonNull IconQuad getFlagEn() {
        return flag_en;
    }

    public @NonNull IconQuad getFlagDe() {
        return flag_de;
    }

    public @NonNull IconQuad getFlagEs() {
        return flag_es;
    }

    public @NonNull IconQuad getFlagIt() {
        return flag_it;
    }

    public @NonNull IconQuad getFlagPt() {
        return flag_pt;
    }
}
