package dev.brauw.mapper.tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagRegistry {

    private final Map<String, List<Tag>> tags = new HashMap<>();

    public void register(String regionName, Tag tag) {
        tags.computeIfAbsent(regionName, k -> new ArrayList<>()).add(tag);
    }

    public List<Tag> getTags(String regionName) {
        return Collections.unmodifiableList(tags.getOrDefault(regionName, Collections.emptyList()));
    }

    public boolean hasTags(String regionName) {
        return tags.containsKey(regionName) && !tags.get(regionName).isEmpty();
    }
}
