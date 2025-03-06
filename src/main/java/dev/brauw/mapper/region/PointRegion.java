package dev.brauw.mapper.region;

import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import java.util.UUID;

@Getter
@CustomLog
public class PointRegion implements Region {
    private final UUID id;
    @Setter
    private String name;
    private final Location location;
    private final RegionOptions options;
    
    public PointRegion(String name, Location location, RegionOptions options) {
        this.options = options;
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location;
    }

    public PointRegion(String name, Location location) {
        this(name, location, RegionOptions.builder().build());
    }
    
    @Override
    public boolean contains(Location location) {
        return this.getLocation().x() == location.x()
                && this.getLocation().y() == location.y()
                && this.getLocation().z() == location.z();
    }
    
    @Override
    public Region.RegionType getType() {
        return RegionType.POINT;
    }
}