package dev.brauw.mapper.region;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import org.bukkit.Location;
import org.bukkit.World;

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

        // make sure they're all in the same world
        Preconditions.checkArgument(!children.isEmpty());
        final World world = children.getFirst().getWorld();
        Preconditions.checkArgument(children.stream().allMatch(region -> region.getWorld().equals(world)));
        this.children = Collections.unmodifiableList(children);
    }

    public PolygonRegion(String name, List<CuboidRegion> children) {
        this(name, children, RegionOptions.builder().build());
    }

    @JsonCreator
    public PolygonRegion(
            UUID id,
            String name,
            List<CuboidRegion> children,
            RegionOptions options) {
        this.id = id;
        this.name = name;
        this.children = Collections.unmodifiableList(children);
        this.options = options;
    }

    @Override
    public boolean contains(Location location) {
        return this.children.stream().anyMatch(region -> region.contains(location));
    }

    @Override
    public RegionType getType() {
        return RegionType.POLYGON;
    }

    @Override
    public World getWorld() {
        return children.getFirst().getWorld();
    }
}
