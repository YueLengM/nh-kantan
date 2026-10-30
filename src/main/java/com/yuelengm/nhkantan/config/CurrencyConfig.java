package com.yuelengm.nhkantan.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.yuelengm.nhkantan.NHKantan;

@Config(
    modid = NHKantan.MODID,
    category = "currency",
    configSubDirectory = ConfigPaths.DIRECTORY_NAME,
    filename = ConfigPaths.CONFIG_FILE_NAME)
@Config.Comment("Market account currency.")
public class CurrencyConfig {

    @Config.Comment("Display name of the market currency.")
    @Config.DefaultString("市场币")
    public static String name;

    @Config.Comment("Balance of a newly created player account.")
    @Config.DefaultInt(0)
    @Config.RangeInt(min = 0)
    public static int initialBalance;
}
