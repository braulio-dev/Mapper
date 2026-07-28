package dev.brauw.mapper.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * What a member is allowed to do inside an {@link EditSession}.
 * <p>
 * A session belongs to a world rather than to a person, so "who started it" is not a permission
 * model on its own - everyone editing the same world shares one region list. Roles are how that
 * shared list stays governable: an {@link #OWNER} decides when the world is written to disk, an
 * {@link #EDITOR} shapes it, and a {@link #VIEWER} watches without being able to change anything.
 */
@Getter
@RequiredArgsConstructor
public enum SessionRole {

    /** Started the session. May edit, and may save, discard or change other members' roles. */
    OWNER("Owner", NamedTextColor.GOLD, true, true),

    /** May create, move and delete regions, but not save or end the session. */
    EDITOR("Editor", NamedTextColor.GREEN, true, false),

    /** Sees every region and every change live, and may change nothing. */
    VIEWER("Viewer", NamedTextColor.AQUA, false, false);

    private final String displayName;
    private final NamedTextColor color;

    /** Whether this role may add, modify or remove regions. */
    private final boolean canEdit;

    /** Whether this role may save, discard, or change another member's role. */
    private final boolean canManage;
}
