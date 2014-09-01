package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.regclient.RegistrationClient;
import com.oddlabs.regclient.RegistrationListener;
import com.oddlabs.registration.RegistrationKeyFormatException;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.gui.CancelButton;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.ServerMessageBundler;
import com.oddlabs.tt.util.Utils;

public final strictfp class RegisterProgressForm extends Form implements RegistrationListener {
	private final RegisterProgressFormListener parent_form;
	//private final RegistrationHttpClient reg_client;
	private final ResourceBundle bundle = ResourceBundle.getBundle(RegisterProgressForm.class.getName());
	private final GUIRoot gui_root;
	
	public RegisterProgressForm(GUIRoot gui_root, RegisterProgressFormListener parent_form) {
		this.gui_root = gui_root;
		this.parent_form = parent_form;
		Label info_label = new Label(Utils.getBundleString(bundle, "registering"), Skin.getSkin().getHeadlineFont());
		addChild(info_label);
		HorizButton cancel_button = new CancelButton(120);
		addChild(cancel_button);
		cancel_button.addMouseClickListener(new CancelListener(this));
		
		// Place objects
		info_label.place();
		cancel_button.place(info_label, BOTTOM_MID);

		// headline
		compileCanvas();
		centerPos();
		gui_root.addModalForm(this);
		//reg_client = RegistrationHttpClient.register(reg_key, this);
	}

	public final void doRemove() {
		super.doRemove();
		Renderer.getRegistrationClient().cancelRegistration();
		//reg_client.close();
	}

	public final void registrationFailed(int reason_id, Exception e) { //interface RegistrationListener
		remove();
		parent_form.registrationFailed();
		
		String failed_message;
		switch (reason_id) {
			case RegistrationClient.KEY_FORMAT_EXCEPTION:
				failed_message = ServerMessageBundler.getRegistrationKeyFormatExceptionMessage((RegistrationKeyFormatException)e);
				break;
			case RegistrationClient.LOAD_FAILED_EXCEPTION:
				failed_message = Utils.getBundleString(bundle, "failed_message", new Object[]{Globals.SUPPORT_ADDRESS, Utils.getBundleString(bundle, "load_failed")});
				break;
			case RegistrationClient.HTTP_EXCEPTION:
				failed_message = Utils.getBundleString(bundle, "failed_message", new Object[]{Globals.SUPPORT_ADDRESS, e.getMessage()});
				break;
			default:
				throw new RuntimeException("reason_id = " + reason_id);
		}
		
		gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle, "failed_caption"), failed_message));
	}

	public final void registrationCompleted() { //interface RegistrationListener
		remove();
		parent_form.registrationCompleted();
	}

	protected final void doCancel() {
		Renderer.getRegistrationClient().cancelRegistration();
		//reg_client.close();
	}
}
