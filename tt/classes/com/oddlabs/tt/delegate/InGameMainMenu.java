package com.oddlabs.tt.delegate;

import java.net.InetAddress;
import java.util.ResourceBundle;

import org.lwjgl.input.Keyboard;

import com.oddlabs.matchmaking.Game;
import com.oddlabs.tt.camera.Camera;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.Form;
import com.oddlabs.tt.gui.FreeQuitLabel;
import com.oddlabs.tt.gui.GUIImage;
import com.oddlabs.tt.gui.GUIObject;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.delegate.GameStatsDelegate;
import com.oddlabs.tt.gui.Group;
import com.oddlabs.tt.delegate.SelectionDelegate;
import com.oddlabs.tt.gui.ImageBuyButton;
import com.oddlabs.tt.gui.KeyboardEvent;
import com.oddlabs.tt.gui.Label;
import com.oddlabs.tt.gui.LabelBox;
import com.oddlabs.tt.gui.LocalInput;
import com.oddlabs.tt.gui.MenuButton;
import com.oddlabs.tt.gui.Renderable;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.tt.guievent.CloseListener;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.model.RacesResources;
import com.oddlabs.tt.net.Client;
import com.oddlabs.tt.net.Network;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.landscape.WorldParameters;
import com.oddlabs.tt.net.Server;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.player.PlayerInfo;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.resource.IslandGenerator;
import com.oddlabs.tt.resource.WorldGenerator;
import com.oddlabs.tt.form.InGameOptionsMenu;
import com.oddlabs.tt.form.TerrainMenu;
import com.oddlabs.tt.form.QuestionForm;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.landscape.WorldParameters;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.trigger.GameOverTrigger;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class InGameMainMenu extends Menu {
	private final WorldParameters world_params;

	private final WorldViewer viewer;
	
	private Group game_infos;

	public InGameMainMenu(WorldViewer viewer, Camera camera, WorldParameters world_params) {
		super(viewer.getNetwork(), viewer.getGUIRoot(), camera);
		this.viewer = viewer;
		this.world_params = world_params;
		reload();
	}

	protected final void doAdd() {
		super.doAdd();
		viewer.setPaused(true);
	}

	protected final void doRemove() {
		super.doRemove();
		viewer.setPaused(false);
	}

	public final void addAbortButton(String abort_text) {
		MenuButton abort = new MenuButton(abort_text, COLOR_NORMAL, COLOR_ACTIVE);
		addChild(abort);
		abort.addMouseClickListener(new AbortListener());
	}

	protected final void addButtons() {
		addResumeButton();

		addOptionsButton(new FormFactory() {
			public final Form create() {
				return new InGameOptionsMenu(getGUIRoot(), viewer);
			}
		});

		game_infos = new Group(false);
		viewer.addGUI(this, game_infos);
		addChild(game_infos);

		addExitButton();
	}

	public final void displayChangedNotify(int width, int height) {
		super.displayChangedNotify(width, height);
		if (game_infos != null)
			game_infos.setPos((width - game_infos.getWidth())/2, (height - game_infos.getHeight())/2);
	}

	protected final void keyPressed(KeyboardEvent event) {
		switch(event.getKeyCode()) {
			case Keyboard.KEY_ESCAPE:
				pop();
				break;
			default:
				super.keyPressed(event);
				break;
		}
	}

	protected final void renderGeometry() {
		super.renderGeometry();
		renderBackgroundAlpha();
	}

	private final strictfp class AbortListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			setMenuCentered(new QuestionForm(Utils.getBundleString(bundle, "end_game_confirm"), new ActionAbortListener()));
		}
	}

	private final strictfp class ActionAbortListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			viewer.abort();
		}
	}
}
