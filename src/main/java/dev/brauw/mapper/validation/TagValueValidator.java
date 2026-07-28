package dev.brauw.mapper.validation;

import dev.brauw.mapper.region.Region;
import dev.brauw.mapper.tag.TagRegistry;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks the tag values applied to each region.
 * <p>
 * A malformed value is an error: consumers split on the first colon, so something that is neither a
 * bare marker nor a {@code key:value} pair will be read as a key with no value and silently ignored.
 * A well-formed value that no registered {@link dev.brauw.mapper.tag.Tag} owns is only a warning -
 * it is the expected state for a tag whose plugin has not loaded, and the free-form tag input exists
 * precisely so a builder can get ahead of the code.
 */
@RequiredArgsConstructor
public class TagValueValidator implements RegionValidator {

    private final TagRegistry tagRegistry;

    @Override
    public String name() {
        return "tag-values";
    }

    @Override
    public List<ValidationIssue> validate(World world, List<Region> regions) {
        final List<ValidationIssue> issues = new ArrayList<>();
        for (Region region : regions) {
            for (String value : region.getOptions().getTags()) {
                if (!TagRegistry.isWellFormedValue(value)) {
                    issues.add(ValidationIssue.error(region, "Malformed tag '" + value + "'"));
                } else if (tagRegistry.match(region.getName(), value).isEmpty()) {
                    issues.add(ValidationIssue.warning(region,
                            "Tag '" + value + "' is not defined for " + region.getName()));
                }
            }
        }
        return issues;
    }
}
