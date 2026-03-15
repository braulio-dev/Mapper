package dev.brauw.mapper.gui;

import dev.brauw.mapper.MapperPlugin;
import dev.brauw.mapper.gui.common.GuiSetName;
import dev.brauw.mapper.gui.metadata.GuiMetadata;
import dev.brauw.mapper.gui.selector.GuiColorSelect;
import dev.brauw.mapper.metadata.MapMetadata;
import dev.brauw.mapper.region.RegionOptions;
import dev.brauw.mapper.session.EditSession;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.window.AnvilWindow;
import xyz.xenondevs.invui.window.Window;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class GuiManager {

    private final MapperPlugin mapperPlugin;

    public GuiManager(MapperPlugin mapperPlugin) {
        this.mapperPlugin = mapperPlugin;
        Structure.addGlobalIngredient('#', new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(""));
    }

    public void openRegionCreateGui(EditSession session, BiConsumer<String, RegionOptions> regionCreator, Runnable onClose) {
        AtomicReference<String> name = new AtomicReference<>("");
        final RegionOptions.RegionOptionsBuilder builder = RegionOptions.builder();

        final GuiSetName setNameGui = new GuiSetName(name, () -> builder.build().getColor().getMaterial(), () -> regionCreator.accept(name.get(), builder.build()));
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

    public void openMetadataEditor(Player player, MapMetadata mapMetadata) {
        Window.single()
                .setTitle("Map Metadata")
                .setGui(new GuiMetadata(mapperPlugin.getTaskScheduler(), this, mapperPlugin.getMetadataManager(), player, mapMetadata))
                .open(player);
    }
}