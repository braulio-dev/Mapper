package dev.brauw.mapper.tag;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagInputTest {

    private static PatternTag levelTag() {
        return new PatternTag("level", "level:\\d+", "level:<number>", "Required level", true, Set.of("dock"));
    }

    @Test
    void prependsTheTagNameToAValueTypedOnItsOwn() {
        PatternTag tag = levelTag();

        assertEquals("level:47", tag.completeInput("47"));
        assertTrue(tag.matches(tag.completeInput("47")));
    }

    @Test
    void keepsAFullyTypedValueAsIs() {
        PatternTag tag = levelTag();

        assertEquals("level:47", tag.completeInput("level:47"));
    }

    @Test
    void stillRejectsInputThePatternDoesNotAccept() {
        PatternTag tag = levelTag();

        assertFalse(tag.matches(tag.completeInput("high")));
        assertFalse(tag.matches(tag.completeInput("")));
    }

    @Test
    void leavesValuesAloneWhenThePatternIsNotNamePrefixed() {
        PatternTag tag = new PatternTag("order", "\\d+", "<number>", "Position in the route", true, Set.of("npc_route"));

        assertEquals("3", tag.completeInput("3"));
    }

    @Test
    void hintsOnlyTheValueHalfOfTheUsage() {
        assertEquals("<number>", levelTag().inputHint());
        assertEquals("<number>", new PatternTag("order", "\\d+", "<number>", "Position", true, Set.of("npc_route")).inputHint());
    }
}
