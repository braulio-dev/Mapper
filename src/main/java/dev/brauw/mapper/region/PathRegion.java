package dev.brauw.mapper.region;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * An <b>ordered</b> sequence of points - a route rather than a volume.
 * <p>
 * The other region types describe <em>where</em> something is; a path also describes <em>in what
 * order</em>. That ordering is intrinsic here: it comes from the order the builder clicked the points,
 * so a route needs no per-point ordering metadata and inserting a point never renumbers the others.
 * Each point keeps its yaw and pitch, so a consumer that stops at a waypoint also knows which way to
 * face there.
 * <p>
 * {@link #contains(Location)} is deliberately proximity-based rather than exact: the region tools
 * resolve a target by raytrace, and an exact-coordinate test would make a path impossible to click.
 */
@Getter
public class PathRegion implements Region {

    /** How close (in blocks) a location must be to a point to count as inside the path. */
    private static final double POINT_RADIUS = 0.5;

    private final UUID id;
    private final RegionOptions options;
    @Setter
    private String name;
    private final List<Location> points;

    public PathRegion(String name, List<Location> points, RegionOptions options) {
        this(UUID.randomUUID(), name, points, options);
    }

    public PathRegion(String name, List<Location> points) {
        this(name, points, RegionOptions.builder().build());
    }

    @JsonCreator
    public PathRegion(
            @JsonProperty("id") UUID id,
            @JsonProperty("name") String name,
            @JsonProperty("points") List<Location> points,
            @JsonProperty("options") RegionOptions options) {
        Preconditions.checkArgument(points != null && !points.isEmpty(), "A path region needs at least one point");
        this.id = id;
        this.name = name;
        this.options = options;
        this.points = new ArrayList<>(points.size());
        for (Location point : points) {
            this.points.add(point.clone());
        }
    }

    /**
     * @return the ordered points of this path, as copies. Mutating them does not affect the region;
     * use {@link #setWorld(World)} to re-home a path onto a world.
     */
    public List<Location> getPoints() {
        final List<Location> copies = new ArrayList<>(points.size());
        for (Location point : points) {
            copies.add(point.clone());
        }
        return copies;
    }

    /**
     * @return how many points the path has
     */
    public int size() {
        return points.size();
    }

    @Override
    public boolean contains(Location location) {
        if (location == null) {
            return false;
        }
        for (Location point : points) {
            // Compared component-wise rather than via Location#distanceSquared, which throws on a null
            // or mismatched world - and points read from a stream are world-less until setWorld runs.
            if (point.getWorld() != null && location.getWorld() != null
                    && !point.getWorld().equals(location.getWorld())) {
                continue;
            }
            final double dx = point.getX() - location.getX();
            final double dy = point.getY() - location.getY();
            final double dz = point.getZ() - location.getZ();
            if (dx * dx + dy * dy + dz * dz <= POINT_RADIUS * POINT_RADIUS) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RegionType getType() {
        return RegionType.PATH;
    }

    @Override
    public World getWorld() {
        return points.getFirst().getWorld();
    }

    @Override
    public void setWorld(World world) {
        points.forEach(point -> point.setWorld(world));
    }
}
