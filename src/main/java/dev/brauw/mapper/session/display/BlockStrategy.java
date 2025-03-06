package dev.brauw.mapper.session.display;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.brauw.mapper.MapperPlugin;
import dev.brauw.mapper.region.CuboidRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Renders a glowing block at the cuboid's location.
 */
public class BlockStrategy implements RegionDisplayStrategy<CuboidRegion> {

    private final Map<CuboidRegion, BlockDisplay> displays = new HashMap<>();
    private final Multimap<CuboidRegion, UUID> viewers = ArrayListMultimap.create();
    private final MapperPlugin plugin;

    public BlockStrategy(MapperPlugin plugin) {
        this.plugin = plugin;
    }

    private BlockDisplay getDisplay(CuboidRegion region) {
        final Location min = region.getMin();
        final Location max = region.getMax();
        final Location center = min.clone().add(
                (max.getX() - min.getX()) / 2,
                (max.getY() - min.getY()) / 2,
                (max.getZ() - min.getZ()) / 2
        );

        final float widthX = (float) Math.abs(max.getX() - min.getX());
        final float widthY = (float) Math.abs(max.getY() - min.getY());
        final float widthZ = (float) Math.abs(max.getZ() - min.getZ());

        return displays.computeIfAbsent(region, key -> {
            return center.getWorld().spawn(center, BlockDisplay.class, spawned -> {
                spawned.setGlowing(true);
                spawned.setVisibleByDefault(false);
                spawned.setGlowColorOverride(region.getOptions().getColor());

                // Block data
                spawned.setBlock(Material.TINTED_GLASS.createBlockData());
                spawned.setTransformation(new Transformation(
                        new Vector3f(widthX / 2, widthY / 2, widthZ / 2).mul(-1),
                        new AxisAngle4f(),
                        new Vector3f(widthX, widthY, widthZ).add(0.01f, 0.01f, 0.01f),
                        new AxisAngle4f()
                ));

                // Important, because we don't want the display to be saved in case the server shuts down
                spawned.setPersistent(false);
            });
        });
    }

    @Override
    public void display(@NotNull CuboidRegion region, @NotNull Player player) {
        // Get or create the block display for this region
        final BlockDisplay blockDisplay = getDisplay(region);

        // Show the display to the player
        player.showEntity(plugin, blockDisplay);
        viewers.put(region, player.getUniqueId());
    }

    @Override
    public void update(@NotNull CuboidRegion region, @NotNull Player player) {
        final BlockDisplay removed = displays.remove(region);
        if (removed != null && removed.isValid()) {
            removed.remove();
        }

        display(region, player);
    }

    @Override
    public void hide(@NotNull CuboidRegion region, @NotNull Player player) {
        final UUID playerUUID = player.getUniqueId();
        if (viewers.remove(region, playerUUID)) {
            // Hide entity if we could remove them as viewers
            final BlockDisplay entity = Objects.requireNonNull(displays.get(region));
            player.hideEntity(plugin, entity);

            // If there are no more viewers, remove the display
            if (viewers.get(region).isEmpty()) {
                final BlockDisplay blockDisplay = displays.remove(region);
                if (blockDisplay != null && blockDisplay.isValid()) {
                    blockDisplay.remove();
                }
            }
        }
    }
}
