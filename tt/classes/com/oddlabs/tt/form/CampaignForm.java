package com.oddlabs.tt.form;

import java.io.FileNotFoundException;
import java.io.InvalidClassException;
import java.util.ResourceBundle;

import com.oddlabs.tt.gui.CancelButton;
import com.oddlabs.tt.gui.CancelListener;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.HorizButton;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.LoadCampaignBox;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.delegate.Menu;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.guievent.RowListener;
import com.oddlabs.tt.player.campaign.Campaign;
import com.oddlabs.tt.player.campaign.CampaignState;
import com.oddlabs.tt.player.campaign.NativeCampaign;
import com.oddlabs.tt.player.campaign.VikingCampaign;
import com.oddlabs.util.DeterministicSerializer;
import com.oddlabs.util.DeterministicSerializerLoopbackInterface;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.net.NetworkSelector;

public final strictfp class CampaignForm extends Form implements DeterministicSerializerLoopbackInterface {
	private final HorizButton button_vikings;
	private final LoadCampaignBox load_campaign_box;
	private final ResourceBundle bundle = ResourceBundle.getBundle(CampaignForm.class.getName());
	private final Menu main_menu;
	private final GUIRoot gui_root;
	private final NetworkSelector network;

	public CampaignForm(NetworkSelector network, GUIRoot gui_root, Menu main_menu) {
		this.main_menu = main_menu;
		this.gui_root = gui_root;
		this.network = network;
		Label headline = new Label(Utils.getBundleString(bundle, "campaign"), Skin.getSkin().getHeadlineFont());
		addChild(headline);
		
		// Combo box
		RowListener listener = new LoadListener();
		load_campaign_box = new LoadCampaignBox(gui_root, listener);

		HorizButton button_delete = new HorizButton(Utils.getBundleString(bundle, "delete"), 120);
		button_delete.addMouseClickListener(new DeleteListener());

		button_vikings = new HorizButton(Utils.getBundleString(bundle, "new"), 120);
		button_vikings.addMouseClickListener(new VikingsListener());

		HorizButton button_load = new HorizButton(Utils.getBundleString(bundle, "load"), 120);
		button_load.addMouseClickListener((MouseClickListener)listener);

		HorizButton button_cancel = new CancelButton(120);
		button_cancel.addMouseClickListener(new CancelListener(this));

		// Add objects
		addChild(button_delete);
		addChild(button_vikings);
		addChild(load_campaign_box);
		addChild(button_load);
		addChild(button_cancel);

		// Place objects
		headline.place();
		load_campaign_box.place(button_vikings, BOTTOM_LEFT);
		button_cancel.place(ORIGIN_BOTTOM_RIGHT);
		button_delete.place(button_cancel, LEFT_MID);
		button_load.place(button_delete, LEFT_MID);
		button_vikings.place(button_load, LEFT_MID);

		// headline
		compileCanvas();
		centerPos();
	}

	public final void setFocus() {
		button_vikings.setFocus();
	}

	public final void load(CampaignState campaign_state) {
		Campaign campaign;
		if (campaign_state.getRace() == CampaignState.RACE_VIKINGS)
			campaign = new VikingCampaign(network, gui_root, campaign_state);
		else
			campaign = new NativeCampaign(network, gui_root, campaign_state);
		setDisabled(true);
		if (campaign_state.getIslandState(0) == CampaignState.ISLAND_COMPLETED) {
			campaign.pushDelegate(network, gui_root.getGUI());
		} else
			campaign.startIsland(network, gui_root, 0);
	}

	public final void saveSucceeded() {
		load_campaign_box.refresh();
	}

	public final void loadSucceeded(Object object) {
		CampaignState[] campaign_states = (CampaignState[])object;
		CampaignState selected = (CampaignState)load_campaign_box.getSelected();
		if (selected != null) {
			CampaignState[] new_states = new CampaignState[campaign_states.length - 1];
			int offset = 0;
			for (int i = 0; i < campaign_states.length; i++) {
				if (campaign_states[i].getName().equals(selected.getName())) {
					offset = 1;
				} else {
					new_states[i - offset] = campaign_states[i];
				}
			}
			LoadCampaignBox.saveSavegames(new_states, this);
		}
	}

	public final void failed(Exception e) {
		if (e instanceof FileNotFoundException) {
		} else if (e instanceof InvalidClassException) {
		} else {
			String failed_message = Utils.getBundleString(bundle, "failed_message", new Object[]{LoadCampaignBox.SAVEGAMES_FILE_NAME, e.getMessage()});
			gui_root.addModalForm(new MessageForm(failed_message));
		}
	}

	private final strictfp class DeleteListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			CampaignState state = (CampaignState)load_campaign_box.getSelected();
			if (state != null) {
				String confirm_str = Utils.getBundleString(bundle, "confirm_delete", new Object[]{state.getName()});
				gui_root.addModalForm(new QuestionForm(confirm_str, new ActionDeleteListener()));
			}
		}
	}

	private final strictfp class ActionDeleteListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			LoadCampaignBox.loadSavegames(CampaignForm.this);
		}
	}

	private final strictfp class VikingsListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			main_menu.setMenu(new NewCampaignForm(network, gui_root, main_menu, CampaignForm.this));
		}
	}

	private final strictfp class LoadListener implements MouseClickListener, RowListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			Object selected = load_campaign_box.getSelected();
			if (selected != null)
				load((CampaignState)selected);
		}

		public final void rowDoubleClicked(Object object) {
			load((CampaignState)object);
		}
		
		public final void rowChosen(Object object) {
		}
	}
}
