package dev.brauw.mapper.gui.metadata;

import dev.brauw.mapper.metadata.MetadataManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.CycleItem;
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem;

@RequiredArgsConstructor
public class GameModeButton implements ItemProvider {

    private final String gameMode;
    private final MetadataManager metadataManager;

    @Override
    public @NotNull ItemStack get(@Nullable String lang) {
        final ItemBuilder builder = new ItemBuilder(Material.PAPER);
        builder.setDisplayName(AdventureComponentWrapper.EMPTY);
        for (String availableGameMode : metadataManager.getGameModes()) {
            if (availableGameMode.equals(gameMode)) {
                builder.addLoreLines(new AdventureComponentWrapper(Component.text("▶ " + gameMode, NamedTextColor.GREEN)));
                continue;
            }

            builder.addLoreLines(new AdventureComponentWrapper(Component.text(availableGameMode, NamedTextColor.GRAY)));
        }
        builder.addLoreLines(AdventureComponentWrapper.EMPTY);
        return builder.get();
    }
}
