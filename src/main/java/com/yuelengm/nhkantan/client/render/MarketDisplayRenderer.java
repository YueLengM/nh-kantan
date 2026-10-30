package com.yuelengm.nhkantan.client.render;

import java.nio.FloatBuffer;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizon.gtnhlib.geometry.Orientation;
import com.yuelengm.nhkantan.block.MarketDisplayOrientation;
import com.yuelengm.nhkantan.block.TileMarketDisplay;
import com.yuelengm.nhkantan.market.PriceHistory;
import com.yuelengm.nhkantan.market.view.ClientMarketData;
import com.yuelengm.nhkantan.market.view.StockView;

/**
 * Draws the "big screen" of a market display in the world.
 * <p>
 * The renderer sets up a screen-local coordinate system whose origin is the top-left corner of the screen surface:
 * <ul>
 * <li>+X points to the screen's right</li>
 * <li>+Y points down along the screen (matching GUI / font conventions)</li>
 * <li>+Z points out of the screen toward the viewer</li>
 * </ul>
 * One unit equals one block, so the screen covers {@code [0, width] x [0, height]}. Content comes from the shared
 * {@link ClientMarketData}; displays never request data on their own.
 */
public class MarketDisplayRenderer extends TileEntitySpecialRenderer {

    /** Pixels per block, used to convert the configured screen depth (in pixels) to blocks. */
    private static final double PIXELS_PER_BLOCK = 16.0D;
    /** Width of the screen bezel in blocks. */
    private static final float BEZEL = 1.0F / 32.0F;
    /** Small depth steps used to layer bezel, background and content without z-fighting. */
    private static final float LAYER_STEP = 0.001F;
    /** Font pixels per block for screen text. */
    private static final float TEXT_PIXELS_PER_BLOCK = 64.0F;
    /** Chart line width in screen pixels. */
    private static final float CHART_LINE_WIDTH = 2.0F;

    private static final int COLOR_BEZEL = 0x2B3038;
    private static final int COLOR_BACKGROUND = 0x0A0E14;
    private static final int COLOR_TITLE = 0x55FF55;
    private static final int COLOR_TEXT = 0xAAAAAA;
    private static final int COLOR_UP = 0x65D7BF;
    private static final int COLOR_DOWN = 0xFF7777;

