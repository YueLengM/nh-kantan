package com.yuelengm.nhkantan.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.cleanroommc.modularui.factory.GuiFactories;
import com.gtnewhorizon.gtnhlib.geometry.Orientation;

/**
 * The market display: a 1/16 thick, fully transparent, non-colliding panel.
 * <p>
 * The block itself renders nothing; the big screen is drawn by the tile entity renderer and always extends from this
 * block to the right and upward (this block is the bottom-left cell). Orientation (12 variants) lives in metadata and
 * is exposed through GTNHLib's block state system, see {@link MarketDisplayOrientation}.
 * Right-clicking opens the market GUI.
 */
public class BlockMarketDisplay extends Block {

    /** Thickness of the panel (selection box), in blocks. */
    public static final float THICKNESS = 1.0F / 16.0F;

    public BlockMarketDisplay() {
        super(Material.iron);
        this.setBlockName("nhkantan.market_display");
        // Used only for the inventory icon and breaking particles; the block never renders in the world
        this.setBlockTextureName("nhkantan:market_display");
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setHardness(2.0F);
        this.setResistance(6.0F);
        this.setLightOpacity(0);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Rendering: fully transparent, the TESR draws the big screen
    // ---------------------------------------------------------------------------------------------------------------

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return false;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Bounds: no collision, thin selection box against the mounting surface
    // ---------------------------------------------------------------------------------------------------------------

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        Orientation orientation = MarketDisplayOrientation.PROPERTY.getValue(world.getBlockMetadata(x, y, z));
        ForgeDirection back = MarketDisplayOrientation.facing(orientation)
            .getOpposite();
        float t = THICKNESS;
        switch (back) {
            case DOWN -> setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, t, 1.0F);
            case UP -> setBlockBounds(0.0F, 1.0F - t, 0.0F, 1.0F, 1.0F, 1.0F);
            case NORTH -> setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, t);
            case SOUTH -> setBlockBounds(0.0F, 0.0F, 1.0F - t, 1.0F, 1.0F, 1.0F);
            case WEST -> setBlockBounds(0.0F, 0.0F, 0.0F, t, 1.0F, 1.0F);
            case EAST -> setBlockBounds(1.0F - t, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
            default -> setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Placement: orientation from clicked side and placer yaw
    // ---------------------------------------------------------------------------------------------------------------

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
        // Provisional orientation from the clicked side only; floor/ceiling rotation is refined in onBlockPlacedBy
        return MarketDisplayOrientation.PROPERTY.getMeta(MarketDisplayOrientation.forPlacement(side, 0.0F), 0);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        Orientation provisional = MarketDisplayOrientation.PROPERTY.getValue(world.getBlockMetadata(x, y, z));
        ForgeDirection facing = MarketDisplayOrientation.facing(provisional);
        Orientation orientation = MarketDisplayOrientation.forPlacement(facing.ordinal(), placer.rotationYaw);
        if (orientation != provisional) {
            MarketDisplayOrientation.PROPERTY.setValue(world, x, y, z, orientation);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Tile entity & interaction
    // ---------------------------------------------------------------------------------------------------------------

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileMarketDisplay();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote && world.getTileEntity(x, y, z) instanceof TileMarketDisplay) {
            GuiFactories.tileEntity()
                .open(player, x, y, z);
        }
        return true;
    }

    @Override
    public int damageDropped(int meta) {
        // Orientation is placement-only state; always drop the plain item
        return 0;
    }
}
