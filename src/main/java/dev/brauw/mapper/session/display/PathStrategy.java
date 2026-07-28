package dev.brauw.mapper.session.display;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.brauw.mapper.region.PathRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Renders a path region as numbered waypoint markers joined by a dotted trail.
 * <p>
 * The numbering is the point of this display: a path's meaning is its order, and a builder editing one
 * needs to see which waypoint is first without clicking anything. The trail between waypoints is drawn
 * with static display entities rather than particles because display strategies are only invoked on
 * show/hide and a once-per-second revalidate - there is no per-tick hook to emit particles from, and
 * entities cost nothing to leave standing.
 */
public class PathStrategy implements RegionDisplayStrategy<PathRegion> {

    /** Spacing, in blocks, between trail dots along a segment. */
    private static final double DOT_SPACING = 1.0;

    /** Upper bound on dots for one segment, so a long leg cannot spawn hundreds of entities. */
    private static final int MAX_DOTS_PER_SEGMENT = 24;

    private final Map<PathRegion, List<Entity>> parts = new HashMap<>();
    private final Multimap<PathRegion, UUID> viewers = HashMultimap.create();
    private final Plugin plugin;

    public PathStrategy(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void display(@NotNull PathRegion region, @NotNull Player player) {
        for (Entity part : getParts(region)) {
            player.showEntity(plugin, part);
        }
        viewers.put(region, player.getUniqueId());
    }

    @Override
    public void update(@NotNull PathRegion region) {
        despawn(parts.remove(region));
        final List<Entity> rebuilt = getParts(region);
        for (Player viewer : viewersOf(region)) {
            for (Entity part : rebuilt) {
                viewer.showEntity(plugin, part);
            }
        }
    }

    @Override
    public void revalidate(@NotNull PathRegion region) {
        final List<Player> currentViewers = viewersOf(region);
        if (currentViewers.isEmpty()) {
            return;
        }

        final List<Entity> existing = parts.get(region);
        if (existing == null || !existing.stream().allMatch(Entity::isValid)) {
            // Every waypoint, not just the first: a path's markers are spread across chunks, so a
            // rebuild is only worth doing once all of them can hold an entity that will track.
            if (region.getPoints().stream().allMatch(RegionDisplayStrategy::canSpawnAt)) {
                update(region);
            }
            return;
        }

        // See BlockStrategy#revalidate: visibility is re-asserted every pass so a lost grant heals
        // itself instead of leaving a path invisible until the viewer rejoins the session.
        for (Player viewer : currentViewers) {
            for (Entity part : existing) {
                viewer.showEntity(plugin, part);
            }
        }
    }

    /** @return the online players currently viewing this region */
    private List<Player> viewersOf(PathRegion region) {
        final List<Player> online = new ArrayList<>();
        for (UUID viewerId : viewers.get(region)) {
            final Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null) {
                online.add(viewer);
            }
        }
        return online;
    }

    @Override
    public void hide(@NotNull PathRegion region, @NotNull Player player) {
        if (!viewers.remove(region, player.getUniqueId())) {
            return;
        }

        final List<Entity> existing = parts.get(region);
        if (existing != null) {
            for (Entity part : existing) {
                player.hideEntity(plugin, part);
            }
        }

        if (viewers.get(region).isEmpty()) {
            despawn(parts.remove(region));
        }
    }

    /**
     * Returns this path's display entities, rebuilding them if any have gone invalid (a chunk unload
     * destroys the non-persistent displays).
     */
    private List<Entity> getParts(PathRegion region) {
        final List<Entity> existing = parts.get(region);
        if (existing != null && !existing.stream().allMatch(Entity::isValid)) {
            despawn(parts.remove(region));
        }
        return parts.computeIfAbsent(region, this::spawnParts);
    }

    private List<Entity> spawnParts(PathRegion region) {
        final List<Location> points = region.getPoints();
        final Color color = region.getOptions().getColor().getBukkitColor();
        final List<Entity> spawned = new ArrayList<>();

        for (int index = 0; index < points.size(); index++) {
            final Location point = points.get(index);
            spawned.add(spawnMarker(point.clone().add(0, 0.1, 0), color, 0.6f));
            // Only the first waypoint carries the region name; repeating it at every point would bury
            // the ordinals, which are what a builder is actually reading.
            final String text = index == 0 ? region.getName() + " #1" : "#" + (index + 1);
            spawned.add(spawnLabel(point, text, color));

            if (index + 1 < points.size()) {
                spawnTrail(point, points.get(index + 1), color, spawned);
            }
        }
        return spawned;
    }

    private void spawnTrail(Location from, Location to, Color color, List<Entity> out) {
        final double dx = to.getX() - from.getX();
        final double dy = to.getY() - from.getY();
        final double dz = to.getZ() - from.getZ();
        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

        final int dots = Math.min((int) (length / DOT_SPACING), MAX_DOTS_PER_SEGMENT);
        for (int dot = 1; dot <= dots; dot++) {
            final double progress = (double) dot / (dots + 1);
            final Location at = new Location(
                    from.getWorld(),
                    from.getX() + dx * progress,
                    from.getY() + dy * progress,
                    from.getZ() + dz * progress);

            // set direction to next
            at.setDirection(to.toVector().subtract(from.toVector()));

            out.add(spawnMarker(at, color, 0.3f));
        }
    }

    private ItemDisplay spawnMarker(Location location, Color color, float scale) {
        return location.getWorld().spawn(location, ItemDisplay.class, spawned -> {
            spawned.setItemStack(new ItemStack(Material.END_ROD));
            spawned.setGlowing(true);
            spawned.setGlowColorOverride(color);
            spawned.setVisibleByDefault(false);
            spawned.setPersistent(false);
            // An end rod models along its own +Y, so it stands upright at pitch 0. Tilting 90 degrees
            // about local X maps +Y onto +Z, laying the rod flat along the marker's forward direction.
            spawned.setTransformation(new Transformation(
                    new Vector3f(),
                    new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0),
                    new Vector3f(scale),
                    new AxisAngle4f()
            ));
        });
    }

    private TextDisplay spawnLabel(Location location, String text, Color color) {
        final Location above = location.clone().add(0, 1.0, 0);
        return above.getWorld().spawn(above, TextDisplay.class, spawned -> {
            spawned.text(Component.text(text).color(TextColor.color(color.getRed(), color.getGreen(), color.getBlue())));
            spawned.setBillboard(Display.Billboard.CENTER);
            spawned.setVisibleByDefault(false);
            spawned.setPersistent(false);
        });
    }

    /**
     * Removal is unconditional. {@code isValid()} is false for an entity whose chunk is merely
     * unloaded, and dropping the list without removing such an entity strands it in the world with
     * nothing left holding a reference to it.
     */
    private void despawn(List<Entity> entities) {
        if (entities == null) {
            return;
        }
        for (Entity entity : entities) {
            entity.remove();
        }
    }
}
