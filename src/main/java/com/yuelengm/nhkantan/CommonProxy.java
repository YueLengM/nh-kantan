package com.yuelengm.nhkantan;

import com.yuelengm.nhkantan.block.ModBlocks;
import com.yuelengm.nhkantan.config.ModConfigs;
import com.yuelengm.nhkantan.item.ModItems;
import com.yuelengm.nhkantan.market.server.MarketEventHandler;
import com.yuelengm.nhkantan.market.server.MarketService;
import com.yuelengm.nhkantan.network.ModNetwork;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        ModConfigs.register();
        ModBlocks.register();
        ModItems.register();
        ModNetwork.register();
        FMLCommonHandler.instance()
            .bus()
            .register(new MarketEventHandler());
        NHKantan.LOG.info("NH kantan version " + Tags.VERSION + " initializing.");
    }

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {
        // Market definitions are read here, on the server only; clients only ever receive public snapshots
        MarketService.start(event.getServer());
    }

    public void serverStopped(FMLServerStoppedEvent event) {
        MarketService.stop();
    }
}
