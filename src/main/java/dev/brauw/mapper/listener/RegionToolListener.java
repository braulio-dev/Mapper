package dev.brauw.mapper.listener;

import dev.brauw.mapper.MapperPlugin;
import dev.brauw.mapper.selection.SelectionHandler;
import dev.brauw.mapper.session.EditSession;
import dev.brauw.mapper.tool.RegionToolManager;
import dev.brauw.mapper.tool.ToolRegistry.ToolType;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class RegionToolListener implements Listener {

    private final MapperPlugin plugin;
    private final RegionToolManager toolManager;
    private final SelectionHandler selectionHandler;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        // Check if player has active session
        if (!plugin.getSessionManager().hasSession(player)) return;
        EditSession session = plugin.getSessionManager().getSession(player);

        // Process tool interactions
        if (toolManager.isTool(item, ToolType.CUBOID_WAND)) {
            event.setCancelled(true);
            handleCuboidWand(event, session);
        }
        else if (toolManager.isTool(item, ToolType.POINT_REGION_CREATOR) &&
                event.getAction().isRightClick()) {
            event.setCancelled(true);
            selectionHandler.createPointRegion(session, event.getInteractionPoint());
        }
        else if (toolManager.isTool(item, ToolType.PERSPECTIVE_REGION_CREATOR) &&
                event.getAction().isRightClick()) {
            event.setCancelled(true);
            selectionHandler.createPerspectiveRegion(session, event.getInteractionPoint());
        }
        else if (toolManager.isTool(item, ToolType.REGION_DELETION_TOOL) &&
                event.getAction().isRightClick()) {
            event.setCancelled(true);
            selectionHandler.handleRegionDeletion(session, event);
        }
        else if (toolManager.isTool(item, ToolType.POLYGON_WAND)) {
            event.setCancelled(true);
            handlePolygonWand(event, session);
        }
        else if (toolManager.isTool(item, ToolType.TAG_TOOL) &&
                event.getAction().isRightClick()) {
            event.setCancelled(true);
            selectionHandler.handleTagEditor(session, event);
        }
    }

    private void handleCuboidWand(PlayerInteractEvent event, EditSession session) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selectionHandler.setFirstPosition(session, event);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selectionHandler.setSecondPosition(session, event);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR && event.getPlayer().isSneaking()) {
            selectionHandler.createCuboidRegion(session);
        }
    }

    private void handlePolygonWand(PlayerInteractEvent event, EditSession session) {
        if (event.getAction().isRightClick() && event.getPlayer().isSneaking()) {
            if (selectionHandler.hasCompleteSelection(session)) {
                selectionHandler.addPolygonChild(session);
            } else {
                selectionHandler.createPolygonRegion(session);
            }
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selectionHandler.setFirstPosition(session, event);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selectionHandler.setSecondPosition(session, event);
        }
    }
}
