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

@CustomLog
public class MapperPlugin extends JavaPlugin {

    @Getter
    private SessionManager sessionManager;
    @Getter
    private ExportManager exportManager;
    @Getter
    private PaperCommandManager<CommandSourceStack> commandManager;
    @Getter
    private ListenerManager listenerManager;
    @Getter
    private MetadataManager metadataManager;
    @Getter
    private RegionToolManager regionToolManager;
    @Getter
    private SelectionHandler selectionHandler;
    @Getter
    private GuiManager guiManager;
    @Getter
    private BukkitTaskScheduler taskScheduler;

    @Override
    public void onEnable() {
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