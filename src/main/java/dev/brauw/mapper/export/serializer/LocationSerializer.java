package dev.brauw.mapper.export.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bukkit.Location;

import java.io.IOException;

public class LocationSerializer extends JsonSerializer<Location> {
    @Override
    public void serialize(Location location, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (location == null) {
            gen.writeNull();
            return;
        }

        gen.writeStartObject();
        gen.writeNumberField("x", location.getX());
        gen.writeNumberField("y", location.getY());
        gen.writeNumberField("z", location.getZ());
        // Rotation is written only when set. Regions that carry a single facing hoist it into their own
        // yaw/pitch fields (see PerspectiveRegion), but one holding a list of oriented points cannot -
        // parallel rotation arrays would be unreadable. Omitting zeroes keeps every already-exported
        // file byte-identical rather than churning every point in it.
        if (location.getYaw() != 0f) {
            gen.writeNumberField("yaw", location.getYaw());
        }
        if (location.getPitch() != 0f) {
            gen.writeNumberField("pitch", location.getPitch());
        }
        gen.writeEndObject();
    }
}

