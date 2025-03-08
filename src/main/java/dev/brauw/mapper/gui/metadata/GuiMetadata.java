package dev.brauw.mapper.gui.metadata;

import dev.brauw.mapper.gui.GuiManager;
import dev.brauw.mapper.gui.button.BackItem;
import dev.brauw.mapper.gui.button.ForwardItem;
import dev.brauw.mapper.gui.metadata.button.AddAuthorButton;
import dev.brauw.mapper.gui.metadata.button.AuthorButton;
import dev.brauw.mapper.gui.metadata.button.NameButton;
import dev.brauw.mapper.gui.metadata.button.SaveButton;
import dev.brauw.mapper.metadata.MapMetadata;
import dev.brauw.mapper.metadata.MetadataManager;
import dev.brauw.mapper.util.BukkitTaskScheduler;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.AbstractPagedGui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.CycleItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GuiMetadata extends AbstractPagedGui<AuthorButton> {
    
    private final MapMetadata metadata;
    private final MetadataManager metadataManager;
    
    public GuiMetadata(BukkitTaskScheduler taskScheduler, GuiManager guiManager, MetadataManager metadataManager, Player player, MapMetadata metadata) {
        super(9, 4, true, 19, 20, 21, 22, 23, 24, 25);
        this.metadata = metadata;
        this.metadataManager = metadataManager;

        Structure structure = new Structure(
            "# # # # # # # # #",
            "# n g # # # # + #",
            "# a a a a a a a #",
            "# # # < s > # # #"
        );
        
        structure.addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ")));
        structure.addIngredient('n', new NameButton(taskScheduler, guiManager, metadata));
        structure.addIngredient('+', new AddAuthorButton(taskScheduler, guiManager, metadata));
        structure.addIngredient('s', new SaveButton(this));
        structure.addIngredient('a', Markers.CONTENT_LIST_SLOT_HORIZONTAL);
        structure.addIngredient('<', new BackItem());
        structure.addIngredient('>', new ForwardItem());

        // gameMode
        final List<String> gameModes = metadataManager.getGameModes();
        int selectedGameMode = gameModes.indexOf(metadata.getGameMode());
        final List<GameModeButton> gameModeButtons = gameModes.stream().map(str -> new GameModeButton(str, metadataManager)).toList();
        structure.addIngredient('g', CycleItem.withStateChangeHandler((p, selected) -> {
            String gameMode = gameModes.get(selected);
            metadata.setGameMode(gameMode);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
        }, selectedGameMode, gameModeButtons.toArray(ItemProvider[]::new)));

        applyStructure(structure);
        updateAuthorList();
    }

    public void updateAuthorList() {
        final List<AuthorButton> authors = metadata.getAuthors().stream().map(authorId -> new AuthorButton(metadata, authorId)).toList();
        setContent(authors);
    }

    @Override
    public void bake() {
        int contentSize = getContentListSlots().length;

        List<List<SlotElement>> pages = new ArrayList<>();
        List<SlotElement> page = new ArrayList<>(contentSize);

        for (Item item : content) {
            page.add(new SlotElement.ItemSlotElement(item));

            if (page.size() >= contentSize) {
                pages.add(page);
                page = new ArrayList<>(contentSize);
            }
        }

        if (!page.isEmpty()) {
            pages.add(page);
        }

        this.pages = pages;
        update();
    }


}