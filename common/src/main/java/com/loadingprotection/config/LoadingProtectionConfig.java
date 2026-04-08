package com.loadingprotection.config;

import com.loadingprotection.LoadingProtectionMod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LoadingProtectionConfig {
    private static final String FILE_NAME = "loadingprotection-common.toml";

    private static Path configPath;
    private static LoadingProtectionConfig instance = defaults();

    public int protectionDuration = 60;
    public boolean showMessages = true;
    public MessageMode messageMode = MessageMode.BOTH;
    public boolean triggerOnJoin = true;
    public boolean triggerOnRespawn = false;
    public boolean triggerOnDimensionChange = false;
    public boolean endOnMovement = true;
    public boolean protectMount = false;
    public boolean preventTargeting = true;
    public boolean preventOutgoingDamage = true;

    public static void init(Path configDir) {
        configPath = configDir.resolve(FILE_NAME);
        load();
    }

    public static LoadingProtectionConfig get() {
        return instance;
    }

    public static Path getConfigPath() {
        return configPath;
    }

    public static synchronized void load() {
        if (configPath == null) {
            throw new IllegalStateException("Config path has not been initialized");
        }

        LoadingProtectionConfig loaded = defaults();

        try {
            Files.createDirectories(configPath.getParent());

            if (Files.exists(configPath)) {
                Map<String, String> values = parseToml(Files.readAllLines(configPath));
                loaded.protectionDuration = getInt(values, "protectionDuration", loaded.protectionDuration, 1, 300);
                loaded.showMessages = getBoolean(values, "showMessages", loaded.showMessages);
                loaded.messageMode = getEnum(values, "messageMode", MessageMode.class, loaded.messageMode);
                loaded.triggerOnJoin = getBoolean(values, "triggerOnJoin", loaded.triggerOnJoin);
                loaded.triggerOnRespawn = getBoolean(values, "triggerOnRespawn", loaded.triggerOnRespawn);
                loaded.triggerOnDimensionChange =
                        getBoolean(values, "triggerOnDimensionChange", loaded.triggerOnDimensionChange);
                loaded.endOnMovement = getBoolean(values, "endOnMovement", loaded.endOnMovement);
                loaded.protectMount = getBoolean(values, "protectMount", loaded.protectMount);
                loaded.preventTargeting = getBoolean(values, "preventTargeting", loaded.preventTargeting);
                loaded.preventOutgoingDamage =
                        getBoolean(values, "preventOutgoingDamage", loaded.preventOutgoingDamage);
            }

            instance = loaded;
            save();
        } catch (IOException exception) {
            LoadingProtectionMod.LOGGER.error("Failed to load config from {}", configPath, exception);
            instance = defaults();
        }
    }

    public static synchronized void save() {
        save(instance);
    }

    public static synchronized void save(LoadingProtectionConfig updatedConfig) {
        instance = updatedConfig.copy();

        if (configPath == null) {
            throw new IllegalStateException("Config path has not been initialized");
        }

        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, instance.toToml());
        } catch (IOException exception) {
            LoadingProtectionMod.LOGGER.error("Failed to save config to {}", configPath, exception);
        }
    }

    public LoadingProtectionConfig copy() {
        LoadingProtectionConfig copy = new LoadingProtectionConfig();
        copy.protectionDuration = protectionDuration;
        copy.showMessages = showMessages;
        copy.messageMode = messageMode;
        copy.triggerOnJoin = triggerOnJoin;
        copy.triggerOnRespawn = triggerOnRespawn;
        copy.triggerOnDimensionChange = triggerOnDimensionChange;
        copy.endOnMovement = endOnMovement;
        copy.protectMount = protectMount;
        copy.preventTargeting = preventTargeting;
        copy.preventOutgoingDamage = preventOutgoingDamage;
        return copy;
    }

    public boolean isTriggerEnabled(ProtectionTrigger trigger) {
        return switch (trigger) {
            case JOIN -> triggerOnJoin;
            case RESPAWN -> triggerOnRespawn;
            case DIMENSION_CHANGE -> triggerOnDimensionChange;
        };
    }

    private String toToml() {
        return String.join(
                "\n",
                "# Loading Protection common config",
                "# Edit manually on servers, or through the YACL screen in singleplayer/local hosting.",
                "",
                "# Duration of protection in seconds after it starts.",
                "protectionDuration = " + protectionDuration,
                "",
                "# Show start/countdown/end notices to the affected player.",
                "showMessages = " + showMessages,
                "",
                "# Valid values: \"chat\", \"action_bar\", \"both\"",
                "messageMode = \"" + serializeMessageMode(messageMode) + "\"",
                "",
                "# Trigger sources",
                "triggerOnJoin = " + triggerOnJoin,
                "triggerOnRespawn = " + triggerOnRespawn,
                "triggerOnDimensionChange = " + triggerOnDimensionChange,
                "",
                "# End protection immediately when the player actually changes position.",
                "endOnMovement = " + endOnMovement,
                "",
                "# Also protect the vehicle currently ridden by the protected player.",
                "protectMount = " + protectMount,
                "",
                "# Clear hostile mob targets while a player is protected.",
                "preventTargeting = " + preventTargeting,
                "",
                "# Protected players cannot deal damage while protection lasts.",
                "preventOutgoingDamage = " + preventOutgoingDamage,
                "");
    }

    private static LoadingProtectionConfig defaults() {
        return new LoadingProtectionConfig();
    }

    private static Map<String, String> parseToml(List<String> lines) {
        Map<String, String> values = new HashMap<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            int commentIndex = trimmed.indexOf('#');
            if (commentIndex >= 0) {
                trimmed = trimmed.substring(0, commentIndex).trim();
            }

            int equalsIndex = trimmed.indexOf('=');
            if (equalsIndex <= 0) {
                continue;
            }

            String key = trimmed.substring(0, equalsIndex).trim();
            String rawValue = trimmed.substring(equalsIndex + 1).trim();
            values.put(key, stripQuotes(rawValue));
        }

        return values;
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }

    private static int getInt(Map<String, String> values, String key, int fallback, int min, int max) {
        String value = values.get(key);
        if (value == null) {
            return fallback;
        }

        try {
            return clamp(Integer.parseInt(value), min, max);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static boolean getBoolean(Map<String, String> values, String key, boolean fallback) {
        String value = values.get(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static <T extends Enum<T>> T getEnum(Map<String, String> values, String key, Class<T> type, T fallback) {
        String value = values.get(key);
        if (value == null) {
            return fallback;
        }

        try {
            return Enum.valueOf(type, normalizeEnumValue(value));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static String normalizeEnumValue(String value) {
        return value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private static String serializeMessageMode(MessageMode mode) {
        return switch (mode) {
            case CHAT -> "chat";
            case ACTION_BAR -> "action_bar";
            case BOTH -> "both";
        };
    }
}
