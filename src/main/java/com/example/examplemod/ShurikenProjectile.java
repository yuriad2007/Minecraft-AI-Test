package com.example.examplemod;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;

public class ShurikenProjectile extends ThrowableItemProjectile {
    public ShurikenProjectile(EntityType<? extends ShurikenProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public ShurikenProjectile(Level level, net.minecraft.world.entity.LivingEntity owner) {
        super(ExampleMod.SHURIKEN_PROJECTILE.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ExampleMod.SHURIKEN.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 4.0F);
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {
    }
}
