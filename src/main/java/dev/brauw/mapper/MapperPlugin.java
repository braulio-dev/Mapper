package dev.brauw.mapper;

import dev.brauw.mapper.command.MapperCommand;
import dev.brauw.mapper.export.ExportManager;
import dev.brauw.mapper.export.ExportStrategy;
import dev.brauw.mapper.listener.ListenerManager;
import dev.brauw.mapper.logger.BukkitLoggerFactory;
import dev.brauw.mapper.session.SessionManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.CustomLog;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.ParserDescriptor;

@CustomLog
public class MapperPlugin extends JavaPlugin {

    @Getter
    private static MapperPlugin instance;
    @Getter
    private SessionManager sessionManager;
    @Getter
    private ExportManager exportManager;
    @Getter
    private PaperCommandManager<CommandSourceStack> commandManager;
    @Getter
    private ListenerManager listenerManager;

    @Override
    public void onEnable() {
        instance = this;
        BukkitLoggerFactory.initialize(this);

        this.listenerManager = new ListenerManager(this);
        this.listenerManager.registerListeners();
        this.sessionManager = new SessionManager(5 * 60 * 1000, this);
        this.exportManager = new ExportManager(this);
        this.commandManager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(this);
        this.setupCommands();

        log.info("Mapper plugin enabled!");
    }

    private void setupCommands() {
        AnnotationParser<CommandSourceStack> parser = new AnnotationParser<>(this.commandManager, CommandSourceStack.class);
        parser.parse(new MapperCommand(this));
    }

    @Override
    public void onDisable() {
        log.info("Mapper plugin disabled!");
    }
}