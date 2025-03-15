package dev.brauw.mapper.gui.common;

import org.bukkit.Material;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class GuiSetName extends GuiSet<String> {
    public GuiSetName(AtomicReference<String> value, Supplier<Material> materialSupplier, Runnable creator) {
        super(value, materialSupplier, creator, name -> !name.isEmpty());
    }
}
