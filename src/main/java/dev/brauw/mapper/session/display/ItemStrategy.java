package dev.brauw.mapper.session.display;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.brauw.mapper.region.PointRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
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
 * Renders a glowing item at the region's location.
 */
public class ItemStrategy implements RegionDisplayStrategy<PointRegion> {

    private final Map<PointRegion, ItemDisplay> displays = new HashMap<>();
    private final Map<PointRegion, TextDisplay> labels = new HashMap<>();
    private final Multimap<PointRegion, UUID> viewers = HashMultimap.create();
    private final Material material;
    private final Plugin plugin;

    public ItemStrategy(Plugin plugin, Material material) {
        this.material = material;
        this.plugin = plugin;
    }

    private ItemDisplay getDisplay(PointRegion region) {
        final ItemDisplay existing = displays.get(region);
        if (existing != null && !existing.isValid()) {
            displays.remove(region);
        }

        final Location location = region.getLocation();

        return displays.computeIfAbsent(region, key -> {
            return location.getWorld().spawn(location, ItemDisplay.class, spawned -> {
                spawned.setGlowing(true);
                spawned.setVisibleByDefault(false);
                spawned.setGlowColorOverride(region.getOptions().getColor().getBukkitColor());

                // Item data
                spawned.setItemStack(new ItemStack(material));
                spawned.setTransformation(new Transformation(
                        new Vector3f(),
                        new AxisAngle4f(),
                        new Vector3f(0.3f),
                        new AxisAngle4f()
                ));

                // Important, because we don't want the item to be saved in case the server shuts down
                spawned.setPersistent(false);

                spawned.teleport(spawned.getLocation().setDirection(location.getDirection()));
            });
        });
    }

    private TextDisplay getLabel(PointRegion region) {
        final TextDisplay existingLabel = labels.get(region);
        if (existingLabel != null && !existingLabel.isValid()) {
            labels.remove(region);
        }

        final Location location = region.getLocation().add(0, 1.5, 0);
        final Color color = region.getOptions().getColor().getBukkitColor();

        return labels.computeIfAbsent(region, key -> location.getWorld().spawn(location, TextDisplay.class, spawned -> {
            spawned.text(Component.text(region.getName()).color(TextColor.color(color.getRed(), color.getGreen(), color.getBlue())));
            spawned.setBillboard(Display.Billboard.CENTER);
            spawned.setVisibleByDefault(false);
            spawned.setPersistent(false);
        }));
    }

    @Override
    public void display(@NotNull PointRegion region, @NotNull Player player) {
        // Get or create the item display for this region
        final ItemDisplay itemDisplay = getDisplay(region);

        // Show the item and label to the player
        player.showEntity(plugin, itemDisplay);
        player.showEntity(plugin, getLabel(region));
        viewers.put(region, player.getUniqueId());
    }

    @Override
    public void update(@NotNull PointRegion region) {
        despawn(region);
        final ItemDisplay display = getDisplay(region);
        final TextDisplay label = getLabel(region);
        for (Player viewer : viewersOf(region)) {
            viewer.showEntity(plugin, display);
            viewer.showEntity(plugin, label);
        }
    }

    @Override
    public void revalidate(@NotNull PointRegion region) {
        final List<Player> currentViewers = viewersOf(region);
        if (currentViewers.isEmpty()) {
            return;
        }

        final ItemDisplay entity = displays.get(region);
        final TextDisplay label = labels.get(region);
        if (entity == null || !entity.isValid() || label == null || !label.isValid()) {
            update(region);
            return;
        }

        // See BlockStrategy#revalidate: visibility is re-asserted every pass so a lost grant heals
        // itself instead of leaving a region invisible until the viewer rejoins the session.
        for (Player viewer : currentViewers) {
            viewer.showEntity(plugin, entity);
            viewer.showEntity(plugin, label);
        }
    }

    @Override
    public void hide(@NotNull PointRegion region, @NotNull Player player) {
        if (!viewers.remove(region, player.getUniqueId())) {
            return;
        }

        final ItemDisplay entity = displays.get(region);
        if (entity != null) {
            player.hideEntity(plugin, entity);
        }
        final TextDisplay label = labels.get(region);
        if (label != null) {
            player.hideEntity(plugin, label);
        }

        if (viewers.get(region).isEmpty()) {
            despawn(region);
        }
    }

    /** @return the online players currently viewing this region */
    private List<Player> viewersOf(PointRegion region) {
        final List<Player> online = new ArrayList<>();
        for (UUID viewerId : viewers.get(region)) {
            final Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null) {
                online.add(viewer);
            }
        }
        return online;
    }

    private void despawn(PointRegion region) {
        final ItemDisplay display = displays.remove(region);
        if (display != null && display.isValid()) {
            display.remove();
        }
        final TextDisplay label = labels.remove(region);
        if (label != null && label.isValid()) {
            label.remove();
        }
    }
}
