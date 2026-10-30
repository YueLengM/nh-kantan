package com.yuelengm.nhkantan.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.yuelengm.nhkantan.NHKantan;

@Config(
    modid = NHKantan.MODID,
    category = "trading",
    configSubDirectory = ConfigPaths.DIRECTORY_NAME,
    filename = ConfigPaths.CONFIG_FILE_NAME)
@Config.Comment("Margin trading rules. Values here are defaults for every stock; see instrumentOverrides.")
public class TradingConfig {

    @Config.Comment("Maximum open positions per player across all stocks.")
    @Config.DefaultInt(1)
    @Config.RangeInt(min = 1, max = 1000)
    public static int maxOpenPositionsPerPlayer;

    @Config.Comment("Default maximum open positions per player on a single stock.")
    @Config.DefaultInt(1)
    @Config.RangeInt(min = 1, max = 1000)
    public static int maxOpenPositionsPerStock;

    @Config.Comment("Default minimum leverage.")
    @Config.DefaultInt(1)
    @Config.RangeInt(min = 1, max = 10000)
    public static int minLeverage;

    @Config.Comment("Default leverage preselected in the trading UI.")
    @Config.DefaultInt(1)
    @Config.RangeInt(min = 1, max = 10000)
    public static int defaultLeverage;

    @Config.Comment("Default maximum leverage.")
    @Config.DefaultInt(100)
    @Config.RangeInt(min = 1, max = 10000)
    public static int maxLeverage;

    @Config.Comment("Default opening fee in percent of the notional position (margin x leverage). 0 disables it.")
    @Config.DefaultDouble(0.055D)
    @Config.RangeDouble(min = 0.0D, max = 100.0D)
    public static double openFeePercent;

    @Config.Comment({ "Default liquidation threshold: unrealized loss in percent of the initial margin.",
        "100 liquidates when the loss equals the margin. 0 disables automatic liquidation." })
    @Config.DefaultDouble(100.0D)
    @Config.RangeDouble(min = 0.0D, max = 10000.0D)
    public static double liquidationPercent;

    @Config.Comment({ "Per-stock overrides, one entry per stock: stockId;key=value;key=value",
        "Keys: maxOpenPositions, minLeverage, defaultLeverage, maxLeverage, openFeePercent, liquidationPercent",
        "Example: gtnh_index;maxLeverage=50;openFeePercent=0.1" })
    @Config.DefaultStringList({})
    public static String[] instrumentOverrides;
}
