package dev.brauw.mapper.session.display;

import dev.brauw.mapper.region.Region;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
}
