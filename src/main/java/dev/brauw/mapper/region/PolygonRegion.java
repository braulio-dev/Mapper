package dev.brauw.mapper.region;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class PolygonRegion implements Region {

    private final UUID id;
    private final RegionOptions options;
    @Setter
    private String name;
    private final List<CuboidRegion> children;

    public PolygonRegion(String name, List<CuboidRegion> children, RegionOptions options) {
        this(UUID.randomUUID(), name, children, options);
    }

    public PolygonRegion(String name, List<CuboidRegion> children) {
        this(name, children, RegionOptions.builder().build());
    }

    @JsonCreator
    public PolygonRegion(
            @JsonProperty("id") UUID id,
            @JsonProperty("name") String name,
            @JsonProperty("children") List<CuboidRegion> children,
            @JsonProperty("options") RegionOptions options) {
        this.id = id;
        this.name = name;
        this.options = options;
        this.children = Collections.unmodifiableList(withOptions(children, options));
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

    @Override
    public void setWorld(World world) {
        children.forEach(region -> region.setWorld(world));
    }

    private static List<CuboidRegion> withOptions(List<CuboidRegion> children, RegionOptions options) {
        Preconditions.checkArgument(children != null && !children.isEmpty());
        Preconditions.checkNotNull(options);

        World world = children.getFirst().getWorld();
        Preconditions.checkArgument(children.stream().allMatch(region -> Objects.equals(region.getWorld(), world)));

        List<CuboidRegion> normalized = new ArrayList<>(children.size());
        for (CuboidRegion child : children) {
            normalized.add(new CuboidRegion(child.getId(), child.getName(), child.getMin(), child.getMax(), options));
        }
        return normalized;
    }
}
