package dev.brauw.mapper.validation;

import dev.brauw.mapper.region.Region;
import dev.brauw.mapper.region.RegionTransform;
import dev.brauw.mapper.util.RegionFormat;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Finds regions that are almost certainly accidents.
 * <p>
 * Two regions sharing a name is normal and intended - a world has many {@code npc_resident} points.
 * Two sharing a name <em>and</em> a position is not: it is a double-click, and it produces a
 * datapoint that a consumer will process twice. A repeated id is worse, because whichever one is
 * read second wins and the other is invisible.
 */
public class DuplicateRegionValidator implements RegionValidator {

    @Override
    public String name() {
        return "duplicates";
    }

    @Override
    public List<ValidationIssue> validate(World world, List<Region> regions) {
        final List<ValidationIssue> issues = new ArrayList<>();
        final Map<String, Region> byPosition = new HashMap<>();
        final Set<UUID> seenIds = new HashSet<>();

        for (Region region : regions) {
            if (!seenIds.add(region.getId())) {
                issues.add(ValidationIssue.error(region, "Duplicate region id " + region.getId()));
            }

            final String key = region.getName() + "@" + RegionFormat.coordinates(RegionTransform.anchor(region));
            final Region previous = byPosition.putIfAbsent(key, region);
            if (previous != null) {
                issues.add(ValidationIssue.warning(region,
                        "Sits in the same place as another '" + region.getName() + "'"));
            }
        }
        return issues;
    }
}
