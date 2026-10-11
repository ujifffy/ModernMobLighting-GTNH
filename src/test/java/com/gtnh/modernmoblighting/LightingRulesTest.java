package com.gtnh.modernmoblighting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import net.minecraftforge.common.config.Configuration;

import org.junit.After;
import org.junit.Test;

public class LightingRulesTest {

    @After
    public void restoreDefaults() {
        ModConfig.enableModernHostileLighting = true;
        ModConfig.maxHostileSpawnBlockLight = 1;
    }

    @Test
    public void thresholdOneAllowsOnlyBlockLightZeroAndOne() {
        assertTrue(LightingRules.isBlockLightAllowed(0, 1));
        assertTrue(LightingRules.isBlockLightAllowed(1, 1));
        assertFalse(LightingRules.isBlockLightAllowed(2, 1));
        assertFalse(LightingRules.isBlockLightAllowed(7, 1));
    }

    @Test
    public void thresholdZeroAndFifteenCoverBothValidBoundaries() {
        assertTrue(LightingRules.isBlockLightAllowed(0, 0));
        assertFalse(LightingRules.isBlockLightAllowed(1, 0));
        assertTrue(LightingRules.isBlockLightAllowed(0, 15));
        assertTrue(LightingRules.isBlockLightAllowed(15, 15));
    }

    @Test
    public void thresholdIsClampedToValidLightRange() {
        assertEquals(0, LightingRules.clampThreshold(-50));
        assertEquals(15, LightingRules.clampThreshold(50));
    }

    @Test
    public void openDaylightStillFailsTheVanillaEffectiveSkyGate() {
        assertFalse(LightingRules.passesEffectiveSkyLightCheck(15, 0, 7));
    }

    @Test
    public void openNightCanPassTheVanillaEffectiveSkyGate() {
        assertTrue(LightingRules.passesEffectiveSkyLightCheck(15, 11, 7));
    }

    @Test
    public void roofedDayAndNoSkyDimensionsCanPass() {
        assertTrue(LightingRules.passesEffectiveSkyLightCheck(0, 0, 0));
    }

    @Test
    public void thunderUsesTheVanillaDarkeningValue() {
        assertTrue(LightingRules.passesEffectiveSkyLightCheck(15, 10, 7));
    }

    @Test
    public void effectiveSkyRandomBoundaryIsInclusive() {
        assertTrue(LightingRules.passesEffectiveSkyLightCheck(12, 5, 7));
        assertFalse(LightingRules.passesEffectiveSkyLightCheck(13, 5, 7));
    }

    @Test
    public void overlayUsesThresholdPlusOneAndRestoresNeiDefaultWhenDisabled() {
        ModConfig.maxHostileSpawnBlockLight = 0;
        assertEquals(1, LightingRules.overlayBlockLightCutoffExclusive());

        ModConfig.maxHostileSpawnBlockLight = 1;
        assertEquals(2, LightingRules.overlayBlockLightCutoffExclusive());

        ModConfig.maxHostileSpawnBlockLight = 15;
        assertEquals(16, LightingRules.overlayBlockLightCutoffExclusive());

        ModConfig.enableModernHostileLighting = false;
        assertEquals(8, LightingRules.overlayBlockLightCutoffExclusive());
    }

    @Test
    public void configRepairsMalformedAndOutOfRangeKnownValues() {
        Configuration config = new Configuration();
        config.get(Configuration.CATEGORY_GENERAL, ModConfig.ENABLED_KEY, true)
            .set("not-a-boolean");
        config.get(Configuration.CATEGORY_GENERAL, ModConfig.THRESHOLD_KEY, 1)
            .set(99);

        ModConfig.readKnownProperties(config);

        assertTrue(ModConfig.enableModernHostileLighting);
        assertEquals(15, ModConfig.maxHostileSpawnBlockLight);
        assertEquals(
            "true",
            config.getCategory(Configuration.CATEGORY_GENERAL)
                .get(ModConfig.ENABLED_KEY)
                .getString());
        assertEquals(
            "15",
            config.getCategory(Configuration.CATEGORY_GENERAL)
                .get(ModConfig.THRESHOLD_KEY)
                .getString());
    }

    @Test
    public void configRemovesObsoletePropertiesAndCategories() {
        Configuration config = new Configuration();
        config.get(Configuration.CATEGORY_GENERAL, "removedSetting", true);
        config.get("obsoleteCategory", "oldValue", 42);
        config.get("obsoleteCategory.child", "nestedOldValue", 43);

        ModConfig.readKnownProperties(config);
        ModConfig.removeUnknownEntries(config);

        assertEquals(
            new HashSet<String>(Arrays.asList(Configuration.CATEGORY_GENERAL)),
            new HashSet<String>(config.getCategoryNames()));
        assertEquals(
            new HashSet<String>(Arrays.asList(ModConfig.ENABLED_KEY, ModConfig.THRESHOLD_KEY)),
            new HashSet<String>(
                config.getCategory(Configuration.CATEGORY_GENERAL)
                    .keySet()));
    }

    @Test
    public void futureSchemaCanBeDetectedAndPreservedOnDowngrade() {
        assertFalse(ModConfig.isFutureSchemaVersion(null));
        assertFalse(ModConfig.isFutureSchemaVersion("0"));
        assertFalse(ModConfig.isFutureSchemaVersion(ModConfig.CONFIG_SCHEMA_VERSION));
        assertFalse(ModConfig.isFutureSchemaVersion("invalid"));
        assertTrue(ModConfig.isFutureSchemaVersion("2"));

        Configuration futureConfig = new Configuration();
        futureConfig.get("futureCategory", "futureSetting", "keep-me");
        ModConfig.readKnownProperties(futureConfig);

        assertTrue(futureConfig.hasCategory("futurecategory"));
        assertEquals(
            "keep-me",
            futureConfig.getCategory("futurecategory")
                .get("futureSetting")
                .getString());
    }
}
