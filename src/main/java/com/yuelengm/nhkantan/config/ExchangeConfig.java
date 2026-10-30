package com.yuelengm.nhkantan.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.yuelengm.nhkantan.NHKantan;

@Config(
    modid = NHKantan.MODID,
    category = "exchange",
    configSubDirectory = ConfigPaths.DIRECTORY_NAME,
    filename = ConfigPaths.CONFIG_FILE_NAME)
@Config.LangKeyPattern
@Config.Comment("Exchange between market currency and items, and items accepted to repay debt.")
public class ExchangeConfig {

    @Config.Comment({ "Task currency items exchangeable in both directions, one per entry:",
        "id;modid:itemName;meta;toMarketRate;toMarketFeePercent;fromMarketRate;fromMarketFeePercent",
        "toMarketRate: market currency per item. fromMarketRate: items per market currency.",
        "Fees are percentages of the exchanged amount, configured per direction." })
    @Config.DefaultStringList({})
    public static String[] taskCurrencies;

    @Config.Comment({ "Items accepted to repay a negative balance, one per entry: modid:itemName;meta;value",
        "value: market currency repaid per item. Repayment never raises the balance above 0." })
    @Config.DefaultStringList({})
    public static String[] debtItems;
}
