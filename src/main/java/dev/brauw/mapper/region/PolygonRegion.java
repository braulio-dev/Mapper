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
    private final RegionOptions options;
    @Setter
    private String name;
    private final List<CuboidRegion> children;

    public PolygonRegion(String name, List<CuboidRegion> children, RegionOptions options) {
        this.name = name;
        this.id = UUID.randomUUID();
        this.options = options;
        this.children = Collections.unmodifiableList(children);
    }

    public PolygonRegion(String name, List<CuboidRegion> children) {
        this(name, children, RegionOptions.builder().build());
    }

    @Override
    public boolean contains(Location location) {
        return this.children.stream().anyMatch(region -> region.contains(location));
    }

    @Override
    public RegionType getType() {
        return RegionType.POLYGON;
    }
}
