package dev.brauw.mapper.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * What a member is allowed to do inside an {@link EditSession}.
 * <p>
 * A session belongs to a world rather than to a person, so "who started it" carries no privilege -
 * everyone editing the same world shares one region list, and anyone shaping it may also write it
 * out. The only distinction left is whether you are here to change the world or to watch someone
 * else change it.
 */
@Getter
@RequiredArgsConstructor
public enum SessionRole {

    /** May create, move and delete regions, and may save, close or re-role the session. */
    EDITOR("Editor", NamedTextColor.GREEN, true),

    /** Sees every region and every change live, and may change nothing. */
    VIEWER("Viewer", NamedTextColor.AQUA, false);

    private final String displayName;
    private final NamedTextColor color;

    /** Whether this role may add, modify or remove regions, save them, or close the session. */
    private final boolean canEdit;
}
