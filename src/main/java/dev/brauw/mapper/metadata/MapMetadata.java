package dev.brauw.mapper.metadata;

import lombok.Data;

import java.util.List;

@Data
public class MapMetadata {
    private final String name;
    private final List<String> authors;
    private final String gamemode;
}
