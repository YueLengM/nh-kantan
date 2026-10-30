package com.yuelengm.nhkantan.network;

import com.yuelengm.nhkantan.market.Candle;

import io.netty.buffer.ByteBuf;

/**
 * Wire format of a {@link Candle}: index followed by open, high, low and close.
 */
public class CandleCodec {

    private CandleCodec() {}

    public static void write(ByteBuf buf, Candle candle) {
        buf.writeLong(candle.index());
        buf.writeDouble(candle.open());
        buf.writeDouble(candle.high());
        buf.writeDouble(candle.low());
        buf.writeDouble(candle.close());
    }

    public static Candle read(ByteBuf buf) {
        return new Candle(buf.readLong(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }
}
