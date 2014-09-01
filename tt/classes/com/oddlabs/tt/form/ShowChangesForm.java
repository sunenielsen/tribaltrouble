package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.OKButton;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.TextBox;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.Utils;

// unicode codes: ae: 00E6 - oe: 00F8 - aa: 00E5 - AE: 00C6 - OE: 00D8 - AA: 00C5 - (C): 00A9

public final strictfp class ShowChangesForm extends Form {
	private final HorizButton ok_button;
	private final ResourceBundle bundle = ResourceBundle.getBundle(ShowChangesForm.class.getName());
	private final GUIRoot gui_root;

	public ShowChangesForm(GUIRoot gui_root, String changelog) {
		this.gui_root = gui_root;
		Label head_label = new Label(Utils.getBundleString(bundle, "update_complete"), Skin.getSkin().getHeadlineFont());
		addChild(head_label);
		head_label.place();
		
		TextBox changes_box = new TextBox(400, 300, Skin.getSkin().getEditFont(), 100000);
		changes_box.append(changelog);
		addChild(changes_box);
		changes_box.place(head_label, BOTTOM_LEFT);
		ok_button = new OKButton(100);
		addChild(ok_button);
		ok_button.addMouseClickListener(new CancelListener(this));
		ok_button.place(ORIGIN_BOTTOM_RIGHT);
		compileCanvas();
		centerPos();
	}

	public final void setFocus() {
		ok_button.setFocus();
	}

	public final void doCancel() {
		gui_root.addModalForm(new QuestionForm(Utils.getBundleString(bundle, "restart"), new QuitListener()));
	}

	private final class QuitListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			Renderer.shutdown();
		}
	}
}
