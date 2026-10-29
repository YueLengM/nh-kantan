package com.yuelengm.nhkantan.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

import com.gtnewhorizon.gtnhlib.blockstate.registry.BlockPropertyRegistry;
import com.yuelengm.nhkantan.NHKantan;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Holds all blocks defined by NH kantan and centralizes their registration.
 * Fields are initialized eagerly so other code can reference them by class load;
 * the actual Forge registration happens during preInit via {@link #register()}.
 */
public class ModBlocks {

    public static final BlockMarketDisplay marketDisplay = new BlockMarketDisplay();

    private ModBlocks() {}

    public static void register() {
        registerBlock(marketDisplay, "market_display");
        BlockPropertyRegistry.registerProperty(marketDisplay, MarketDisplayOrientation.PROPERTY);

        GameRegistry.registerTileEntity(TileMarketDisplay.class, NHKantan.MODID + ":market_display");
    }

    private static void registerBlock(Block block, String name) {
        GameRegistry.registerBlock(block, ItemBlock.class, name);
    }
}
