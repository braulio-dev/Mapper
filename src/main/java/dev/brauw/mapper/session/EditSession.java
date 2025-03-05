package dev.brauw.mapper.session;

import dev.brauw.mapper.MapperPlugin;
import dev.brauw.mapper.export.ExportStrategy;
import dev.brauw.mapper.region.Region;
import lombok.CustomLog;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a session for a player defining regions.
 * Stores regions in-memory until they are committed or exported.
 */
@Getter
@CustomLog
public class EditSession {
    private final UUID sessionId;
    
    private final Player owner;
    
    private final List<Region> regions;

    private long lastActivity = System.currentTimeMillis();
    
    /**
     * Creates a new edit session for the specified player.
     *
     * @param owner the player who owns this session
     */
    public EditSession(Player owner) {
        this.sessionId = UUID.randomUUID();
        this.owner = owner;
        this.regions = new ArrayList<>();
        log.info("Created new edit session for player " + owner.getName());
    }
    
    /**
     * Adds a region to this session.
     *
     * @param region the region to add
     * @return true if the region was added successfully
     */
    public boolean addRegion(Region region) {
        boolean added = regions.add(region);
        if (added) {
            log.info("Player " + owner.getName() + " added region '" + 
                     region.getName() + "' to their session");
            lastActivity = System.currentTimeMillis();
        }
        return added;
    }
    
    /**
     * Removes a region from this session.
     *
     * @param region the region to remove
     * @return true if the region was removed successfully
     */
    public boolean removeRegion(Region region) {
        return regions.remove(region);
    }
    
    /**
     * Removes a region from this session by its ID.
     *
     * @param regionId the ID of the region to remove
     * @return true if the region was removed successfully
     */
    public boolean removeRegion(UUID regionId) {
        return regions.removeIf(region -> region.getId().equals(regionId));
    }
    
    /**
     * Clears all regions from this session.
     */
    public void clearRegions() {
        regions.clear();
        lastActivity = System.currentTimeMillis();
        log.info("Player " + owner.getName() + " cleared their session regions");
    }
    
    /**
     * Exports all regions in this session using the specified export strategy.
     *
     * @param strategyId the ID of the export strategy to use
     * @return true if the export was successful
     */
    public boolean exportRegions(String strategyId) {
        if (regions.isEmpty()) {
            owner.sendMessage("No regions to export");
            return false;
        }
        
        boolean success = MapperPlugin.getInstance().getExportManager()
                .exportRegions(strategyId, regions);
                
        if (success) {
            owner.sendMessage("Successfully exported " + regions.size() + 
                             " regions using " + strategyId + " strategy");
            log.info("Player " + owner.getName() + " exported " + 
                    regions.size() + " regions");
        } else {
            owner.sendMessage("Failed to export regions");
        }
        
        return success;
    }

}