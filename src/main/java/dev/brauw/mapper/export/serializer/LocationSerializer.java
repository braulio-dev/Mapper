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
        gen.writeStringField("world", location.getWorld().getName());
        gen.writeNumberField("x", location.getX());
        gen.writeNumberField("y", location.getY());
        gen.writeNumberField("z", location.getZ());
        gen.writeEndObject();
    }
}

