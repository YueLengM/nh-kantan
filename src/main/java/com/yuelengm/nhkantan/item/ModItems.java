package com.yuelengm.nhkantan.item;

import net.minecraft.item.Item;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Holds all items defined by NH kantan and centralizes their registration.
 * The actual Forge registration happens during preInit via {@link #register()}.
 */
public class ModItems {

    public static final ItemMarketTerminal marketTerminal = new ItemMarketTerminal();

    private ModItems() {}

    public static void register() {
        registerItem(marketTerminal, "market_terminal");
    }

    private static void registerItem(Item item, String name) {
        GameRegistry.registerItem(item, name);
    }
}
