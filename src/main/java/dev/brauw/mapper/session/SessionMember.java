package dev.brauw.mapper.session;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * One participant in an {@link EditSession}.
 * <p>
 * Identity is the player's UUID rather than the {@link Player} object, so a member survives a
 * reconnect: the session keeps their role and their place in the member list while they are offline,
 * and re-shows every region when they come back.
 */
@Getter
public class SessionMember {

    private final UUID playerId;
    private final String name;
    private final long joinedAt;

    @Setter
    private SessionRole role;

    public SessionMember(Player player, SessionRole role) {
        this.playerId = player.getUniqueId();
        this.name = player.getName();
        this.role = role;
        this.joinedAt = System.currentTimeMillis();
    }

    /**
     * @return the online player behind this member, or {@code null} if they have disconnected
     */
    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(playerId);
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    public boolean canEdit() {
        return role.isCanEdit();
    }
}
