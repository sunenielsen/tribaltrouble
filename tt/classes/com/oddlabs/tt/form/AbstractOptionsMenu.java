package com.oddlabs.tt.form;

import java.util.Locale;
import java.util.ResourceBundle;

import org.lwjgl.input.Cursor;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.CheckBox;
import com.oddlabs.tt.gui.ColumnInfo;
import com.oddlabs.tt.gui.DoNowListener;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIObject;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.IconLabel;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.LabelBox;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.gui.MultiColumnComboBox;
import com.oddlabs.tt.gui.Panel;
import com.oddlabs.tt.gui.PanelGroup;
import com.oddlabs.tt.gui.Languages;
import com.oddlabs.tt.gui.PulldownButton;
import com.oddlabs.tt.gui.PulldownItem;
import com.oddlabs.tt.gui.PulldownMenu;
import com.oddlabs.tt.gui.Row;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.Slider;
import com.oddlabs.tt.gui.SortedLabel;
import com.oddlabs.tt.guievent.CheckBoxListener;
import com.oddlabs.tt.guievent.CloseListener;
import com.oddlabs.tt.guievent.ItemChosenListener;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.guievent.RowListener;
import com.oddlabs.tt.guievent.ValueListener;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.render.SerializableDisplayMode;
import com.oddlabs.tt.util.ServerMessageBundler;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.util.Quad;

