package com.oddlabs.tt.gui;

import org.jspecify.annotations.NonNull;

import java.util.stream.Stream;

public final class Languages {
    private static final boolean POLISH_ENABLED = false;
    private static final String[][] languages = Stream.of(
            new String[][]{{"en", "English"}, {"da", "Dansk"}, {"de", "Deutsch"}, {"es", "Español"}, {"it", "Italiano"}, {"pt", "Português brasileiro"}, {"pl", "Polski"}}).filter(
                    lang -> POLISH_ENABLED || !lang[0].equals("pl")).toArray(String[][]::new);

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
        IconQuad[] flags = {Skin.getSkin().getFlagEn(), Skin.getSkin().getFlagDa(), Skin.getSkin().getFlagDe(), Skin.getSkin().getFlagEs(), Skin.getSkin().getFlagIt(), Skin.getSkin().getFlagPt(), Skin.getSkin().getFlagPl()};
        return flags;
    }
}
