package com.yuelengm.nhkantan.block;

import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.gtnhlib.blockstate.properties.OrientationBlockProperty;
import com.gtnewhorizon.gtnhlib.geometry.Orientation;

/**
 * Orientation handling for the market display, backed by GTNHLib's block state system.
 * <p>
 * An {@link Orientation} is a pair of directions: {@code a} is the facing (the normal of the screen surface, pointing
 * toward the viewer) and {@code b} is the screen's "up" direction. The display supports 12 combinations:
 * <ul>
 * <li>4 wall orientations: facing N/S/W/E with up = UP</li>
 * <li>4 floor orientations: facing UP with up = N/S/W/E</li>
 * <li>4 ceiling orientations: facing DOWN with up = N/S/W/E</li>
 * </ul>
 * The orientation index is stored directly in the block metadata (0..11).
 */
public class MarketDisplayOrientation {

    /** Metadata index to orientation. */
    private static final Orientation[] BY_META = { Orientation.NORTH_UP, Orientation.SOUTH_UP, Orientation.WEST_UP,
        Orientation.EAST_UP, Orientation.UP_NORTH, Orientation.UP_SOUTH, Orientation.UP_WEST, Orientation.UP_EAST,
        Orientation.DOWN_NORTH, Orientation.DOWN_SOUTH, Orientation.DOWN_WEST, Orientation.DOWN_EAST };

    public static final Orientation DEFAULT = Orientation.NORTH_UP;

    public static final int META_MASK = 0b1111;

    /**
     * The block property exposed to GTNHLib. Registered on the block so tools that understand block states
     * (rotation/mirroring, structure copy, /blockstate command) can read and transform the display orientation.
     */
    public static final OrientationBlockProperty.MetaOrientationBlockProperty PROPERTY = new OrientationBlockProperty.MetaOrientationBlockProperty(
        MarketDisplayOrientation::fromMeta,
        META_MASK,
        MarketDisplayOrientation::toMeta) {

        @Override
        public boolean isValidOrientation(Orientation value) {
            return MarketDisplayOrientation.isValid(value);
        }
    };

    private MarketDisplayOrientation() {}

    public static Orientation fromMeta(int meta) {
        int index = meta & META_MASK;
        return index < BY_META.length ? BY_META[index] : DEFAULT;
    }

    public static int toMeta(Orientation orientation) {
        for (int i = 0; i < BY_META.length; i++) {
            if (BY_META[i] == orientation) return i;
        }
        return 0;
    }

    public static boolean isValid(Orientation orientation) {
        if (orientation == null || orientation == Orientation.UNKNOWN) return false;
        boolean facingVertical = isVertical(orientation.a);
        boolean upVertical = isVertical(orientation.b);
        if (facingVertical) {
            // Floor / ceiling: "up" must be horizontal
            return !upVertical;
        }
        // Wall: "up" must be world up
        return orientation.b == ForgeDirection.UP;
    }

    /** The screen normal, pointing from the screen surface toward the viewer. */
    public static ForgeDirection facing(Orientation orientation) {
        return orientation.a;
    }

    /** The screen's upward direction (toward the top edge of the big screen). */
    public static ForgeDirection up(Orientation orientation) {
        return orientation.b;
    }

    /** The screen's rightward direction as seen by a viewer looking at the screen; computed as up x facing. */
    public static ForgeDirection right(Orientation orientation) {
        ForgeDirection a = orientation.a;
        ForgeDirection b = orientation.b;
        int x = b.offsetY * a.offsetZ - b.offsetZ * a.offsetY;
        int y = b.offsetZ * a.offsetX - b.offsetX * a.offsetZ;
        int z = b.offsetX * a.offsetY - b.offsetY * a.offsetX;
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (dir.offsetX == x && dir.offsetY == y && dir.offsetZ == z) return dir;
        }
        return ForgeDirection.UNKNOWN;
    }

    /**
     * Computes the orientation for a display placed against the given block side by an entity with the given yaw.
     *
     * @param side the clicked side (vanilla side index, equal to the resulting facing)
     * @param yaw  the placer's rotation yaw in degrees
     */
    public static Orientation forPlacement(int side, float yaw) {
        ForgeDirection facing = ForgeDirection.getOrientation(side);
        if (facing == ForgeDirection.UNKNOWN) return DEFAULT;
        if (!isVertical(facing)) {
            return Orientation.getOrientation(facing, ForgeDirection.UP);
        }

        ForgeDirection look = horizontalLook(yaw);
        // On the floor the top edge points away from the viewer; on the ceiling it points toward the viewer,
        // so in both cases the content reads upright from where the player is standing.
        ForgeDirection up = facing == ForgeDirection.UP ? look : look.getOpposite();
        return Orientation.getOrientation(facing, up);
    }

    /** The horizontal direction the entity is looking toward. */
    public static ForgeDirection horizontalLook(float yaw) {
        int quadrant = MathHelper.floor_double(yaw * 4.0F / 360.0F + 0.5D) & 3;
        return switch (quadrant) {
            case 0 -> ForgeDirection.SOUTH;
            case 1 -> ForgeDirection.WEST;
            case 2 -> ForgeDirection.NORTH;
            default -> ForgeDirection.EAST;
        };
    }

    private static boolean isVertical(ForgeDirection dir) {
        return dir == ForgeDirection.UP || dir == ForgeDirection.DOWN;
    }
}
