package dev.brauw.mapper.gui.metadata.button;

import dev.brauw.mapper.MapperPlugin;
import dev.brauw.mapper.gui.GuiManager;
import dev.brauw.mapper.gui.common.GuiSetName;
import dev.brauw.mapper.metadata.MapMetadata;
import dev.brauw.mapper.util.BukkitTaskScheduler;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.AnvilWindow;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class AddAuthorButton extends AbstractItem {

    private final BukkitTaskScheduler taskScheduler;
    private final GuiManager guiManager;
    private final MapMetadata mapMetadata;

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName(new AdventureComponentWrapper(Component.text("Add Author", NamedTextColor.GREEN)))
                .addLoreLines(new AdventureComponentWrapper(Component.text("Click to add a player", NamedTextColor.GRAY)));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        player.closeInventory();
        taskScheduler.scheduleTask(() -> openAddAuthorWindow(player), 1L);
    }

    private void openAddAuthorWindow(Player player) {
        final AtomicReference<String> name = new AtomicReference<>("");
        final GuiSetName gui = new GuiSetName(name, () -> Material.PLAYER_HEAD, () -> {
            UUID uuid = null;
            try {
                uuid = UUID.fromString(name.get());
            } catch (IllegalArgumentException e) {
                // Try to find by name
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name.get());
                if (offlinePlayer != null) {
                    uuid = offlinePlayer.getUniqueId();
                }
            }

            if (uuid != null) {
                final Set<UUID> authors = mapMetadata.getAuthors();
                if (authors.add(uuid)) {
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    taskScheduler.scheduleTask(() -> guiManager.openMetadataEditor(player, mapMetadata), 1L);
                }
            } else {
                player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            }
        });

        AnvilWindow.single()
                .setGui(gui)
                .setTitle("Enter Player UUID/Name")
                .addRenameHandler(input -> {
                    name.set(input);
                    gui.update();
                })
                .addCloseHandler(() -> {
                    taskScheduler.scheduleTask(() -> guiManager.openMetadataEditor(player, mapMetadata), 1L);
                })
                .open(player);
    }
}
