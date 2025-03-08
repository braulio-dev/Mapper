package dev.brauw.mapper.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@Jacksonized
public class MapMetadata {
    private @NotNull String name;
    private @NotNull Set<@NotNull UUID> authors;
    private @NotNull String gameMode;
}
