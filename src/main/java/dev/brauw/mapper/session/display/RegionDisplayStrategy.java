package dev.brauw.mapper.session.display;

import dev.brauw.mapper.region.Region;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

/**
 * Renders a region to the players editing it.
 * <p>
 * A region has <em>one</em> set of display entities shared by every viewer; visibility is granted
 * per-player with {@code showEntity}/{@code hideEntity} on top of {@code setVisibleByDefault(false)}.
 * That is why only {@link #display} and {@link #hide} take a player - they change who may see the
 * entities. {@link #update} and {@link #revalidate} replace the entities themselves, which affects
 * every viewer at once, so naming a single player there could only mean "and silently break it for
 * everyone else".
 */
public interface RegionDisplayStrategy<T extends Region> {

    /**
     * Shows the region to a player, creating its display entities if this is the first viewer.
     *
     * @param region The region to display.
     * @param player The player to display the region to.
     */
    void display(@NotNull T region, @NotNull Player player);

    /**
     * Rebuilds the region's display entities and re-shows them to every current viewer. Call after
     * something about the region itself changed - its shape, colour or name.
     *
     * @param region The region to rebuild.
     */
    void update(@NotNull T region);

    /**
     * Rebuilds the region's display entities only if they have become invalid, e.g. because a chunk
     * unload destroyed the non-persistent entities, and re-shows them to every current viewer.
     *
     * @param region The region to check.
     */
    void revalidate(@NotNull T region);

    /**
     * Hides the region from a player, destroying its display entities once the last viewer leaves.
     *
     * @param region The region to remove.
     * @param player The player to remove the region from.
     */
    void hide(@NotNull T region, @NotNull Player player);

    /**
     * @return whether a display entity spawned here would actually start tracking. An entity spawned
     * into an unloaded chunk is registered but never becomes {@code isValid()}, so rebuilding one
     * there means rebuilding it again on every sweep for as long as nobody is nearby.
     */
    static boolean canSpawnAt(@NotNull Location location) {
        final World world = location.getWorld();
        return world != null && world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    /**
     * Returns a region's cached display entities, spawning and caching them if it has none.
     * <p>
     * Deliberately not {@link java.util.Map#computeIfAbsent}: spawning an entity runs events through
     * every other listener on the server, and anything that reaches back into a strategy - a chunk
     * loading, a region being hidden - writes to the very cache the mapping function was called
     * from. {@code computeIfAbsent} answers that with a {@link java.util.ConcurrentModificationException}.
     * Spawning outside the map and storing the result afterwards leaves the cache untouched for as
     * long as the spawn is running, so a reentrant write is merely overwritten rather than fatal.
     *
     * @param cache   the strategy's cache of display entities
     * @param region  the region to spawn for
     * @param spawner spawns the display entities for a region with none
     * @return the cached entities, or the freshly spawned ones
     */
    static <T extends Region, V> V spawnIfAbsent(@NotNull Map<T, V> cache, @NotNull T region,
                                                 @NotNull Function<T, V> spawner) {
        final V existing = cache.get(region);
        if (existing != null) {
            return existing;
        }
        final V spawned = spawner.apply(region);
        cache.put(region, spawned);
        return spawned;
    }
}
