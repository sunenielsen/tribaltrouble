package com.oddlabs.tt.gui;

import com.oddlabs.tt.global.Settings;
import com.oddlabs.util.Quad;

public final strictfp class Languages {
	private final String[][] languages;
	private final boolean beta_mode;
	
	public Languages(boolean beta_mode) {
		this.beta_mode = beta_mode;
		if (beta_mode) {
			languages = new String[][]{{"da", "Dansk"}, {"en", "English"}, {"de", "Deutsch"}, {"es","Espa\u00F1ol"}, {"it", "Italiano"}};
		} else {
			languages = new String[][]{{"da", "Dansk"}, {"en", "English"}, {"de", "Deutsch"}, {"es","Espa\u00F1ol"}, {"it", "Italiano"}};
		}
	}

	public final boolean hasLanguage(String language) {
		for (int i = 0; i < languages.length; i++)
			if (languages[i][0].equals(language))
				return true;
		return false;
	}
	
	public final String[][] getLanguages() {
		return languages;
	}

	public final Quad[] getFlags() {
		Quad[] flags;
		if (beta_mode) {
			flags = new Quad[]{Skin.getSkin().getFlagDa(), Skin.getSkin().getFlagEn(), Skin.getSkin().getFlagDe(), Skin.getSkin().getFlagEs(), Skin.getSkin().getFlagIt()};
		} else {
			flags = new Quad[]{Skin.getSkin().getFlagDa(), Skin.getSkin().getFlagEn(), Skin.getSkin().getFlagDe(), Skin.getSkin().getFlagEs(), Skin.getSkin().getFlagIt()};
		}
		return flags;
	}
}
