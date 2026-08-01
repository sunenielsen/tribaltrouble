package com.oddlabs.tt.gui;

import org.jspecify.annotations.NonNull;

public final class Languages {
    private static final String[][] languages = new String[][]{{"en", "English"}, {"da", "Dansk"}, {"de", "Deutsch"}, {"es", "Español"}, {"it", "Italiano"}, {"pt", "Português brasileiro"}};

    public static boolean hasLanguage(@NonNull String language) {
        for (String[] aLanguage : languages) {
            if (aLanguage[0].equals(language)) {
                return true;
            }
        }
        return false;
    }

    public static @NonNull String @NonNull [] @NonNull [] getLanguages() {
        return languages;
    }

    public static @NonNull IconQuad @NonNull [] getFlags() {
        IconQuad[] flags = {Skin.getSkin().getFlagEn(), Skin.getSkin().getFlagDa(), Skin.getSkin().getFlagDe(), Skin.getSkin().getFlagEs(), Skin.getSkin().getFlagIt(), Skin.getSkin().getFlagPt()};
        return flags;
    }
}
