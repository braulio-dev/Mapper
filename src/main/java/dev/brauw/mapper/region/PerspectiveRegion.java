package dev.brauw.mapper.region;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.bukkit.Location;

import java.util.UUID;

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

    @JsonCreator
    public PerspectiveRegion(
            @JsonProperty("id") UUID id,
            @JsonProperty("name") String name,
            @JsonProperty("location") Location location,
            @JsonProperty("options") RegionOptions options,
            @JsonProperty("yaw") float yaw,
            @JsonProperty("pitch") float pitch) {
        super(id, name, location, options);
        location.setPitch(pitch);
        location.setYaw(yaw);
        this.yaw = yaw;
        this.pitch = pitch;
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
