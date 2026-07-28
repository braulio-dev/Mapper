package dev.brauw.mapper.validation;

import dev.brauw.mapper.region.Region;
import dev.brauw.mapper.tag.TagRegistry;
import lombok.CustomLog;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the checks run against a world's regions before a save.
 * <p>
 * Registration is idempotent by validator name, for the same reason {@link TagRegistry} skips
 * duplicates: a module that rebuilds its checks on reload would otherwise report every problem
 * twice, then three times.
 */
@CustomLog
public class ValidationRegistry {

    private final List<RegionValidator> validators = new ArrayList<>();

    public ValidationRegistry(TagRegistry tagRegistry) {
        register(new RegionNameValidator());
        register(new TagValueValidator(tagRegistry));
        register(new DuplicateRegionValidator());
    }

    /**
     * Registers validators, replacing any already registered under the same {@link RegionValidator#name()}.
     *
     * @param toRegister the validators to add
     */
    public void register(RegionValidator... toRegister) {
        for (RegionValidator validator : toRegister) {
            validators.removeIf(existing -> existing.name().equals(validator.name()));
            validators.add(validator);
        }
    }

    /**
     * Removes validators by name.
     *
     * @param names the validator names to remove
     */
    public void unregister(String... names) {
        for (String name : names) {
            validators.removeIf(existing -> existing.name().equals(name));
        }
    }

    /**
     * Runs every registered validator.
     * <p>
     * A validator that throws is reported as an error against the save rather than allowed to abort
     * it: a broken check is itself a problem worth seeing, and it must not be able to make a world
     * unsaveable.
     *
     * @param world   the world being saved
     * @param regions the regions about to be written
     * @return every issue found, errors first
     */
    public List<ValidationIssue> validate(World world, List<Region> regions) {
        final List<Region> snapshot = List.copyOf(regions);
        final List<ValidationIssue> issues = new ArrayList<>();

        for (RegionValidator validator : List.copyOf(validators)) {
            try {
                issues.addAll(validator.validate(world, snapshot));
            } catch (RuntimeException exception) {
                log.severe("Validator '" + validator.name() + "' failed: " + exception.getMessage());
                issues.add(ValidationIssue.warning(null,
                        "Check '" + validator.name() + "' could not run: " + exception.getMessage()));
            }
        }

        issues.sort((left, right) -> Boolean.compare(right.isError(), left.isError()));
        return issues;
    }
}
