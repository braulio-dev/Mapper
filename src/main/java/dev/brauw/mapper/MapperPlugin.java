package dev.brauw.mapper;

import dev.brauw.mapper.command.MapperCommand;
import dev.brauw.mapper.export.ExportManager;
import dev.brauw.mapper.gui.GuiManager;
import dev.brauw.mapper.listener.ListenerManager;
import dev.brauw.mapper.logger.BukkitLoggerFactory;
import dev.brauw.mapper.metadata.MetadataManager;
import dev.brauw.mapper.selection.SelectionHandler;
import dev.brauw.mapper.session.SessionManager;
import dev.brauw.mapper.tool.RegionToolManager;
import dev.brauw.mapper.util.BukkitTaskScheduler;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.CustomLog;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;

import java.util.List;

@Getter
@CustomLog
public class MapperPlugin extends JavaPlugin {

    @Getter
    private static MapperPlugin instance;
    private SessionManager sessionManager;
    private ExportManager exportManager;
    private PaperCommandManager<CommandSourceStack> commandManager;
    private ListenerManager listenerManager;
    private MetadataManager metadataManager;
    private RegionToolManager regionToolManager;
    private SelectionHandler selectionHandler;
    private GuiManager guiManager;
    private BukkitTaskScheduler taskScheduler;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        BukkitLoggerFactory.initialize(this);

        this.taskScheduler = new BukkitTaskScheduler(this);
        this.regionToolManager = new RegionToolManager(this);
        this.guiManager = new GuiManager(this);
        this.selectionHandler = new SelectionHandler(guiManager);
        this.listenerManager = new ListenerManager(this, regionToolManager, selectionHandler);
        this.listenerManager.registerListeners();
        this.sessionManager = new SessionManager(5 * 60 * 1000, this);
        this.exportManager = new ExportManager(this);
        this.commandManager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(this);

        // metadata
        String defaultName = getConfig().getString("metadata.default-map-name", "Unnamed Map");
        List<String> gamemodes = getConfig().getStringList("metadata.available-gamemodes");
        this.metadataManager = new MetadataManager(defaultName, gamemodes);

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