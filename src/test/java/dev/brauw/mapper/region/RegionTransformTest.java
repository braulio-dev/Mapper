package dev.brauw.mapper.region;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionTransformTest {

    private World world;

    @BeforeEach
    void setUp() {
        final ServerMock server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private Location at(double x, double y, double z) {
        return new Location(world, x, y, z);
    }

    private Location facing(double x, double y, double z, float yaw, float pitch) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    @Test
    void translateMovesAPointAndKeepsItsIdentity() {
        PointRegion point = new PointRegion("dock", at(10, 64, 10));

        PointRegion moved = (PointRegion) RegionTransform.translate(point, new Vector(5, -4, 0));

        assertEquals(15, moved.getLocation().getX());
        assertEquals(60, moved.getLocation().getY());
        assertEquals(10, moved.getLocation().getZ());
        assertEquals(point.getId(), moved.getId(), "moving a region does not make it a different region");
    }

    @Test
    void duplicateMintsANewIdSoTheCopyIsItsOwnDatapoint() {
        PointRegion point = new PointRegion("npc_resident", at(0, 64, 0));

        PointRegion copy = (PointRegion) RegionTransform.duplicate(point, new Vector(3, 0, 0), "npc_resident");

        assertNotEquals(point.getId(), copy.getId());
        assertEquals(3, copy.getLocation().getX());
        assertEquals("npc_resident", copy.getName());
    }

    @Test
    void mirroringACuboidKeepsMinBelowMax() {
        CuboidRegion cuboid = new CuboidRegion("plaza", at(10, 64, 10), at(20, 70, 20));

        CuboidRegion mirrored = (CuboidRegion) RegionTransform.mirror(cuboid, Axis.X, 0);

        assertTrue(mirrored.getMin().getX() <= mirrored.getMax().getX(),
                "a mirror swaps which corner is lower, so min/max must be re-derived");
        assertEquals(-20, mirrored.getMin().getX());
        assertEquals(-10, mirrored.getMax().getX());
        // The untouched axes are unchanged.
        assertEquals(64, mirrored.getMin().getY());
        assertEquals(20, mirrored.getMax().getZ());
    }

    @Test
    void mirroringAcrossXNegatesYaw() {
        PerspectiveRegion view = new PerspectiveRegion("lookout", facing(8, 64, 0, 90, 20));

        PerspectiveRegion mirrored = (PerspectiveRegion) RegionTransform.mirror(view, Axis.X, 0);

        assertEquals(-8, mirrored.getLocation().getX());
        assertEquals(-90, mirrored.getYaw());
        assertEquals(20, mirrored.getPitch());
    }

    @Test
    void mirroringAcrossZReflectsYawAboutOneEighty() {
        PerspectiveRegion view = new PerspectiveRegion("lookout", facing(0, 64, 8, 45, 0));

        PerspectiveRegion mirrored = (PerspectiveRegion) RegionTransform.mirror(view, Axis.Z, 0);

        assertEquals(-8, mirrored.getLocation().getZ());
        assertEquals(135, mirrored.getYaw(), "south-east becomes north-east across a Z plane");
    }

    @Test
    void mirroringAcrossYFlipsPitch() {
        PerspectiveRegion view = new PerspectiveRegion("lookout", facing(0, 70, 0, 0, 30));

        PerspectiveRegion mirrored = (PerspectiveRegion) RegionTransform.mirror(view, Axis.Y, 64);

        assertEquals(58, mirrored.getLocation().getY());
        assertEquals(-30, mirrored.getPitch());
    }

    @Test
    void translatingAPathMovesEveryWaypointInOrder() {
        PathRegion path = new PathRegion("npc_route", List.of(at(0, 64, 0), at(5, 64, 0), at(5, 64, 5)));

        PathRegion moved = (PathRegion) RegionTransform.translate(path, new Vector(0, 10, 0));

        assertEquals(3, moved.size());
        assertEquals(74, moved.getPoints().get(0).getY());
        assertEquals(5, moved.getPoints().get(1).getX());
        assertEquals(5, moved.getPoints().get(2).getZ());
    }

    @Test
    void anchorOfACuboidIsItsCentre() {
        CuboidRegion cuboid = new CuboidRegion("plaza", at(10, 60, 10), at(20, 70, 30));

        Location anchor = RegionTransform.anchor(cuboid);

        assertEquals(15, anchor.getX());
        assertEquals(65, anchor.getY());
        assertEquals(20, anchor.getZ());
    }

    @Test
    void anchorOfAPathIsItsFirstWaypoint() {
        PathRegion path = new PathRegion("npc_route", List.of(at(1, 64, 2), at(9, 64, 9)));

        Location anchor = RegionTransform.anchor(path);

        assertEquals(1, anchor.getX());
        assertEquals(2, anchor.getZ());
    }
}
