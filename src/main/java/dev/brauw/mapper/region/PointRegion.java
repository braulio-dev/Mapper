package dev.brauw.mapper.region;

import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import java.util.UUID;

@CustomLog
public class PointRegion implements Region {
    @Getter
    private final UUID id;
    @Getter @Setter
    private String name;
    @Getter
    private final Location location;
    
    public PointRegion(String name, Location location) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location;
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