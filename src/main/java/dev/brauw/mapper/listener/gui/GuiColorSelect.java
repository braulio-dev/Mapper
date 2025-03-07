package dev.brauw.mapper.listener.gui;

import dev.brauw.mapper.region.Region;
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

    public GuiColorSelect(RegionOptions.RegionOptionsBuilder builder) {
        super(9, 3);
        this.builder = builder;

        final Structure structure = new Structure(
                "# # # # # # # # #",
                "# r o y g b p w #",
                "# # # # # # # # #"
        );
        structure.addIngredient('r', new ColorButton(Color.RED, Material.RED_CONCRETE, "Red"));
        structure.addIngredient('o', new ColorButton(Color.ORANGE, Material.ORANGE_CONCRETE, "Orange"));
        structure.addIngredient('y', new ColorButton(Color.YELLOW, Material.YELLOW_CONCRETE, "Yellow"));
        structure.addIngredient('g', new ColorButton(Color.GREEN, Material.GREEN_CONCRETE, "Green"));
        structure.addIngredient('b', new ColorButton(Color.BLUE, Material.BLUE_CONCRETE, "Blue"));
        structure.addIngredient('p', new ColorButton(Color.PURPLE, Material.PURPLE_CONCRETE, "Purple"));
        structure.addIngredient('m', new ColorButton(Color.WHITE, Material.WHITE_CONCRETE, "White"));
        applyStructure(structure);
    }

    @EqualsAndHashCode(callSuper = true)
    @Value
    private class ColorButton extends AbstractItem {

        Color color;
        Material material;
        String name;

        @Override
        public ItemProvider getItemProvider() {
            final Component text = Component.text(name, TextColor.color(color.getRed(), color.getGreen(), color.getBlue()));
            return new ItemBuilder(material)
                    .setDisplayName(new AdventureComponentWrapper(text));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
            builder.color(color);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        }
    }

}
