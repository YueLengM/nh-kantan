package com.yuelengm.nhkantan.network;

import java.util.ArrayList;
import java.util.List;

import com.yuelengm.nhkantan.market.PriceTick;
import com.yuelengm.nhkantan.market.view.ClientMarketData;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Server to client: the new prices of one market update, broadcast once per update to every player.
 */
public class MarketUpdateMessage implements IMessage {

    private List<PriceTick> ticks;

    /** Required by FML for decoding. */
    public MarketUpdateMessage() {}

    public MarketUpdateMessage(List<PriceTick> ticks) {
        this.ticks = ticks;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(ticks.size());
        for (PriceTick tick : ticks) {
            ByteBufUtils.writeUTF8String(buf, tick.stockId());
            buf.writeLong(tick.sequence());
            buf.writeDouble(tick.price());
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int count = buf.readInt();
        ticks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ticks.add(new PriceTick(ByteBufUtils.readUTF8String(buf), buf.readLong(), buf.readDouble()));
        }
    }

    public static class Handler implements IMessageHandler<MarketUpdateMessage, IMessage> {

        @Override
        public IMessage onMessage(MarketUpdateMessage message, MessageContext ctx) {
            ClientMarketData.INSTANCE.applyTicks(message.ticks);
            return null;
        }
    }
}
