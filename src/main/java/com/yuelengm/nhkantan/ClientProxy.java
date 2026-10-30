package com.yuelengm.nhkantan;

import com.yuelengm.nhkantan.block.TileMarketDisplay;
import com.yuelengm.nhkantan.client.ClientMarketEventHandler;
import com.yuelengm.nhkantan.client.render.MarketDisplayRenderer;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.bindTileEntitySpecialRenderer(TileMarketDisplay.class, new MarketDisplayRenderer());
        FMLCommonHandler.instance()
            .bus()
            .register(new ClientMarketEventHandler());
    }
}
