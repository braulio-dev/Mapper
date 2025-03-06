package dev.brauw.mapper.session.display;

import dev.brauw.mapper.region.PolygonRegion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Renders a polygon region to the player.
 * <p>
 * Delegates to the {@link BlockStrategy} to render the polygon as a cuboid.
 */
public class PolygonStrategy implements RegionDisplayStrategy<PolygonRegion> {

    private final BlockStrategy blockStrategy;

    public PolygonStrategy(BlockStrategy blockStrategy) {
        this.blockStrategy = blockStrategy;
    }

    @Override
    public void display(@NotNull PolygonRegion region, @NotNull Player player) {
        region.getChildren().forEach(child -> blockStrategy.display(child, player));
    }

    @Override
    public void update(@NotNull PolygonRegion region, @NotNull Player player) {
        region.getChildren().forEach(child -> blockStrategy.update(child, player));
    }

    @Override
    public void hide(@NotNull PolygonRegion region, @NotNull Player player) {
        region.getChildren().forEach(child -> blockStrategy.hide(child, player));
    }
}
