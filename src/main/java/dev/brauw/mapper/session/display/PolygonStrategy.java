package dev.brauw.mapper.session.display;

import dev.brauw.mapper.region.PolygonRegion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Renders a polygon region to the player.
 * <p>
 * Delegates to the {@link CuboidStrategy} to render the polygon as a cuboid.
 */
public class PolygonStrategy implements RegionDisplayStrategy<PolygonRegion> {

    private final CuboidStrategy cuboidStrategy;

    public PolygonStrategy(CuboidStrategy cuboidStrategy) {
        this.cuboidStrategy = cuboidStrategy;
    }

    @Override
    public void display(@NotNull PolygonRegion region, @NotNull Player player) {
        region.getChildren().forEach(child -> cuboidStrategy.display(child, player));
    }

    @Override
    public void update(@NotNull PolygonRegion region, @NotNull Player player) {
        region.getChildren().forEach(child -> cuboidStrategy.update(child, player));
    }

    @Override
    public void hide(@NotNull PolygonRegion region, @NotNull Player player) {
        region.getChildren().forEach(child -> cuboidStrategy.hide(child, player));
    }
}
