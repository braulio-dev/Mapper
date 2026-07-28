package dev.brauw.mapper.session.event;

import dev.brauw.mapper.session.EditSession;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a world's {@link EditSession} is opened - that is, when the first editor joins it.
 * Subsequent editors joining the same world fire {@link SessionJoinEvent} instead.
 */
@Getter
public class SessionCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final EditSession session;

    public SessionCreateEvent(EditSession session) {
        this.session = session;
    }

    public World getWorld() {
        return session.getWorld();
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
