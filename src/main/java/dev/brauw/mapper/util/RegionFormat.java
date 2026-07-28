package dev.brauw.mapper.util;

import dev.brauw.mapper.region.Region;
import dev.brauw.mapper.region.RegionTransform;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

import java.util.List;
import java.util.Set;

/**
 * Renders regions for chat and item lore.
 * <p>
 * Region names are not unique - a world holds dozens of {@code npc_resident} points - so anything
 * that names a region without also placing it is ambiguous. Every helper here carries the
 * coordinates for that reason.
 */
public final class RegionFormat {

    private RegionFormat() {
    }

    /** @return {@code (12.5, 64.0, -30.5)} */
    public static Component location(Location location) {
        return Component.text(coordinates(location), NamedTextColor.GRAY);
    }

    /** @return {@code 12.5, 64.0, -30.5}, without brackets or colour */
    public static String coordinates(Location location) {
        return String.format("%.1f, %.1f, %.1f", location.getX(), location.getY(), location.getZ());
    }

    /** @return the coordinates of a region's anchor, e.g. a cuboid's centre */
    public static String coordinates(Region region) {
        return coordinates(RegionTransform.anchor(region));
    }

    /**
     * @return {@code #dock_spawn (12.5, 64.0, -30.5)} - the shortest form that still says which of
     * several same-named regions this one is
     */
    public static Component describe(Region region) {
        return Component.text("#" + region.getName(), NamedTextColor.YELLOW)
                .append(Component.text(" (" + coordinates(region) + ")", NamedTextColor.GRAY));
    }

    /**
     * @return {@code #dock_spawn} with the type and coordinates on either side, for list rows where
     * the type is worth the width
     */
    public static Component describeWithType(Region region) {
        return Component.text("#" + region.getName(), NamedTextColor.YELLOW)
                .append(Component.text(" " + region.getType().name().toLowerCase(), NamedTextColor.DARK_GRAY))
                .append(Component.text(" (" + coordinates(region) + ")", NamedTextColor.GRAY));
    }

    /** @return the region's applied tags as a comma-joined component, or an empty component */
    public static Component tags(Region region) {
        final Set<String> applied = region.getOptions().getTags();
        if (applied.isEmpty()) {
            return Component.empty();
        }

        final List<Component> parts = applied.stream()
                .sorted()
                .map(tag -> Component.text("#" + tag, NamedTextColor.AQUA))
                .map(Component.class::cast)
                .toList();
        return Component.join(JoinConfiguration.commas(true), parts);
    }
}
