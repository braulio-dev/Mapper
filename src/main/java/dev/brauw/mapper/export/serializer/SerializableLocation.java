package dev.brauw.mapper.export.serializer;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.Location;

/**
 * Serializable representation of a Bukkit Location.
 * This class simplifies the Location object for export purposes.
 */
@Getter
@Builder
public class SerializableLocation {
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final String world;
    
    /**
     * Converts a Bukkit Location to a SerializableLocation.
     *
     * @param location the Bukkit Location to convert
     * @param includeRotation whether to include yaw and pitch values
     * @return a SerializableLocation instance
     */
    public static SerializableLocation fromLocation(Location location, boolean includeRotation) {
        SerializableLocationBuilder builder = SerializableLocation.builder()
                .x(location.getX())
                .y(location.getY())
                .z(location.getZ());
                
        if (includeRotation) {
            builder.yaw(location.getYaw())
                   .pitch(location.getPitch());
        }
        
        if (location.getWorld() != null) {
            builder.world(location.getWorld().getName());
        }
        
        return builder.build();
    }
}