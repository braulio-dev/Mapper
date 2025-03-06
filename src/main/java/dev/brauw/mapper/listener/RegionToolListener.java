package dev.brauw.mapper.listener;

import com.google.common.base.Preconditions;
import dev.brauw.mapper.MapperPlugin;
import dev.brauw.mapper.listener.RegionToolManager.ToolType;
import dev.brauw.mapper.listener.model.SelectionCorners;
import dev.brauw.mapper.region.*;
import dev.brauw.mapper.session.EditSession;
import dev.brauw.mapper.session.SessionManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.AnvilWindow;
import xyz.xenondevs.invui.window.Window;
import xyz.xenondevs.invui.window.WindowManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class RegionToolListener implements Listener {

    private final MapperPlugin plugin;
    private final RegionToolManager toolManager;
    private final Map<UUID, SelectionCorners> selections = new HashMap<>();
    private final Map<UUID, RegionOptions> selectedOptions = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // Check if player has active session
        SessionManager sessionManager = plugin.getSessionManager();
        if (!sessionManager.hasSession(player)) return;

        EditSession session = sessionManager.getSession(player);

        // Handle point region creator
        final Location point = event.getInteractionPoint();
        if (toolManager.isTool(item, ToolType.POINT_REGION_CREATOR) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            openColorSelectionGui(session, (name, options) -> new PointRegion(name, point, options));
        }

        // Handle perspective region creator
        else if (toolManager.isTool(item, ToolType.PERSPECTIVE_REGION_CREATOR) && event.getAction().isRightClick()) {
            event.setCancelled(true);
            openColorSelectionGui(session, (name, options) -> new PerspectiveRegion(name, point, options));
        }

        // Handle cuboid wand with new selection system
        else if (toolManager.isTool(item, ToolType.CUBOID_WAND)) {
            event.setCancelled(true);
            handleCuboidSelection(event, session);
        }

        // Handle polygon wand - creates multiple cuboids to form a polygon
        else if (toolManager.isTool(item, ToolType.POLYGON_WAND)) {
            player.sendMessage(Component.text("This tool is not implemented yet.", NamedTextColor.DARK_RED));
            player.playSound(player, Sound.ENTITY_WITHER_DEATH, 1.0f, 0.4f);
        }

        // Handle region deletion tool
        else if (toolManager.isTool(item, ToolType.REGION_DELETION_TOOL) && event.getAction().isRightClick()) {
            event.setCancelled(true);
            handleRegionDeletion(session);
        }
    }

    private void handleCuboidSelection(PlayerInteractEvent event, EditSession session) {
        final RayTraceResult result = getTarget(event.getPlayer());
        if (result == null) return;
        Location point = result.getHitPosition().toLocation(event.getPlayer().getWorld());

        Player player = session.getOwner();
        UUID playerId = player.getUniqueId();

        // Get or create selection corners
        SelectionCorners selection = selections.computeIfAbsent(playerId, id -> new SelectionCorners());

        // Left click - set first corner
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selection.setFirstCorner(point);
            player.sendMessage(Component.text("First corner set ", NamedTextColor.GREEN)
                    .append(Component.text(String.format("(%.1f, %.1f, %.1f)", point.x(), point.y(), point.z()), NamedTextColor.DARK_GRAY)));
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
        }
        // Right click - set second corner or create region
        else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // If shift is pressed and both corners are set, create region
            if (event.getPlayer().isSneaking() && selection.isComplete()) {
                player.sendMessage(Component.text("Creating region with selection...", NamedTextColor.YELLOW));
                openColorSelectionGui(session, (name, options) ->
                        new CuboidRegion(name, selection.getFirstCorner(), selection.getSecondCorner(), options)
                );
                // Clear selection after creating
                selections.remove(playerId);
            } else {
                // Just set second corner
                selection.setSecondCorner(point);
                player.sendMessage(Component.text("Second corner set ", NamedTextColor.YELLOW)
                        .append(Component.text(String.format("(%.1f, %.1f, %.1f)", point.x(), point.y(), point.z()), NamedTextColor.GOLD)));
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.5f);
            }
        }
    }

    private void handleRegionDeletion(EditSession session) {
        // Raytracing to find region 3 blocks in front of player
        Player player = session.getOwner();
        final RayTraceResult result = getTarget(player);
        if (result == null) return;

        // Find region at this location
        Location location = result.getHitPosition().toLocation(player.getWorld());
        session.getRegions().stream()
                .filter(region -> region.contains(location) || (region instanceof PointRegion point && point.getLocation().distance(location) < 0.2))
                .findFirst()
                .ifPresent(region -> {
                    session.removeRegion(region);
                    player.sendMessage(Component.text("Region deleted: ", NamedTextColor.RED)
                            .append(Component.text(region.getName(), NamedTextColor.DARK_RED)));
                    player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.0f);
                });
    }

    private static @Nullable RayTraceResult getTarget(Player player) {
        return player.getWorld().rayTraceBlocks(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                3.2,
                FluidCollisionMode.NEVER,
                true
        );
    }

    // Remaining methods unchanged
    private void openColorSelectionGui(EditSession session, BiFunction<String, RegionOptions, Region> creator) {
        // Simple color selection GUI implementation for point/perspective regions
        Gui gui = Gui.normal()
                .setStructure("# # # # # # # # #",
                        "# r o y g b p w #",
                        "# # # # # # # # #")
                .addIngredient('r', createColorItem(session, creator, Material.RED_CONCRETE, "Red", Color.RED))
                .addIngredient('o', createColorItem(session, creator, Material.ORANGE_CONCRETE, "Orange", Color.ORANGE))
                .addIngredient('y', createColorItem(session, creator, Material.YELLOW_CONCRETE, "Yellow", Color.YELLOW))
                .addIngredient('g', createColorItem(session, creator, Material.LIME_CONCRETE, "Green", Color.LIME))
                .addIngredient('b', createColorItem(session, creator, Material.BLUE_CONCRETE, "Blue", Color.BLUE))
                .addIngredient('p', createColorItem(session, creator, Material.PURPLE_CONCRETE, "Purple", Color.PURPLE))
                .addIngredient('w', createColorItem(session, creator, Material.WHITE_CONCRETE, "White", Color.WHITE))
                .addIngredient('#', new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(""))
                .build();

        Window.single()
                .setTitle("Select Region Color")
                .setCloseable(false)
                .setGui(gui)
                .open(session.getOwner());
    }

    private SimpleItem createColorItem(EditSession session, BiFunction<String, RegionOptions, Region> creator, Material material, String itemName, Color color) {
        final TextColor textColor = TextColor.color(color.getRed(), color.getGreen(), color.getBlue());
        final ItemStack itemStack = ItemStack.of(material);
        itemStack.editMeta(meta -> meta.displayName(Component.text(itemName, textColor)));
        return new SimpleItem(itemStack, click -> selectColorAndOpenNameGui(session, creator, color));
    }

    private void selectColorAndOpenNameGui(EditSession session, BiFunction<String, RegionOptions, Region> creator, Color color) {
        // Store selected color
        final Player player = session.getOwner();
        RegionOptions options = RegionOptions.builder()
                .color(color)
                .build();
        selectedOptions.put(player.getUniqueId(), options);

        // Open name selection GUI
        AtomicReference<String> name = new AtomicReference<>("");
        final AdventureComponentWrapper closeName = new AdventureComponentWrapper(Component.text("Done!", NamedTextColor.GREEN));
        final Gui gui = Gui.normal()
                .setStructure("..#")
                .addIngredient('.', new ItemBuilder(Material.PAPER).setDisplayName(""))
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.GREEN_CONCRETE).setDisplayName(closeName),
                        click -> {
                            final String finalName = name.get();
                            if (finalName.isEmpty()) {
                                player.sendMessage(Component.text("Please enter a valid name.", NamedTextColor.RED));
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.4f);
                                return;
                            }

                            WindowManager.getInstance().getWindow(click.getEvent().getClickedInventory()).close();
                            player.sendMessage(Component.text("Creating region...", NamedTextColor.GREEN));
                            final RegionOptions savedOptions = selectedOptions.remove(player.getUniqueId());
                            final Region created = creator.apply(name.get(), savedOptions);
                            session.addRegion(created);
                            player.sendMessage(Component.text("Region created: ", NamedTextColor.GREEN)
                                    .append(Component.text(name.get(), NamedTextColor.DARK_GREEN)));
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                        }))
                .build();

        AnvilWindow.single()
                .setTitle("Enter Region Name")
                .setGui(gui)
                .addRenameHandler(name::set)
                .setCloseable(false)
                .open(player);
    }
}