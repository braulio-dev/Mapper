package dev.brauw.mapper.region;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
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
        float minX = (float) Math.min(pos1.getX(), pos2.getX());
        float minY = (float) Math.min(pos1.getY(), pos2.getY());
        float minZ = (float) Math.min(pos1.getZ(), pos2.getZ());
        float maxX = (float) Math.max(pos1.getX(), pos2.getX());
        float maxY = (float) Math.max(pos1.getY(), pos2.getY());
        float maxZ = (float) Math.max(pos1.getZ(), pos2.getZ());
        
        this.min = new Location(pos1.getWorld(), minX, minY, minZ);
        this.max = new Location(pos1.getWorld(), maxX, maxY, maxZ);
    }

    public CuboidRegion(String name, Location pos1, Location pos2) {
        this(name, pos1, pos2, RegionOptions.builder().build());
    }

    @JsonCreator
    public CuboidRegion(
            @JsonProperty("id") UUID id,
            @JsonProperty("name") String name,
            @JsonProperty("min") Location min,
            @JsonProperty("max") Location max,
            @JsonProperty("options") RegionOptions options) {
        this.id = id;
        this.name = name;
        this.min = min;
        this.max = max;
        this.options = options;
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

    public Location getMin() {
        return min.clone();
    }

    public Location getMax() {
        return max.clone();
    }

    @Override
    public void setWorld(World world) {
        min.setWorld(world);
        max.setWorld(world);
    }
}