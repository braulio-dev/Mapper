package dev.brauw.mapper.gui.common;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class GuiSetName extends AbstractGui {

    private final Runnable creator;
    private final AtomicReference<String> name;
    private final DoneButton doneButton;
    private final Supplier<Material> materialSupplier;

    public GuiSetName(AtomicReference<String> name, Supplier<Material> materialSupplier, Runnable creator) {
        super(3, 1);
        this.creator = creator;
        this.materialSupplier = materialSupplier;
        this.name = name;
        this.doneButton = new DoneButton();

        Structure structure = new Structure("-+.");
        structure.addIngredient('-', new ItemBuilder(Material.PAPER).setDisplayName(""));
        structure.addIngredient('.', doneButton);
        applyStructure(structure);
    }

    public void update() {
        doneButton.notifyWindows();
    }

    private class DoneButton extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            if (!name.get().isEmpty()) {
                ItemBuilder itemBuilder = new ItemBuilder(materialSupplier.get());
                itemBuilder.setDisplayName(new AdventureComponentWrapper(Component.text("Done!", NamedTextColor.GREEN)));
                return itemBuilder;
            } else {
                ItemBuilder itemBuilder = new ItemBuilder(Material.TNT);
                itemBuilder.setDisplayName(new AdventureComponentWrapper(Component.text("Choose another name.", NamedTextColor.RED)));
                return itemBuilder;
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            player.closeInventory();
            creator.run();
        }
    }

}
