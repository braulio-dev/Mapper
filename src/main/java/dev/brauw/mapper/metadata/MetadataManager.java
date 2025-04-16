package dev.brauw.mapper.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.CustomLog;
import org.bukkit.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

@CustomLog
public class MetadataManager {
    private final ObjectMapper objectMapper;
    private final String defaultName;
    private final List<String> gameModes;

    public MetadataManager(String defaultName, List<String> gameModes) {
        this.defaultName = defaultName;
        this.gameModes = gameModes;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public MapMetadata loadMetadata(File metadataFile) throws IllegalArgumentException {
        if (metadataFile.exists()) {
            try (FileReader reader = new FileReader(metadataFile)) {
                return objectMapper.readValue(reader, MapMetadata.class);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed to load metadata from file: " + metadataFile.getAbsolutePath(), e);
            }
        }
        throw new IllegalArgumentException("Metadata file not found: " + metadataFile.getAbsolutePath());
    }

    public MapMetadata loadMetadata(World world) {
        return loadMetadata(world.getWorldFolder());
    }

    public void saveMetadata(File worldFolder, MapMetadata metadata) {
        File metadataFile = new File(worldFolder, "metadata.json");
        try (FileWriter writer = new FileWriter(metadataFile)) {
            objectMapper.writeValue(writer, metadata);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to save metadata for world " + worldFolder.getName(), e);
        }
    }

    public void saveMetadata(World world, MapMetadata metadata) {
        saveMetadata(world.getWorldFolder(), metadata);
    }

    public MapMetadata createDefaultMetadata() {
        return new MapMetadata(defaultName,
                new HashSet<>(),
                gameModes.isEmpty() ? "Unknown" : gameModes.getFirst(),
                100);
    }

    public List<String> getGameModes() {
        return Collections.unmodifiableList(gameModes);
    }
}