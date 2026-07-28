package dev.brauw.mapper.selection;

import dev.brauw.mapper.gui.GuiManager;
import dev.brauw.mapper.region.CuboidRegion;
import dev.brauw.mapper.region.PathRegion;
import dev.brauw.mapper.region.PerspectiveRegion;
import dev.brauw.mapper.region.PointRegion;
import dev.brauw.mapper.region.PolygonRegion;
import dev.brauw.mapper.region.Region;
import dev.brauw.mapper.region.RegionOptions;
import dev.brauw.mapper.session.EditSession;
import dev.brauw.mapper.tag.TagRegistry;
import dev.brauw.mapper.util.RegionFormat;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.BiFunction;

/**
 * Turns player interactions into regions.
 * <p>
 * In-progress selections (cuboid corners, polygon parts, path waypoints) are per-player, not
 * per-session: several members edit the same world at once, and one builder's half-placed corners
 * are not something anybody else should be able to finish or clobber. Only the finished region is
 * shared, which is why every entry point takes the acting player explicitly rather than reading a
 * single owner off the session.
 */
@RequiredArgsConstructor
public class SelectionHandler {

    private static final double MIN_DISTANCE_TO_INTERACT = 0.4;

    private final GuiManager guiManager;
    private final TagRegistry tagRegistry;
    private final Map<Player, SelectionCorners> selections = new WeakHashMap<>();
    private final Map<Player, List<CuboidRegion>> polygonSelections = new WeakHashMap<>();
    private final Map<Player, List<Location>> pathSelections = new WeakHashMap<>();

    private SelectionCorners getSelection(Player player) {
        return selections.computeIfAbsent(player, key -> new SelectionCorners());
    }

    private List<CuboidRegion> getPolygonSelection(Player player) {
        return polygonSelections.computeIfAbsent(player, key -> new ArrayList<>());
    }

    private List<Location> getPathSelection(Player player) {
        return pathSelections.computeIfAbsent(player, key -> new ArrayList<>());
    }

    public void clearSelections(Player player) {
        selections.remove(player);
        polygonSelections.remove(player);
        pathSelections.remove(player);
    }

    public boolean hasCompleteSelection(Player player) {
        return getSelection(player).isComplete();
    }

    public int getPathPointCount(Player player) {
        return getPathSelection(player).size();
    }

    public int getPolygonPartCount(Player player) {
        return getPolygonSelection(player).size();
    }

