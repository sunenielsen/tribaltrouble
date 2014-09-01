package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.gui.CancelButton;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.updater.UpdateHandler;
import com.oddlabs.updater.UpdateStatus;
import com.oddlabs.updater.Updater;
import com.oddlabs.tt.delegate.Menu;

public final strictfp class UpdateGameForm extends Form implements Animated, UpdateHandler {
	private final static int MAX_ERROR_LENGTH = 100;

	private final Menu main_menu;
	private final Updater updater;
	private final Label progress;
	private final ResourceBundle bundle = ResourceBundle.getBundle(UpdateGameForm.class.getName());
	private final GUIRoot gui_root;

	public UpdateGameForm(GUIRoot gui_root, Menu main_menu) {
		this.main_menu = main_menu;
		this.gui_root = gui_root;
		Label info_label = new Label(Utils.getBundleString(bundle, "updating_caption"), Skin.getSkin().getHeadlineFont());
		addChild(info_label);
		progress = new Label("", Skin.getSkin().getEditFont());
		progress.setDim(400, progress.getHeight());
		progress.set(Utils.getBundleString(bundle, "updating_start"));
		addChild(progress);
		HorizButton cancel_button = new CancelButton(80);
		addChild(cancel_button);
		cancel_button.addMouseClickListener(new CancelListener(this));
		// Place objects
		info_label.place();
		progress.place(info_label, BOTTOM_LEFT);
		cancel_button.place(ORIGIN_BOTTOM_RIGHT);

		// headline
		compileCanvas();
		centerPos();
		this.updater = new Updater(LocalEventQueue.getQueue().getDeterministic(), LocalInput.getUpdateInfo(), this);
		LocalEventQueue.getQueue().getManager().registerAnimation(this);
	}

	public final void statusLog(int subtype, String argument) {
		String message;
		switch (subtype) {
			case UpdateStatus.DELETING:
				message = Utils.getBundleString(bundle, "log_deleting", new Object[]{argument});
				break;
			case UpdateStatus.COPYING:
				message = Utils.getBundleString(bundle, "log_copying", new Object[]{argument});
				break;
			case UpdateStatus.UPDATED:
				message = Utils.getBundleString(bundle, "log_updated", new Object[]{argument});
				break;
			case UpdateStatus.UPDATING:
				message = Utils.getBundleString(bundle, "log_updating");
				break;
			case UpdateStatus.CHECKED:
				message = Utils.getBundleString(bundle, "log_checked", new Object[]{argument});
				break;
			case UpdateStatus.INIT:
				message = Utils.getBundleString(bundle, "log_init", new Object[]{argument});
				break;
			case UpdateStatus.CHECKING:
				message = Utils.getBundleString(bundle, "log_checking", new Object[]{argument});
				break;
			case UpdateStatus.COPIED:
				message = Utils.getBundleString(bundle, "log_copied", new Object[]{argument});
				break;
			default:
				throw new RuntimeException("Unknown enum: " + subtype);
		}
		progress.set(message);
	}

	public final void statusError(Throwable exception) {
		exception.printStackTrace();
		remove();
		String error = exception.toString();
		if (error.length() > MAX_ERROR_LENGTH)
			error = error.substring(0, MAX_ERROR_LENGTH) + "...";
		String error_message = Utils.getBundleString(bundle, "update_failed", new Object[]{error});
		gui_root.addModalForm(new MessageForm(error_message));
	}

	public final void statusUpdated(String changelog) {
		main_menu.setMenu(new ShowChangesForm(gui_root, changelog));
	}

	public final void statusNoUpdates() {
		remove();
		gui_root.addModalForm(new MessageForm(Utils.getBundleString(bundle, "no_updates")));
	}

	public final void animate(float delta) {
		updater.updateStatus();
	}
	
	public final void updateChecksum(StateChecksum checksum) {
	}

	public final void doRemove() {
		super.doRemove();
		updater.cancel();
		LocalEventQueue.getQueue().getManager().removeAnimation(this);
	}
}
