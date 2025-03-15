package dev.brauw.mapper.selection;

import dev.brauw.mapper.gui.GuiManager;
import dev.brauw.mapper.region.CuboidRegion;
import dev.brauw.mapper.region.PointRegion;
import dev.brauw.mapper.region.PerspectiveRegion;
import dev.brauw.mapper.region.RegionOptions;
import dev.brauw.mapper.session.EditSession;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * Handles the selection and creation of regions based on player interactions.
 * This class manages the selection corners for cuboid regions and provides
 * methods for creating different types of regions (Cuboid, Point, Perspective).
 */
@RequiredArgsConstructor
public class SelectionHandler {

    private final GuiManager guiManager;
    private final Map<Player, SelectionCorners> selections = new WeakHashMap<>();

    /**
     * Retrieves the SelectionCorners object for a given player.
     * If the player does not have a SelectionCorners object yet, it creates a new one.
     *
     * @param player The player for whom to retrieve the SelectionCorners.
     * @return The SelectionCorners object for the player.
     */
    private SelectionCorners getSelection(Player player) {
        return selections.computeIfAbsent(player, key -> new SelectionCorners());
    }

    /**
     * Sets the first position for a cuboid region selection.
     * This method is called when a player interacts with the world to define the first corner of a region.
     *
     * @param session The EditSession for the player.
     * @param event   The PlayerInteractEvent containing information about the interaction.
     */
    public void setFirstPosition(EditSession session, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location location = getTargetPoint(player);
        if (location == null) return;

        getSelection(player).setFirstCorner(location);

        player.sendMessage(Component.text("First position set ", NamedTextColor.GREEN)
                .append(formatLocation(location)));
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
    }

    /**
     * Sets the second position for a cuboid region selection.
     * This method is called when a player interacts with the world to define the second corner of a region.
     *
     * @param session The EditSession for the player.
     * @param event   The PlayerInteractEvent containing information about the interaction.
     */
    public void setSecondPosition(EditSession session, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location location = getTargetPoint(player);
        if (location == null) return;

        getSelection(player).setSecondCorner(location);

        player.sendMessage(Component.text("Second position set ", NamedTextColor.YELLOW)
                .append(formatLocation(location)));
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.5f);
    }

    /**
     * Creates a cuboid region based on the selected positions.
     * This method checks if both positions have been set and then opens the GUI
     * to create the region with a name and options.
     *
     * @param session The EditSession for the player.
     */
    public void createCuboidRegion(EditSession session) {
        Player player = session.getOwner();

        final SelectionCorners selection = getSelection(player);
        if (!selection.isComplete()) {
            player.sendMessage(Component.text("You need to set both positions first!", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        final Location first = selection.getFirstCorner();
        final Location second = selection.getSecondCorner();
        player.sendMessage(Component.text("Creating cuboid region...", NamedTextColor.YELLOW));
        guiManager.openRegionCreateGui(session, (name, options) -> {
            if (!validate(player, name, options)) {
                return;
            }

            CuboidRegion region = new CuboidRegion(name, first, second, options);
            session.addRegion(region);

            // Clear selections after creating region
            selections.remove(player);
        }, () -> {
            // Clear selections if GUI is closed
            selections.remove(player);
        });
    }

    /**
     * Creates a point region at the specified location.
     * This method opens the GUI to create the region with a name and options.
     *
     * @param session  The EditSession for the player.
     * @param location The location at which to create the point region.
     */
    public void createPointRegion(EditSession session, Location location) {
        final Player player = session.getOwner();
        final Location target = player.isSneaking() ? player.getLocation() : location;

        if (target == null) {
            return;
        }

        guiManager.openRegionCreateGui(session, (name, options) -> {
            if (!validate(session.getOwner(), name, options)) {
                return;
            }

            PointRegion region = new PointRegion(name, target, options);
            session.addRegion(region);
        }, () -> {
            // Clear selections if GUI is closed
            selections.remove(session.getOwner());
        });
    }

    /**
     * Creates a perspective region at the specified location.
     * This method opens the GUI to create the region with a name and options.
     *
     * @param session  The EditSession for the player.
     * @param location The location at which to create the perspective region.
     */
    public void createPerspectiveRegion(EditSession session, Location location) {
        final Player player = session.getOwner();
        final Location target = player.isSneaking() ? player.getLocation() : location;

        if (target == null) {
            return;
        }

        target.setDirection(player.getLocation().getDirection());
        guiManager.openRegionCreateGui(session, (name, options) -> {
            if (!validate(player, name, options)) {
                return;
            }

            PerspectiveRegion region = new PerspectiveRegion(name, target, options);
            session.addRegion(region);
        }, () -> {
            // Clear selections if GUI is closed
            selections.remove(player);
        });
    }

    /**
     * Handles the deletion of a region based on player interaction.
     * This method ray traces to find the block the player is looking at and deletes
     * the region at that location, if one exists.
     *
     * @param session The EditSession for the player.
     * @param event   The PlayerInteractEvent containing information about the interaction.
     */
    public void handleRegionDeletion(EditSession session, PlayerInteractEvent event) {
        Player player = session.getOwner();
        final Location location = getTargetPoint(player);
        if (location == null) return;

        session.getRegions().stream()
                .filter(region -> region.contains(location) || (region instanceof PointRegion point) && point.getLocation().distance(location) < 0.2)
                .findFirst()
                .ifPresentOrElse(
                        region -> {
                            session.removeRegion(region);
                            player.sendMessage(Component.text("Region deleted: ", NamedTextColor.RED)
                                    .append(Component.text(region.getName(), NamedTextColor.DARK_RED)));
                            player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.0f);
                        },
                        () -> {
                            player.sendMessage(Component.text("No region found at this location.", NamedTextColor.RED));
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                        }
                );
    }

    private static @Nullable Location getTargetPoint(Player player) {
        RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)).getValue() + 0.2,
                FluidCollisionMode.NEVER,
                true
        );

        if (result == null) {
            return null;
        }

        return result.getHitPosition().toLocation(player.getWorld());
    }

    /**
     * Formats a Location object into a Component for displaying coordinates.
     *
     * @param loc The Location to format.
     * @return A Component containing the formatted location coordinates.
     */
    private Component formatLocation(Location loc) {
        return Component.text(String.format("(%.1f, %.1f, %.1f)",
                loc.getX(), loc.getY(), loc.getZ()), NamedTextColor.GRAY);
    }

    private boolean validate(Player player, String name, RegionOptions regionOptions) {
        if (name == null || name.isEmpty()) {
            player.sendMessage(Component.text("Please enter a name for the region.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return false;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        return true;
    }
}