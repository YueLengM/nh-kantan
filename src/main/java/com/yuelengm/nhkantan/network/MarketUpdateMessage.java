package com.yuelengm.nhkantan.network;

import java.util.ArrayList;
import java.util.List;

import com.yuelengm.nhkantan.market.PriceUpdate;
import com.yuelengm.nhkantan.market.view.ClientMarketData;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Server to client: the new price of every updated stock, broadcast once per market update to every player.
 */
public class MarketUpdateMessage implements IMessage {

    private List<PriceUpdate> updates;

    /** Required by FML for decoding. */
    public MarketUpdateMessage() {}

    public MarketUpdateMessage(List<PriceUpdate> updates) {
        this.updates = updates;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(updates.size());
        for (PriceUpdate update : updates) {
            ByteBufUtils.writeUTF8String(buf, update.stockId());
            buf.writeFloat(update.price());
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int count = buf.readInt();
        updates = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            updates.add(new PriceUpdate(ByteBufUtils.readUTF8String(buf), buf.readFloat()));
        }
    }

    public static class Handler implements IMessageHandler<MarketUpdateMessage, IMessage> {

        @Override
        public IMessage onMessage(MarketUpdateMessage message, MessageContext ctx) {
            ClientMarketData.INSTANCE.applyUpdates(message.updates);
            return null;
        }
    }
}
