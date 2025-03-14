package dev.brauw.mapper.export;

import dev.brauw.mapper.MapperPlugin;
import dev.brauw.mapper.region.Region;
import lombok.CustomLog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the export strategies for regions.
 */
@CustomLog
public class ExportManager {
    private final MapperPlugin plugin;
    private final Map<String, ExportStrategy> exportStrategies = new HashMap<>();

    /**
     * Constructs an ExportManager with the specified plugin.
     *
     * @param plugin the plugin instance
     */
    public ExportManager(MapperPlugin plugin) {
        this.plugin = plugin;
        registerDefaultStrategies();
    }

    /**
     * Registers the default export strategies.
     */
    private void registerDefaultStrategies() {
        // JSON export strategy
        registerExportStrategy("json", new JsonExportStrategy());

        log.info("Registered default export strategies");
    }

    /**
     * Registers a new export strategy.
     *
     * @param id the identifier for the export strategy
     * @param strategy the export strategy to register
     */
    public void registerExportStrategy(String id, ExportStrategy strategy) {
        exportStrategies.put(id.toLowerCase(), strategy);
    }

    /**
     * Gets the available export strategies.
     *
     * @return a map of available export strategies
     */
    public Map<String, ExportStrategy> getAvailableStrategies() {
        return new HashMap<>(exportStrategies);
    }
}