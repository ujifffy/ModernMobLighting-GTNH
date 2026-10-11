package com.gtnh.modernmoblighting;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public final class ModConfig {

    static final String CONFIG_SCHEMA_VERSION = "1";
    static final String ENABLED_KEY = "enableModernHostileLighting";
    static final String THRESHOLD_KEY = "maxHostileSpawnBlockLight";

    private static final boolean DEFAULT_ENABLED = true;
    private static final int DEFAULT_THRESHOLD = 1;
    private static final List<String> KNOWN_GENERAL_PROPERTIES = Arrays.asList(ENABLED_KEY, THRESHOLD_KEY);

    public static boolean enableModernHostileLighting = true;
    public static int maxHostileSpawnBlockLight = 1;

    private ModConfig() {}

    public static void load(File file) {
        Configuration config = new Configuration(file, CONFIG_SCHEMA_VERSION);
        String loadedSchemaVersion = config.getLoadedConfigVersion();
        boolean futureSchema = isFutureSchemaVersion(loadedSchemaVersion);

        readKnownProperties(config);

        // An older jar must never destroy settings written by a newer config schema. Current and older schemas are
        // canonicalized so removed keys/categories disappear without requiring users to clean the file manually.
        if (futureSchema) {
            return;
        }

        removeUnknownEntries(config);
        if (config.hasChanged() || !CONFIG_SCHEMA_VERSION.equals(loadedSchemaVersion)) {
            config.save();
        }
    }

    static void readKnownProperties(Configuration config) {
        Property enabledProperty = config.get(
            Configuration.CATEGORY_GENERAL,
            ENABLED_KEY,
            DEFAULT_ENABLED,
            "Use a fixed block-light threshold for natural EntityMob spawning and the optional client F7 overlay.");
        enableModernHostileLighting = enabledProperty.getBoolean(DEFAULT_ENABLED);
        String normalizedEnabled = Boolean.toString(enableModernHostileLighting);
        if (!normalizedEnabled.equals(enabledProperty.getString())) {
            enabledProperty.set(enableModernHostileLighting);
        }

        Property thresholdProperty = config.get(
            Configuration.CATEGORY_GENERAL,
            THRESHOLD_KEY,
            DEFAULT_THRESHOLD,
            "Highest saved block-light value at which natural hostile mobs may spawn (0..15).",
            LightingRules.MIN_LIGHT,
            LightingRules.MAX_LIGHT);
        maxHostileSpawnBlockLight = LightingRules.clampThreshold(thresholdProperty.getInt(DEFAULT_THRESHOLD));
        String normalizedThreshold = Integer.toString(maxHostileSpawnBlockLight);
        if (!normalizedThreshold.equals(thresholdProperty.getString())) {
            thresholdProperty.set(maxHostileSpawnBlockLight);
        }

        config.getCategory(Configuration.CATEGORY_GENERAL)
            .setPropertyOrder(new ArrayList<String>(KNOWN_GENERAL_PROPERTIES));
    }

    static void removeUnknownEntries(Configuration config) {
        ConfigCategory general = config.getCategory(Configuration.CATEGORY_GENERAL);
        for (String propertyName : new ArrayList<String>(general.keySet())) {
            if (!KNOWN_GENERAL_PROPERTIES.contains(propertyName)) {
                general.remove(propertyName);
            }
        }

        for (String categoryName : new ArrayList<String>(config.getCategoryNames())) {
            if (!Configuration.CATEGORY_GENERAL.equals(categoryName) && config.hasCategory(categoryName)) {
                config.removeCategory(config.getCategory(categoryName));
            }
        }
    }

    static boolean isFutureSchemaVersion(String loadedSchemaVersion) {
        if (loadedSchemaVersion == null) {
            return false;
        }

        try {
            return Integer.parseInt(loadedSchemaVersion) > Integer.parseInt(CONFIG_SCHEMA_VERSION);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
