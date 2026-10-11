package com.gtnh.modernmoblighting;

import net.minecraft.entity.EntityLiving;

public final class NaturalSpawnContext {

    private static final ThreadLocal<EntityLiving> CHECKED_ENTITY = new ThreadLocal<>();

    private NaturalSpawnContext() {}

    public static EntityLiving enter(EntityLiving entity) {
        EntityLiving previous = CHECKED_ENTITY.get();
        CHECKED_ENTITY.set(entity);
        return previous;
    }

    public static void exit(EntityLiving previous) {
        if (previous == null) {
            CHECKED_ENTITY.remove();
        } else {
            CHECKED_ENTITY.set(previous);
        }
    }

    public static boolean isChecking(EntityLiving entity) {
        return CHECKED_ENTITY.get() == entity;
    }
}
