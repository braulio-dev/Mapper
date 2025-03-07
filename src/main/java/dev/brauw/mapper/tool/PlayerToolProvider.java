package dev.brauw.mapper.tool;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages providing tools to players and restoring their previous inventory
 */
public class PlayerToolProvider {

    private final ToolRegistry toolRegistry;
    private final InventoryCacheManager cacheManager;

    public PlayerToolProvider(ToolRegistry toolRegistry, InventoryCacheManager cacheManager) {
        this.toolRegistry = toolRegistry;
        this.cacheManager = cacheManager;
    }

    public void giveTools(Player player) {
        Map<Integer, ItemStack> cache = new HashMap<>();
        
        for (ToolRegistry.ToolType type : ToolRegistry.ToolType.values()) {
            int slot = type.getSlot();
            ItemStack old = player.getInventory().getItem(slot);
            if (old != null) {
                cache.put(slot, old);
            }
            player.getInventory().setItem(slot, toolRegistry.createTool(type));
        }
        
        cacheManager.cacheItems(player.getUniqueId(), cache);
    }

    public void removeTools(Player player) {
        Map<Integer, ItemStack> cache = cacheManager.getAndRemoveCache(player.getUniqueId());
        
        for (ToolRegistry.ToolType type : ToolRegistry.ToolType.values()) {
            int slot = type.getSlot();
            ItemStack replacement = cache.get(slot);
            player.getInventory().setItem(slot, replacement);
        }
    }
}