package dev.brauw.mapper.logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class BukkitLoggerFactory {
    private static Plugin plugin;

    public static void initialize(Plugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static java.util.logging.Logger getLogger(String name) {
        if (plugin == null) {
            return Bukkit.getLogger();
        }
        return plugin.getLogger();
    }

    public static java.util.logging.Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }
}