package dev.brauw.mapper.listener.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.view.AnvilView;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.AbstractGui;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.concurrent.atomic.AtomicReference;

public class GuiSetName extends AbstractGui {
    public GuiSetName(AtomicReference<String> name, Runnable creator) {
        super(3, 1);

        ItemBuilder itemBuilder = new ItemBuilder(Material.GREEN_CONCRETE);
        itemBuilder.setDisplayName(new AdventureComponentWrapper(Component.text("Done!", NamedTextColor.GREEN)));

        Structure structure = new Structure("..#");
        structure.addIngredient('#', new SimpleItem(itemBuilder, click -> {
            final InventoryView view = click.getEvent().getView();
            if (!(view instanceof AnvilView anvil)) {
                throw new IllegalStateException("Anvil inventory not found");
            }
            name.set(anvil.getRenameText());
            creator.run();
        }));
        applyStructure(structure);
    }
}
