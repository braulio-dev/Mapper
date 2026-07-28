package dev.brauw.mapper.tag;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Decides which regions a {@link Tag} is offered on.
 * <p>
 * Naming every region up front ({@link #names}) works when the set is short and known at registration
 * time. It stops working when a tag is generic - an {@code order} or {@code label} tag applies to whole
 * families of regions whose names a plugin may not know until its own content is parsed. {@link #pattern}
 * and {@link #any} cover those cases so a tag can be registered once per concept rather than once per
 * region name.
 *
 * <pre>{@code
 * RegionScope.names("npc_waypoint", "npc_resident")  // exact, case-insensitive
 * RegionScope.pattern("npc_.*")                      // every region whose name starts with npc_
 * RegionScope.any()                                  // every region
 * }</pre>
 */
public final class RegionScope {

    private static final RegionScope ANY = new RegionScope(Set.of(), null, true);

    private final Set<String> names;
    private final Pattern pattern;
    private final boolean any;

    private RegionScope(Set<String> names, Pattern pattern, boolean any) {
        this.names = names;
        this.pattern = pattern;
        this.any = any;
    }

    /**
     * @return a scope matching every region, whatever its name
     */
    public static RegionScope any() {
        return ANY;
    }

    /**
     * @param names the region names this scope covers, matched case-insensitively
     * @return a scope matching exactly those names
     */
    public static RegionScope names(Set<String> names) {
        final Set<String> lowered = new HashSet<>(names.size());
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                lowered.add(name.toLowerCase());
            }
        }
        return new RegionScope(Collections.unmodifiableSet(lowered), null, false);
    }

    /** @see #names(Set) */
    public static RegionScope names(String... names) {
        return names(Set.of(names));
    }

    /**
     * @param regex a regular expression matched (anchored, case-insensitively) against the region name
     * @return a scope matching every region whose name satisfies the expression
     */
    public static RegionScope pattern(String regex) {
        return new RegionScope(Set.of(), Pattern.compile(regex, Pattern.CASE_INSENSITIVE), false);
    }

    /**
     * @param regionName the region name to test
     * @return true if a tag with this scope should be offered on that region
     */
    public boolean matches(String regionName) {
        if (any) {
            return true;
        }
        if (regionName == null) {
            return false;
        }
        if (pattern != null) {
            return pattern.matcher(regionName).matches();
        }
        return names.contains(regionName.toLowerCase());
    }

    /**
     * @return the explicit region names this scope covers, lower-cased. Empty for pattern and
     * {@link #any()} scopes, which do not enumerate.
     */
    public Set<String> names() {
        return names;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RegionScope scope)) {
            return false;
        }
        return any == scope.any
                && names.equals(scope.names)
                && Objects.equals(patternText(), scope.patternText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(any, names, patternText());
    }

    private String patternText() {
        return pattern == null ? null : pattern.pattern();
    }
}
