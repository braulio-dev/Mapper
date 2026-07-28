package dev.brauw.mapper.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Holds the set of known {@link Tag} definitions.
 * <p>
 * Tags are stored as a flat list; the region they apply to is decided by each
 * tag via {@link Tag#supportsRegion(String)} rather than by a registry key. This
 * means a single tag can cover many regions and does not have to be registered
 * region by region.
 */
public class TagRegistry {

    private final List<Tag> tags = new ArrayList<>();

    /**
     * Registers tag definitions, skipping any that are already registered.
     * <p>
     * The skip makes registration idempotent: a module that rebuilds its tags on every reload can
     * re-register freely without accumulating duplicates (which would surface as repeated buttons in
     * the tag editor). {@link Tag} compares by definition rather than identity, so a freshly
     * constructed but equivalent tag is recognised as already present.
     *
     * @param tags the tags to register
     */
    public void register(Tag... tags) {
        for (Tag tag : tags) {
            if (!this.tags.contains(tag)) {
                this.tags.add(tag);
            }
        }
    }

    /**
     * Removes tag definitions. Pass the same definitions that were registered - matching is by
     * definition, not identity, so the caller does not have to retain the original instances.
     *
     * @param tags the tags to remove
     */
    public void unregister(Tag... tags) {
        this.tags.removeAll(List.of(tags));
    }

    /**
     * Removes every registered tag definition.
     */
    public void clear() {
        this.tags.clear();
    }

    /**
     * Returns all tags offered on the given region.
     *
     * @param regionName the region to look up
     * @return the tags supporting the region, in registration order
     */
    public List<Tag> getTags(String regionName) {
        return tags.stream()
                .filter(tag -> tag.supportsRegion(regionName))
                .toList();
    }

    /**
     * Checks whether any tag is offered on the given region.
     *
     * @param regionName the region to look up
     * @return true if at least one tag supports the region
     */
    public boolean hasTags(String regionName) {
        return tags.stream().anyMatch(tag -> tag.supportsRegion(regionName));
    }

    /**
     * Resolves a concrete tag value applied on a region to its definition.
     *
     * @param regionName the region the value is applied on
     * @param value      the concrete tag value (e.g. {@code level:37})
     * @return the matching tag definition, if any
     */
    public Optional<Tag> match(String regionName, String value) {
        return tags.stream()
                .filter(tag -> tag.supportsRegion(regionName) && tag.matches(value))
                .findFirst();
    }
}
