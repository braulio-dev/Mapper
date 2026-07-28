package dev.brauw.mapper.session.event;

import dev.brauw.mapper.region.Region;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Fired after a world's regions have been written to disk.
 * <p>
 * This is the hook a consuming plugin listens to in order to re-read the file and rebuild whatever
 * it derives from it - zones, resource nodes, NPC routes - without a restart. It fires after the
 * write completes, so the file is already readable when handlers run, and the regions are supplied
 * directly for handlers that would rather not re-read it.
 */
@Getter
public class RegionsSavedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final World world;
    private final File file;
    private final List<Region> regions;
    private final @Nullable Player savedBy;

    public RegionsSavedEvent(World world, File file, List<Region> regions, @Nullable Player savedBy) {
        this.world = world;
        this.file = file;
        this.regions = Collections.unmodifiableList(regions);
        this.savedBy = savedBy;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
