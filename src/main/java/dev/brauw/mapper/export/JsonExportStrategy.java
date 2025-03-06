package dev.brauw.mapper.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Preconditions;
import dev.brauw.mapper.export.serializer.LocationDeserializer;
import dev.brauw.mapper.export.serializer.LocationSerializer;
import dev.brauw.mapper.region.Region;
import lombok.CustomLog;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of ExportStrategy that exports regions to a JSON file.
 * Regions are grouped by name in the output file.
 */
@CustomLog
public class JsonExportStrategy implements ExportStrategy {
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new JsonExportStrategy.
     */
    public JsonExportStrategy() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(Location.class, new LocationDeserializer());
        module.addSerializer(Location.class, new LocationSerializer());
        this.objectMapper.registerModule(module);
    }

    @Override
    public boolean export(List<Region> regions) {
        Preconditions.checkNotNull(regions);
        Preconditions.checkArgument(!regions.isEmpty());
        final World world = regions.getFirst().getWorld();
        Preconditions.checkArgument(regions.stream().allMatch(region -> region.getWorld().equals(world)));

        try {
            // Write to file with current date (number-based after copies)
            File exportFile = new File(world.getWorldFolder(), "dataPoints.json");
            objectMapper.writeValue(exportFile, regions);

            log.info("Exported " + regions.size() + " regions to " + exportFile.getName());
            return true;
        } catch (IOException e) {
            log.severe("Failed to export regions to JSON: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reads regions from a World with a dataPoints.json file and a dataTypes.json file.
     *
     * @param world the world to read the regions into
     * @return the list of regions read from the file
     */
    public List<Region> read(World world) {
        try {
            final File importFile = new File(world.getWorldFolder(), "dataPoints.json");
            if (!importFile.exists()) {
                return Collections.emptyList();
            }

            List<Region> regions = objectMapper.readValue(importFile, List.class);
            log.info("Read " + regions.size() + " regions from world" + world.getName());
            return regions;
        } catch (IOException e) {
            log.severe("Failed to read regions from JSON: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public String getName() {
        return "JSON";
    }

    @Override
    public String getDescription() {
        return "Exports regions to a JSON file format";
    }
}