package com.oddlabs.tt.delegate;

import com.oddlabs.net.NetworkSelector;
import com.oddlabs.tt.camera.Camera;
import com.oddlabs.tt.form.CampaignForm;
import com.oddlabs.tt.form.LoginForm;
import com.oddlabs.tt.form.MatchmakingConnectingForm;
import com.oddlabs.tt.form.SelectGameMenu;
import com.oddlabs.tt.form.TerrainMenuForm;
import com.oddlabs.tt.form.TutorialForm;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.gui.GUIRoot;
import com.oddlabs.tt.gui.MenuButton;
import com.oddlabs.tt.net.Network;
import com.oddlabs.tt.steam.SteamManager;
import org.jspecify.annotations.NonNull;

/**
 * The game main menu
 */
public final class MainMenu extends Menu {
    public MainMenu(@NonNull NetworkSelector network, @NonNull GUIRoot gui_root, @NonNull Camera camera) {
        super(network, gui_root, camera);
        reload();
        SteamManager.clearRichPresence();
        SteamManager.setInActiveWorld(false);
    }

    private void addGameTypeButtons() {
        MenuButton tutorial = new MenuButton(Menu.i18n("tutorial"), COLOR_NORMAL, COLOR_ACTIVE);
        tutorial.addMouseClickListener((_, _, _, _) -> setMenu(new TutorialForm(getNetwork(), getGUIRoot())));
        addChild(tutorial);

        MenuButton campaign_menu = new MenuButton(Menu.i18n("campaign"), COLOR_NORMAL, COLOR_ACTIVE);
        campaign_menu.addMouseClickListener((_, _, _, _) -> setMenu(new CampaignForm(getNetwork(), getGUIRoot(),
                MainMenu.this)));
        addChild(campaign_menu);

        MenuButton single_player = new MenuButton(Menu.i18n("skirmish"), COLOR_NORMAL, COLOR_ACTIVE);
        single_player.addMouseClickListener((_, _, _, _) -> setMenu(new TerrainMenuForm(getNetwork(), getGUIRoot(),
                MainMenu.this)));
        addChild(single_player);

        if (!Settings.getSettings().hide_multiplayer) {
            MenuButton multi_player = new MenuButton(Menu.i18n("multiplayer"), COLOR_NORMAL, COLOR_ACTIVE);
            multi_player.addMouseClickListener((_, _, _, _) -> {
                if (Network.getMatchmakingClient().isConnected()) {
                    new SelectGameMenu(getNetwork(), getGUIRoot(), MainMenu.this);
                } else {
                    Network.getMatchmakingClient().close();
                    if (Settings.getSettings().isOfficialServer() && SteamManager.getInstance() != null) {
                        new MatchmakingConnectingForm(getNetwork(), getGUIRoot(), null, MainMenu.this, true);
                    } else {
                        new LoginForm(getNetwork(), getGUIRoot(), MainMenu.this);
                    }
                }
            });
            addChild(multi_player);
        }
    }

    @Override
    protected void addButtons() {
        addGameTypeButtons();

        addDefaultOptionsButton();

        addExitButton();

        if (Network.getMatchmakingClient().isConnected()) {
            new SelectGameMenu(getNetwork(), getGUIRoot(), this);
        }
    }
}
