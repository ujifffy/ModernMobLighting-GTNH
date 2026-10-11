package com.gtnh.modernmoblighting.mixins;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.MathHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnh.modernmoblighting.LightingRules;
import com.gtnh.modernmoblighting.ModConfig;
import com.gtnh.modernmoblighting.NaturalSpawnContext;

@Mixin(EntityMob.class)
public abstract class MixinEntityMobLightLevel {

    @Inject(method = "isValidLightLevel", at = @At("HEAD"), cancellable = true, require = 1)
    private void modernmoblighting$useFixedBlockLightThreshold(CallbackInfoReturnable<Boolean> cir) {
        EntityMob self = (EntityMob) (Object) this;
        if (!ModConfig.enableModernHostileLighting || !NaturalSpawnContext.isChecking(self)) {
            return;
        }

        int x = MathHelper.floor_double(self.posX);
        int y = MathHelper.floor_double(self.boundingBox.minY);
        int z = MathHelper.floor_double(self.posZ);
        cir.setReturnValue(LightingRules.isValidNaturalHostileLight(self.worldObj, self.getRNG(), x, y, z));
    }
}
