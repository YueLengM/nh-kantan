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

    @Config.Comment("Number of recent prices kept per stock and sent to clients for charts.")
    @Config.DefaultInt(240)
    @Config.RangeInt(min = 2, max = 4096)
    @Config.RequiresWorldRestart
    public static int historyLength;
}
