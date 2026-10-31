package com.yuelengm.nhkantan.market.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.yuelengm.nhkantan.NHKantan;
import com.yuelengm.nhkantan.market.engine.PriceStore;

/**
 * Stores each stock's prices in its own binary file of big-endian 4-byte floats, one per price update. A record is
 * written with a single call, so a crash can at most leave a partial last record, which {@link #repair} removes.
 * I/O failures are logged and never interrupt the market.
 */
public class FilePriceStore implements PriceStore {

    private static final String EXTENSION = ".prices";
    private static final int RECORD_BYTES = Float.BYTES;
    private static final float[] EMPTY = new float[0];

    private final Path directory;

    public FilePriceStore(Path directory) {
        this.directory = directory;
    }

    private Path fileOf(String stockId) {
        return directory.resolve(stockId + EXTENSION);
    }

    @Override
    public void repair(String stockId) {
        Path file = fileOf(stockId);
        if (!Files.exists(file)) return;
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "rw")) {
            long complete = raf.length() / RECORD_BYTES * RECORD_BYTES;
            if (raf.length() != complete) {
                NHKantan.LOG.warn("Dropping a partially written price record of stock '{}'", stockId);
                raf.setLength(complete);
            }
        } catch (IOException e) {
            NHKantan.LOG.error("Failed to repair price history of stock '{}'", stockId, e);
        }
    }

    @Override
    public void append(String stockId, float price) {
        Path file = fileOf(stockId);
        try {
            Files.createDirectories(directory);
            byte[] record = ByteBuffer.allocate(RECORD_BYTES)
                .putFloat(price)
                .array();
            try (FileOutputStream out = new FileOutputStream(file.toFile(), true)) {
                out.write(record);
            }
        } catch (IOException e) {
            NHKantan.LOG.error("Failed to append price of stock '{}'", stockId, e);
        }
    }

    @Override
    public float[] readTail(String stockId, int count) {
        Path file = fileOf(stockId);
        if (count <= 0 || !Files.exists(file)) return EMPTY;
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            long records = raf.length() / RECORD_BYTES;
            int read = (int) Math.min(records, count);
            raf.seek((records - read) * RECORD_BYTES);
            float[] prices = new float[read];
            for (int i = 0; i < read; i++) prices[i] = raf.readFloat();
            return prices;
        } catch (IOException e) {
            NHKantan.LOG.error("Failed to read price history of stock '{}'", stockId, e);
            return EMPTY;
        }
    }
}
