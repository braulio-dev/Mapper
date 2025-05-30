package dev.brauw.mapper.export;

import dev.brauw.mapper.region.Region;

import java.io.File;
import java.util.List;

/**
 * Interface representing an export strategy for regions.
 * Implementations of this interface define how regions are exported.
 */
public interface ExportStrategy {

    /**
     * Exports a list of regions.
     *
     * @param regions the list of regions to export
     * @param folder the folder to export the regions to
     * @return true if the export was successful, false otherwise
     */
    boolean export(List<Region> regions, File folder);

    /**
     * Gets the name of the export strategy.
     *
     * @return the name of the export strategy
     */
    String getName();

    /**
     * Gets the description of the export strategy.
     *
     * @return the description of the export strategy
     */
    String getDescription();
}