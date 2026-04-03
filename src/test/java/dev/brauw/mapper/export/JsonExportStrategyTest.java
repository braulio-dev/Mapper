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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonExportStrategyTest {

    private ServerMock server;

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void roundTripsPolygonRegions() throws IOException {
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
        java.io.File exportFile = new java.io.File(world.getWorldFolder(), "regions.json");

        assertTrue(strategy.export(List.of(polygon), exportFile));
        String writtenJson = Files.readString(exportFile.toPath());
        assertTrue(!writtenJson.contains("\"world\""));

        RegionCollection loaded = strategy.read(exportFile);
        assertEquals(1, loaded.size());

        Region region = loaded.getFirst();
        PolygonRegion loadedPolygon = assertInstanceOf(PolygonRegion.class, region);
        assertEquals("poly", loadedPolygon.getName());
        assertEquals(2, loadedPolygon.getChildren().size());
        assertEquals(RegionColor.BLUE, loadedPolygon.getOptions().getColor());
        assertEquals(world, loadedPolygon.getWorld());
        assertTrue(loadedPolygon.getChildren().stream()
                .allMatch(child -> child.getOptions().getColor() == RegionColor.BLUE));
    }

    @Test
    void leavesWorldUnsetWhenReadingFromInputStream() {
        server = MockBukkit.mock();

        String json = """
                [
                  {
                    "@ctype": "PolygonRegion",
                    "id": "11111111-1111-1111-1111-111111111111",
                    "name": "poly",
                    "children": [
                      {
                        "@ctype": "CuboidRegion",
                        "id": "22222222-2222-2222-2222-222222222222",
                        "name": "part-1",
                        "min": {
                          "x": 0.0,
                          "y": 64.0,
                          "z": 0.0
                        },
                        "max": {
                          "x": 2.0,
                          "y": 66.0,
                          "z": 2.0
                        },
                        "options": {}
                      }
                    ],
                    "options": {}
                  }
                ]
                """;

        JsonExportStrategy strategy = new JsonExportStrategy();
        RegionCollection loaded = strategy.read(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, loaded.size());
        PolygonRegion polygon = assertInstanceOf(PolygonRegion.class, loaded.getFirst());
        assertNull(polygon.getWorld());
        assertTrue(polygon.getChildren().stream().allMatch(child -> child.getWorld() == null));
    }
}
