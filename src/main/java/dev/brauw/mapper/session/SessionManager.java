package dev.brauw.mapper.session;

import lombok.CustomLog;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages player edit sessions for region definitions.
 */
@CustomLog
public class SessionManager {
    private final Map<UUID, EditSession> playerSessions = new HashMap<>();
    private final long sessionTimeoutMillis;
    
    /**
     * Creates a new SessionManager with the default timeout.
     */
    public SessionManager() {
        this(TimeUnit.HOURS.toMillis(1)); // Default 1 hour timeout
    }
    
    /**
     * Creates a new SessionManager with a custom timeout.
     *
     * @param sessionTimeoutMillis timeout in milliseconds for inactive sessions
     */
    public SessionManager(long sessionTimeoutMillis) {
        this.sessionTimeoutMillis = sessionTimeoutMillis;
        log.info("Session manager initialized with " + 
                 TimeUnit.MILLISECONDS.toMinutes(sessionTimeoutMillis) + 
                 " minute timeout");
    }
    
    /**
     * Gets or creates an edit session for the specified player.
     *
     * @param player the player to get a session for
     * @return the player's edit session
     */
    public EditSession getSession(Player player) {
        return playerSessions.computeIfAbsent(
            player.getUniqueId(), 
            uuid -> new EditSession(player)
        );
    }
    
    /**
     * Checks if the player has an active edit session.
     *
     * @param player the player to check
     * @return true if the player has an active session
     */
    public boolean hasSession(Player player) {
        return playerSessions.containsKey(player.getUniqueId());
    }
    
    /**
     * Ends the edit session for the specified player.
     *
     * @param player the player whose session to end
     * @return true if a session was ended, false if no session existed
     */
    public boolean endSession(Player player) {
        EditSession removed = playerSessions.remove(player.getUniqueId());
        if (removed != null) {
            log.info("Ended edit session for player " + player.getName());
            return true;
        }
        return false;
    }
    
    /**
     * Cleans up expired sessions.
     * This should be called periodically, e.g., by a scheduled task.
     */
    public void cleanupExpiredSessions() {
        final Iterator<EditSession> iterator = playerSessions.values().iterator();
        while (iterator.hasNext()) {
            EditSession session = iterator.next();
            if (System.currentTimeMillis() - session.getLastActivity() > sessionTimeoutMillis) {
                iterator.remove();
                log.info("Removed expired session for player " + session.getOwner().getName());
            }
        }
    }
}