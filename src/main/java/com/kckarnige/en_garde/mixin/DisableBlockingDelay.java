package com.kckarnige.en_garde.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.kckarnige.en_garde.En_garde.BLOCKING_ITEMS_TAG;

@Mixin(LivingEntity.class)
public abstract class DisableBlockingDelay {

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    private void parry$instantBlock(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity self = (PlayerEntity)(Object)this;
        if (!self.isUsingItem()) return;

        ItemStack active = self.getActiveItem();
        if (active.isEmpty()) return;

        Item item = active.getItem();
        boolean recognized = (active.isIn(BLOCKING_ITEMS_TAG));
        if (!recognized) return;

        if (self.getItemCooldownManager().isCoolingDown(item.getDefaultStack())) return;

        cir.setReturnValue(true);
    }
}