    /**
     * Sets the first corner of a cuboid selection at what the player is looking at.
     *
     * @param actor the acting player
     */
    public void setFirstPosition(Player actor) {
        final Location location = getTargetBlock(actor);
        if (location == null) {
            actor.sendMessage(Component.text("Look at a block to set a position.", NamedTextColor.RED));
            return;
        }

        getSelection(actor).setFirstCorner(location);
        actor.sendMessage(Component.text("First position set ", NamedTextColor.GREEN)
                .append(RegionFormat.location(location)));
        actor.playSound(actor.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
    }

    /**
     * Sets the second corner of a cuboid selection at what the player is looking at.
     *
     * @param actor the acting player
     */
    public void setSecondPosition(Player actor) {
        final Location location = getTargetBlock(actor);
        if (location == null) {
            actor.sendMessage(Component.text("Look at a block to set a position.", NamedTextColor.RED));
            return;
        }

        getSelection(actor).setSecondCorner(location);
        actor.sendMessage(Component.text("Second position set ", NamedTextColor.YELLOW)
                .append(RegionFormat.location(location)));
        actor.playSound(actor.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.5f);
    }

    /**
     * Creates a cuboid region from the player's two corners.
     *
     * @param session the session to add the region to
     * @param actor   the acting player
     * @param name    the region name, or {@code null} to prompt for one in the create GUI
     */
    public void createCuboidRegion(EditSession session, Player actor, @Nullable String name) {
        final SelectionCorners selection = getSelection(actor);
        if (!selection.isComplete()) {
            actor.sendMessage(Component.text("You need to set both positions first!", NamedTextColor.RED));
            actor.playSound(actor.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        final Location first = selection.getFirstCorner();
        final Location second = selection.getSecondCorner();
        create(session, actor, name, (regionName, options) -> new CuboidRegion(regionName, first, second, options));
    }

    public void addPolygonChild(Player actor) {
        final SelectionCorners selection = getSelection(actor);
        if (!selection.isComplete()) {
            actor.sendMessage(Component.text("You need to set both positions before adding a polygon part.", NamedTextColor.RED));
            actor.playSound(actor.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        final Location first = selection.getFirstCorner();
        final Location second = selection.getSecondCorner();
        final List<CuboidRegion> children = getPolygonSelection(actor);
        children.add(new CuboidRegion("polygon-child-" + children.size(), first, second));
        selections.remove(actor);

        actor.sendMessage(Component.text("Added polygon part ", NamedTextColor.GREEN)
                .append(Component.text("#" + children.size(), NamedTextColor.YELLOW)));
        actor.playSound(actor.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_STEP, 1.0f, 1.2f);
    }

    /**
     * Creates a polygon region from the parts the player has added.
     *
     * @param session the session to add the region to
     * @param actor   the acting player
     * @param name    the region name, or {@code null} to prompt for one in the create GUI
     */
    public void createPolygonRegion(EditSession session, Player actor, @Nullable String name) {
        final List<CuboidRegion> children = polygonSelections.get(actor);
        if (children == null || children.isEmpty()) {
            actor.sendMessage(Component.text("Add at least one cuboid part before creating a polygon region.", NamedTextColor.RED));
            actor.playSound(actor.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        if (getSelection(actor).isComplete()) {
            actor.sendMessage(Component.text("Finish the current cuboid part first.", NamedTextColor.RED));
            actor.playSound(actor.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        final List<CuboidRegion> snapshot = List.copyOf(children);
        create(session, actor, name, (regionName, options) -> new PolygonRegion(regionName, snapshot, options));
    }

    /**
     * Appends a waypoint to the player's in-progress path, capturing their facing so the point
     * carries a direction as well as a position.
     *
     * @param actor    the acting player
     * @param location the interaction point, or {@code null} when the player clicked open air
     */
    public void addPathPoint(Player actor, @Nullable Location location) {
        // Clicking air yields no interaction point; standing position is the sensible fallback and is
        // usually what a builder wants for a waypoint anyway.
        final Location target = (location == null ? actor.getLocation() : location).clone();
        target.setDirection(actor.getLocation().getDirection());

        final List<Location> points = getPathSelection(actor);
        points.add(target);

        actor.sendMessage(Component.text("Added waypoint ", NamedTextColor.GREEN)
                .append(Component.text("#" + points.size(), NamedTextColor.YELLOW))
                .append(Component.text(" ", NamedTextColor.GRAY))
                .append(RegionFormat.location(target)));
        actor.playSound(actor.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_STEP, 1.0f, 1.2f);
    }

    /**
     * Removes the most recently added waypoint from the player's in-progress path.
     *
     * @param actor the acting player
     */
    public void undoPathPoint(Player actor) {
        final List<Location> points = getPathSelection(actor);
        if (points.isEmpty()) {
            actor.sendMessage(Component.text("No waypoints to undo.", NamedTextColor.RED));
            actor.playSound(actor.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        points.removeLast();
        actor.sendMessage(Component.text("Removed the last waypoint, ", NamedTextColor.YELLOW)
                .append(Component.text(points.size() + " remaining", NamedTextColor.GRAY)));
        actor.playSound(actor.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.4f);
    }

    /**
     * Creates a path region from the waypoints the player has placed, in click order.
     *
     * @param session the session to add the region to
     * @param actor   the acting player
     * @param name    the region name, or {@code null} to prompt for one in the create GUI
     */
    public void createPathRegion(EditSession session, Player actor, @Nullable String name) {
        final List<Location> points = pathSelections.get(actor);
        if (points == null || points.size() < 2) {
            actor.sendMessage(Component.text("Add at least two waypoints before creating a path region.", NamedTextColor.RED));
            actor.playSound(actor.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        final List<Location> snapshot = List.copyOf(points);
        create(session, actor, name, (regionName, options) -> new PathRegion(regionName, snapshot, options));
    }

    /**
     * Creates a point region at the given location, or at the player's feet when they are sneaking.
     *
     * @param session  the session to add the region to
     * @param actor    the acting player
     * @param location the location to place the point at, or {@code null} to use the player's feet
     * @param name     the region name, or {@code null} to prompt for one in the create GUI
     */
    public void createPointRegion(EditSession session, Player actor, @Nullable Location location, @Nullable String name) {
        final Location target = (actor.isSneaking() || location == null ? actor.getLocation() : location).clone();
        target.setYaw(0);
        target.setPitch(0);
        create(session, actor, name, (regionName, options) -> new PointRegion(regionName, target, options));
    }

    /**
     * Creates a perspective region - a point that also records which way the player was facing.
     *
     * @param session  the session to add the region to
     * @param actor    the acting player
     * @param location the location to place the point at, or {@code null} to use the player's feet
     * @param name     the region name, or {@code null} to prompt for one in the create GUI
     */
    public void createPerspectiveRegion(EditSession session, Player actor, @Nullable Location location, @Nullable String name) {
        final Location target = (actor.isSneaking() || location == null ? actor.getLocation() : location).clone();
        target.setDirection(actor.getLocation().getDirection());
        create(session, actor, name, (regionName, options) -> new PerspectiveRegion(regionName, target, options));
    }

    /**
     * Builds a region and adds it to the session, prompting for a name and colour in the create GUI
     * when the caller did not supply a name.
     * <p>
     * The tool path and the command path share this, so a region is announced, validated and has its
     * selection cleared identically either way.
     */
    private void create(EditSession session, Player actor, @Nullable String name,
                        BiFunction<String, RegionOptions, Region> factory) {
        if (name != null) {
            if (!validate(actor, name)) {
                return;
            }
            addAndAnnounce(session, actor, factory.apply(name, RegionOptions.builder().build()));
            clearSelections(actor);
            return;
        }

        guiManager.openRegionCreateGui(actor, (chosenName, options) -> {
            if (!validate(actor, chosenName)) {
                return;
            }
            addAndAnnounce(session, actor, factory.apply(chosenName, options));
            clearSelections(actor);
        }, () -> clearSelections(actor));
    }

    private void addAndAnnounce(EditSession session, Player actor, Region region) {
        session.addRegion(region);
        actor.sendMessage(Component.text("Created ", NamedTextColor.GREEN)
                .append(RegionFormat.describe(region)));
        session.broadcastExcept(actor, Component.text(actor.getName() + " added ", NamedTextColor.GRAY)
                .append(RegionFormat.describe(region)));
    }

    /**
     * Deletes the region the player is looking at.
     *
     * @param session the session to delete from
     * @param actor   the acting player
     */
    public void handleRegionDeletion(EditSession session, Player actor) {
        final Location location = getTargetPoint(actor);
        if (location == null) return;

        findRegionAt(session, location)
                .ifPresentOrElse(region -> deleteRegion(session, actor, region), () -> notifyNoRegion(actor));
    }

    /**
     * Opens the tag editor for the region the player is looking at.
     *
     * @param session the session to search
     * @param actor   the acting player
     */
    public void handleTagEditor(EditSession session, Player actor) {
        final Location location = getTargetPoint(actor);
        if (location == null) return;

        findRegionAt(session, location)
                .ifPresentOrElse(region -> openTagEditor(actor, region), () -> notifyNoRegion(actor));
    }

    /**
     * Finds the region the player is targeting at the given location, resolving overlaps in favour of
     * the most specific (smallest) region. This is what lets a player select a label region nested
     * inside a larger cuboid instead of always grabbing the enclosing cuboid.
     *
     * @param session  the edit session whose regions to search
     * @param location the targeted location
     * @return the most specific region containing the location, if any
     */
    public Optional<Region> findRegionAt(EditSession session, Location location) {
        return session.getRegions().stream()
                .filter(region -> region.contains(location)
                        || (region instanceof PointRegion point && point.getLocation().distance(location) < 0.2))
                .min(Comparator.comparingDouble(this::regionSpecificity));
    }

    /**
     * Computes a specificity score for a region: lower means "more specific" and should win when
     * regions overlap. A point is the most specific thing a player can target; a large cuboid the
     * least.
     *
     * <ul>
     *   <li>{@link PointRegion} / {@link PerspectiveRegion} → 0 (always wins)</li>
     *   <li>{@link CuboidRegion} → its block volume (inclusive bounds)</li>
     *   <li>{@link PolygonRegion} → the volume of its smallest child, since the player is standing
     *       inside exactly one child cuboid</li>
     * </ul>
     *
     * @param region the region to score
     * @return the specificity score; smaller wins
     */
    private double regionSpecificity(Region region) {
        return switch (region) {
            case CuboidRegion cuboid -> cuboidVolume(cuboid);
            case PolygonRegion polygon -> polygon.getChildren().stream()
                    .mapToDouble(this::cuboidVolume)
                    .min()
                    .orElse(Double.MAX_VALUE);
            default -> 0; // PointRegion / PerspectiveRegion: point-like, always most specific
        };
    }

    /**
     * Computes the inclusive block volume of a cuboid. Bounds are inclusive (see
     * {@link CuboidRegion#contains}), so each span gets a +1 and a single block scores 1 rather
     * than 0.
     *
     * @param cuboid the cuboid to measure
     * @return the block volume
     */
    private double cuboidVolume(CuboidRegion cuboid) {
        final Location min = cuboid.getMin();
        final Location max = cuboid.getMax();
        final double width = max.getBlockX() - min.getBlockX() + 1;
        final double height = max.getBlockY() - min.getBlockY() + 1;
        final double depth = max.getBlockZ() - min.getBlockZ() + 1;
        return width * height * depth;
    }

    /**
     * Deletes a region that has already been resolved (e.g. from an entity click or a command),
     * skipping the raytrace and the "no region found" path.
     *
     * @param session the owning edit session
     * @param actor   the acting player
     * @param region  the region to delete
     */
    public void deleteRegion(EditSession session, Player actor, Region region) {
        // A region resolved a moment ago (a pending pick, an open browser) may already be gone -
        // somebody else shares this session. Reporting a delete that did not happen is worse than
        // saying so.
        if (!session.removeRegion(region)) {
            actor.sendMessage(Component.text("That region is already gone.", NamedTextColor.RED));
            actor.playSound(actor.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        actor.sendMessage(Component.text("Deleted ", NamedTextColor.RED)
                .append(RegionFormat.describe(region)));
        actor.playSound(actor.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.0f);
        session.broadcastExcept(actor, Component.text(actor.getName() + " deleted ", NamedTextColor.GRAY)
                .append(RegionFormat.describe(region)));
    }

    /**
     * Opens the tag editor for a region that has already been resolved.
     *
     * @param actor  the acting player
     * @param region the region whose tags to edit
     */
    public void openTagEditor(Player actor, Region region) {
        // Opened unconditionally: even with no tag registered for this region name the editor is useful,
        // since it can write a free-form tag and can remove values already applied.
        guiManager.openTagEditor(actor, region, tagRegistry);
    }

    private void notifyNoRegion(Player player) {
        player.sendMessage(Component.text("No region found at this location.", NamedTextColor.RED));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }

    private static @Nullable Location getTargetBlock(Player player) {
        final RayTraceResult result = rayTrace(player);
        if (result == null || result.getHitBlock() == null) {
            return null;
        }

        return player.isSneaking()
                ? result.getHitBlock().getLocation().toCenterLocation()
                : result.getHitPosition().toLocation(result.getHitBlock().getWorld());
    }

    private static @Nullable Location getTargetPoint(Player player) {
        final RayTraceResult result = rayTrace(player);
        return result == null ? null : result.getHitPosition().toLocation(player.getWorld());
    }

    private static @Nullable RayTraceResult rayTrace(Player player) {
        return player.getWorld().rayTraceBlocks(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)).getValue()
                        + MIN_DISTANCE_TO_INTERACT,
                FluidCollisionMode.NEVER,
                true
        );
    }

    private boolean validate(Player player, String name) {
        if (name == null || name.isEmpty()) {
            player.sendMessage(Component.text("Please enter a name for the region.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return false;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        return true;
    }
}
