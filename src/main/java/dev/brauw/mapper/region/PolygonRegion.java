package dev.brauw.mapper.region;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class PolygonRegion implements Region {

    private final UUID id;
    @Setter
    private String name;
    private final List<CuboidRegion> regions;

    public PolygonRegion(String name, List<CuboidRegion> regions) {
        this.name = name;
        this.id = UUID.randomUUID();
        this.regions = Collections.unmodifiableList(regions);
    }

    @Override
    public boolean contains(Location location) {
        return this.regions.stream().anyMatch(region -> region.contains(location));
    }

    @Override
    public RegionType getType() {
        return RegionType.POLYGON;
    }
}
