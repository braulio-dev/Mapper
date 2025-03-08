package dev.brauw.mapper.listener;

import dev.brauw.mapper.MapperPlugin;
import dev.brauw.mapper.selection.SelectionHandler;
import dev.brauw.mapper.tool.RegionToolManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.PluginManager;

@RequiredArgsConstructor
public class ListenerManager {

    private final MapperPlugin plugin;
    private final RegionToolManager regionToolManager;
    private final SelectionHandler selectionHandler;

    public void registerListeners() {
        PluginManager pm = plugin.getServer().getPluginManager();
        
        // Register listeners
        pm.registerEvents(new RegionToolListener(plugin, regionToolManager, selectionHandler), plugin);
        pm.registerEvents(new SessionListener(plugin, regionToolManager), plugin);
    }

}