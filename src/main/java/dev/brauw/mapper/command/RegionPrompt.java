package dev.brauw.mapper.command;

import dev.brauw.mapper.region.Region;
import dev.brauw.mapper.util.Messages;
import dev.brauw.mapper.util.RegionFormat;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Resolves a command's region argument down to exactly one region, asking the player when it cannot.
 * <p>
 * Commands address regions by name, and names are deliberately not unique - a world has one
 * {@code npc_resident} per NPC. Acting on "the first match" would silently move, copy or delete a
 * region the player never meant, and the mistake would only surface much later, in a different
 * world, as a missing NPC. So an ambiguous name never resolves silently: the player gets every
 * candidate listed with its coordinates and tags, and picks the one they meant.
 */
public class RegionPrompt {

    /** How long a pending choice stays clickable before it is treated as abandoned. */
    private static final long CHOICE_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(2);

    private final Map<UUID, PendingChoice> pending = new HashMap<>();

    /**
     * Runs {@code onPick} against the single region the player meant.
     * <p>
     * Resolves immediately when there is exactly one candidate; otherwise prints the choices and
     * waits for {@link #pick(Player, int)}.
     *
     * @param player     the player to resolve for
     * @param candidates the regions matching what they typed
     * @param action     what will happen to the chosen region, e.g. {@code "teleport to"}
     * @param onPick     what to do with the chosen region
     */
    public void choose(Player player, List<Region> candidates, String action, Consumer<Region> onPick) {
        if (candidates.isEmpty()) {
            player.sendMessage(Messages.PREFIX.append(Component.text("No matching region.", NamedTextColor.RED)));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        if (candidates.size() == 1) {
            onPick.accept(candidates.getFirst());
            return;
        }

        final List<Region> snapshot = List.copyOf(candidates);
        pending.put(player.getUniqueId(), new PendingChoice(snapshot, onPick, System.currentTimeMillis()));

        player.sendMessage(Messages.PREFIX
                .append(Component.text(snapshot.size() + " regions match", NamedTextColor.GOLD))
                .append(Component.text(" - pick which one to " + action + ":", NamedTextColor.GRAY)));

        for (int index = 0; index < snapshot.size(); index++) {
            player.sendMessage(choiceLine(index + 1, snapshot.get(index), action));
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }

    /**
     * Applies a previously offered choice.
     *
     * @param player the choosing player
     * @param index  the 1-based index from the printed list
     * @return true if the choice was applied
     */
    public boolean pick(Player player, int index) {
        final PendingChoice choice = pending.get(player.getUniqueId());
        if (choice == null || choice.isExpired()) {
            pending.remove(player.getUniqueId());
            player.sendMessage(Messages.PREFIX.append(
                    Component.text("Nothing to pick - run the command again.", NamedTextColor.RED)));
            return false;
        }

        if (index < 1 || index > choice.getCandidates().size()) {
            player.sendMessage(Messages.PREFIX.append(Component.text(
                    "Pick a number between 1 and " + choice.getCandidates().size() + ".", NamedTextColor.RED)));
            return false;
        }

        pending.remove(player.getUniqueId());
        choice.getOnPick().accept(choice.getCandidates().get(index - 1));
        return true;
    }

    /** Forgets a player's pending choice, e.g. when they leave their session. */
    public void clear(Player player) {
        pending.remove(player.getUniqueId());
    }

    private Component choiceLine(int number, Region region, String action) {
        Component line = Component.text(" " + number + ". ", NamedTextColor.WHITE)
                .append(RegionFormat.describeWithType(region));

        final Component tags = RegionFormat.tags(region);
        if (!Component.empty().equals(tags)) {
            line = line.append(Component.text(" ", NamedTextColor.GRAY)).append(tags);
        }

        return line
                .hoverEvent(HoverEvent.showText(Component.text("Click to " + action + " this one", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.runCommand("/mapper pick " + number));
    }

    /**
     * A choice offered to one player. The candidate list is captured rather than re-derived on pick,
     * so the numbers the player is reading stay bound to the regions they were printed for even if
     * somebody else edits the session in between.
     */
    @Value
    private static class PendingChoice {

        List<Region> candidates;
        Consumer<Region> onPick;
        long offeredAt;

        boolean isExpired() {
            return System.currentTimeMillis() - offeredAt > CHOICE_TIMEOUT_MILLIS;
        }
    }
}
