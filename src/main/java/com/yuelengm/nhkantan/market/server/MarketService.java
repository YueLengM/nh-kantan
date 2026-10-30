package com.yuelengm.nhkantan.market.server;

import java.security.SecureRandom;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.MapStorage;

import com.yuelengm.nhkantan.NHKantan;
import com.yuelengm.nhkantan.config.ConfigPaths;
import com.yuelengm.nhkantan.config.MarketConfig;
import com.yuelengm.nhkantan.market.PriceTick;
import com.yuelengm.nhkantan.market.definition.DefinitionException;
import com.yuelengm.nhkantan.market.definition.MarketDefinitionLoader;
import com.yuelengm.nhkantan.market.definition.MarketDefinitions;
import com.yuelengm.nhkantan.market.engine.StockMarket;
import com.yuelengm.nhkantan.network.MarketSnapshotMessage;
import com.yuelengm.nhkantan.network.MarketUpdateMessage;
import com.yuelengm.nhkantan.network.ModNetwork;

/**
 * Server-side owner of the running market for the lifetime of a server: loads definitions and saved state, advances
 * prices on the configured tick interval and pushes changes to clients.
 * <p>
 * Clients receive one full snapshot on login and then a single small update packet per market update, independent of
 * how many displays or terminals they have.
 */
public class MarketService {

    @Nullable
    private static MarketService instance;

    private final StockMarket market;
    private final MarketSavedData savedData;
    private int ticksSinceUpdate;

    private MarketService(StockMarket market, MarketSavedData savedData) {
        this.market = market;
        this.savedData = savedData;
    }

    /** The running service, or {@code null} when no server is running. */
    @Nullable
    public static MarketService get() {
        return instance;
    }

    public static void start(MinecraftServer server) {
        MarketDefinitions definitions = loadDefinitions();
        MarketSavedData savedData = loadSavedData(server);
        SecureRandom seeds = new SecureRandom();
        StockMarket market = new StockMarket(
            definitions,
            savedData.states(),
            MarketConfig.historyLength,
            seeds::nextLong);
        savedData.markDirty();
        instance = new MarketService(market, savedData);
    }

    public static void stop() {
        instance = null;
    }

    private static MarketDefinitions loadDefinitions() {
        try {
            MarketDefinitions definitions = MarketDefinitionLoader.load(ConfigPaths.directory());
            NHKantan.LOG.info(
                "Loaded {} market modes and {} stocks",
                definitions.modes()
                    .size(),
                definitions.stocks()
                    .size());
            return definitions;
        } catch (DefinitionException e) {
            NHKantan.LOG.error("Market definitions are invalid, the market is disabled: {}", e.getMessage());
            return MarketDefinitions.empty();
        }
    }

    private static MarketSavedData loadSavedData(MinecraftServer server) {
        // The overworld map storage is global and shared by every dimension
        MapStorage storage = server.worldServerForDimension(0).mapStorage;
        MarketSavedData data = (MarketSavedData) storage.loadData(MarketSavedData.class, MarketSavedData.DATA_NAME);
        if (data == null) {
            data = new MarketSavedData(MarketSavedData.DATA_NAME);
            storage.setData(MarketSavedData.DATA_NAME, data);
        }
        return data;
    }

    /** Called once per server tick. */
    public void tick() {
        if (market.isEmpty() || ++ticksSinceUpdate < MarketConfig.updateIntervalTicks) return;
        ticksSinceUpdate = 0;
        List<PriceTick> ticks = market.update();
        if (ticks.isEmpty()) return;
        savedData.markDirty();
        ModNetwork.CHANNEL.sendToAll(new MarketUpdateMessage(ticks));
    }

    public void sendSnapshot(EntityPlayerMP player) {
        ModNetwork.CHANNEL.sendTo(new MarketSnapshotMessage(MarketConfig.historyLength, market.snapshots()), player);
    }
}
