package dev.brauw.mapper;

import dev.brauw.mapper.logger.BukkitLoggerFactory;
import dev.brauw.mapper.export.ExportManager;
import dev.brauw.mapper.session.SessionManager;
import lombok.CustomLog;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@CustomLog
public class MapperPlugin extends JavaPlugin {

    @Getter
    private static MapperPlugin instance;
    @Getter
    private SessionManager sessionManager;
    @Getter
    private ExportManager exportManager;

    @Override
    public void onEnable() {
        instance = this;
        BukkitLoggerFactory.initialize(this);

        this.sessionManager = new SessionManager(5 * 60 * 1000, this);
        this.exportManager = new ExportManager(this);

        // PaperCommandManager<CommandSourceStack> commandManager = PaperCommandManager.builder()
        //        .executionCoordinator(executionCoordinator)
        //        .buildOnEnable(javaPlugin);

        log.info("Mapper plugin enabled!");
    }

    @Override
    public void onDisable() {
        log.info("Mapper plugin disabled!");
    }
}