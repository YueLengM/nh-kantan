package com.yuelengm.nhkantan.network;

import com.yuelengm.nhkantan.NHKantan;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * The mod's network channel and message registry.
 */
public class ModNetwork {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(NHKantan.MODID);

    private static final int SNAPSHOT_ID = 0;
    private static final int UPDATE_ID = 1;

    private ModNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(
            MarketSnapshotMessage.Handler.class,
            MarketSnapshotMessage.class,
            SNAPSHOT_ID,
            Side.CLIENT);
        CHANNEL.registerMessage(MarketUpdateMessage.Handler.class, MarketUpdateMessage.class, UPDATE_ID, Side.CLIENT);
    }
}
