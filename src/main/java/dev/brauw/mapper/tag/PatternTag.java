package dev.brauw.mapper.tag;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * A tag that matches many concrete values against a regular expression.
 * <p>
 * For example a pattern of {@code level:\d+} owns both {@code level:37} and
 * {@code level:30}, treating them as versions of the same {@code level} tag.
 */
public class PatternTag extends Tag {

    private final Pattern pattern;

    /**
     * Creates a pattern tag offered on the given regions.
     *
     * @param name             the display identity of the tag (e.g. {@code level})
     * @param pattern          the regular expression matched against tag values
     * @param usage            a short usage hint shown in commands (e.g. {@code level:<number>})
     * @param description      a human-readable description
     * @param requiresInput    whether selecting this tag prompts for a typed value
     * @param supportedRegions the region names this tag is offered on
     */
    public PatternTag(String name, String pattern, String usage, String description, boolean requiresInput, Set<String> supportedRegions) {
        super(name, usage, description, supportedRegions, requiresInput);
        this.pattern = Pattern.compile(pattern);
    }

    /**
     * Creates a pattern tag offered on whichever regions {@code scope} matches.
     *
     * @param name          the display identity of the tag (e.g. {@code order})
     * @param pattern       the regular expression matched against tag values
     * @param usage         a short usage hint shown in commands (e.g. {@code order:<number>})
     * @param description   a human-readable description
     * @param requiresInput whether selecting this tag prompts for a typed value
     * @param scope         decides which regions this tag is offered on
     */
    public PatternTag(String name, String pattern, String usage, String description, boolean requiresInput, RegionScope scope) {
        super(name, usage, description, scope, requiresInput);
        this.pattern = Pattern.compile(pattern);
    }

    /**
     * Convenience constructor accepting the supported regions as varargs. Pattern
     * tags prompt for a typed value by default, since a concrete value is needed.
     */
    public PatternTag(String name, String pattern, String usage, String description, String... supportedRegions) {
        this(name, pattern, usage, description, true, Set.of(supportedRegions));
    }

    /**
     * @return the compiled pattern matched against tag values
     */
    public Pattern pattern() {
        return pattern;
    }

    @Override
    public boolean matches(String value) {
        // Anchored match: the whole value must satisfy the pattern, so "level:\d+"
        // owns "level:37" and "level:30" but rejects "level:37x" or "xlevel:30".
        return value != null && pattern.matcher(value).matches();
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && pattern.pattern().equals(((PatternTag) other).pattern.pattern());
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + pattern.pattern().hashCode();
    }
}
