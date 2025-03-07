package dev.brauw.mapper.listener;

import dev.brauw.mapper.listener.gui.GuiColorSelect;
import dev.brauw.mapper.listener.gui.GuiSetName;
import dev.brauw.mapper.region.Region;
import dev.brauw.mapper.region.RegionOptions;
import dev.brauw.mapper.session.EditSession;
import xyz.xenondevs.invui.window.AnvilWindow;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class GuiManager {

    public void openRegionCreateGui(EditSession session, BiConsumer<String, RegionOptions> regionCreator, Runnable onClose) {
        AtomicReference<String> name = new AtomicReference<>();
        final RegionOptions.RegionOptionsBuilder builder = RegionOptions.builder();

        AnvilWindow.split()
                .setUpperGui(new GuiSetName(name, () -> {
                    regionCreator.accept(name.get(), builder.build());
                }))
                .setLowerGui(new GuiColorSelect(builder))
                .setTitle("Create a new region")
                .addCloseHandler(onClose)
                .open(session.getOwner().getPlayer());
    }
}