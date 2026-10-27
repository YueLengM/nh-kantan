package com.yuelengm.nhkantan.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/**
 * Decorative block representing a stock market display terminal.
 * Placeholder implementation without functionality; UI and behavior will be wired in later phases.
 */
public class BlockMarketDisplay extends Block {

    public BlockMarketDisplay() {
        super(Material.iron);
        this.setBlockName("nhkantan.market_display");
        this.setBlockTextureName("nhkantan:market_display");
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setHardness(2.0F);
        this.setResistance(6.0F);
    }
}
