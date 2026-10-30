package com.yuelengm.nhkantan.item;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.yuelengm.nhkantan.NHKantan;
import com.yuelengm.nhkantan.market.view.ClientMarketData;
import com.yuelengm.nhkantan.market.view.StockView;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Handheld market terminal. Right-clicking opens the market GUI, which currently lists live stock prices from the
 * shared client market data; trading is not implemented yet.
 */
public class ItemMarketTerminal extends Item implements IGuiHolder<PlayerInventoryGuiData> {

    public ItemMarketTerminal() {
        this.setUnlocalizedName("nhkantan.market_terminal");
        this.setTextureName("nhkantan:market_terminal");
        this.setCreativeTab(CreativeTabs.tabTools);
        this.setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            GuiFactories.playerInventory()
                .openFromMainHand(player);
        }
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(PlayerInventoryGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(NHKantan.MODID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData data, PanelSyncManager syncManager, UISettings settings) {
        Flow column = Flow.column()
            .widthRel(1f)
            .heightRel(1f)
            .padding(7)
            .childPadding(4)
            .child(
                IKey.lang("gui.nhkantan.market_terminal.title")
                    .asWidget()
                    .left(0));

        // Prices are client-side display data; the server-side copy of the panel carries no stock rows
        Collection<StockView> stocks = data.getPlayer().worldObj.isRemote ? ClientMarketData.INSTANCE.stocks()
            : Collections.emptyList();
        if (stocks.isEmpty()) {
            column.child(
                IKey.lang("gui.nhkantan.market_terminal.no_data")
                    .asWidget()
                    .left(0));
        }
        for (StockView stock : stocks) {
            column.child(stockRow(stock.id(), stock.name()));
        }

        ModularPanel panel = ModularPanel.defaultPanel("nhkantan_market_terminal", 200, 120);
        panel.child(column)
            .child(ButtonWidget.panelCloseButton());
        return panel;
    }

    /** Name on the left, live price and change on the right; looks the stock up by id on every frame. */
    private static Flow stockRow(String stockId, String name) {
        return Flow.row()
            .widthRel(1f)
            .height(12)
            .child(
                IKey.str(name)
                    .asWidget()
                    .widthRel(0.5f))
            .child(
                IKey.dynamic(() -> formatPrice(ClientMarketData.INSTANCE.get(stockId)))
                    .asWidget()
                    .widthRel(0.5f));
    }

    private static String formatPrice(StockView stock) {
        if (stock == null) return "-";
        double change = stock.historyChange();
        EnumChatFormatting color = change > 0.0D ? EnumChatFormatting.DARK_GREEN
            : change < 0.0D ? EnumChatFormatting.RED : EnumChatFormatting.DARK_GRAY;
        return String.format("%.2f %s%+.2f%%", stock.price(), color, change * 100.0D);
    }
}
