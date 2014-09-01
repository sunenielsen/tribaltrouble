package com.oddlabs.tt.delegate;

import java.util.ResourceBundle;

import com.oddlabs.tt.camera.StaticCamera;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.delegate.CameraDelegate;
import com.oddlabs.tt.form.CampaignDialogForm;
import com.oddlabs.tt.gui.GUIIcon;
import com.oddlabs.tt.gui.GUIObject;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.GUI;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.gui.MapIslandData;
import com.oddlabs.tt.gui.NonFocusIconButton;
import com.oddlabs.tt.gui.Renderable;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.gui.KeyboardEvent;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.player.campaign.Campaign;
import com.oddlabs.tt.player.campaign.CampaignState;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.net.NetworkSelector;

import org.lwjgl.input.Keyboard;

public final strictfp class CampaignMapForm extends CameraDelegate {
	private final static int base_width = 800;
	private final static int base_height = 600;

	private final Campaign campaign;
	private final NetworkSelector network;

	public CampaignMapForm(final NetworkSelector network, final GUIRoot gui_root, Campaign campaign) {
		super(gui_root, new StaticCamera(new CameraState()));
		this.campaign = campaign;
		this.network = network;
		final ResourceBundle bundle = ResourceBundle.getBundle(CampaignMapForm.class.getName());

		float scale_x = LocalInput.getViewWidth()/(float)base_width;
		float scale_y = LocalInput.getViewHeight()/(float)base_height;
		setScale(scale_x, scale_y);

		if (campaign.getState().getRace() == CampaignState.RACE_VIKINGS) {
			if (campaign.getState().getIslandState(10) != CampaignState.ISLAND_HIDDEN) {
				addChild(campaign.getIcons().getHiddenRoutes()[0]);
				addChild(campaign.getIcons().getHiddenRoutes()[1]);
			}

			if (campaign.getState().getCurrentIsland() == 14) {
				final Runnable runnable_menu = new Runnable() {
					public final void run() {
						closeCampaign(network, gui_root.getGUI());
					}
				};
				final Runnable runnable_next = new Runnable() {
					public final void run() {
						CampaignDialogForm dialog = new CampaignDialogForm(Utils.getBundleString(bundle, "native_campaign_opened_header"),
								Utils.getBundleString(bundle, "native_campaign_opened"),
								null,
								CampaignDialogForm.ALIGN_IMAGE_LEFT,
								runnable_menu);
						gui_root.addModalForm(dialog);
					}
				};
				CampaignDialogForm dialog = new CampaignDialogForm(Utils.getBundleString(bundle, "viking_header"),
						Utils.getBundleString(bundle, "viking_campaign_completed"),
						campaign.getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						runnable_next);
				gui_root.addModalForm(dialog);
				Settings.getSettings().has_native_campaign = true;
			}
		}
		if (campaign.getState().getRace() == CampaignState.RACE_NATIVES) {
			if (campaign.getState().getIslandState(7) != CampaignState.ISLAND_HIDDEN) {
				addChild(campaign.getIcons().getHiddenRoutes()[0]);
			}

			if (campaign.getState().getCurrentIsland() == 7) {
				Runnable runnable = new Runnable() {
					public final void run() {
						closeCampaign(network, gui_root.getGUI());
					}
				};
				CampaignDialogForm dialog = new CampaignDialogForm(Utils.getBundleString(bundle, "native_header"),
						Utils.getBundleString(bundle, "native_campaign_completed"),
						campaign.getIcons().getFaces()[0],
						CampaignDialogForm.ALIGN_IMAGE_LEFT,
						runnable);
				gui_root.addModalForm(dialog);
			}
		}
		// Islands
		for (int i = 0; i < campaign.getIcons().getNumIslands(); i++) {
			MapIslandData data = campaign.getIcons().getMapIslandData(i);
			int state = campaign.getState().getIslandState(i);
			GUIObject island;
			switch (state) {
				case CampaignState.ISLAND_AVAILABLE:
					island = new NonFocusIconButton(data.getButton(), "");
					island.addMouseClickListener(new IslandClickListener(i));
					addChild(island);
					break;
				case CampaignState.ISLAND_SEMI_AVAILABLE:
				case CampaignState.ISLAND_UNAVAILABLE:
					island = new GUIIcon(data.getButton()[Skin.DISABLED]);
					addChild(island);
					break;
				case CampaignState.ISLAND_COMPLETED:
					island = new GUIIcon(data.getButton()[Skin.NORMAL]);
					addChild(island);
					if (campaign.getState().getCurrentIsland() != i) {
						GUIIcon flag = new GUIIcon(data.getFlag());
						flag.setPos(data.getPinX(), data.getPinY());
						addChild(flag);
					} else {
						GUIIcon boat = new GUIIcon(data.getBoat());
						boat.setPos(data.getPinX(), data.getPinY());
						addChild(boat);
					}
					break;
				case CampaignState.ISLAND_HIDDEN:
					island = null;
					break;
				default:
					throw new RuntimeException();
			}
			if (island != null)
				island.setPos(data.getX(), data.getY());
		}
	}

	protected final void keyPressed(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_ESCAPE:
				getGUIRoot().pushDelegate(new CampaignMapMenu(network, getGUIRoot(), new StaticCamera(getCamera().getState())));
				break;
			default:
				super.keyPressed(event);
				break;
		}
	}

	public final static void closeCampaign(NetworkSelector network, GUI gui) {
		Renderer.startMenu(network, gui);
	}

	public final boolean forceRender() {
		return true;
	}

	protected final void renderGeometry() {
		campaign.getIcons().getMap().render(0, 0);
//		campaign.extraRender();
	}

	/*
	protected final void keyPressed(KeyboardEvent event) {
		if (event.getKeyCode() == Keyboard.KEY_ESCAPE) {
		} else {
			super.keyPressed(event);
		}
	}
	*/
	private final strictfp class IslandClickListener implements MouseClickListener {
		private final int number;

		public IslandClickListener(int number) {
			this.number = number;
		}
		
		public final void mouseClicked(int button, int x, int y, int clicks) {
			campaign.islandChosen(network, getGUIRoot(), number);
		}
	}
}
