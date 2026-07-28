package dev.brauw.mapper.tag;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a tag that can be applied to regions.
 * <p>
 * A tag is a <em>matcher</em> across two independent axes:
 * <ul>
 *     <li><b>Region support</b> &mdash; which region names this tag is offered
 *     on. A single tag may support many regions, so they do not have to be
 *     registered one by one (see {@link #supportsRegion(String)}).</li>
 *     <li><b>Value matching</b> &mdash; which concrete tag values this
 *     definition owns. A single definition may match several versions of the
 *     same tag, e.g. {@code level:37} and {@code level:30} (see
 *     {@link #matches(String)}).</li>
 * </ul>
 * This class is abstract and meant to be extended. {@link SimpleTag} matches a
 * single exact value; {@link PatternTag} matches values against a regular
 * expression. Further strategies can be added by subclassing.
 */
public abstract class Tag {

    private final String name;
    private final String usage;
    private final String description;
    private final RegionScope scope;
    private final boolean requiresInput;

    /**
     * Creates a new tag offered on an explicit set of region names.
     *
     * @param name             the identity of the tag, shown in GUIs and used as
     *                         the stored value for exact tags
     * @param usage            a short usage hint shown in commands
     * @param description      a human-readable description shown in commands and GUIs
     * @param supportedRegions the region names this tag is offered on
     * @param requiresInput    whether selecting this tag prompts the player to type
     *                         a concrete value (e.g. {@code level:47}) instead of
     *                         toggling its fixed {@link #name() name}
     */
    protected Tag(String name, String usage, String description, Set<String> supportedRegions, boolean requiresInput) {
        this(name, usage, description, RegionScope.names(supportedRegions), requiresInput);
    }

    /**
     * Creates a new tag offered on whichever regions {@code scope} matches. Prefer this over the
     * name-set form for generic tags, so the tag is registered once per concept rather than once
     * per region name.
     *
     * @param name          the identity of the tag, shown in GUIs and used as the stored value for exact tags
     * @param usage         a short usage hint shown in commands
     * @param description   a human-readable description shown in commands and GUIs
     * @param scope         decides which regions this tag is offered on
     * @param requiresInput whether selecting this tag prompts the player to type a concrete value
     */
    protected Tag(String name, String usage, String description, RegionScope scope, boolean requiresInput) {
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.scope = scope;
        this.requiresInput = requiresInput;
    }

    /**
     * Checks whether a concrete tag value belongs to this definition.
     * <p>
     * For example a {@code level} pattern tag would match both {@code level:37}
     * and {@code level:30}.
     *
     * @param value the concrete tag value to test
     * @return true if the value belongs to this tag, false otherwise
     */
    public abstract boolean matches(String value);

    /**
     * Completes what a player typed into the concrete value this tag stores.
     * <p>
     * The value prompt is already scoped to one tag, so the player types only the value half
     * ({@code 47}) and the {@code level:} half is supplied here. Text that already satisfies
     * {@link #matches(String)} is taken verbatim, which keeps the fully typed form working and
     * leaves tags whose values are not {@code name:value} shaped alone.
     *
     * @param input the raw text typed by the player
     * @return the concrete value to validate and store
     */
    public String completeInput(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return matches(input) ? input : name + ":" + input;
    }

    /**
     * @return the value half of {@link #usage()} (e.g. {@code <number>} for a usage of
     * {@code level:<number>}), which is all the player types into the value prompt.
     */
    public String inputHint() {
        String prefix = name + ":";
        return usage.startsWith(prefix) ? usage.substring(prefix.length()) : usage;
    }

    /**
     * Checks whether this tag is offered on the given region, by asking this tag's
     * {@link RegionScope}. Subclasses may override for matching the scope cannot express.
     *
     * @param regionName the name of the region to test
     * @return true if this tag supports the region, false otherwise
     */
    public boolean supportsRegion(String regionName) {
        return scope.matches(regionName);
    }

    /**
     * @return the scope deciding which regions this tag is offered on
     */
    public RegionScope scope() {
        return scope;
    }

    /**
     * @return an unmodifiable view of the region names this tag is offered on. Empty for pattern
     * and {@link RegionScope#any()} scopes, which match without enumerating.
     */
    public Set<String> supportedRegions() {
        return scope.names();
    }

    /**
     * Whether selecting this tag should prompt the player to type a concrete
     * value (validated by {@link #matches(String)}) rather than toggling its
     * fixed {@link #name() name}.
     *
     * @return true if this tag requires typed input
     */
    public boolean requiresInput() {
        return requiresInput;
    }

    /**
     * @return the identity of the tag, shown in GUIs and used as the stored value for exact tags
     */
    public String name() {
        return name;
    }

    /**
     * @return a short usage hint shown in commands
     */
    public String usage() {
        return usage;
    }

    /**
     * @return a human-readable description shown in commands and GUIs
     */
    public String description() {
        return description;
    }

    /**
     * Tags compare by <em>definition</em>, not identity, so registering the same definition twice is
     * detectable. {@link TagRegistry} relies on this to stay idempotent across module reloads, where
     * callers rebuild their tags from scratch and would otherwise accumulate duplicates.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final Tag tag = (Tag) other;
        return requiresInput == tag.requiresInput
                && Objects.equals(name, tag.name)
                && Objects.equals(usage, tag.usage)
                && Objects.equals(description, tag.description)
                && Objects.equals(scope, tag.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), name, usage, description, scope, requiresInput);
    }
}
