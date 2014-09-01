package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import org.lwjgl.input.Keyboard;

import com.oddlabs.registration.RegistrationKey;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.ButtonObject;
import com.oddlabs.tt.gui.CancelButton;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.EditLine;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.KeyboardEvent;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.LabelBox;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.guievent.EnterListener;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.delegate.MainMenu;

import org.lwjgl.Sys;

public final strictfp class RegistrationForm extends Form implements RegisterProgressFormListener {
	private final static int BUTTON_WIDTH = 100;
	private final static int BUTTON_WIDE_WIDTH = 235;
	private final static int EDITLINE_WIDTH = 220;

	private final boolean online;
	private final GUIRoot gui_root;
	private final EditLine editline_reg;
	private final MainMenu main_menu;
	
	public RegistrationForm(GUIRoot gui_root, boolean online, MainMenu main_menu) {
		this.gui_root = gui_root;
		this.online = online;
		this.main_menu = main_menu;
		ResourceBundle bundle = ResourceBundle.getBundle(RegistrationForm.class.getName());
		// headline
		Label label_headline = new Label(Utils.getBundleString(bundle, "enter_code_caption"), Skin.getSkin().getHeadlineFont());
		addChild(label_headline);

		// login
		LabelBox label_reg = new LabelBox(Utils.getBundleString(bundle, "info_message"), Skin.getSkin().getEditFont(), 400);
		editline_reg = new EditLine(EDITLINE_WIDTH, RegistrationKey.LENGTH, RegistrationKey.ALLOWED_CHARS, EditLine.LEFT_ALIGNED);
		editline_reg.addEnterListener(new RegistrationListener());

		Label reg = new Label(Utils.getBundleString(bundle, "registration_code"), Skin.getSkin().getEditFont());
		if (online)
			addChild(label_reg);
		addChild(reg);
		addChild(editline_reg);
		// buttons
		Group group_buttons = new Group();

		ButtonObject button_paste = new HorizButton(Utils.getBundleString(bundle, "paste"), BUTTON_WIDE_WIDTH);
		button_paste.addMouseClickListener(new PasteListener());
		ButtonObject button_register = new HorizButton(Utils.getBundleString(bundle, "register"), BUTTON_WIDTH);
		button_register.addMouseClickListener(new RegistrationListener());
		ButtonObject button_cancel_quit;
		if (online) {
			button_cancel_quit = new CancelButton(BUTTON_WIDTH);
			button_cancel_quit.addMouseClickListener(new CancelListener(this));
		} else {
			button_cancel_quit = new HorizButton(Utils.getBundleString(ResourceBundle.getBundle(MainMenu.class.getName()), "quit"), BUTTON_WIDTH);
			button_cancel_quit.addMouseClickListener(new ExitListener());
		}
		
		group_buttons.addChild(button_paste);
		group_buttons.addChild(button_register);
		group_buttons.addChild(button_cancel_quit);

		button_cancel_quit.place();
		button_register.place(button_cancel_quit, LEFT_MID);
		button_paste.place(button_register, LEFT_MID);
		
		group_buttons.compileCanvas();
		addChild(group_buttons);
		
		// headline
		label_headline.place();
		if (online) {
			label_reg.place(label_headline, BOTTOM_LEFT);
			reg.place(label_reg, BOTTOM_LEFT);
		} else
			reg.place(label_headline, BOTTOM_LEFT);
		
		editline_reg.place(reg, RIGHT_MID);
		group_buttons.place(ORIGIN_BOTTOM_RIGHT);

		compileCanvas();
	}
	
	public final void setFocus() {
		editline_reg.setFocus();
	}
	
	private final void register() {
		String reg_key = editline_reg.getContents();
		RegisterProgressForm form = new RegisterProgressForm(gui_root, this);
		Renderer.getRegistrationClient().setListener(form);
		Renderer.getRegistrationClient().setKey(reg_key);
		Renderer.getRegistrationClient().register(false);
		
		/*try {
			RegistrationKey.decode(reg_key); // Key check
			doRegister(reg_key);
		} catch (RegistrationKeyFormatException e) {
			gui_root.addModalForm(new MessageForm(ServerMessageBundler.getRegistrationKeyFormatExceptionMessage(e)));
		}*/
	}

	/*private final void doRegister(String key_string) {
		Settings.getSettings().reg_key = key_string;
		Settings.getSettings().save();
		Renderer.loadRegistrationInfo();
	}*/

	public final void registrationFailed() {
	}
	
	public final void registrationCompleted() {
		Settings.getSettings().reg_key = editline_reg.getContents();
		remove();
		ResourceBundle bundle2 = ResourceBundle.getBundle(RegisterProgressForm.class.getName());
		String success_message = Utils.getBundleString(bundle2, "success_message", new Object[]{Globals.SUPPORT_ADDRESS});
		gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle2, "success_caption"), success_message));
		main_menu.reload();
	}
	
	private final void pasteClipboard(String contents) {
		contents = contents.trim();
		editline_reg.clear();
		StringBuffer append_buffer = new StringBuffer();
		for (int i = 0; i < contents.length(); i++) {
			char c = contents.charAt(i);
			if (editline_reg.isAllowed(c))
				append_buffer.append(c);
		}
		editline_reg.append(append_buffer);
	}
	
	private final void showQuitForm() {
		gui_root.addModalForm(new QuitForm(gui_root, false));
	}

	protected final void keyRepeat(KeyboardEvent event) {
		if (online)
			super.keyPressed(event);
		else if (event.getKeyCode() == Keyboard.KEY_ESCAPE)
			showQuitForm();
	}

	private final strictfp class PasteListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			String clipboard = (String)LocalEventQueue.getQueue().getDeterministic().log(Sys.getClipboard());
			if (clipboard != null)
				pasteClipboard(clipboard);
		}
	}

	private final strictfp class RegistrationListener implements MouseClickListener, EnterListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			register();
		}

		public final void enterPressed(CharSequence text) {
			register();
		}
	}

	private final strictfp class ExitListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			showQuitForm();
		}
	}
}
