package dev.brauw.mapper.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/** Shared chat furniture, so every part of Mapper announces itself the same way. */
public final class Messages {

    public static final Component PREFIX =
            MiniMessage.miniMessage().deserialize("<gradient:#ff2424:#ff0000><bold>Mapper</bold></gradient> ");

    private Messages() {
    }
}
