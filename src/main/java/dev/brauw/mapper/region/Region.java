package dev.brauw.mapper.region;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/**
 * Interface representing a region in the game world.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PointRegion.class, name = "PointRegion"),
        @JsonSubTypes.Type(value = PerspectiveRegion.class, name = "PerspectiveRegion"),
        @JsonSubTypes.Type(value = CuboidRegion.class, name = "CuboidRegion"),
        @JsonSubTypes.Type(value = PolygonRegion.class, name = "PolygonRegion"),
})
public interface Region {

    /**
     * Gets the options of the region.
     * @return the options of the region
     */
    RegionOptions getOptions();

    /**
     * Gets the unique identifier of the region.
     *
     * @return the UUID of the region
     */
    UUID getId();

    /**
     * Gets the name of the region.
     *
     * @return the name of the region
     */
    String getName();

    /**
     * Sets the name of the region.
     *
     * @param name the new name of the region
     */
    void setName(String name);

    /**
     * Checks if the region contains the specified location.
     *
     * @param location the location to check
     * @return true if the region contains the location, false otherwise
     */
    boolean contains(Location location);

    /**
     * Gets the type of the region.
     *
     * @return the type of the region
     */
    RegionType getType();

    /**
     * Get the world
     *
     * @return the world this region is in
     */
    World getWorld();

    /**
     * Enum representing the different types of regions.
     */
    enum RegionType {
        /**
         * A point region.
         */
        POINT,

        /**
         * A point region with pitch and yaw.
         */
        PERSPECTIVE,

        /**
         * A cuboid region.
         */
        CUBOID,

        /**
         * A polygon region.
         */
        POLYGON
    }
}