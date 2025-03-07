package dev.brauw.mapper.tool;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the caching of player inventory items
 */
public class InventoryCacheManager {

    private final Map<UUID, Map<Integer, ItemStack>> itemCache = new HashMap<>();

    public Map<Integer, ItemStack> getAndRemoveCache(UUID playerId) {
        Map<Integer, ItemStack> cache = itemCache.remove(playerId);
        return cache != null ? cache : new HashMap<>();
    }

    public void cacheItems(UUID playerId, Map<Integer, ItemStack> items) {
        itemCache.put(playerId, items);
    }

    public boolean hasCache(UUID playerId) {
        return itemCache.containsKey(playerId);
    }

    public void clearCache(UUID playerId) {
        itemCache.remove(playerId);
    }
}