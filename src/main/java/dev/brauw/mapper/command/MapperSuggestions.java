package dev.brauw.mapper.command;

import dev.brauw.mapper.Mapper;
import dev.brauw.mapper.region.Region;
import dev.brauw.mapper.session.EditSession;
import dev.brauw.mapper.session.SessionMember;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;

import java.util.List;

/**
 * Tab-completion for the arguments that name something in a session.
 * <p>
 * Held apart from the command classes and parsed before them, because a suggestion provider has to
 * be registered by the time a command referring to it by name is built. Keeping them in one place
 * also means both command classes complete region names identically.
 */
@RequiredArgsConstructor
public class MapperSuggestions {

    private final Mapper mapper;

    /**
     * Suggests region names from the runner's own session. Names repeat across regions, so this
     * deliberately lists each distinct name once - the choice between same-named regions is made
     * afterwards, by {@link RegionPrompt}, where the coordinates can be shown.
     */
    @Suggestions("regionNames")
    public List<String> regionNames(CommandContext<CommandSourceStack> context, String input) {
        final EditSession session = sessionOf(context);
        if (session == null) {
            return List.of();
        }
        return session.getRegions().stream()
                .map(Region::getName)
                .distinct()
                .sorted()
                .toList();
    }

    /** Suggests the members of the runner's session, which is who {@code /mapper role} can act on. */
    @Suggestions("sessionMembers")
    public List<String> sessionMembers(CommandContext<CommandSourceStack> context, String input) {
        final EditSession session = sessionOf(context);
        if (session == null) {
            return List.of();
        }
        return session.getMembers().stream()
                .map(SessionMember::getName)
                .sorted()
                .toList();
    }

    private EditSession sessionOf(CommandContext<CommandSourceStack> context) {
        if (!(context.sender().getSender() instanceof Player player)) {
            return null;
        }
        return mapper.getSessionManager().getSession(player);
    }
}
