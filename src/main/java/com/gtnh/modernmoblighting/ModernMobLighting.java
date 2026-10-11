package com.gtnh.modernmoblighting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = ModernMobLighting.MODID,
    name = ModernMobLighting.NAME,
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    acceptableRemoteVersions = "*")
public final class ModernMobLighting {

    public static final String MODID = "modernmoblighting";
    public static final String NAME = "ModernMobLighting-GTNH";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModConfig.load(event.getSuggestedConfigurationFile());
        LOG.info(
            "Modern hostile lighting is {} (maximum block light: {})",
            ModConfig.enableModernHostileLighting ? "enabled" : "disabled",
            ModConfig.maxHostileSpawnBlockLight);
    }
}
