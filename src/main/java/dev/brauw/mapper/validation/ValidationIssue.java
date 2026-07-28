package dev.brauw.mapper.validation;

import dev.brauw.mapper.region.Region;
import lombok.Value;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

/**
 * One problem found in a world's regions.
 * <p>
 * An issue names the region it is about wherever it can, because the point of validating at save
 * time is to be able to go and look at the thing that is wrong.
 */
@Value
public class ValidationIssue {

    Severity severity;

    /** The offending region, or {@code null} for an issue about the set as a whole. */
    @Nullable Region region;

    String message;

    public static ValidationIssue error(@Nullable Region region, String message) {
        return new ValidationIssue(Severity.ERROR, region, message);
    }

    public static ValidationIssue warning(@Nullable Region region, String message) {
        return new ValidationIssue(Severity.WARNING, region, message);
    }

    public boolean isError() {
        return severity == Severity.ERROR;
    }

    /**
     * How much a problem matters.
     */
    public enum Severity {

        /** Would produce a file a consumer cannot use. Blocks the save unless it is forced. */
        ERROR(NamedTextColor.RED, "Error"),

        /** Probably a mistake, but readable. Reported and then saved anyway. */
        WARNING(NamedTextColor.YELLOW, "Warning");

        private final NamedTextColor color;
        private final String label;

        Severity(NamedTextColor color, String label) {
            this.color = color;
            this.label = label;
        }

        public NamedTextColor getColor() {
            return color;
        }

        public String getLabel() {
            return label;
        }
    }
}
