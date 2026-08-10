package dev.brauw.mapper.region;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

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

    @Builder.Default
    @NotNull Set<String> tags = new HashSet<>();

    /**
     * A detached copy, holding its own tag set.
     * <p>
     * The tag editor mutates {@link #tags} in place rather than rebuilding the region, so two
     * regions sharing one options instance would be tagged together. Anything that produces a
     * second region from an existing one takes a copy so the two can be edited apart.
     *
     * @return an equal options with an independent tag set
     */
    public RegionOptions copy() {
        return RegionOptions.builder()
                .color(color)
                .tags(new HashSet<>(tags))
                .build();
    }

}
