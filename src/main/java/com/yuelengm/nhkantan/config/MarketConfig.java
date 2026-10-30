package com.yuelengm.nhkantan.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.yuelengm.nhkantan.NHKantan;

@Config(
    modid = NHKantan.MODID,
    category = "market",
    configSubDirectory = ConfigPaths.DIRECTORY_NAME,
    filename = ConfigPaths.CONFIG_FILE_NAME)
@Config.Comment("Price engine settings. Stocks and modes are defined in stocks.json and modes.json.")
public class MarketConfig {

    @Config.Comment("Server ticks between two price updates. Prices only advance while the server is running.")
    @Config.DefaultInt(100)
    @Config.RangeInt(min = 1, max = 72000)
    public static int updateIntervalTicks;

    @Config.Comment({ "Price updates aggregated into one K-line candle.",
        "1 makes every price update its own candle (open = previous price, close = new price), shifting the chart",
        "left by one candle per update. Larger values give candles with wicks but only shift every N updates." })
    @Config.DefaultInt(1)
    @Config.RangeInt(min = 1, max = 10000)
    public static int updatesPerCandle;

    @Config.Comment({ "Number of candles visible on displays and terminals, including the one still forming.",
        "Clients only ever receive these candles; the full history stays on the server." })
    @Config.DefaultInt(50)
    @Config.RangeInt(min = 5, max = 500)
    @Config.RequiresWorldRestart
    public static int visibleCandles;
}
