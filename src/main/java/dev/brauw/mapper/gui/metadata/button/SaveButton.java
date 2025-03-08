package dev.brauw.mapper.gui.metadata.button;

import dev.brauw.mapper.gui.metadata.GuiMetadata;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class SaveButton extends AbstractItem {
    private final GuiMetadata guiMetadata;

    public SaveButton(GuiMetadata guiMetadata) {
        this.guiMetadata = guiMetadata;
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.EMERALD)
                .setDisplayName(new AdventureComponentWrapper(Component.text("Save Metadata", NamedTextColor.GREEN)))
                .addLoreLines(new AdventureComponentWrapper(Component.text("Click to save changes", NamedTextColor.GRAY)));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        guiMetadata.getMetadataManager().saveMetadata(player.getWorld(), guiMetadata.getMetadata());
        player.closeInventory();
        player.sendMessage(Component.text("Map metadata saved successfully!", NamedTextColor.GREEN));
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
    }
}
