package dev.brauw.mapper.tool;

import dev.brauw.mapper.MapperPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Facade class that provides a simplified interface to the tool system
 */
public class RegionToolManager {

    private final ToolRegistry toolRegistry;
    private final PlayerToolProvider toolProvider;
    private final InventoryCacheManager cacheManager;

    public RegionToolManager(MapperPlugin plugin) {
        this.toolRegistry = new ToolRegistry(plugin);
        this.cacheManager = new InventoryCacheManager();
        this.toolProvider = new PlayerToolProvider(toolRegistry, cacheManager);
    }

    public void giveTools(Player player) {
        toolProvider.giveTools(player);
    }

    public void removeTools(Player player) {
        toolProvider.removeTools(player);
    }

    public boolean isTool(ItemStack item, ToolRegistry.ToolType type) {
        return toolRegistry.isTool(item, type);
    }

    public ToolRegistry getToolRegistry() {
        return toolRegistry;
    }
}