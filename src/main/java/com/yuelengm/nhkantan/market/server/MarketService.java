package com.yuelengm.nhkantan.market.server;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.yuelengm.nhkantan.NHKantan;
import com.yuelengm.nhkantan.config.ConfigPaths;
import com.yuelengm.nhkantan.config.MarketConfig;
import com.yuelengm.nhkantan.market.PriceUpdate;
import com.yuelengm.nhkantan.market.definition.DefinitionException;
import com.yuelengm.nhkantan.market.definition.MarketDefinitionLoader;
import com.yuelengm.nhkantan.market.definition.MarketDefinitions;
import com.yuelengm.nhkantan.market.engine.StockMarket;
import com.yuelengm.nhkantan.network.MarketSnapshotMessage;
import com.yuelengm.nhkantan.network.MarketUpdateMessage;
import com.yuelengm.nhkantan.network.ModNetwork;

/**
 * Server-side owner of the running market for the lifetime of a server: loads definitions and price history, advances
 * prices on the configured tick interval and pushes changes to clients.
 * <p>
 * Clients receive one full snapshot on login and then a single small update packet per market update, independent of
 * how many displays or terminals they have.
 */
public class MarketService {

    private static final String PRICE_DIRECTORY = "nhkantan/prices";

    @Nullable
    private static MarketService instance;

    private final StockMarket market;
    private int ticksSinceUpdate;

    private MarketService(StockMarket market) {
        this.market = market;
    }

    /** The running service, or {@code null} when no server is running. */
    @Nullable
    public static MarketService get() {
        return instance;
    }

    public static void start(MinecraftServer server) {
        MarketDefinitions definitions = loadDefinitions();
        StockMarket market = new StockMarket(
            definitions,
            new FilePriceStore(priceDirectory(server)),
            recentPrices(),
            new Random());
        instance = new MarketService(market);
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

    /** Price history files live in the world save, so each world has its own market history. */
    private static Path priceDirectory(MinecraftServer server) {
        return server.worldServerForDimension(0)
            .getSaveHandler()
            .getWorldDirectory()
            .toPath()
            .resolve(PRICE_DIRECTORY);
    }

    /** Each candle spans two consecutive prices, so N visible candles need N + 1 prices. */
    private static int recentPrices() {
        return MarketConfig.visibleCandles + 1;
    }

    /** Called once per server tick. */
    public void tick() {
        if (market.isEmpty() || ++ticksSinceUpdate < MarketConfig.updateIntervalTicks) return;
        ticksSinceUpdate = 0;
        List<PriceUpdate> updates = market.update();
        if (updates.isEmpty()) return;
        ModNetwork.CHANNEL.sendToAll(new MarketUpdateMessage(updates));
    }

    public void sendSnapshot(EntityPlayerMP player) {
        ModNetwork.CHANNEL.sendTo(new MarketSnapshotMessage(recentPrices(), market.snapshots()), player);
    }
}
