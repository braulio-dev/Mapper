package dev.brauw.mapper.session.event;

import dev.brauw.mapper.session.EditSession;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Fired when a player leaves a world's edit session, whether by choice or because it ended. */
@Getter
public class SessionLeaveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final EditSession session;
    private final Player player;

    public SessionLeaveEvent(EditSession session, Player player) {
        this.session = session;
        this.player = player;
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
