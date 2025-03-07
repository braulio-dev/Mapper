package dev.brauw.mapper.region;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class PointRegion implements Region {
    private UUID id;
    @Setter
    private String name;
    private Location location;
    private RegionOptions options;

    public PointRegion(String name, Location location, RegionOptions options) {
        this.options = options;
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location;
    }

    public PointRegion(String name, Location location) {
        this(name, location, RegionOptions.builder().build());
    }

    @JsonCreator
    public PointRegion(@JsonProperty("id") UUID id,
                       @JsonProperty("name") String name,
                       @JsonProperty("location") Location location,
                       @JsonProperty("options") RegionOptions options) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.options = options;
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

    @Override
    public World getWorld() {
        return location.getWorld();
    }
}
