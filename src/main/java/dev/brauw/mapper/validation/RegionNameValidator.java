package dev.brauw.mapper.validation;

import dev.brauw.mapper.region.Region;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Rejects regions a consumer could not look up.
 * <p>
 * The region name is the only thing a consuming plugin matches on - it is what decides whether a
 * datapoint is a resource node, an NPC or a spawn. A blank or whitespace name therefore does not
 * produce a mislabelled region, it produces one that no code will ever find.
 */
public class RegionNameValidator implements RegionValidator {

    @Override
    public String name() {
        return "region-names";
    }

    @Override
    public List<ValidationIssue> validate(World world, List<Region> regions) {
        final List<ValidationIssue> issues = new ArrayList<>();
        for (Region region : regions) {
            final String regionName = region.getName();
            if (regionName == null || regionName.isBlank()) {
                issues.add(ValidationIssue.error(region, "Region has no name"));
            } else if (!regionName.equals(regionName.trim())) {
                issues.add(ValidationIssue.warning(region,
                        "Name '" + regionName + "' has leading or trailing spaces"));
            }
        }
        return issues;
    }
}
