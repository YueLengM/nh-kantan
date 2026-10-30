package com.yuelengm.nhkantan.market.definition;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.yuelengm.nhkantan.NHKantan;

/**
 * Loads {@code modes.json} and {@code stocks.json} from the mod config directory. Missing files are created from the
 * defaults bundled in the jar. Must only be called on the server.
 */
public class MarketDefinitionLoader {

    public static final String MODES_FILE = "modes.json";
    public static final String STOCKS_FILE = "stocks.json";
    public static final int SCHEMA_VERSION = 1;

    private static final String DEFAULTS_ROOT = "/nhkantan/defaults/";

    private MarketDefinitionLoader() {}

    /**
     * @throws DefinitionException if a file cannot be read or fails validation
     */
    public static MarketDefinitions load(Path directory) {
        Path modesFile = ensureExists(directory, MODES_FILE);
        Path stocksFile = ensureExists(directory, STOCKS_FILE);
        Map<String, ModeDefinition> modes = ModeDefinitionParser.parse(readRoot(modesFile));
        List<StockDefinition> stocks = StockDefinitionParser.parse(readRoot(stocksFile), modes);
        return new MarketDefinitions(modes, stocks);
    }

    private static Path ensureExists(Path directory, String fileName) {
        Path file = directory.resolve(fileName);
        if (Files.exists(file)) return file;
        try (InputStream defaults = MarketDefinitionLoader.class.getResourceAsStream(DEFAULTS_ROOT + fileName)) {
            if (defaults == null) {
                throw new DefinitionException("Bundled default " + fileName + " is missing from the mod jar");
            }
            Files.createDirectories(directory);
            Files.copy(defaults, file);
            NHKantan.LOG.info("Created default market definition {}", file);
            return file;
        } catch (IOException e) {
            throw new DefinitionException("Failed to create default " + file, e);
        }
    }

    private static JsonObject readRoot(Path file) {
        String fileName = file.getFileName()
            .toString();
        JsonElement element;
        try (Reader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
            element = new JsonParser().parse(reader);
        } catch (IOException | JsonParseException e) {
            throw new DefinitionException("Failed to read " + fileName + ": " + e.getMessage(), e);
        }
        JsonObject root = JsonFields.asObject(element, fileName);
        if (root.has("schemaVersion")) {
            double version = JsonFields.requireNumber(root, "schemaVersion", fileName);
            if (version != SCHEMA_VERSION) {
                throw new DefinitionException(fileName + " has unsupported schemaVersion " + version);
            }
        }
        return root;
    }
}
