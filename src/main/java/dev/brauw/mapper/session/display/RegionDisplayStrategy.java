package dev.brauw.mapper.session.display;

import dev.brauw.mapper.region.Region;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Marker interface to display a region to the user.
 */
public interface RegionDisplayStrategy<T extends Region> {

    /**
     * Display the region to the player.
     * @param region The region to display.
     * @param player The player to display the region to.
     */
    void display(@NotNull T region, @NotNull Player player);

    /**
     * Updates the region display. This function is called every tick, for
     * every player, for every region.
     * @param region The region to update.
     * @param player The player to update the region for.
     */
    void update(@NotNull T region, @NotNull Player player);

    /**
     * Remove the region from the player's display.
     * @param region The region to remove.
     * @param player The player to remove the region from.
     */
    void hide(@NotNull T region, @NotNull Player player);
}
