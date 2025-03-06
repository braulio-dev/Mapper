package dev.brauw.mapper.listener;

import dev.brauw.mapper.MapperPlugin;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class RegionToolManager {

    private final MapperPlugin plugin;
    @Getter private final NamespacedKey toolTypeKey;
    private final Map<UUID, Map<Integer, ItemStack>> itemCache = new HashMap<>();
    
    public enum ToolType {
        POINT_REGION_CREATOR(0, Material.BEACON, "Point Region Creator"),
        PERSPECTIVE_REGION_CREATOR(1, Material.SPYGLASS, "Perspective Region Creator"),
        CUBOID_WAND(2, Material.GOLDEN_AXE, "Cuboid Region Wand"),
        POLYGON_WAND(3, Material.GOLDEN_HOE, "Polygon Region Wand"),
        REGION_DELETION_TOOL(4, Material.SHEARS, "Region Deletion Tool");
        
        @Getter final int slot;
        @Getter final Material material;
        @Getter final String displayName;
        
        ToolType(int slot, Material material, String displayName) {
            this.slot = slot;
            this.material = material;
            this.displayName = displayName;
        }
    }
    
    public RegionToolManager(MapperPlugin plugin) {
        this.plugin = plugin;
        this.toolTypeKey = new NamespacedKey(plugin, "region_tool_type");
    }
    
    public ItemStack createTool(ToolType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(type.getDisplayName(), NamedTextColor.YELLOW));
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(toolTypeKey, PersistentDataType.STRING, type.name());
        item.setItemMeta(meta);
        return item;
    }
    
    public boolean isTool(ItemStack item, ToolType type) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String toolType = container.get(toolTypeKey, PersistentDataType.STRING);
        return toolType != null && toolType.equals(type.name());
    }
    
    public void giveTools(Player player) {
        final HashMap<Integer, ItemStack> map = new HashMap<>();
        itemCache.put(player.getUniqueId(), map);
        for (ToolType type : ToolType.values()) {
            final ItemStack old = player.getInventory().getItem(type.getSlot());
            if (old != null) {
                map.put(type.getSlot(), old);
            }

            player.getInventory().setItem(type.getSlot(), createTool(type));
        }
    }

    public void removeTools(Player player) {
        Map<Integer, ItemStack> map = itemCache.remove(player.getUniqueId());
        if (map == null) {
            map = new HashMap<>();
        }

        for (ToolType type : ToolType.values()) {
            final ItemStack replacement = map.get(type.getSlot());
            player.getInventory().setItem(type.getSlot(), replacement);
        }
    }
}