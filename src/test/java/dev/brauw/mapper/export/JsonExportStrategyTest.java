package dev.brauw.mapper.export;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import dev.brauw.mapper.export.model.RegionCollection;
import dev.brauw.mapper.region.CuboidRegion;
import dev.brauw.mapper.region.PolygonRegion;
import dev.brauw.mapper.region.Region;
import dev.brauw.mapper.region.RegionColor;
import dev.brauw.mapper.region.RegionOptions;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonExportStrategyTest {

    private ServerMock server;

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void roundTripsPolygonRegions(@TempDir Path tempDir) {
        server = MockBukkit.mock();
        World world = server.addSimpleWorld("world");

        RegionOptions options = RegionOptions.builder()
                .color(RegionColor.BLUE)
                .build();

        PolygonRegion polygon = new PolygonRegion("poly", List.of(
                new CuboidRegion("part-1", new Location(world, 0, 64, 0), new Location(world, 2, 66, 2)),
                new CuboidRegion("part-2", new Location(world, 3, 64, 3), new Location(world, 5, 66, 5))
        ), options);

        JsonExportStrategy strategy = new JsonExportStrategy();
        Path exportFile = tempDir.resolve("regions.json");

        assertTrue(strategy.export(List.of(polygon), exportFile.toFile()));

        RegionCollection loaded = strategy.read(exportFile.toFile());
        assertEquals(1, loaded.size());

        Region region = loaded.getFirst();
        PolygonRegion loadedPolygon = assertInstanceOf(PolygonRegion.class, region);
        assertEquals("poly", loadedPolygon.getName());
        assertEquals(2, loadedPolygon.getChildren().size());
        assertEquals(RegionColor.BLUE, loadedPolygon.getOptions().getColor());
        assertTrue(loadedPolygon.getChildren().stream()
                .allMatch(child -> child.getOptions().getColor() == RegionColor.BLUE));
    }
}
