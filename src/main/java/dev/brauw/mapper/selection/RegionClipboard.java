package dev.brauw.mapper.selection;

import dev.brauw.mapper.region.Region;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * What each player has copied, ready to be pasted somewhere else.
 * <p>
 * Per-player, like the in-progress selections: several members edit one world at once, and taking a
 * copy is a private step on the way to placing something, not a change to what everyone can see.
 * <p>
 * The stored value is the source region itself. Regions are immutable, so the snapshot stays valid
 * even after the original is moved or deleted - which is what a clipboard should do, and means a
 * builder can copy something, remove it, and put it back somewhere better.
 */
public class RegionClipboard {

    private final Map<UUID, Region> copied = new HashMap<>();

    /**
     * Stores a region as this player's clipboard contents, replacing whatever was there.
     *
     * @param player the copying player
     * @param region the region to remember
     */
    public void copy(Player player, Region region) {
        copied.put(player.getUniqueId(), region);
    }

    /**
     * @return what this player last copied, or {@code null} if they have copied nothing
     */
    public @Nullable Region get(Player player) {
        return copied.get(player.getUniqueId());
    }

    public boolean isEmpty(Player player) {
        return !copied.containsKey(player.getUniqueId());
    }

    public void clear(Player player) {
        copied.remove(player.getUniqueId());
    }
}