    private final FloatBuffer basis = BufferUtils.createFloatBuffer(16);

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!(tile instanceof TileMarketDisplay display)) return;

        Orientation orientation = display.getOrientation();
        ForgeDirection normal = MarketDisplayOrientation.facing(orientation);
        ForgeDirection up = MarketDisplayOrientation.up(orientation);
        ForgeDirection right = MarketDisplayOrientation.right(orientation);
        if (right == ForgeDirection.UNKNOWN) return;

        int width = display.getScreenWidth();
        int height = display.getScreenHeight();

        // Top-left corner of the screen surface, relative to the block origin.
        // surfaceCenter = blockCenter - normal * (0.5 - offset); bottomLeft = surfaceCenter - right/2 - up/2
        // Depth 0 sits flush against the mounting surface; 16 px reaches the opposite face of the block cell
        double back = 0.5D - display.getScreenDepth() / PIXELS_PER_BLOCK;
        double ox = 0.5D - normal.offsetX * back - right.offsetX * 0.5D + up.offsetX * (height - 0.5D);
        double oy = 0.5D - normal.offsetY * back - right.offsetY * 0.5D + up.offsetY * (height - 0.5D);
        double oz = 0.5D - normal.offsetZ * back - right.offsetZ * 0.5D + up.offsetZ * (height - 0.5D);

        GL11.glPushMatrix();
        GL11.glTranslated(x + ox, y + oy, z + oz);
        applyScreenBasis(right, up, normal);

        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        // Screens are self-illuminated
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        drawBackground(width, height);
        drawContent(width, height);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
        GL11.glPopMatrix();
    }

    /** Multiplies the current matrix by the basis (right, -up, normal) so screen-local coordinates can be used. */
    private void applyScreenBasis(ForgeDirection right, ForgeDirection up, ForgeDirection normal) {
        basis.clear();
        // Column-major: column 0 = local X, column 1 = local Y, column 2 = local Z, column 3 = translation
        basis.put(right.offsetX)
            .put(right.offsetY)
            .put(right.offsetZ)
            .put(0.0F);
        basis.put(-up.offsetX)
            .put(-up.offsetY)
            .put(-up.offsetZ)
            .put(0.0F);
        basis.put(normal.offsetX)
            .put(normal.offsetY)
            .put(normal.offsetZ)
            .put(0.0F);
        basis.put(0.0F)
            .put(0.0F)
            .put(0.0F)
            .put(1.0F);
        basis.flip();
        GL11.glMultMatrix(basis);
    }

    private void drawBackground(int width, int height) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(COLOR_BEZEL);
        addQuad(tessellator, 0.0F, 0.0F, width, height, 0.0F);
        tessellator.setColorOpaque_I(COLOR_BACKGROUND);
        addQuad(tessellator, BEZEL, BEZEL, width - BEZEL, height - BEZEL, LAYER_STEP);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private static void addQuad(Tessellator tessellator, float x0, float y0, float x1, float y1, float depth) {
        tessellator.addVertex(x0, y0, depth);
        tessellator.addVertex(x0, y1, depth);
        tessellator.addVertex(x1, y1, depth);
        tessellator.addVertex(x1, y0, depth);
    }

    /** Draws the first stock of the shared client market data, or a "no data" notice. */
    private void drawContent(int width, int height) {
        FontRenderer font = func_147498_b();
        if (font == null) return;

        StockView stock = ClientMarketData.INSTANCE.first();
        float margin = BEZEL * 2.0F;
        float lineHeight = (font.FONT_HEIGHT + 2) / TEXT_PIXELS_PER_BLOCK;

        if (stock == null) {
            drawText(
                font,
                StatCollector.translateToLocal("screen.nhkantan.market_display.title"),
                margin,
                margin,
                COLOR_TITLE);
            drawText(
                font,
                StatCollector.translateToLocal("screen.nhkantan.market_display.no_data"),
                margin,
                margin + lineHeight,
                COLOR_TEXT);
            return;
        }

        double change = stock.historyChange();
        drawText(font, stock.name(), margin, margin, COLOR_TITLE);
        drawText(
            font,
            String.format("%.2f  %+.2f%%", stock.price(), change * 100.0D),
            margin,
            margin + lineHeight,
            colorOf(change));

        float chartTop = margin + lineHeight * 2.0F + margin;
        float chartBottom = height - margin;
        if (chartBottom - chartTop > margin) {
            drawChart(stock.history(), margin, chartTop, width - margin, chartBottom);
        }
    }

    private static void drawText(FontRenderer font, String text, float x, float y, int color) {
        float scale = 1.0F / TEXT_PIXELS_PER_BLOCK;
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, LAYER_STEP * 2.0F);
        GL11.glScalef(scale, scale, 1.0F);
        font.drawString(text, 0, 0, color);
        GL11.glPopMatrix();
    }

    /** Line chart of the history inside the given screen-local rectangle, each segment colored by direction. */
    private static void drawChart(PriceHistory history, float x0, float y0, float x1, float y1) {
        int count = history.size();
        if (count < 2) return;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            double price = history.get(i);
            min = Math.min(min, price);
            max = Math.max(max, price);
        }
        double padding = Math.max((max - min) * 0.08D, Math.abs(max) * 0.005D);
        min -= padding;
        max += padding;
        double span = max - min;
        float stepX = (x1 - x0) / (count - 1);
        float depth = LAYER_STEP * 2.0F;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(CHART_LINE_WIDTH);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_LINES);
        double previous = history.get(0);
        for (int i = 1; i < count; i++) {
            double current = history.get(i);
            tessellator.setColorOpaque_I(colorOf(current - previous));
            tessellator.addVertex(x0 + stepX * (i - 1), y1 - (previous - min) / span * (y1 - y0), depth);
            tessellator.addVertex(x0 + stepX * i, y1 - (current - min) / span * (y1 - y0), depth);
            previous = current;
        }
        tessellator.draw();
        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private static int colorOf(double delta) {
        return delta > 0.0D ? COLOR_UP : delta < 0.0D ? COLOR_DOWN : COLOR_TEXT;
    }
}
