package dev.brauw.mapper.region;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the options for a region.
 */
@Builder
@Jacksonized
@Value
public class RegionOptions {

    /**
     * The color of the region.
     */
    @Builder.Default
    @NotNull RegionColor color = RegionColor.WHITE;

}
