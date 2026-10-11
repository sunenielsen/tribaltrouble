package com.oddlabs.matchserver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import discord4j.core.object.emoji.Emoji;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public final class ServerConfiguration {
    private static final Logger logger = Logger.getLogger(ServerConfiguration.class.getName());

    public static final String DB_CONNECTION = "DB_CONNECTION";
    public static final String DB_USER = "DB_USER";
    public static final String SQL_PASS = "SQL_PASS";
    public static final String DISCORD_BOT_TOKEN = "DISCORD_BOT_TOKEN";
    public static final String DISCORD_SERVER_ID = "DISCORD_SERVER_ID";
    public static final String WEBSITE_DOMAIN = "WEBSITE_DOMAIN";
    public static final String VIKING_CHIEF_EMOJI = "VIKING_CHIEF_EMOJI";
    public static final String NATIVE_CHIEF_EMOJI = "NATIVE_CHIEF_EMOJI";
    public static final String STEAM_WEB_API_KEY = "STEAM_WEB_API_KEY";
    public static final String STEAM_APP_ID = "STEAM_APP_ID";
    public static final String STEAM_ONLY_AUTH = "STEAM_ONLY_AUTH";
    public static final String SPECTATOR_DATA_DIR = "SPECTATOR_DATA_DIR";
    /*
     * Key for emoji role mappings
     * Example JSON format for EMOJI_ROLE_MAPPINGS:
     * [{"role id":"<numeric discord role id>","emoji id":"<custom emoji id or unicode (U+1F602)>"}]
     */
    public static final String EMOJI_ROLE_MAPPINGS = "EMOJI_ROLE_MAPPINGS";
    public static final String REACTION_ROLE_MESSAGE_ID = "REACTION_ROLE_MESSAGE_ID";

    private static ServerConfiguration instance;

    public static ServerConfiguration getInstance() {
        if (instance == null) {
            instance = new ServerConfiguration("server.properties");
        }
        return instance;
    }

    private final Properties properties = new Properties();

    public ServerConfiguration(String configFilePath) {
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            logger.severe("Configuration file not found: " + configFile.getAbsolutePath());
            logger.severe("Edit server.properties with your settings and restart.");
            System.exit(1);
        }
        try (FileInputStream in = new FileInputStream(configFile)) {
            properties.load(in);
            logger.info("Loaded configuration from " + configFile.getAbsolutePath());
        } catch (IOException e) {
            logger.severe("Failed to read configuration from " + configFile.getAbsolutePath());
            System.exit(1);
        }
    }

    public String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            return null;
        }
        return substituteEnvVars(value);
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Support {{ENV_VAR}} syntax for environment variable substitution.
     */
    private String substituteEnvVars(String value) {
        if (value == null || !value.contains("{{")) {
            return value;
        }
        String envVarName = value.replace("{{", "").replace("}}", "").trim();
        String envValue = System.getenv(envVarName);
        return (envValue != null) ? envValue : value;
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    public Map<String, String> getEmojiRoleMappings() {
        Map<String, String> mappings = new HashMap<>();
        String mappingString = get(EMOJI_ROLE_MAPPINGS);

        if (mappingString != null && !mappingString.trim().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, String>> jsonData = mapper.readValue(
                        mappingString, new TypeReference<List<Map<String, String>>>() {
                        });

                for (Map<String, String> item : jsonData) {
                    String emojiId = item.get("emoji id");
                    emojiId = normalizeEmojiKey(emojiId);
                    if (emojiId == null) continue;
                    String roleId = item.get("role id");

                    if (roleId != null) {
                        mappings.put(emojiId, roleId);
                    }
                }

            } catch (Exception e) {
                logger.warning("Error parsing emoji role mappings JSON: " + e.getMessage());
            }
        }

        return mappings;
    }

    private String normalizeEmojiKey(String emojiId) {
        long val = -1;
        try {
            val = Long.parseLong(emojiId);
        } catch (NumberFormatException e) {
            if (!emojiId.startsWith("U+")) {
                logger.warning(
                        "The argument(s) to this method should use the \"U+\" notation for" + " codepoints. Skipping mapping: " + emojiId);
                return null;
            }
        }

        if (val == -1) {
            logger.info("Interpreting emoji id as codepoint: " + emojiId);
            emojiId = Emoji.codepoints(emojiId).getRaw();
        }
        return emojiId;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    public boolean isSteamOnlyAuth() {
        return getBoolean(STEAM_ONLY_AUTH, false);
    }

    /**
     * Parses STEAM_APP_ID as a comma-separated whitelist of Steam app IDs that the
     * server accepts auth tickets for. Lets one server pool serve the full game
     * plus a demo and/or playtest build with their own app IDs.
     */
    public Set<Integer> getSteamAppIds() {
        String raw = get(STEAM_APP_ID);
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptySet();
        }
        Set<Integer> ids = new HashSet<>();
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            try {
                ids.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException e) {
                logger.warning("Invalid Steam app ID in STEAM_APP_ID: '" + trimmed + "' — skipping");
            }
        }
        return ids;
    }

    public boolean isWhitelistedSteamAppId(int appId) {
        return getSteamAppIds().contains(appId);
    }

    /**
     * First app ID from STEAM_APP_ID — used as the target for multiplayer stats
     * and achievement pushes. Returns -1 if not configured.
     */
    public int getMainSteamAppId() {
        String raw = get(STEAM_APP_ID);
        if (raw == null || raw.trim().isEmpty()) {
            return -1;
        }
        String first = raw.split(",")[0].trim();
        try {
            return Integer.parseInt(first);
        } catch (NumberFormatException e) {
            logger.warning("Invalid first Steam app ID in STEAM_APP_ID: '" + first + "'");
            return -1;
        }
    }

    /**
     * True when the Steam Web API is configured for stat and achievement pushes:
     * a Web API key is set and STEAM_APP_ID yields a valid target app ID. This is
     * independent of {@link #isSteamOnlyAuth()} — a server can push Steam stats
     * while still allowing non-Steam logins.
     */
    public boolean isSteamStatsConfigured() {
        String apiKey = get(STEAM_WEB_API_KEY);
        return apiKey != null && !apiKey.isEmpty() && getMainSteamAppId() != -1;
    }
}
