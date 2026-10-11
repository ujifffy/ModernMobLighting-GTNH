package com.gtnh.modernmoblighting.mixins;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.SpawnerAnimals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gtnh.modernmoblighting.NaturalSpawnContext;

@Mixin(value = SpawnerAnimals.class, priority = 1000)
public abstract class MixinSpawnerAnimalsNaturalContext {

    @Redirect(
        method = "findChunksForSpawning",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;getCanSpawnHere()Z"),
        require = 1)
    private boolean modernmoblighting$markNaturalSpawnCheck(EntityLiving entity) {
        EntityLiving previous = NaturalSpawnContext.enter(entity);
        try {
            return entity.getCanSpawnHere();
        } finally {
            NaturalSpawnContext.exit(previous);
        }
    }
}
