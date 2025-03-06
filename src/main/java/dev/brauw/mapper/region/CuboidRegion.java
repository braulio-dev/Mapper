package dev.brauw.mapper.region;

import com.google.common.base.Preconditions;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

@Getter
@CustomLog
public class CuboidRegion implements Region {
    private final UUID id;
    @Setter
    private String name;
    private final Location min;
    private final Location max;
    private final RegionOptions options;
    
    public CuboidRegion(String name, Location pos1, Location pos2, RegionOptions options) {
        Preconditions.checkArgument(pos1.getWorld().equals(pos2.getWorld()));
        this.options = options;
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

    public CuboidRegion(String name, Location pos1, Location pos2) {
        this(name, pos1, pos2, RegionOptions.builder().build());
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

    @Override
    public World getWorld() {
        return min.getWorld();
    }
}