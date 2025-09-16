package com.kckarnige.en_garde;

import com.kckarnige.en_garde.config.MidnightConfigStuff;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class En_garde implements ModInitializer {

    public static final String MOD_ID = "en_garde";
    public static final TagKey<Item> BLOCKING_ITEMS_TAG =
            TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "shields"));

    @Override
    public void onInitialize() {
        MidnightConfig.init(MOD_ID, MidnightConfigStuff.class);
    }
}
