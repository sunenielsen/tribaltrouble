package com.oddlabs.tt.form;

import com.oddlabs.tt.gui.*;
import com.oddlabs.tt.guievent.*;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.viewer.WorldViewer;
import com.oddlabs.util.Quad;

public strictfp class CampaignDialogForm extends Form {
	public final static int ALIGN_IMAGE_LEFT = 1;
	public final static int ALIGN_IMAGE_RIGHT = 2;

	private final static int WIDTH = 300;

	private final Runnable runnable;
	private final boolean cancel;

	private HorizButton ok_button;
	
	public CampaignDialogForm(CharSequence header, CharSequence text, Quad image, int align) {
		this(header, text, image, align, null);
	}

	public CampaignDialogForm(CharSequence header, CharSequence text, Quad image, int align, Runnable runnable) {
		this(header, text, image, align, runnable, false);
	}

	public CampaignDialogForm(CharSequence header, CharSequence text, Quad image, int align, Runnable runnable, boolean cancel) {
		this.runnable = runnable;
		this.cancel = cancel;
		buildForm(header, text, image, align, cancel);
		ok_button.addMouseClickListener(new MouseClickListener() {
			public final void mouseClicked(int button, int x, int y, int clicks) {
				remove();
				run();
			}
		});
	}

	protected void run() {
		if (runnable != null)
			runnable.run();
	}

	protected final void doCancel() {
		if (!cancel)
			run();
	}

	private final void buildForm(CharSequence header, CharSequence text, Quad image, int align, boolean cancel) {
		GUIIcon gui_icon = null;
		if (image != null) {
			gui_icon = new GUIIcon(image);
			addChild(gui_icon);
		}
		Label header_label = new Label(header, Skin.getSkin().getHeadlineFont());
		addChild(header_label);
		LabelBox label_box = new LabelBox(text, Skin.getSkin().getEditFont(), WIDTH);
		addChild(label_box);
		ok_button = new OKButton(80);
		addChild(ok_button);

		if (gui_icon != null) {
			gui_icon.place();
			if (align == ALIGN_IMAGE_LEFT) {
				label_box.place(gui_icon, RIGHT_MID);
			} else {
				label_box.place(gui_icon, LEFT_MID);
			}
		} else {
			label_box.place();
		}
		header_label.place(label_box, TOP_LEFT);
		ok_button.place(ORIGIN_BOTTOM_RIGHT);
		if (cancel) {
			HorizButton cancel_button = new CancelButton(80);
			addChild(cancel_button);
			cancel_button.place(ok_button, RIGHT_MID);
			cancel_button.addMouseClickListener(new CancelListener(this));
		}

		compileCanvas();
		centerPos();
	}

	public final void setFocus() {
		ok_button.setFocus();
	}
}
