package dev.brauw.mapper.tag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagRegistryTest {

    private static PatternTag orderTag(RegionScope scope) {
        return new PatternTag("order", "order:\\d+", "order:<number>", "Position in the route", true, scope);
    }

    @Test
    void offersPatternScopedTagsOnEveryMatchingRegion() {
        TagRegistry registry = new TagRegistry();
        registry.register(orderTag(RegionScope.pattern("npc_.*")));

        assertTrue(registry.hasTags("npc_waypoint"));
        assertTrue(registry.hasTags("npc_resident"));
        assertFalse(registry.hasTags("dock"));
    }

    @Test
    void offersAnyScopedTagsEverywhere() {
        TagRegistry registry = new TagRegistry();
        registry.register(orderTag(RegionScope.any()));

        assertTrue(registry.hasTags("anything"));
        assertTrue(registry.hasTags("something_else"));
    }

    @Test
    void matchesExplicitNamesCaseInsensitively() {
        TagRegistry registry = new TagRegistry();
        registry.register(orderTag(RegionScope.names("Npc_Waypoint")));

        assertTrue(registry.hasTags("npc_waypoint"));
        assertTrue(registry.hasTags("NPC_WAYPOINT"));
    }

    @Test
    void skipsDuplicateRegistrationsSoReloadsDoNotAccumulate() {
        TagRegistry registry = new TagRegistry();
        registry.register(orderTag(RegionScope.pattern("npc_.*")));
        registry.register(orderTag(RegionScope.pattern("npc_.*")));

        assertEquals(1, registry.getTags("npc_waypoint").size());
    }

    @Test
    void treatsDifferentScopesAsDistinctDefinitions() {
        TagRegistry registry = new TagRegistry();
        registry.register(orderTag(RegionScope.pattern("npc_.*")));
        registry.register(orderTag(RegionScope.pattern("prop_.*")));

        assertEquals(1, registry.getTags("npc_waypoint").size());
        assertEquals(1, registry.getTags("prop_bench").size());
    }

    @Test
    void unregistersByDefinitionRatherThanIdentity() {
        TagRegistry registry = new TagRegistry();
        registry.register(orderTag(RegionScope.any()));
        registry.unregister(orderTag(RegionScope.any()));

        assertFalse(registry.hasTags("npc_waypoint"));
    }

    @Test
    void resolvesAppliedValuesToTheirDefinition() {
        TagRegistry registry = new TagRegistry();
        registry.register(orderTag(RegionScope.any()));

        assertTrue(registry.match("npc_waypoint", "order:20").isPresent());
        assertTrue(registry.match("npc_waypoint", "order:banana").isEmpty());
    }
}
