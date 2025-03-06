package dev.brauw.mapper.session.display;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.brauw.mapper.MapperPlugin;
import dev.brauw.mapper.region.PointRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Renders a glowing item at the region's location.
 */
public class ItemStrategy implements RegionDisplayStrategy<PointRegion> {

    private final Map<PointRegion, ItemDisplay> displays = new HashMap<>();
    private final Multimap<PointRegion, UUID> viewers = ArrayListMultimap.create();
    private final MapperPlugin plugin;

    public ItemStrategy(MapperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void display(@NotNull PointRegion region, @NotNull Player player) {
        final Location location = region.getLocation();

        // Get or create the item display for this region
        final ItemDisplay itemDisplay = displays.computeIfAbsent(region, key -> {
            return location.getWorld().spawn(location, ItemDisplay.class, spawned -> {
                spawned.setGlowing(true);
                spawned.setVisibleByDefault(false);
                spawned.setGlowColorOverride(region.getOptions().getColor());

                // Item data
                spawned.setItemStack(new ItemStack(Material.SEA_LANTERN));

                // Important, because we don't want the item to be saved in case the server shuts down
                spawned.setPersistent(false);
            });
        });

        // Show the item to the player
        player.showEntity(plugin, itemDisplay);
        viewers.put(region, player.getUniqueId());
    }

    @Override
    public void update(@NotNull PointRegion region, @NotNull Player player) {
        // ignored, items wont do anything each tick
    }

    @Override
    public void hide(@NotNull PointRegion region, @NotNull Player player) {
        final UUID playerUUID = player.getUniqueId();
        if (viewers.remove(region, playerUUID)) {
            // Hide entity if we could remove them as viewers
            final ItemDisplay entity = Objects.requireNonNull(displays.get(region));
            player.hideEntity(plugin, entity);

            // If there are no more viewers, remove the display
            if (viewers.get(region).isEmpty()) {
                final ItemDisplay itemDisplay = displays.remove(region);
                if (itemDisplay != null && itemDisplay.isValid()) {
                    itemDisplay.remove();
                }
            }
        }
    }
}
