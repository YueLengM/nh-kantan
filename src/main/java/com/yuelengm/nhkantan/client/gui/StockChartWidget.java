package com.yuelengm.nhkantan.client.gui;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import com.yuelengm.nhkantan.client.render.StockPanelRenderer;
import com.yuelengm.nhkantan.market.view.StockView;

/**
 * ModularUI2 widget showing a stock panel with its K-line chart. Uses the same {@link StockPanelRenderer} as the
 * in-world display, with one font pixel per GUI pixel.
 */
public class StockChartWidget extends Widget<StockChartWidget> {

    private final Supplier<StockView> stock;

    /**
     * @param stock looked up on every frame so the widget follows live updates and selection changes
     */
    public StockChartWidget(Supplier<StockView> stock) {
        this.stock = stock;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        StockPanelRenderer.draw(
            Minecraft.getMinecraft().fontRenderer,
            stock.get(),
            0.0F,
            0.0F,
            getArea().width,
            getArea().height,
            1.0F,
            0.0F,
            0.0F);
    }
}
