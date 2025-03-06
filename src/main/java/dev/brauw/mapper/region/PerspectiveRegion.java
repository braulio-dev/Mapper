package dev.brauw.mapper.region;

import lombok.Getter;
import org.bukkit.Location;

@Getter
public class PerspectiveRegion extends PointRegion {

    private final float yaw;
    private final float pitch;

    public PerspectiveRegion(String name, Location location, RegionOptions options) {
        super(name, location, options);
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public PerspectiveRegion(String name, Location location) {
        this(name, location, RegionOptions.builder().build());
    }

    @Override
    public boolean contains(Location location) {
        return this.getLocation().x() == location.x()
                && this.getLocation().y() == location.y()
                && this.getLocation().z() == location.z()
                && this.getLocation().getYaw() == location.getYaw()
                && this.getLocation().getPitch() == location.getPitch();
    }

    @Override
    public RegionType getType() {
        return RegionType.PERSPECTIVE;
    }
}
