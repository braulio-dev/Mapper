package dev.brauw.mapper.listener;

import dev.brauw.mapper.MapperPlugin;
import dev.brauw.mapper.tool.RegionToolManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.PluginManager;

@RequiredArgsConstructor
public class ListenerManager {

    private final MapperPlugin plugin;
    private RegionToolManager toolManager;
    private SelectionHandler selectionHandler;
    private GuiManager guiManager;
    
    public void registerListeners() {
        PluginManager pm = plugin.getServer().getPluginManager();
        
        // Initialize
        toolManager = new RegionToolManager(plugin);
        guiManager = new GuiManager();
        selectionHandler = new SelectionHandler(guiManager);
        
        // Register listeners
        pm.registerEvents(new RegionToolListener(plugin, toolManager, selectionHandler), plugin);
        pm.registerEvents(new SessionListener(plugin, toolManager), plugin);
    }

}