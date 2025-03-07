package dev.brauw.mapper.listener;

import dev.brauw.mapper.listener.gui.GuiColorSelect;
import dev.brauw.mapper.listener.gui.GuiSetName;
import dev.brauw.mapper.region.Region;
import dev.brauw.mapper.region.RegionOptions;
import dev.brauw.mapper.session.EditSession;
import org.bukkit.Material;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.window.AnvilWindow;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class GuiManager {

    public GuiManager() {
        Structure.addGlobalIngredient('#', new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(""));
    }

    public void openRegionCreateGui(EditSession session, BiConsumer<String, RegionOptions> regionCreator, Runnable onClose) {
        AtomicReference<String> name = new AtomicReference<>("");
        final RegionOptions.RegionOptionsBuilder builder = RegionOptions.builder();

        final GuiSetName setNameGui = new GuiSetName(name, builder, () -> regionCreator.accept(name.get(), builder.build()));
        final GuiColorSelect colorGui = new GuiColorSelect(builder, setNameGui::update);
        AnvilWindow.split()
                .setUpperGui(setNameGui)
                .setLowerGui(colorGui)
                .addRenameHandler(updated -> {
                    name.set(updated);
                    setNameGui.update();
                })
                .setTitle("Create a new region")
                .addCloseHandler(onClose)
                .open(session.getOwner().getPlayer());
    }
}