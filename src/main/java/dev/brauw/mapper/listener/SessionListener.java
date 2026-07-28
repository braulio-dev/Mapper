package dev.brauw.mapper.listener;

import dev.brauw.mapper.Mapper;
import dev.brauw.mapper.session.EditSession;
import dev.brauw.mapper.session.SessionManager;
import dev.brauw.mapper.session.event.SessionJoinEvent;
import dev.brauw.mapper.session.event.SessionLeaveEvent;
import dev.brauw.mapper.tool.RegionToolManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Keeps a member's tools and region displays in step with their membership.
 * <p>
 * Both are per-player grants the session does not own: tools live in the player's inventory, and
 * display visibility is a {@code showEntity} grant that dies when they disconnect. A member who
 * reconnects, or who walks out of the world and back, has to be re-equipped and re-shown rather
 * than left staring at nothing.
 */
@RequiredArgsConstructor
public class SessionListener implements Listener {

    private final Mapper mapper;
    private final RegionToolManager toolManager;

    @EventHandler
    public void onSessionJoin(SessionJoinEvent event) {
        if (event.getRole().isCanEdit()) {
            toolManager.giveTools(event.getPlayer());
        }
    }

    @EventHandler
    public void onSessionLeave(SessionLeaveEvent event) {
        toolManager.removeTools(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final SessionManager sessionManager = mapper.getSessionManager();
        final EditSession session = sessionManager.getSession(player);
        if (session == null) {
            return;
        }

        // Membership outlives a disconnect; the display grants do not.
        sessionManager.restore(player);
        if (session.canEdit(player)) {
            toolManager.giveTools(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (mapper.getSessionManager().hasSession(player)) {
            toolManager.removeTools(player);
        }
    }

    /**
     * A session belongs to one world, so leaving that world suspends membership rather than ending
     * it. Ending it would discard everyone's unsaved regions the moment the last editor stepped
     * through a portal; suspending only takes away what is meaningless elsewhere.
     */
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        final EditSession session = mapper.getSessionManager().getSession(player);
        if (session == null) {
            return;
        }

        if (session.getWorld().equals(player.getWorld())) {
            session.showAll(player);
            if (session.canEdit(player)) {
                toolManager.giveTools(player);
            }
            player.sendMessage(Component.text("Resumed editing ", NamedTextColor.GREEN)
                    .append(Component.text(session.getWorld().getName(), NamedTextColor.WHITE)));
        } else {
            session.hideAll(player);
            toolManager.removeTools(player);
            player.sendMessage(Component.text("Your edit session for ", NamedTextColor.GRAY)
                    .append(Component.text(session.getWorld().getName(), NamedTextColor.WHITE))
                    .append(Component.text(" is paused while you are away.", NamedTextColor.GRAY)));
        }
    }
}
