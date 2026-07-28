package dev.brauw.mapper.session.event;

import dev.brauw.mapper.session.EditSession;
import dev.brauw.mapper.session.SessionRole;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Fired when a player joins a world's edit session, including the member who opened it. */
@Getter
public class SessionJoinEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final EditSession session;
    private final Player player;
    private final SessionRole role;

    public SessionJoinEvent(EditSession session, Player player, SessionRole role) {
        this.session = session;
        this.player = player;
        this.role = role;
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
