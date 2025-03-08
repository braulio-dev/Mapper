package dev.brauw.mapper.gui.metadata.button;

import dev.brauw.mapper.gui.metadata.GuiMetadata;
import dev.brauw.mapper.metadata.MapMetadata;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class AuthorButton extends ControlItem<GuiMetadata> {

    private final MapMetadata mapMetadata;
    private final UUID authorId;

    @Override
    public ItemProvider getItemProvider(GuiMetadata gui) {
        ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName(new AdventureComponentWrapper(Component.text("Author: ", NamedTextColor.GOLD)
                        .append(Component.text(getPlayerName(), NamedTextColor.WHITE))))
                .addLoreLines(new AdventureComponentWrapper(Component.text("Click to remove", NamedTextColor.RED)));

        try {
            ItemStack skull = builder.get();
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(authorId));
            skull.setItemMeta(meta);
            return new ItemBuilder(skull);
        } catch (Exception e) {
            return builder;
        }
    }

    private String getPlayerName() {
        try {
            return Objects.requireNonNullElse(Bukkit.getOfflinePlayer(authorId).getName(), authorId.toString());
        } catch (Exception e) {
            return authorId.toString();
        }
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        mapMetadata.getAuthors().remove(authorId);
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.8f);
        getGui().updateAuthorList();
        notifyWindows();
    }
}
