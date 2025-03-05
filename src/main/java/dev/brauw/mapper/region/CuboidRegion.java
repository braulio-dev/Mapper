package dev.brauw.mapper.region;

import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import java.util.UUID;

@Getter
@CustomLog
public class CuboidRegion implements Region {
    private final UUID id;
    @Setter
    private String name;
    private final Location min;
    private final Location max;
    
    public CuboidRegion(String name, Location pos1, Location pos2) {
        this.id = UUID.randomUUID();
        this.name = name;
        
        // Calculate min/max coordinates
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        this.min = new Location(pos1.getWorld(), minX, minY, minZ);
        this.max = new Location(pos1.getWorld(), maxX, maxY, maxZ);
    }
    
    @Override
    public boolean contains(Location location) {
        if (!location.getWorld().equals(min.getWorld())) return false;
        
        return location.getBlockX() >= min.getBlockX() && location.getBlockX() <= max.getBlockX() &&
               location.getBlockY() >= min.getBlockY() && location.getBlockY() <= max.getBlockY() &&
               location.getBlockZ() >= min.getBlockZ() && location.getBlockZ() <= max.getBlockZ();
    }
    
    @Override
    public RegionType getType() {
        return RegionType.CUBOID;
    }
}