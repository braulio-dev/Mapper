package dev.brauw.mapper.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.brauw.mapper.export.serializer.SerializableLocation;
import dev.brauw.mapper.region.PerspectiveRegion;
import dev.brauw.mapper.region.Region;
import dev.brauw.mapper.region.PointRegion;
import dev.brauw.mapper.region.CuboidRegion;
import lombok.CustomLog;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
    }

    @Override
    public boolean export(List<Region> regions) {
        try {
            // Group regions by name
            Map<String, List<SerializableLocation>> groupedRegions = regions.stream()
                    .collect(Collectors.groupingBy(
                            Region::getName,
                            Collectors.mapping(this::convertRegionToLocations, Collectors.flatMapping(Collection::stream, Collectors.toList()))
                    ));

            // Write to file with current date (number-based after copies)
            File exportFile = new File("exports/regions.json");
            int copyNumber = 1;
            while (exportFile.exists()) {
                exportFile = new File("exports/regions" + copyNumber + ".json");
                copyNumber++;
            }
            objectMapper.writeValue(exportFile, groupedRegions);

            log.info("Exported " + regions.size() + " regions to " + exportFile.getName());
            return true;
        } catch (IOException e) {
            log.severe("Failed to export regions to JSON: " + e.getMessage());
            return false;
        }
    }

    /**
     * Converts a region to a list of SerializableLocations.
     *
     * @param region the region to convert
     * @return a list of SerializableLocations
     */
    private List<SerializableLocation> convertRegionToLocations(Region region) {
        List<SerializableLocation> locations = new ArrayList<>();

        switch (region.getType()) {
            case POINT:
                if (region instanceof PointRegion pointRegion) {
                    locations.add(SerializableLocation.fromLocation(
                            pointRegion.getLocation(), false));
                }
                break;

            case PERSPECTIVE:
                if (region instanceof PerspectiveRegion perspectiveRegion) {
                    locations.add(SerializableLocation.fromLocation(
                            perspectiveRegion.getLocation(), true));
                }
                break;

            case CUBOID:
                if (region instanceof CuboidRegion cuboidRegion) {
                    locations.add(SerializableLocation.fromLocation(
                            cuboidRegion.getMin(), false));
                    locations.add(SerializableLocation.fromLocation(
                            cuboidRegion.getMax(), false));
                }
                break;

            case POLYGON:
                throw new IllegalArgumentException("Polygon regions are not supported for JSON export");
        }

        return locations;
    }

    @Override
    public String getName() {
        return "JSON";
    }

    @Override
    public String getDescription() {
        return "Exports regions to a JSON file format, grouped by region name";
    }
}