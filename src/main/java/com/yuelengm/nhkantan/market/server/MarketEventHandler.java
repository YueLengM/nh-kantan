package com.yuelengm.nhkantan.market.server;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * FML bus hooks driving the market service: ticking and initial sync of joining players.
 */
public class MarketEventHandler {

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MarketService service = MarketService.get();
        if (service != null) service.tick();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        MarketService service = MarketService.get();
        if (service != null && event.player instanceof EntityPlayerMP player) {
            service.sendSnapshot(player);
        }
    }
}