public abstract strictfp class AbstractOptionsMenu extends Form {
	private final static int BUTTON_WIDTH = 100;
	private final static int MAX_VALUE = 20;

	private final static int SLIDER_WIDTH = 270;

	private final static boolean TEMPORARILY_DISABLE_MUSIC_CONTROLS = false;
	private final CheckBox cb_fullscreen;
	
	private final Slider slider_music;
	private final Slider slider_sound;
	private final GUIRoot gui_root;
	
	private final PulldownMenu pm_detail;
	private final ResourceBundle bundle = ResourceBundle.getBundle(OptionsMenu.class.getName());
	private final MultiColumnComboBox language_list_box;
	private final PulldownMenu pm_gamespeed;
	private int last_detail_value;
	
	public AbstractOptionsMenu(GUIRoot gui_root) {
		super();
		this.gui_root = gui_root;
		Label label_headline = new Label(Utils.getBundleString(bundle, "options_caption"), Skin.getSkin().getHeadlineFont());
		addChild(label_headline);
		
		Panel general = new Panel(Utils.getBundleString(bundle, "general_settings_caption"));
		Panel display = new Panel(Utils.getBundleString(bundle, "graphics_caption"));
		Panel sound = new Panel(Utils.getBundleString(bundle, "sound_caption"));
		Panel language = new Panel(Utils.getBundleString(bundle, "language_caption"));
		Panel report_bug = new Panel(Utils.getBundleString(bundle, "report_bug_caption"));
		
		// Sound	
		Group group_music = new Group();
		sound.addChild(group_music);
		Label label_music_low = new Label(Utils.getBundleString(bundle, "low"), Skin.getSkin().getEditFont());
		group_music.addChild(label_music_low);
		Label label_music_high = new Label(Utils.getBundleString(bundle, "high"), Skin.getSkin().getEditFont());
		group_music.addChild(label_music_high);
		CheckBox cb_music = new CheckBox(Settings.getSettings().play_music, Utils.getBundleString(bundle, "music"));
		group_music.addChild(cb_music);
		Label label_music = new Label(Utils.getBundleString(bundle, "music_volume"), Skin.getSkin().getEditFont());
		group_music.addChild(label_music);
		cb_music.addCheckBoxListener(new CBMusicListener());
		slider_music = new Slider(SLIDER_WIDTH, 0, MAX_VALUE, (int)(Settings.getSettings().music_gain*(MAX_VALUE)));
		if (TEMPORARILY_DISABLE_MUSIC_CONTROLS)
			slider_music.setDisabled(true);
		else
			slider_music.setDisabled(!cb_music.isMarked());
		group_music.addChild(slider_music);
		slider_music.addValueListener(new SliderMusicListener());
		cb_music.place();
		label_music.place(cb_music, BOTTOM_LEFT);
		label_music_low.place(label_music, BOTTOM_LEFT);
		slider_music.place(label_music_low, RIGHT_MID);
		label_music_high.place(slider_music, RIGHT_MID);
		group_music.compileCanvas();
		if (TEMPORARILY_DISABLE_MUSIC_CONTROLS)
			group_music.setDisabled(true);
		else
			group_music.setDisabled(!LocalInput.alIsCreated());

		
		Group group_sound = new Group();
		sound.addChild(group_sound);
		Label label_sound_low = new Label(Utils.getBundleString(bundle, "low"), Skin.getSkin().getEditFont());
		group_sound.addChild(label_sound_low);
		Label label_sound_high = new Label(Utils.getBundleString(bundle, "high"), Skin.getSkin().getEditFont());
		group_sound.addChild(label_sound_high);
		CheckBox cb_sound = new CheckBox(Settings.getSettings().play_sfx, Utils.getBundleString(bundle, "sound_effects"));
		group_sound.addChild(cb_sound);
		Label label_sound = new Label(Utils.getBundleString(bundle, "sound_effects_volume"), Skin.getSkin().getEditFont());
		group_sound.addChild(label_sound);
		cb_sound.addCheckBoxListener(new CBSFXListener());
		slider_sound = new Slider(SLIDER_WIDTH, 0, MAX_VALUE, (int)(Settings.getSettings().sound_gain*(MAX_VALUE)));
		slider_sound.setDisabled(!cb_sound.isMarked());
		group_sound.addChild(slider_sound);
		slider_sound.addValueListener(new SliderSFXListener());
		cb_sound.place();
		label_sound.place(cb_sound, BOTTOM_LEFT);
		label_sound_low.place(label_sound, BOTTOM_LEFT);
		slider_sound.place(label_sound_low, RIGHT_MID);
		label_sound_high.place(slider_sound, RIGHT_MID);
		group_sound.compileCanvas();
		group_sound.setDisabled(!LocalInput.alIsCreated());

		// Fullscreen
		Group group_fullscreen = new Group();
		display.addChild(group_fullscreen);
		cb_fullscreen = new CheckBox(LocalInput.inFullscreen(), Utils.getBundleString(bundle, "fullscreen"), Utils.getBundleString(bundle, "fullscreen_tip"));
		cb_fullscreen.addCheckBoxListener(new CBFullscreen());
		group_fullscreen.addChild(cb_fullscreen);
		cb_fullscreen.place();
		group_fullscreen.compileCanvas();

		// Hardware cursor
		Group group_hardware_cursor = new Group();
		display.addChild(group_hardware_cursor);
		CheckBox cb_hardware_cursor = new CheckBox(Settings.getSettings().use_native_cursor, Utils.getBundleString(bundle, "hardware_cursor"), Utils.getBundleString(bundle, "hardware_cursor_tip", new Object[]{"Ctrl-H"}));
		cb_hardware_cursor.addCheckBoxListener(new CBHardwareCursor());
		group_hardware_cursor.addChild(cb_hardware_cursor);
		cb_hardware_cursor.place();
		group_hardware_cursor.compileCanvas();
		group_hardware_cursor.setDisabled((LocalInput.getNativeCursorCaps() & Cursor.CURSOR_ONE_BIT_TRANSPARENCY) == 0);

		// Invert camera
		Group group_invert_camera = new Group();
		general.addChild(group_invert_camera);
		CheckBox cb_invert_camera = new CheckBox(Settings.getSettings().invert_camera_pitch, Utils.getBundleString(bundle, "invert_camera"), Utils.getBundleString(bundle, "invert_camera_tip"));
		cb_invert_camera.addCheckBoxListener(new CBInvertCamera());
		group_invert_camera.addChild(cb_invert_camera);
		cb_invert_camera.place();
		group_invert_camera.compileCanvas();
		
		// Aggressive units
		Group group_aggressive_units = new Group();
		general.addChild(group_aggressive_units);
		CheckBox cb_aggressive_units = new CheckBox(Settings.getSettings().aggressive_units, Utils.getBundleString(bundle, "aggressive_units"), Utils.getBundleString(bundle, "aggressive_units_tip", new Object[]{"Ctrl-A"}));
		cb_aggressive_units.addCheckBoxListener(new CBAggressiveUnits());
		group_aggressive_units.addChild(cb_aggressive_units);
		cb_aggressive_units.place();
		group_aggressive_units.compileCanvas();
		
		// gfx detail
		Group group_detail = new Group();
		display.addChild(group_detail);
		
		Label label_detail = new Label(Utils.getBundleString(bundle, "graphical_detail"), Skin.getSkin().getEditFont());
		group_detail.addChild(label_detail);

		last_detail_value = Settings.getSettings().graphic_detail;
		pm_detail = new PulldownMenu();
		pm_detail.addItem(new PulldownItem(Utils.getBundleString(bundle, "low")));
		pm_detail.addItem(new PulldownItem(Utils.getBundleString(bundle, "medium")));
		pm_detail.addItem(new PulldownItem(Utils.getBundleString(bundle, "high")));
		PulldownButton pb_detail = new PulldownButton(gui_root, pm_detail, last_detail_value, 150);
		
		group_detail.addChild(pb_detail);
		addCloseListener(new OptionsCloseListener());
		label_detail.place();
		pb_detail.place(label_detail, BOTTOM_LEFT);
		group_detail.compileCanvas();

		// Display mode
		Group mode_group = new Group();
		display.addChild(mode_group);

		Label mode_label = new Label(Utils.getBundleString(bundle, "display_mode"), Skin.getSkin().getEditFont());
		mode_group.addChild(mode_label);

		ColumnInfo[] mode_infos = new ColumnInfo[]{new ColumnInfo("", 150)};
		MultiColumnComboBox mode_list_box = new MultiColumnComboBox(gui_root, mode_infos, 200, false);
		addChild(mode_list_box);

		SerializableDisplayMode[] modes = LocalInput.getAvailableModes();
		SerializableDisplayMode current_mode = LocalInput.getCurrentMode();
//		PulldownMenu mode_menu = new PulldownMenu();
		Row current_row = null;
		int index = 0;
		for (int i = 0; i < modes.length; i++) {
			if (modes[i].getBitsPerPixel() == current_mode.getBitsPerPixel()) {
				String mode_string = Utils.getBundleString(bundle, "mode", new Object[]{
					Integer.toString(modes[i].getWidth()), Integer.toString(modes[i].getHeight()), Integer.toString(modes[i].getFrequency())});
//				mode_menu.addItem(new PulldownItem(mode_string, modes[i]));
				Label label = new SortedLabel(mode_string, i, Skin.getSkin().getMultiColumnComboBoxData().getFont());
				Row row = new Row(new GUIObject[]{label}, modes[i]);
				mode_list_box.addRow(row);
				if (modes[i].equals(current_mode))
					current_row = row;
				index++;
			}
		}
		if (current_row != null)
			mode_list_box.selectRow(current_row);
//		PulldownButton mode_pulldown = new PulldownButton(mode_menu, current_index, 180);
//		mode_menu.addItemChosenListener(new DisplayModeListener());
		mode_list_box.addRowListener(new DisplayModeListener());

		mode_group.addChild(mode_list_box);
		mode_label.place();
		mode_list_box.place(mode_label, BOTTOM_LEFT);
		mode_group.compileCanvas();
		
		// Mapmode delay
		Group group_mapmode = new Group();
		general.addChild(group_mapmode);
		Label label_mapmode_headline = new Label(Utils.getBundleString(bundle, "map_mode_delay"), Skin.getSkin().getEditFont());
		group_mapmode.addChild(label_mapmode_headline);
		Label label_mapmode_none = new Label(Utils.getBundleString(bundle, "delay_none"), Skin.getSkin().getEditFont());
		group_mapmode.addChild(label_mapmode_none);
		Label label_mapmode_high = new Label(Utils.getBundleString(bundle, "delay_high"), Skin.getSkin().getEditFont());
		group_mapmode.addChild(label_mapmode_high);
		Slider slider_mapmode = new Slider(SLIDER_WIDTH, 0, MAX_VALUE, (int)(Settings.getSettings().mapmode_delay*(MAX_VALUE)));
		group_mapmode.addChild(slider_mapmode);
		slider_mapmode.addValueListener(new SliderMapmodeListener());
		label_mapmode_headline.place();
		label_mapmode_none.place(label_mapmode_headline, BOTTOM_LEFT);
		slider_mapmode.place(label_mapmode_none, RIGHT_MID);
		label_mapmode_high.place(slider_mapmode, RIGHT_MID);
		group_mapmode.compileCanvas();
		
		// Tooltip delay
		Group group_tooltip = new Group();
		general.addChild(group_tooltip);
		Label label_tooltip_headline = new Label(Utils.getBundleString(bundle, "tool_tip_delay"), Skin.getSkin().getEditFont());
		group_tooltip.addChild(label_tooltip_headline);
		Label label_tooltip_none = new Label(Utils.getBundleString(bundle, "delay_none"), Skin.getSkin().getEditFont());
		group_tooltip.addChild(label_tooltip_none);
		Label label_tooltip_high = new Label(Utils.getBundleString(bundle, "delay_high"), Skin.getSkin().getEditFont());
		group_tooltip.addChild(label_tooltip_high);
		Slider slider_tooltip = new Slider(SLIDER_WIDTH, 0, MAX_VALUE, (int)(Settings.getSettings().tooltip_delay*(MAX_VALUE)));
		group_tooltip.addChild(slider_tooltip);
		slider_tooltip.addValueListener(new SliderTooltipListener());
		label_tooltip_headline.place();
		label_tooltip_none.place(label_tooltip_headline, BOTTOM_LEFT);
		slider_tooltip.place(label_tooltip_none, RIGHT_MID);
		label_tooltip_high.place(slider_tooltip, RIGHT_MID);
		group_tooltip.compileCanvas();

		// Gamespeed
		Group group_gamespeed = new Group();
		general.addChild(group_gamespeed);
		Label label_gamespeed = new Label(Utils.getBundleString(bundle, "gamespeed"), Skin.getSkin().getEditFont());
		group_gamespeed.addChild(label_gamespeed);
		this.pm_gamespeed = new PulldownMenu();
		pm_gamespeed.addItem(new PulldownItem(ServerMessageBundler.getGamespeedString(Game.GAMESPEED_PAUSE)));
		pm_gamespeed.addItem(new PulldownItem(ServerMessageBundler.getGamespeedString(Game.GAMESPEED_SLOW)));
		pm_gamespeed.addItem(new PulldownItem(ServerMessageBundler.getGamespeedString(Game.GAMESPEED_NORMAL)));
		pm_gamespeed.addItem(new PulldownItem(ServerMessageBundler.getGamespeedString(Game.GAMESPEED_FAST)));
		pm_gamespeed.addItem(new PulldownItem(ServerMessageBundler.getGamespeedString(Game.GAMESPEED_LUDICROUS)));
		PulldownButton pb_gamespeed = new PulldownButton(gui_root, pm_gamespeed, 150);
		pm_gamespeed.addItemChosenListener(new GamespeedListener());
		group_gamespeed.addChild(pb_gamespeed);
		label_gamespeed.place();
		pb_gamespeed.place(label_gamespeed, RIGHT_MID);
		group_gamespeed.compileCanvas();

		// language
		Group language_group = new Group();
		language.addChild(language_group);
		Label language_label = new Label(Utils.getBundleString(bundle, "language_label"), Skin.getSkin().getEditFont());
		language_group.addChild(language_label);

		ColumnInfo[] language_infos = new ColumnInfo[]{new ColumnInfo("", 300)};
		language_list_box = new MultiColumnComboBox(gui_root, language_infos, 200, false);
//		addChild(language_list_box);

		checkLanguage();
		current_row = null;
		IconLabel label = new IconLabel(Skin.getSkin().getFlagDefault(), new Label(Utils.getBundleString(bundle, "system_default"), Skin.getSkin().getMultiColumnComboBoxData().getFont()));
		Row row = new Row(new GUIObject[]{label}, Renderer.getRenderer().getDefaultLocale());
		language_list_box.addRow(row);
		if (Settings.getSettings().language.equals("default"))
			current_row = row;
		String[][] languages = gui_root.getGUI().getLanguages().getLanguages();
		Quad[] flags = gui_root.getGUI().getLanguages().getFlags();
		for (int i = 0; i < languages.length; i++) {
			label = new IconLabel(flags[i], new Label(languages[i][1], Skin.getSkin().getMultiColumnComboBoxData().getFont()));
			row = new Row(new GUIObject[]{label}, new Locale(languages[i][0]));
			language_list_box.addRow(row);
			if (languages[i][0].equals(Settings.getSettings().language))
				current_row = row;
		}

		assert current_row != null;
		language_list_box.selectRow(current_row);
		language_list_box.addRowListener(new LanguageListener());

		language_group.addChild(language_list_box);
		language_label.place();
		language_list_box.place(language_label, BOTTOM_LEFT);
		language_group.compileCanvas();

		// report bug
		String text = Utils.getBundleString(bundle, "report_bug_text");
		LabelBox label_box = new LabelBox(text, Skin.getSkin().getEditFont(), 309);
		report_bug.addChild(label_box);
		HorizButton button_bug = new HorizButton(Utils.getBundleString(bundle, "report_bug"), 130);
		button_bug.addMouseClickListener(new BugReportListener());
		report_bug.addChild(button_bug);
		
		// Buttons
		HorizButton button_close = new HorizButton(Utils.getBundleString(bundle, "close"), BUTTON_WIDTH);
		button_close.addMouseClickListener(new CancelListener(this));
		addChild(button_close);

		HorizButton button_about = new HorizButton(Utils.getBundleString(bundle, "about"), BUTTON_WIDTH);
		button_about.addMouseClickListener(new AboutListener());
		addChild(button_about);

		//general panel
		group_gamespeed.place();
		group_mapmode.place(group_gamespeed, BOTTOM_LEFT);
		group_tooltip.place(group_mapmode, BOTTOM_LEFT);
		group_invert_camera.place(group_tooltip, BOTTOM_LEFT);
		group_aggressive_units.place(group_invert_camera, BOTTOM_LEFT);
		general.compileCanvas();
		
		// display
		mode_group.place();
		group_detail.place(mode_group, RIGHT_TOP);
		group_fullscreen.place(group_detail, BOTTOM_LEFT);
		group_hardware_cursor.place(group_fullscreen, BOTTOM_LEFT);
		display.compileCanvas();
		
		group_music.place();
		group_sound.place(group_music, BOTTOM_LEFT);
		sound.compileCanvas();

		// language
		language_group.place();
		language.compileCanvas();

		//report bug
		label_box.place();
		button_bug.place(label_box, BOTTOM_MID);
		report_bug.compileCanvas();
		
		Panel[] panels;
		if (Settings.getSettings().hide_bugreporter)
			panels = new Panel[]{general, display, sound, language};
		else
			panels = new Panel[]{general, display, sound, language, report_bug};

		PanelGroup panel_group = new PanelGroup(panels, 0);
		addChild(panel_group);

		// Place objects
		label_headline.place();
		panel_group.place(label_headline, BOTTOM_LEFT);
		button_close.place(ORIGIN_BOTTOM_RIGHT);
		button_about.place(button_close, LEFT_MID);
		compileCanvas();
	}

	protected final void chooseGamespeed(int speed) {
		pm_gamespeed.chooseItem(speed);
	}

	private final void checkLanguage() {
		if (Settings.getSettings().language.equals("default"))
			return;

		String[][] languages = gui_root.getGUI().getLanguages().getLanguages();
		for (int i = 0; i < languages.length; i++) {
			if (Settings.getSettings().language.equals(languages[i][0]))
				return;
		}
		Settings.getSettings().language = "default";
	}

	protected void changeGamespeed(int index) {
		Globals.gamespeed = index;
	}

	private final void checkDetailChange() {
		int slider_value = pm_detail.getChosenItemIndex();
		if (last_detail_value != slider_value) {
			last_detail_value = slider_value;
			Settings.getSettings().graphic_detail = slider_value;
			gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle, "change_next_run")));
		}
	}

	// Sound
	private final strictfp class CBMusicListener implements CheckBoxListener {
		public final void checked(boolean marked) {
			if (Settings.getSettings().play_music != marked)
				Renderer.getRenderer().toggleMusic();
			slider_music.setDisabled(!marked);
			Settings.getSettings().play_music = marked;
		}
	}

	private final strictfp class CBSFXListener implements CheckBoxListener {
		public final void checked(boolean marked) {
			if (Settings.getSettings().play_sfx != marked)
				Renderer.getRenderer().toggleSound();
			slider_sound.setDisabled(!marked);
			Settings.getSettings().play_sfx = marked;
		}
	}

	private final strictfp class SliderMusicListener implements ValueListener {
		public final void valueSet(int value) {
			float music_gain = (float)value/(MAX_VALUE);
			Settings.getSettings().music_gain = music_gain;
			Renderer.getMusicPlayer().setGain(music_gain);
		}
	}

	private final strictfp class SliderSFXListener implements ValueListener {
		public final void valueSet(int value) {
			Settings.getSettings().sound_gain = (float)value/(MAX_VALUE);
		}
	}

	private final strictfp class SliderMapmodeListener implements ValueListener {
		public final void valueSet(int value) {
			Settings.getSettings().mapmode_delay = (float)value/(MAX_VALUE);
		}
	}

	private final strictfp class SliderTooltipListener implements ValueListener {
		public final void valueSet(int value) {
			Settings.getSettings().tooltip_delay = (float)value/(MAX_VALUE);
			gui_root.setToolTipTimer();
		}
	}

	private final strictfp class CBFullscreen implements CheckBoxListener, DoNowListener {
		public final void doChange(boolean switch_now) {
			SerializableDisplayMode.setFullscreen(cb_fullscreen.isMarked(), switch_now);
		}

		public final void checked(boolean marked) {
			DisplayChangeForm display_change_form = new DisplayChangeForm(this);
			gui_root.addModalForm(display_change_form);
		}
	}


	private final strictfp class CBHardwareCursor implements CheckBoxListener {
		public final void checked(boolean marked) {
			Settings.getSettings().use_native_cursor = marked;
		}
	}

	private final strictfp class OptionsCloseListener implements CloseListener {
		public final void closed() {
			checkDetailChange();
		}
	}

	private final strictfp class CBAggressiveUnits implements CheckBoxListener {
		public final void checked(boolean marked) {
			Settings.getSettings().aggressive_units = marked;
		}
	}

	private final strictfp class CBInvertCamera implements CheckBoxListener {
		public final void checked(boolean marked) {
			Settings.getSettings().invert_camera_pitch = marked;
		}
	}

	private final strictfp class DisplayModeListener implements RowListener, DoNowListener {
		private SerializableDisplayMode mode;
		
		public final void doChange(boolean switch_now) {
			LocalInput.getLocalInput().switchMode(mode, switch_now);
			gui_root.displayChanged();
		}

		public final void rowChosen(Object o) {
			mode = (SerializableDisplayMode)o;
			DisplayChangeForm display_change_form = new DisplayChangeForm(this);
			gui_root.addModalForm(display_change_form);
		}
		
		public final void rowDoubleClicked(Object o) {
		}
	}  

	private final strictfp class LanguageListener implements RowListener {
		public final void rowChosen(Object o) {
			Locale locale = (Locale)o;
			if (locale.getVariant().equals("default"))
				Settings.getSettings().language = "default";
			else
				Settings.getSettings().language = locale.getLanguage();
			gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle, "language_change_next_run")));
		}
		
		public final void rowDoubleClicked(Object o) {
		}
	}  

	private final strictfp class GamespeedListener implements ItemChosenListener {
		public final void itemChosen(PulldownMenu menu, int item_index) {
			changeGamespeed(item_index);
		}
	}  

	private final strictfp class BugReportListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			gui_root.addModalForm(new BugReportConfirmForm());
		}
	}

	private final strictfp class AboutListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			gui_root.addModalForm(new CreditsForm());
		}
	}
}
