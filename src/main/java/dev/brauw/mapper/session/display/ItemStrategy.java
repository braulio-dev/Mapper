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
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

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

    private ItemDisplay getDisplay(PointRegion region) {
        final Location location = region.getLocation();

        return displays.computeIfAbsent(region, key -> {
            return location.getWorld().spawn(location, ItemDisplay.class, spawned -> {
                spawned.setGlowing(true);
                spawned.setVisibleByDefault(false);
                spawned.setGlowColorOverride(region.getOptions().getColor());

                // Item data
                spawned.setItemStack(new ItemStack(Material.SEA_LANTERN));
                spawned.setTransformation(new Transformation(
                        new Vector3f(),
                        new AxisAngle4f(),
                        new Vector3f(0.3f),
                        new AxisAngle4f()
                ));

                // Important, because we don't want the item to be saved in case the server shuts down
                spawned.setPersistent(false);
            });
        });
    }

    @Override
    public void display(@NotNull PointRegion region, @NotNull Player player) {
        // Get or create the item display for this region
        final ItemDisplay itemDisplay = getDisplay(region);

        // Show the item to the player
        player.showEntity(plugin, itemDisplay);
        viewers.put(region, player.getUniqueId());
    }

    @Override
    public void update(@NotNull PointRegion region, @NotNull Player player) {
        final ItemDisplay removed = displays.remove(region);
        if (removed != null && removed.isValid()) {
            removed.remove();
        }
        display(region, player);
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
