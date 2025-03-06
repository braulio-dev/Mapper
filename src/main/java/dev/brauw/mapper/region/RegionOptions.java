package dev.brauw.mapper.region;

import lombok.Builder;
import lombok.Value;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the options for a region.
 */
@Builder
@Value
public class RegionOptions {

    /**
     * The color of the region.
     */
    @Builder.Default
    @NotNull Color color = Color.WHITE;

}
