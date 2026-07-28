package dev.brauw.mapper.session;

import dev.brauw.mapper.Mapper;
import dev.brauw.mapper.region.Region;
import dev.brauw.mapper.session.display.ArmorStandStrategy;
import dev.brauw.mapper.session.display.BlockStrategy;
import dev.brauw.mapper.session.display.ItemStrategy;
import dev.brauw.mapper.session.display.PathStrategy;
import dev.brauw.mapper.session.display.PolygonStrategy;
import dev.brauw.mapper.session.display.RegionDisplayStrategy;
import dev.brauw.mapper.session.event.SessionCreateEvent;
import dev.brauw.mapper.session.event.SessionEndEvent;
import dev.brauw.mapper.session.event.SessionJoinEvent;
import dev.brauw.mapper.session.event.SessionLeaveEvent;
import lombok.CustomLog;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Owns one {@link EditSession} per world and routes players to the session for the world they are
 * standing in.
 * <p>
 * A player belongs to at most one session at a time. That is enforced here rather than in
 * {@link EditSession}, because it is a statement about the player, not about any one session:
 * walking into a second world while editing a first would otherwise leave display entities shown
 * from a session they can no longer act on.
 */
@CustomLog
public class SessionManager {

    private final EnumMap<Region.RegionType, RegionDisplayStrategy<?>> displayStrategies;

    /** Sessions by world id. */
    private final Map<UUID, EditSession> sessions = new HashMap<>();

    /** Which world's session each player is in, so a player resolves to a session in one lookup. */
    private final Map<UUID, UUID> membership = new HashMap<>();

    private final long sessionTimeoutMillis;
    private final Mapper mapper;
    private BukkitTask revalidateTask;

    public SessionManager(Mapper mapper) {
        this(TimeUnit.HOURS.toMillis(1), mapper);
    }

    public SessionManager(long sessionTimeoutMillis, Mapper mapper) {
        this.sessionTimeoutMillis = sessionTimeoutMillis;
        this.mapper = mapper;
        this.displayStrategies = new EnumMap<>(Region.RegionType.class);
        createDisplayStrategies();
        startRevalidateTask();
        log.info("Session manager initialized with " +
                TimeUnit.MILLISECONDS.toMinutes(sessionTimeoutMillis) + " minute timeout");
    }

    private void startRevalidateTask() {
        this.revalidateTask = mapper.getTaskScheduler().scheduleRecurringTask(() -> {
            for (EditSession session : List.copyOf(sessions.values())) {
                revalidate(session);
            }
            cleanupExpiredSessions();
        }, 20L, 20L);
    }

    /**
     * Re-checks every region's display entities in one session. A strategy only rebuilds a region
     * whose chunk is loaded, so this is safe to call as often as it is useful.
     *
     * @param session the session to sweep
     */
    public void revalidate(EditSession session) {
        if (session.onlinePlayers().isEmpty()) {
            return;
        }
        for (Region region : session.getRegions()) {
            try {
                getDisplayStrategy(region).revalidate(region);
            } catch (RuntimeException exception) {
                // Isolated per region: this sweep is what re-shows displays after a chunk cycles, so
                // letting one region's failure abort the pass would leave every region after it in
                // the list invisible, intermittently and for no visible reason.
                log.severe("Failed to revalidate region '" + region.getName() + "': " + exception);
            }
        }
    }

    private void createDisplayStrategies() {
        final ItemStrategy pointStrategy = new ItemStrategy(mapper.getPlugin(), Material.REDSTONE_LAMP);
        final ArmorStandStrategy perspectiveStrategy =
                new ArmorStandStrategy(mapper.getPlugin(), mapper.getRegionIdKey());
        final BlockStrategy blockStrategy = new BlockStrategy(mapper.getPlugin());
        final PolygonStrategy polygonStrategy = new PolygonStrategy(mapper.getPlugin(), blockStrategy);
        final PathStrategy pathStrategy = new PathStrategy(mapper.getPlugin());
        this.displayStrategies.put(Region.RegionType.PATH, pathStrategy);
        this.displayStrategies.put(Region.RegionType.POLYGON, polygonStrategy);
        this.displayStrategies.put(Region.RegionType.CUBOID, blockStrategy);
        this.displayStrategies.put(Region.RegionType.POINT, pointStrategy);
        this.displayStrategies.put(Region.RegionType.PERSPECTIVE, perspectiveStrategy);
    }

