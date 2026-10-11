package com.gtnh.modernmoblighting;

import java.util.Random;

import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public final class LightingRules {

    public static final int MIN_LIGHT = 0;
    public static final int MAX_LIGHT = 15;

    private LightingRules() {}

    public static boolean isValidNaturalHostileLight(World world, Random random, int x, int y, int z) {
        int savedSkyLight = world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z);

        // This is the first vanilla EntityMob sky-light gate, kept byte-for-byte in meaning.
        if (savedSkyLight > random.nextInt(32)) {
            return false;
        }

        // Vanilla next checks the maximum of time-adjusted sky light and block light. Isolate the sky component so
        // daylight, night and thunderstorms retain vanilla behaviour while block light uses the configured cutoff.
        int skyDarkening = world.isThundering() ? 10 : world.skylightSubtracted;
        if (!passesEffectiveSkyLightCheck(savedSkyLight, skyDarkening, random.nextInt(8))) {
            return false;
        }

        int blockLight = world.getSavedLightValue(EnumSkyBlock.Block, x, y, z);
        return isBlockLightAllowed(blockLight, ModConfig.maxHostileSpawnBlockLight);
    }

    static boolean passesEffectiveSkyLightCheck(int savedSkyLight, int skyDarkening, int effectiveSkyRandom) {
        int effectiveSkyLight = Math.max(MIN_LIGHT, savedSkyLight - skyDarkening);
        return effectiveSkyLight <= effectiveSkyRandom;
    }

    public static boolean isBlockLightAllowed(int blockLight, int threshold) {
        return blockLight <= clampThreshold(threshold);
    }

    public static int overlayBlockLightCutoffExclusive() {
        return ModConfig.enableModernHostileLighting ? clampThreshold(ModConfig.maxHostileSpawnBlockLight) + 1 : 8;
    }

    public static int clampThreshold(int value) {
        return Math.max(MIN_LIGHT, Math.min(MAX_LIGHT, value));
    }
}
