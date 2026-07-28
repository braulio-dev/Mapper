package dev.brauw.mapper.validation;

import dev.brauw.mapper.region.Region;
import org.bukkit.World;

import java.util.List;

/**
 * Checks a world's regions for problems before they are written to disk.
 * <p>
 * Mapper only knows the rules that hold for any map - a region needs a name, a tag value needs a
 * shape. The interesting rules belong to whoever consumes the file: that an {@code npc_route} needs
 * an {@code order}, that a {@code route:} must name a resident that exists. Those consumers register
 * their own validators here, so a mistake surfaces at save time next to the region that caused it
 * rather than hours later as a stack trace during world load.
 */
public interface RegionValidator {

    /**
     * @return a short name for this validator, shown when it reports something
     */
    String name();

    /**
     * Checks a complete set of regions. Called once per save with everything in the session, so
     * cross-region rules (unique ids, dangling references) are expressible.
     *
     * @param world   the world being saved
     * @param regions every region about to be written, unmodifiable
     * @return the problems found, or an empty list
     */
    List<ValidationIssue> validate(World world, List<Region> regions);
}
