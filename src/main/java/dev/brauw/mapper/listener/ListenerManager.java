package dev.brauw.mapper.listener;

import dev.brauw.mapper.MapperPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.PluginManager;

@RequiredArgsConstructor
public class ListenerManager {

    private final MapperPlugin plugin;
    private RegionToolManager toolManager;
    
    public void registerListeners() {
        PluginManager pm = plugin.getServer().getPluginManager();
        
        // Initialize tool manager
        toolManager = new RegionToolManager(plugin);
        
        // Register listeners
        pm.registerEvents(new RegionToolListener(plugin, toolManager), plugin);
        pm.registerEvents(new SessionListener(plugin, toolManager), plugin);
    }

}