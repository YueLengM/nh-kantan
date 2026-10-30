package com.yuelengm.nhkantan.network;

import java.util.ArrayList;
import java.util.List;

import com.yuelengm.nhkantan.market.StockSnapshot;
import com.yuelengm.nhkantan.market.view.ClientMarketData;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Server to client: full public market state. Sent on login and whenever the market is (re)started.
 */
public class MarketSnapshotMessage implements IMessage {

    private int historyCapacity;
    private List<StockSnapshot> stocks;

    /** Required by FML for decoding. */
    public MarketSnapshotMessage() {}

    public MarketSnapshotMessage(int historyCapacity, List<StockSnapshot> stocks) {
        this.historyCapacity = historyCapacity;
        this.stocks = stocks;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(historyCapacity);
        buf.writeInt(stocks.size());
        for (StockSnapshot stock : stocks) {
            ByteBufUtils.writeUTF8String(buf, stock.id());
            ByteBufUtils.writeUTF8String(buf, stock.name());
            buf.writeDouble(stock.lowerBound());
            buf.writeDouble(stock.upperBound());
            buf.writeLong(stock.sequence());
            double[] history = stock.history();
            buf.writeInt(history.length);
            for (double price : history) buf.writeDouble(price);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        historyCapacity = buf.readInt();
        int count = buf.readInt();
        stocks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String id = ByteBufUtils.readUTF8String(buf);
            String name = ByteBufUtils.readUTF8String(buf);
            double lowerBound = buf.readDouble();
            double upperBound = buf.readDouble();
            long sequence = buf.readLong();
            double[] history = new double[buf.readInt()];
            for (int j = 0; j < history.length; j++) history[j] = buf.readDouble();
            stocks.add(new StockSnapshot(id, name, lowerBound, upperBound, sequence, history));
        }
    }

    public static class Handler implements IMessageHandler<MarketSnapshotMessage, IMessage> {

        @Override
        public IMessage onMessage(MarketSnapshotMessage message, MessageContext ctx) {
            ClientMarketData.INSTANCE.applySnapshot(message.historyCapacity, message.stocks);
            return null;
        }
    }
}
