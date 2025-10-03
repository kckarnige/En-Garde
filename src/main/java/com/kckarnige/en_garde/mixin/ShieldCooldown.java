package com.kckarnige.en_garde.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.kckarnige.en_garde.En_garde.BLOCKING_ITEMS_TAG;

@Mixin(LivingEntity.class)
public abstract class ShieldCooldown {

    @Inject(method = "stopUsingItem", at = @At("HEAD"))
    private void parry$cooldownOnStop(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof PlayerEntity player)) return;
        if (player.getEntityWorld().isClient()) return;

        ItemStack active = player.getActiveItem();
        if (active.isEmpty()) return;

        if (active.isIn(BLOCKING_ITEMS_TAG)) {
            player.getItemCooldownManager().set(active, 10);
        }
    }
}
