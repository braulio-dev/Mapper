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
        // A second hand-out while the player already holds the tools would cache the tools themselves
        // as the "previous" inventory, and removeTools would then hand them back forever.
        if (cacheManager.hasCache(player.getUniqueId())) {
            return;
        }

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
        // No cache means this player was never given tools - a viewer, or someone who left twice.
        // Without this the restore below would write null into seven of their hotbar slots and
        // destroy whatever they were actually carrying.
        if (!cacheManager.hasCache(player.getUniqueId())) {
            return;
        }

        Map<Integer, ItemStack> cache = cacheManager.getAndRemoveCache(player.getUniqueId());

        for (ToolRegistry.ToolType type : ToolRegistry.ToolType.values()) {
            int slot = type.getSlot();
            ItemStack replacement = cache.get(slot);
            player.getInventory().setItem(slot, replacement);
        }
    }
}