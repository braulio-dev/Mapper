package dev.brauw.mapper.command;

import dev.brauw.mapper.Mapper;
import dev.brauw.mapper.region.Region;
import dev.brauw.mapper.session.EditSession;
import dev.brauw.mapper.util.Messages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * The checks every Mapper command starts with: are you a player, are you in a session, and may you
 * change it.
 * <p>
 * Shared as a collaborator rather than a base class so the command classes stay plain annotated
 * objects that Cloud can parse, and so this can be exercised without a command at all.
 */
@RequiredArgsConstructor
public class CommandSupport {

    @Getter
    private final Mapper mapper;

    /**
     * @param action what they were trying to do, used in the error message
     * @return the sender as a player, or {@code null} after telling the console it cannot do this
     */
    public @Nullable Player player(CommandSourceStack source, String action) {
        final CommandSender sender = source.getSender();
        if (sender instanceof Player player) {
            return player;
        }
        sender.sendMessage(Messages.PREFIX.append(
                Component.text("Only players can " + action + ".", NamedTextColor.RED)));
        return null;
    }

    /**
     * @return the player's session, of any role, or {@code null} after telling them they have none
     */
    public @Nullable EditSession session(Player player) {
        final EditSession session = mapper.getSessionManager().getSession(player);
        if (session == null) {
            deny(player, "You do not have an active session. Use /mapper edit.");
            return null;
        }
        return session;
    }

    /**
     * @return the player's session if they may change it, or {@code null} after explaining why not
     */
    public @Nullable EditSession editing(Player player) {
        final EditSession session = session(player);
        if (session == null) {
            return null;
        }
        if (!session.canEdit(player)) {
            deny(player, "You are viewing this session, not editing it.");
            return null;
        }
        return session;
    }

    /**
     * Like {@link #editing(Player)}, but also requires the player to be standing in the session's
     * world.
     * <p>
     * Anything that places a region uses this. A session holds one world's regions and the export
     * refuses a mixed set, so a member who wandered into another world and ran {@code /mapper point}
     * would otherwise add a region that makes the whole world unsaveable.
     *
     * @return the player's session if they may change it from where they are standing
     */
    public @Nullable EditSession editingHere(Player player) {
        final EditSession session = editing(player);
        if (session == null) {
            return null;
        }
        if (!session.getWorld().equals(player.getWorld())) {
            deny(player, "Your session is in " + session.getWorld().getName() + ". Go back there first.");
            return null;
        }
        return session;
    }

    /**
     * @return the player's session if they may save or close it, or {@code null} after explaining
     */
    public @Nullable EditSession managing(Player player) {
        final EditSession session = session(player);
        if (session == null) {
            return null;
        }
        if (!session.canManage(player)) {
            deny(player, "Only the session owner can do that.");
            return null;
        }
        return session;
    }

    /**
     * Finds the regions a player's typed name could mean.
     * <p>
     * An exact name match wins outright. Only when nothing matches exactly does this widen to a
     * substring search, so typing a full name never drags in the longer names that contain it.
     *
     * @param session the session to search
     * @param name    the typed name
     * @return the candidate regions, possibly empty
     */
    public List<Region> matching(EditSession session, String name) {
        final String needle = name.toLowerCase(Locale.ROOT);
        final List<Region> exact = session.getRegions().stream()
                .filter(region -> region.getName().toLowerCase(Locale.ROOT).equals(needle))
                .toList();
        if (!exact.isEmpty()) {
            return exact;
        }

        return session.getRegions().stream()
                .filter(region -> region.getName().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    public void deny(Player player, String message) {
        player.sendMessage(Messages.PREFIX.append(Component.text(message, NamedTextColor.RED)));
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.4f);
    }

    public void confirm(Player player, Component message) {
        player.sendMessage(Messages.PREFIX.append(message));
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
    }
}
