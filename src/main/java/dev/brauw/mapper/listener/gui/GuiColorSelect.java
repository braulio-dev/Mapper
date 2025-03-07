package dev.brauw.mapper.listener.gui;

import dev.brauw.mapper.region.RegionColor;
import dev.brauw.mapper.region.RegionOptions;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.AbstractGui;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class GuiColorSelect extends AbstractGui {

    private final RegionOptions.RegionOptionsBuilder builder;
    private final Runnable updater;

    public GuiColorSelect(RegionOptions.RegionOptionsBuilder builder, Runnable updater) {
        super(9, 4);
        this.builder = builder;
        this.updater = updater;

        final Structure structure = new Structure(
                "# # # # # # # # #",
                "# r o y g b p w #",
                "# # # # # # # # #",
                "# # # # # # # # #"
        );
        structure.addIngredient('r', new ColorButton(RegionColor.RED));
        structure.addIngredient('o', new ColorButton(RegionColor.ORANGE));
        structure.addIngredient('y', new ColorButton(RegionColor.YELLOW));
        structure.addIngredient('g', new ColorButton(RegionColor.GREEN));
        structure.addIngredient('b', new ColorButton(RegionColor.BLUE));
        structure.addIngredient('p', new ColorButton(RegionColor.PURPLE));
        structure.addIngredient('w', new ColorButton(RegionColor.WHITE));
        applyStructure(structure);
    }

    @EqualsAndHashCode(callSuper = true)
    @Value
    private class ColorButton extends AbstractItem {

        RegionColor color;

        @Override
        public ItemProvider getItemProvider() {
            final Color color = this.color.getBukkitColor();
            final Material material = this.color.getMaterial();
            final String name = this.color.getName();
            final Component text = Component.text(name, TextColor.color(color.getRed(), color.getGreen(), color.getBlue()));
            return new ItemBuilder(material)
                    .setDisplayName(new AdventureComponentWrapper(text));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
            builder.color(color);
            updater.run();
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        }
    }

}
