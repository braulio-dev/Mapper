package dev.brauw.mapper.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.CustomLog;
import org.bukkit.World;

import java.io.*;
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

    public MapMetadata loadMetadata(InputStream metadataFile) throws IllegalArgumentException {
        if (metadataFile != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(metadataFile))) {
                return objectMapper.readValue(reader, MapMetadata.class);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed to load metadata from input stream", e);
            }
        }
        throw new IllegalArgumentException("Metadata file not found");
    }

    public MapMetadata loadMetadata(File metadataFile) throws IllegalArgumentException {
        if (metadataFile != null && metadataFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(metadataFile))) {
                return objectMapper.readValue(reader, MapMetadata.class);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed to load metadata from file " + metadataFile.getName(), e);
            }
        }
        throw new IllegalArgumentException("Metadata file not found");
    }

    public MapMetadata loadMetadata(World world) {
        File metadataFile = new File(world.getWorldFolder(), "metadata.json");
        if (metadataFile.exists()) {
            return loadMetadata(metadataFile);
        }
        throw new IllegalArgumentException("Metadata file not found in world " + world.getName());
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