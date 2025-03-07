package dev.brauw.mapper.export.model;

import dev.brauw.mapper.region.Region;

import java.util.ArrayList;

/**
 * Represents a collection of regions.
 * <p>
 * Used by {@link dev.brauw.mapper.export.JsonExportStrategy} to export regions to JSON, as Jackson
 * requires a collection type to serialize. Because of type erasure, we cannot use a raw ArrayList
 * as the type parameter is lost at runtime.
 */
public class RegionCollection extends ArrayList<Region> {

    public RegionCollection() {}

}
