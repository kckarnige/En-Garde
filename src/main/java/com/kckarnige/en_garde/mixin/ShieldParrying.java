package com.kckarnige.en_garde.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.kckarnige.en_garde.En_garde.BLOCKING_ITEMS_TAG;
import static com.kckarnige.en_garde.config.MidnightConfigStuff.*;

@Mixin(LivingEntity.class)
public abstract class ShieldParrying {

    // Tuning
    @Unique
    private static final int PARRY_WINDOW_TICKS = parryWindow;
    @Unique
    private static final double PARRY_KNOCKBACK_STRENGTH = parryKnockback;
    @Unique
    private static final double  NORMAL_BLOCK_MULTIPLIER = blockDamageMultiplier;
    @Unique
    private static final boolean APPLY_TO_PROJECTILES = projectileRequireParry;

    @Unique
    private static final ThreadLocal<Boolean> parry$REENTER = ThreadLocal.withInitial(() -> false);

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void parry$onDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (parry$REENTER.get()) return; // skip our own re-entry path

        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof PlayerEntity player)) return;
        if (player.getWorld().isClient()) return;

        // Must be actively using a recognized blocking item
        if (!player.isUsingItem()) return;
        ItemStack active = player.getActiveItem();
        if (!parry$isBlockingItem(active)) return;

        // Respect un-blockable damage types
        if (source.isIn(DamageTypeTags.BYPASSES_SHIELD)) return;
        if (!APPLY_TO_PROJECTILES && source.isIn(DamageTypeTags.IS_PROJECTILE)) return;

        Entity attacker = source.getAttacker();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;
        if (!parry$inFront(player, attacker)) return;

        int max = active.getMaxUseTime(player);
        int left = player.getItemUseTimeLeft();
        int elapsed = max - left;

        // --- Perfect Parry ---
        if (elapsed >= 0 && elapsed <= PARRY_WINDOW_TICKS) {
            double dx = player.getX() - livingAttacker.getX();
            double dz = player.getZ() - livingAttacker.getZ();
            livingAttacker.takeKnockback(PARRY_KNOCKBACK_STRENGTH, dx, dz);

            World w = player.getWorld();
            w.playSound(null, player.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK,
                    SoundCategory.PLAYERS, 1.0F, 1.25F);

            cir.setReturnValue(false); // negate all damage
            return;
        }

        // --- Normal Block: Take partial damage ---
        Item blockingItem = active.getItem();
        player.stopUsingItem();

        float reduced = (float) (amount * NORMAL_BLOCK_MULTIPLIER);

        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK,
                SoundCategory.PLAYERS, 0.7F, 0.9F);

        boolean result;
        parry$REENTER.set(true);
        try {
            result = ((LivingEntity)(Object)this).damage(world, source, reduced);
        } finally {
            parry$REENTER.set(false);
        }

        cir.setReturnValue(result);
    }

    @Unique
    private static boolean parry$isBlockingItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.isIn(BLOCKING_ITEMS_TAG);
    }

    @Unique
    private static boolean parry$inFront(PlayerEntity player, Entity attacker) {
        Vec3d look = player.getRotationVec(1.0F).normalize();
        Vec3d toAttacker = attacker.getPos().subtract(player.getPos()).normalize();
        return toAttacker.dotProduct(look) > 0.0D;
    }
}
