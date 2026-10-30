package com.yuelengm.nhkantan.market.server;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.yuelengm.nhkantan.NHKantan;
import com.yuelengm.nhkantan.market.Candle;
import com.yuelengm.nhkantan.market.engine.CandleStore;

/**
 * Stores each stock's candles in its own binary file of fixed-size records ({@code index, open, high, low, close}).
 * Appends are cheap and reading the tail is a single seek, so the history can grow without affecting world saves.
 * I/O failures are logged and never interrupt the market.
 */
public class FileCandleStore implements CandleStore {

    private static final String EXTENSION = ".candles";
    private static final int RECORD_BYTES = Long.BYTES + 4 * Double.BYTES;

    private final Path directory;

    public FileCandleStore(Path directory) {
        this.directory = directory;
    }

    private Path fileOf(String stockId) {
        return directory.resolve(stockId + EXTENSION);
    }

    @Override
    public void append(String stockId, Candle candle) {
        Path file = fileOf(stockId);
        try {
            Files.createDirectories(directory);
            try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file.toFile(), true), RECORD_BYTES))) {
                out.writeLong(candle.index());
                out.writeDouble(candle.open());
                out.writeDouble(candle.high());
                out.writeDouble(candle.low());
                out.writeDouble(candle.close());
            }
        } catch (IOException e) {
            NHKantan.LOG.error("Failed to append candle {} of stock '{}'", candle.index(), stockId, e);
        }
    }

    @Override
    public void truncateFrom(String stockId, long firstInvalidIndex) {
        Path file = fileOf(stockId);
        if (!Files.exists(file)) return;
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "rw")) {
            // A partially written trailing record is dropped as well
            long count = raf.length() / RECORD_BYTES;
            while (count > 0) {
                raf.seek((count - 1) * RECORD_BYTES);
                if (raf.readLong() < firstInvalidIndex) break;
                count--;
            }
            if (raf.length() != count * RECORD_BYTES) {
                raf.setLength(count * RECORD_BYTES);
            }
        } catch (IOException e) {
            NHKantan.LOG.error("Failed to truncate candle history of stock '{}'", stockId, e);
        }
    }

    @Override
    public List<Candle> readTail(String stockId, int count) {
        Path file = fileOf(stockId);
        if (count <= 0 || !Files.exists(file)) return Collections.emptyList();
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            long records = raf.length() / RECORD_BYTES;
            long first = Math.max(0, records - count);
            raf.seek(first * RECORD_BYTES);
            List<Candle> candles = new ArrayList<>((int) (records - first));
            for (long i = first; i < records; i++) {
                candles.add(
                    new Candle(raf.readLong(), raf.readDouble(), raf.readDouble(), raf.readDouble(), raf.readDouble()));
            }
            return candles;
        } catch (IOException e) {
            NHKantan.LOG.error("Failed to read candle history of stock '{}'", stockId, e);
            return Collections.emptyList();
        }
    }
}
