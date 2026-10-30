package com.yuelengm.nhkantan.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Handheld market terminal. Right-clicking opens the trading GUI; the GUI is currently a placeholder.
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
        ModularPanel panel = ModularPanel.defaultPanel("nhkantan_market_terminal", 200, 120);
        panel.child(
            Flow.column()
                .widthRel(1f)
                .heightRel(1f)
                .padding(7)
                .childPadding(4)
                .child(
                    IKey.lang("gui.nhkantan.market_terminal.title")
                        .asWidget()
                        .left(0))
                .child(
                    IKey.lang("gui.nhkantan.market_terminal.placeholder")
                        .asWidget()
                        .left(0)))
            .child(ButtonWidget.panelCloseButton());
        return panel;
    }
}
