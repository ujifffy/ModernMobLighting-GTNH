package com.gtnh.modernmoblighting.mixins.nei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.gtnh.modernmoblighting.LightingRules;

@Pseudo
@Mixin(targets = "codechicken.nei.WorldOverlayRenderer", remap = false)
public abstract class MixinWorldOverlayRenderer {

    @ModifyConstant(
        method = "getSpawnMode",
        constant = @Constant(intValue = 8, ordinal = 0),
        require = 1,
        remap = false)
    private static int modernmoblighting$useConfiguredBlockLightCutoff(int vanillaCutoff) {
        return LightingRules.overlayBlockLightCutoffExclusive();
    }
}