    /**
     * Gets the display strategy for the specified region type.
     *
     * @param region the region to get the display strategy for
     * @return the display strategy for the region type
     */
    public <T extends Region> RegionDisplayStrategy<T> getDisplayStrategy(T region) {
        //noinspection unchecked
        return (RegionDisplayStrategy<T>) displayStrategies.get(region.getType());
    }

    /**
     * @return the session for a world, or {@code null} if nobody is editing it
     */
    public @Nullable EditSession getSession(World world) {
        return sessions.get(world.getUID());
    }

    /**
     * @return the session this player is a member of, or {@code null} if they are not editing
     */
    public @Nullable EditSession getSession(Player player) {
        final UUID worldId = membership.get(player.getUniqueId());
        return worldId == null ? null : sessions.get(worldId);
    }

    public Optional<EditSession> findSession(Player player) {
        return Optional.ofNullable(getSession(player));
    }

    public boolean hasSession(Player player) {
        return membership.containsKey(player.getUniqueId());
    }

    /**
     * @return every open session, as an unmodifiable view
     */
    public Collection<EditSession> getSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }

    /**
     * Puts a player into the session for the world they are standing in, opening that session if it
     * is the first one there.
     * <p>
     * Opening a session confers nothing extra. Every {@link SessionRole#EDITOR} may save and close
     * it, so a session never depends on one particular member being present to be finishable.
     *
     * @param player        the joining player
     * @param requestedRole the role to join with
     * @return the member record for this player
     */
    public SessionMember join(Player player, SessionRole requestedRole) {
        final World world = player.getWorld();
        final EditSession existing = getSession(player);
        if (existing != null && !existing.getWorld().equals(world)) {
            leave(player);
        }

        EditSession session = sessions.get(world.getUID());
        final boolean created = session == null;
        if (created) {
            session = new EditSession(this, world);
            sessions.put(world.getUID(), session);
            new SessionCreateEvent(session).callEvent();
        }

        final SessionMember member = session.join(player, requestedRole);
        membership.put(player.getUniqueId(), world.getUID());
        new SessionJoinEvent(session, player, requestedRole).callEvent();
        return member;
    }

    /**
     * Removes a player from their session, ending the session if they were the last member.
     *
     * @param player the leaving player
     * @return true if they were in a session
     */
    public boolean leave(Player player) {
        final EditSession session = getSession(player);
        if (session == null) {
            return false;
        }

        session.leave(player);
        membership.remove(player.getUniqueId());
        new SessionLeaveEvent(session, player).callEvent();

        if (session.getMembers().isEmpty()) {
            end(session);
        }
        return true;
    }

    /**
     * Closes a session, detaching every member first so each one's display entities are cleaned up
     * and each one is told they are no longer editing.
     *
     * @param session the session to close
     */
    public void end(EditSession session) {
        for (SessionMember member : List.copyOf(session.getMembers())) {
            final Player player = member.getPlayer();
            membership.remove(member.getPlayerId());
            if (player != null) {
                session.leave(player);
                new SessionLeaveEvent(session, player).callEvent();
            } else {
                session.removeMember(member.getPlayerId());
            }
        }

        sessions.remove(session.getWorld().getUID());
        new SessionEndEvent(session).callEvent();
        log.info("Ended edit session for world " + session.getWorld().getName());
    }

    /**
     * Re-shows a reconnecting member's regions. Display entities are per-player grants that do not
     * survive a disconnect, so a member who rejoins the server sees nothing until this runs.
     *
     * @param player the reconnecting player
     */
    public void restore(Player player) {
        final EditSession session = getSession(player);
        if (session != null && session.isMember(player)) {
            session.showAll(player);
        }
    }

    /**
     * Closes sessions that have gone quiet and have nobody online to act on them.
     * <p>
     * The "nobody online" half matters: a session holds unsaved work for everyone in it, so timing
     * one out from under an editor who is simply thinking would lose their work. An abandoned
     * session, by contrast, only holds display entities nobody can see.
     */
    public void cleanupExpiredSessions() {
        final long now = System.currentTimeMillis();
        final List<EditSession> expired = new ArrayList<>();
        for (EditSession session : sessions.values()) {
            final boolean stale = now - session.getLastActivity() > sessionTimeoutMillis;
            if (stale && session.onlinePlayers().isEmpty()) {
                expired.add(session);
            }
        }

        for (EditSession session : expired) {
            log.info("Expiring abandoned edit session for world " + session.getWorld().getName()
                    + " (" + session.getRegions().size() + " unsaved regions discarded)");
            end(session);
        }
    }

    /** Cancels the background revalidation task. */
    public void shutdown() {
        mapper.getTaskScheduler().cancelTask(revalidateTask);
    }
}
