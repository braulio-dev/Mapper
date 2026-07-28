package dev.brauw.mapper.export;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Preconditions;
import dev.brauw.mapper.export.model.RegionCollection;
import dev.brauw.mapper.export.serializer.LocationDeserializer;
import dev.brauw.mapper.export.serializer.LocationSerializer;
import dev.brauw.mapper.region.Region;
import lombok.CustomLog;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

        // Forward compatibility: a file written by a newer Mapper may contain region types or region
        // properties this build has never heard of. Failing on either aborts the whole read, and since
        // read() swallows the exception the caller silently receives *zero* regions - one unknown entry
        // would take every zone, node and NPC datapoint in the world with it. Instead, unknown subtypes
        // deserialize to null (dropped by pruneUnreadable) and unknown properties are ignored, so a file
        // degrades to "the regions this build understands".
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        final SimpleModule module = new SimpleModule();
        module.addDeserializer(Location.class, new LocationDeserializer());
        module.addSerializer(Location.class, new LocationSerializer());
        this.objectMapper.registerModule(module);
    }

    @Override
    public boolean export(List<Region> regions, File exportFile) {
        Preconditions.checkNotNull(regions);
        Preconditions.checkArgument(!regions.isEmpty());
        final World world = regions.getFirst().getWorld();
        Preconditions.checkArgument(regions.stream().allMatch(region -> region.getWorld().equals(world)));

        try {
            // Write to file with current date (number-based after copies)
            final RegionCollection collection = new RegionCollection();
            collection.addAll(regions);
            objectMapper.writeValue(exportFile, collection);

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
     * @param file the file to read from
     * @return the list of regions read from the file
     */
    public RegionCollection read(File file) {
        try {
            if (!file.exists()) {
                return new RegionCollection();
            }

            RegionCollection regions = objectMapper.readValue(
                    file,
                    RegionCollection.class
            );
            pruneUnreadable(regions, file.getName());
            applyWorldFromFile(regions, file);

            log.info("Read " + regions.size() + " regions");
            return regions;
        } catch (IOException e) {
            log.severe("Failed to read regions from JSON: " + e.getMessage());
            return new RegionCollection();
        }
    }

    /**
     * Reads regions from a World with a dataPoints.json file and a dataTypes.json file.
     * @param file the file to read from
     * @return the list of regions read from the file
     */
    public RegionCollection read(InputStream file) {
        try {
            if (file == null) {
                return new RegionCollection();
            }

            RegionCollection regions = objectMapper.readValue(
                    file,
                    RegionCollection.class
            );
            pruneUnreadable(regions, "stream");

            log.info("Read " + regions.size() + " regions");
            return regions;
        } catch (IOException e) {
            log.severe("Failed to read regions from JSON: " + e.getMessage());
            return new RegionCollection();
        }
    }

    @Override
    public String serialize(List<Region> regions) {
        Preconditions.checkNotNull(regions);
        Preconditions.checkArgument(!regions.isEmpty());

        try {
            final RegionCollection collection = new RegionCollection();
            collection.addAll(regions);
            // INDENT_OUTPUT is enabled on the mapper, so this is already prettified.
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(collection);
        } catch (IOException e) {
            log.severe("Failed to serialize regions to JSON: " + e.getMessage());
            return null;
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

    /**
     * Drops the nulls that {@code FAIL_ON_INVALID_SUBTYPE} leaves behind for region types this build does
     * not know, so callers never see a null region. Logs how many were skipped, since a silent drop would
     * look identical to the region never having been authored.
     *
     * @param regions the freshly deserialized collection, modified in place
     * @param source  a human-readable origin used in the warning
     */
    private void pruneUnreadable(RegionCollection regions, String source) {
        final int before = regions.size();
        regions.removeIf(Objects::isNull);
        final int skipped = before - regions.size();
        if (skipped > 0) {
            log.warning("Skipped " + skipped + " region(s) of an unknown type while reading " + source
                    + " - this Mapper build is older than the file that produced them");
        }
    }

    private void applyWorldFromFile(RegionCollection regions, File file) {
        World world = resolveWorld(file);
        if (world == null) {
            return;
        }
        regions.forEach(region -> region.setWorld(world));
    }

    private World resolveWorld(File file) {
        File parent = file.getParentFile();
        if (parent == null) {
            return null;
        }

        File normalizedParent = parent.getAbsoluteFile();
        for (World world : Bukkit.getWorlds()) {
            File worldFolder = world.getWorldFolder();
            if (worldFolder != null && worldFolder.getAbsoluteFile().equals(normalizedParent)) {
                return world;
            }
        }

        return Bukkit.getWorld(parent.getName());
    }
}
