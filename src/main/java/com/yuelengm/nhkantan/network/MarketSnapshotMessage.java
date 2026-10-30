package com.yuelengm.nhkantan.network;

import java.util.ArrayList;
import java.util.List;

import com.yuelengm.nhkantan.market.Candle;
import com.yuelengm.nhkantan.market.StockSnapshot;
import com.yuelengm.nhkantan.market.view.ClientMarketData;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Server to client: public market state with the visible candles of every stock. Sent on login.
 */
public class MarketSnapshotMessage implements IMessage {

    private int visibleCandles;
    private List<StockSnapshot> stocks;

    /** Required by FML for decoding. */
    public MarketSnapshotMessage() {}

    public MarketSnapshotMessage(int visibleCandles, List<StockSnapshot> stocks) {
        this.visibleCandles = visibleCandles;
        this.stocks = stocks;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(visibleCandles);
        buf.writeInt(stocks.size());
        for (StockSnapshot stock : stocks) {
            ByteBufUtils.writeUTF8String(buf, stock.id());
            ByteBufUtils.writeUTF8String(buf, stock.name());
            buf.writeDouble(stock.lowerBound());
            buf.writeDouble(stock.upperBound());
            buf.writeDouble(stock.price());
            Candle[] candles = stock.candles();
            buf.writeInt(candles.length);
            for (Candle candle : candles) CandleCodec.write(buf, candle);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        visibleCandles = buf.readInt();
        int count = buf.readInt();
        stocks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String id = ByteBufUtils.readUTF8String(buf);
            String name = ByteBufUtils.readUTF8String(buf);
            double lowerBound = buf.readDouble();
            double upperBound = buf.readDouble();
            double price = buf.readDouble();
            Candle[] candles = new Candle[buf.readInt()];
            for (int j = 0; j < candles.length; j++) candles[j] = CandleCodec.read(buf);
            stocks.add(new StockSnapshot(id, name, lowerBound, upperBound, price, candles));
        }
    }

    public static class Handler implements IMessageHandler<MarketSnapshotMessage, IMessage> {

        @Override
        public IMessage onMessage(MarketSnapshotMessage message, MessageContext ctx) {
            ClientMarketData.INSTANCE.applySnapshot(message.visibleCandles, message.stocks);
            return null;
        }
    }
}
