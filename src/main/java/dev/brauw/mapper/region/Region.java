package dev.brauw.mapper.region;

import org.bukkit.Location;
import java.util.UUID;

/**
 * Interface representing a region in the game world.
 */
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